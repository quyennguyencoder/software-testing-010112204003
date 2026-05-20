'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { productViewService } from '@/services/product-view.service';
import type { ProductDetailViewResponse, ProductViewResponse } from '@/types/product-view';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Star,
  ShoppingCart,
  Heart,
  Share2,
  ChevronLeft,
  Check,
  Package,
  Shield,
  Truck,
  ArrowRight,
  CheckCircle,
  Battery,
  Camera,
  Cpu,
  Smartphone,
  Wifi,
  Shield as ShieldIcon,
  Weight,
  Layers,
} from 'lucide-react';
import Image from 'next/image';
import { ProductCard } from '@/components/features/products/ProductCard';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import { useWishlistStore } from '@/store/wishlistStore';
import { useCartActions } from '@/hooks/useCartActions';
import type { CartItemDetails } from '@/hooks/useCartActions';
import { toast } from 'sonner';

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params.id);

  const [product, setProduct] = useState<ProductDetailViewResponse | null>(null);
  const [relatedProducts, setRelatedProducts] = useState<ProductViewResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedImage, setSelectedImage] = useState(0);
  const [selectedVariant, setSelectedVariant] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);

  // Cart and Wishlist
  const { addToCartWithDetails, buyNowWithDetails } = useCartActions();
  const { toggleItem: toggleWishlistItem, isInWishlist } = useWishlistStore();

  // Fetch product details
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        console.log('🔍 Fetching product detail for ID:', productId);

        if (!productId || isNaN(productId)) {
          console.error('❌ Invalid product ID:', productId);
          setError('ID sản phẩm không hợp lệ');
          setLoading(false);
          return;
        }

        setLoading(true);
        setError(null);

        console.log('📡 Calling API for product:', productId);
        const [productData, relatedData] = await Promise.all([
          productViewService.getProductById(productId),
          productViewService.getRelatedProducts(productId, 8),
        ]);

        console.log('✅ Product data received:', productData);

        setProduct(productData);
        setRelatedProducts(relatedData);

        // Select first available variant
        if (productData.variants && productData.variants.length > 0 && productData.variants[0]) {
          setSelectedVariant(productData.variants[0].id);
          console.log('✅ Selected variant:', productData.variants[0].id);
        } else {
          console.warn('⚠️ No variants available for product');
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load product';
        setError(errorMessage);
        console.error('❌ Fetch product error:', err);
      } finally {
        setLoading(false);
      }
    };

    if (productId) {
      fetchProduct();
    } else {
      console.error('❌ No product ID provided');
      setError('Không tìm thấy ID sản phẩm');
      setLoading(false);
    }
  }, [productId]);

  if (loading) {
    return <ProductDetailSkeleton />;
  }

  if (error || !product) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center px-4">
        <div className="max-w-md text-center space-y-6">
          <div className="space-y-2">
            <h2 className="text-3xl font-bold tracking-tight">Không tìm thấy sản phẩm</h2>
            <p className="text-muted-foreground">{error}</p>
          </div>
          <Button
            onClick={() => router.push('/products')}
            className="gap-2"
          >
            <ChevronLeft className="w-4 h-4" />
            Quay lại danh sách sản phẩm
          </Button>
        </div>
      </div>
    );
  }

  const primaryImage = product.images?.find(img => img.isPrimary) || product.images?.[0];
  const currentImage = product.images?.[selectedImage] || primaryImage;
  const imageUrl = currentImage?.imageUrl || product.thumbnailUrl || '/placeholder-product.png';

  const selectedVariantData = product.variants?.find(v => v.id === selectedVariant);
  const currentPrice = selectedVariantData?.discountedPrice ?? selectedVariantData?.originalPrice ?? 0;
  const originalPrice = selectedVariantData?.originalPrice ?? 0;
  const currentStock = selectedVariantData?.stockQuantity || 0;
  const inStock = currentStock > 0;
  const discountPercentage = selectedVariantData?.discountPercentage ?? 0;

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const formatRating = (rating: number) => {
    return rating.toFixed(1);
  };

  const handleAddToCart = () => {
    if (!product || !selectedVariantData) return;

    const details: CartItemDetails = {
      productId: product.id,
      productName: `${product.name}${selectedVariantData.color ? ` - ${selectedVariantData.color}` : ''}${selectedVariantData.storage ? ` ${selectedVariantData.storage}` : ''}`,
      productImage: imageUrl,
      price: currentPrice,
      discountPercent: discountPercentage,
      quantity: quantity,
      color: selectedVariantData.color,
      storage: selectedVariantData.storage,
    };
    addToCartWithDetails(details);
  };

  const handleBuyNow = () => {
    if (!product || !selectedVariantData) return;

    const details: CartItemDetails = {
      productId: product.id,
      productName: `${product.name}${selectedVariantData.color ? ` - ${selectedVariantData.color}` : ''}${selectedVariantData.storage ? ` ${selectedVariantData.storage}` : ''}`,
      productImage: imageUrl,
      price: currentPrice,
      discountPercent: discountPercentage,
      quantity: quantity,
      color: selectedVariantData.color,
      storage: selectedVariantData.storage,
    };
    buyNowWithDetails(details);
  };

  const handleToggleWishlist = () => {
    if (!product) return;

    const wasInWishlist = isInWishlist(product.id);
    toggleWishlistItem({
      productId: product.id,
      productName: product.name,
      productImage: imageUrl,
      price: currentPrice,
      inStock: inStock,
    });

    if (wasInWishlist) {
      toast.info('Đã xóa khỏi danh sách yêu thích');
    } else {
      toast.success('Đã thêm vào danh sách yêu thích', {
        description: product.name,
      });
    }
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: product.name,
        text: product.description?.substring(0, 100),
        url: window.location.href,
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert('Link đã được sao chép!');
    }
  };

  const renderSpecIcon = (label: string) => {
    switch (label) {
      case 'Màn hình':
        return <Smartphone className="w-4 h-4" />;
      case 'CPU':
        return <Cpu className="w-4 h-4" />;
      case 'Camera':
        return <Camera className="w-4 h-4" />;
      case 'Pin':
        return <Battery className="w-4 h-4" />;
      case 'Bảo mật':
        return <ShieldIcon className="w-4 h-4" />;
      case 'Kết nối':
        return <Wifi className="w-4 h-4" />;
      case 'Trọng lượng':
        return <Weight className="w-4 h-4" />;
      default:
        return <Layers className="w-4 h-4" />;
    }
  };

  const specifications = [
    { label: 'Màn hình', value: product.technicalSpecs?.screenSize ? `${product.technicalSpecs.screenSize}" ${product.technicalSpecs.screenTechnology || ''}`.trim() : product.technicalSpecs?.screenResolution },
    { label: 'Tần số quét', value: product.technicalSpecs?.refreshRate ? `${product.technicalSpecs.refreshRate}Hz` : undefined },
    { label: 'Hệ điều hành', value: product.technicalSpecs?.operatingSystem },
    { label: 'CPU', value: product.technicalSpecs?.cpuChipset },
    { label: 'GPU', value: product.technicalSpecs?.gpu },
    { label: 'Camera', value: product.technicalSpecs?.cameraDetails },
    { label: 'Camera trước', value: product.technicalSpecs?.frontCameraMegapixels ? `${product.technicalSpecs.frontCameraMegapixels}MP` : undefined },
    { label: 'Pin', value: product.technicalSpecs?.batteryCapacity ? `${product.technicalSpecs.batteryCapacity} mAh` : undefined },
    { label: 'Sạc', value: product.technicalSpecs?.chargingPower ? `${product.technicalSpecs.chargingPower}W ${product.technicalSpecs.chargingType || ''}`.trim() : product.technicalSpecs?.chargingType },
    { label: 'Kết nối', value: product.technicalSpecs?.wirelessConnectivity },
    { label: 'SIM', value: product.technicalSpecs?.simType },
    { label: 'Chống nước', value: product.technicalSpecs?.waterResistance },
    { label: 'Âm thanh', value: product.technicalSpecs?.audioFeatures },
    { label: 'Bảo mật', value: product.technicalSpecs?.securityFeatures },
    { label: 'Trọng lượng', value: product.technicalSpecs?.weight ? `${product.technicalSpecs.weight} g` : undefined },
    { label: 'Kích thước', value: product.technicalSpecs?.dimensions },
    { label: 'Chất liệu', value: product.technicalSpecs?.material },
    { label: 'Khác', value: product.technicalSpecs?.additionalSpecs },
  ].filter(item => item.value);

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-gray-50/30">
      {/* Navigation */}
      <div className="sticky top-0 z-10 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b">
        <div className="container mx-auto px-4 py-3">
          <div className="flex items-center gap-2 text-sm">
            <Button
              variant="ghost"
              size="sm"
              className="h-8 text-muted-foreground hover:text-foreground"
              onClick={() => router.push('/')}
            >
              Trang chủ
            </Button>
            <span className="text-muted-foreground">/</span>
            <Button
              variant="ghost"
              size="sm"
              className="h-8 text-muted-foreground hover:text-foreground"
              onClick={() => router.push('/products')}
            >
              Sản phẩm
            </Button>
            <span className="text-muted-foreground">/</span>
            <span className="text-foreground font-medium truncate max-w-[200px]">
              {product.name}
            </span>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-2 gap-8 lg:gap-12 mb-16">
          {/* Images Section */}
          <div className="space-y-6">
            {/* Main Image */}
            <div className="relative aspect-square bg-gradient-to-br from-gray-50 to-gray-100 rounded-2xl overflow-hidden border shadow-lg">
              <Image
                src={imageUrl}
                alt={product.name}
                fill
                className="object-contain p-8"
                priority
                sizes="(max-width: 768px) 100vw, 50vw"
              />

              {/* Badges */}
              <div className="absolute top-4 left-4 flex flex-col gap-2">
                {discountPercentage > 0 && (
                  <div className="relative">
                    <div className="absolute inset-0 bg-gradient-to-r from-red-500 to-pink-500 blur-sm"></div>
                    <Badge className="relative bg-gradient-to-r from-red-500 to-pink-500 border-0 text-white font-bold px-3 py-1.5">
                      -{discountPercentage}%
                    </Badge>
                  </div>
                )}
                {!inStock && (
                  <Badge variant="secondary" className="font-semibold px-3 py-1.5">
                    Hết hàng
                  </Badge>
                )}
              </div>
            </div>

            {/* Thumbnail Images */}
            {product.images && product.images.length > 1 && (
              <div className="px-4">
                <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                  {product.images.map((img, index) => (
                    <button
                      key={img.id}
                      className={cn(
                        "relative flex-shrink-0 w-20 h-20 bg-gradient-to-br from-gray-50 to-gray-100 rounded-lg overflow-hidden border transition-all duration-200",
                        selectedImage === index
                          ? "border-primary ring-2 ring-primary ring-offset-2"
                          : "border-transparent hover:border-primary/50 hover:scale-105"
                      )}
                      onClick={() => setSelectedImage(index)}
                    >
                      <Image
                        src={img.imageUrl}
                        alt={img.altText || `${product.name} ${index + 1}`}
                        fill
                        className="object-cover"
                      />
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Product Info Section */}
          <div className="space-y-8">
            {/* Header */}
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <Badge variant="outline" className="text-xs font-semibold py-1 px-3 border-primary/30">
                  {product.category?.name}
                </Badge>
                <Badge variant="outline" className="text-xs font-semibold py-1 px-3">
                  {product.brand?.name}
                </Badge>
              </div>

              <h1 className="text-3xl lg:text-4xl font-bold tracking-tight bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
                {product.name}
              </h1>

              {/* Rating & Sold */}
              <div className="flex items-center gap-6">
                {product.averageRating && product.averageRating > 0 && (
                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-1.5 bg-gradient-to-br from-yellow-400 to-orange-500 text-white px-3 py-1.5 rounded-full">
                      <Star className="w-4 h-4 fill-current" />
                      <span className="font-bold text-sm">{formatRating(product.averageRating)}</span>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      ({product.totalReviews || 0} đánh giá)
                    </span>
                  </div>
                )}
              </div>
            </div>

            <Separator className="bg-gradient-to-r from-transparent via-border to-transparent" />

            {/* Price */}
            <div className="space-y-2">
              <div className="flex items-end gap-4">
                <p className="text-4xl font-bold bg-gradient-to-r from-primary to-primary/80 bg-clip-text text-transparent">
                  {formatPrice(currentPrice)}
                </p>
                {discountPercentage > 0 && (
                  <>
                    <p className="text-xl text-muted-foreground line-through mb-1">
                      {formatPrice(originalPrice)}
                    </p>
                    <Badge variant="secondary" className="text-sm font-semibold">
                      Tiết kiệm {formatPrice(originalPrice - currentPrice)}
                    </Badge>
                  </>
                )}
              </div>
              <p className="text-sm text-muted-foreground flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-green-500" />
                {inStock ? 'Còn hàng' : 'Tạm hết hàng'}
                {inStock && currentStock > 0 && ` • ${currentStock} sản phẩm có sẵn`}
              </p>
            </div>

            {/* Variants Selection */}
            {product.variants && product.variants.length > 0 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-lg">Phiên bản:</h3>
                  <span className="text-sm text-muted-foreground">Chọn 1 tuỳ chọn</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {product.variants.map((variant) => {
                    const isSelected = selectedVariant === variant.id;
                    const variantInStock = variant.stockQuantity && variant.stockQuantity > 0;

                    return (
                      <Card
                        key={variant.id}
                        className={cn(
                          "cursor-pointer transition-all duration-300 hover:shadow-lg",
                          isSelected
                            ? "border-primary ring-2 ring-primary ring-offset-2"
                            : "hover:border-primary/30",
                          !variantInStock && "opacity-60 cursor-not-allowed"
                        )}
                        onClick={() => variantInStock && setSelectedVariant(variant.id)}
                      >
                        <CardContent className="p-4">
                          <div className="flex justify-between items-start mb-2">
                            <div className="space-y-1">
                              {variant.ram && (
                                <p className="font-semibold text-foreground">{variant.ram}</p>
                              )}
                              {variant.storage && (
                                <p className="text-sm text-muted-foreground">{variant.storage}</p>
                              )}
                              {variant.color && (
                                <div className="flex items-center gap-2">
                                  <div
                                    className="w-4 h-4 rounded-full border"
                                    style={{ backgroundColor: variant.color.toLowerCase() }}
                                  />
                                  <span className="text-sm">{variant.color}</span>
                                </div>
                              )}
                            </div>
                            {isSelected && (
                              <Check className="w-5 h-5 text-primary" />
                            )}
                          </div>
                          <div className="flex items-center justify-between">
                            <p className="font-bold text-primary text-lg">
                              {formatPrice(variant.discountedPrice ?? variant.originalPrice ?? 0)}
                            </p>
                            <p className={cn(
                              "text-xs px-2 py-1 rounded-full",
                              variantInStock
                                ? "bg-green-100 text-green-700"
                                : "bg-red-100 text-red-700"
                            )}>
                              {variantInStock ? `Còn ${variant.stockQuantity}` : 'Hết hàng'}
                            </p>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Quantity */}
            <div className="space-y-3">
              <h3 className="font-semibold text-lg">Số lượng:</h3>
              <div className="flex items-center gap-3 max-w-xs">
                <Button
                  variant="outline"
                  size="icon"
                  className="rounded-full w-10 h-10"
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  disabled={quantity <= 1}
                >
                  -
                </Button>
                <div className="flex-1 text-center">
                  <span className="text-2xl font-bold">{quantity}</span>
                </div>
                <Button
                  variant="outline"
                  size="icon"
                  className="rounded-full w-10 h-10"
                  onClick={() => setQuantity(Math.min(currentStock, quantity + 1))}
                  disabled={quantity >= currentStock}
                >
                  +
                </Button>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Button
                  size="lg"
                  className="h-14 text-base font-semibold rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 bg-gradient-to-r from-primary to-primary/90 hover:from-primary/90 hover:to-primary"
                  onClick={handleBuyNow}
                  disabled={!inStock}
                >
                  <ShoppingCart className="w-5 h-5 mr-2" />
                  Mua ngay
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="h-14 text-base font-semibold rounded-xl border-2 hover:border-primary hover:bg-primary/5 transition-all duration-300"
                  onClick={handleAddToCart}
                  disabled={!inStock}
                >
                  Thêm vào giỏ hàng
                </Button>
              </div>

              <div className="flex gap-2">
                <Button
                  variant="ghost"
                  size="lg"
                  className="flex-1 rounded-lg hover:bg-accent"
                  onClick={handleToggleWishlist}
                >
                  <Heart className={cn(
                    "w-5 h-5 mr-2 transition-colors",
                    product && isInWishlist(product.id) ? "fill-red-500 text-red-500" : ""
                  )} />
                  {product && isInWishlist(product.id) ? 'Đã thích' : 'Yêu thích'}
                </Button>
                <Button
                  variant="ghost"
                  size="lg"
                  className="flex-1 rounded-lg hover:bg-accent"
                  onClick={handleShare}
                >
                  <Share2 className="w-5 h-5 mr-2" />
                  Chia sẻ
                </Button>
              </div>
            </div>

            {/* Features */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 pt-6 border-t">
              <div className="flex items-center gap-3 p-3 bg-gradient-to-br from-blue-50 to-blue-100/30 rounded-xl">
                <div className="p-2 bg-blue-100 rounded-lg">
                  <Truck className="w-5 h-5 text-blue-600" />
                </div>
                <div className="text-xs">
                  <p className="font-semibold">Giao hàng nhanh</p>
                  <p className="text-muted-foreground">2-3 ngày</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-gradient-to-br from-green-50 to-green-100/30 rounded-xl">
                <div className="p-2 bg-green-100 rounded-lg">
                  <Package className="w-5 h-5 text-green-600" />
                </div>
                <div className="text-xs">
                  <p className="font-semibold">Miễn phí vận chuyển</p>
                  <p className="text-muted-foreground">Đơn từ 500K</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-gradient-to-br from-purple-50 to-purple-100/30 rounded-xl">
                <div className="p-2 bg-purple-100 rounded-lg">
                  <Shield className="w-5 h-5 text-purple-600" />
                </div>
                <div className="text-xs">
                  <p className="font-semibold">Bảo hành chính hãng</p>
                  <p className="text-muted-foreground">12 tháng</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-gradient-to-br from-orange-50 to-orange-100/30 rounded-xl">
                <div className="p-2 bg-orange-100 rounded-lg">
                  <CheckCircle className="w-5 h-5 text-orange-600" />
                </div>
                <div className="text-xs">
                  <p className="font-semibold">Hỗ trợ 24/7</p>
                  <p className="text-muted-foreground">Hotline 1800-xxx</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Product Details Tabs */}
        <div className="mb-16">
          <Tabs defaultValue="description" className="space-y-6">
            <TabsList className="w-full justify-start h-auto p-0 bg-transparent border-b rounded-none">
              <TabsTrigger
                value="description"
                className="relative px-6 py-3 text-base font-semibold data-[state=active]:bg-transparent rounded-none data-[state=active]:after:absolute data-[state=active]:after:bottom-0 data-[state=active]:after:left-0 data-[state=active]:after:right-0 data-[state=active]:after:h-0.5 data-[state=active]:after:bg-primary"
              >
                Mô tả sản phẩm
              </TabsTrigger>
              <TabsTrigger
                value="specifications"
                className="relative px-6 py-3 text-base font-semibold data-[state=active]:bg-transparent rounded-none data-[state=active]:after:absolute data-[state=active]:after:bottom-0 data-[state=active]:after:left-0 data-[state=active]:after:right-0 data-[state=active]:after:h-0.5 data-[state=active]:after:bg-primary"
              >
                Thông số kỹ thuật
              </TabsTrigger>
              <TabsTrigger
                value="reviews"
                className="relative px-6 py-3 text-base font-semibold data-[state=active]:bg-transparent rounded-none data-[state=active]:after:absolute data-[state=active]:after:bottom-0 data-[state=active]:after:left-0 data-[state=active]:after:right-0 data-[state=active]:after:h-0.5 data-[state=active]:after:bg-primary"
              >
                Đánh giá ({product.totalReviews || 0})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="description" className="mt-0">
              <Card className="border-0 shadow-lg rounded-2xl overflow-hidden">
                <CardContent className="p-8">
                  <div className="prose prose-lg max-w-none">
                    {product.description ? (
                      <div className="space-y-4">
                        <p className="text-lg leading-relaxed text-foreground/90 whitespace-pre-wrap">
                          {product.description}
                        </p>
                      </div>
                    ) : (
                      <div className="text-center py-12">
                        <p className="text-muted-foreground text-lg">Chưa có mô tả sản phẩm</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="specifications" className="mt-0">
              <Card className="border-0 shadow-lg rounded-2xl overflow-hidden">
                <CardContent className="p-8">
                  {specifications.length > 0 ? (
                    <div className="grid md:grid-cols-2 gap-6">
                      {specifications.map((item, index) => (
                        <div
                          key={`${item.label}-${index}`}
                          className="group flex items-start gap-4 p-4 rounded-xl hover:bg-accent/50 transition-colors duration-200"
                        >
                          <div className="p-2 bg-primary/10 rounded-lg group-hover:bg-primary/20 transition-colors">
                            {renderSpecIcon(item.label)}
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 className="font-semibold text-sm text-primary mb-1">
                              {item.label}
                            </h4>
                            <p className="text-foreground break-words">
                              {item.value}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-12">
                      <p className="text-muted-foreground text-lg">Chưa có thông số kỹ thuật</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="reviews" className="mt-0">
              <Card className="border-0 shadow-lg rounded-2xl overflow-hidden">
                <CardContent className="p-8">
                  <div className="text-center py-12">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-primary/10 rounded-full mb-4">
                      <Star className="w-8 h-8 text-primary" />
                    </div>
                    <h3 className="text-xl font-semibold mb-2">Đánh giá sản phẩm</h3>
                    <p className="text-muted-foreground mb-6">
                      Tính năng đánh giá đang được phát triển
                    </p>
                    <Button variant="outline" className="gap-2">
                      Xem tất cả đánh giá
                      <ArrowRight className="w-4 h-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* Related Products */}
        {relatedProducts.length > 0 && (
          <div className="mb-12">
            <div className="flex items-center justify-between mb-8">
              <div>
                <h2 className="text-2xl lg:text-3xl font-bold tracking-tight bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
                  Sản phẩm liên quan
                </h2>
                <p className="text-muted-foreground mt-2">
                  Khám phá thêm các sản phẩm tương tự
                </p>
              </div>
              <Button
                variant="ghost"
                className="gap-2"
                onClick={() => router.push('/products')}
              >
                Xem tất cả
                <ArrowRight className="w-4 h-4" />
              </Button>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {relatedProducts.map((relatedProduct) => (
                <div key={relatedProduct.id} className="transform transition-transform duration-300 hover:-translate-y-1">
                  <ProductCard product={relatedProduct} />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function ProductDetailSkeleton() {
  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb Skeleton */}
      <div className="flex items-center gap-2 mb-8">
        <Skeleton className="h-6 w-20" />
        <Skeleton className="h-4 w-4" />
        <Skeleton className="h-6 w-24" />
        <Skeleton className="h-4 w-4" />
        <Skeleton className="h-6 w-32" />
      </div>

      <div className="grid lg:grid-cols-2 gap-8 lg:gap-12 mb-16">
        {/* Image Section Skeleton */}
        <div className="space-y-6">
          <Skeleton className="aspect-square rounded-2xl" />
          <div className="flex gap-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="w-20 h-20 rounded-lg" />
            ))}
          </div>
        </div>

        {/* Info Section Skeleton */}
        <div className="space-y-8">
          <div className="space-y-4">
            <div className="flex gap-2">
              <Skeleton className="h-6 w-16 rounded-full" />
              <Skeleton className="h-6 w-20 rounded-full" />
            </div>
            <Skeleton className="h-12 w-full" />
            <div className="flex gap-6">
              <Skeleton className="h-8 w-32" />
              <Skeleton className="h-8 w-40" />
            </div>
          </div>

          <Skeleton className="h-px w-full" />

          <div className="space-y-2">
            <Skeleton className="h-12 w-48" />
            <Skeleton className="h-6 w-32" />
          </div>

          <div className="space-y-4">
            <div className="flex justify-between">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-32" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-24 rounded-xl" />
              ))}
            </div>
          </div>

          <div className="space-y-3">
            <Skeleton className="h-6 w-24" />
            <div className="flex items-center gap-3 max-w-xs">
              <Skeleton className="w-10 h-10 rounded-full" />
              <Skeleton className="flex-1 h-12" />
              <Skeleton className="w-10 h-10 rounded-full" />
            </div>
          </div>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <Skeleton className="h-14 rounded-xl" />
              <Skeleton className="h-14 rounded-xl" />
            </div>
            <div className="flex gap-2">
              <Skeleton className="flex-1 h-12 rounded-lg" />
              <Skeleton className="flex-1 h-12 rounded-lg" />
            </div>
          </div>

          <div className="grid grid-cols-4 gap-4 pt-6">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-20 rounded-xl" />
            ))}
          </div>
        </div>
      </div>

      {/* Tabs Skeleton */}
      <div className="mb-16 space-y-6">
        <div className="flex gap-8 border-b pb-2">
          <Skeleton className="h-10 w-32" />
          <Skeleton className="h-10 w-40" />
          <Skeleton className="h-10 w-36" />
        </div>
        <Skeleton className="h-96 rounded-2xl" />
      </div>

      {/* Related Products Skeleton */}
      <div className="space-y-8">
        <div className="flex justify-between items-center">
          <Skeleton className="h-10 w-64" />
          <Skeleton className="h-10 w-24" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="aspect-[3/4] rounded-xl" />
          ))}
        </div>
      </div>
    </div>
  );
}