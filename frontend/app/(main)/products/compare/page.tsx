'use client';

import React, { useState, useEffect, Suspense, useMemo } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { compareProducts } from '@/services/product-view.service';
import type { ProductComparisonResponse } from '@/types/product-view';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Skeleton } from '@/components/ui/skeleton';
import {
  ArrowLeft,
  X,
  Star,
  ShoppingCart,
  Heart,
  Info,
  TrendingUp,
  Award,
  Shield,
  Zap,
  LayoutGrid,
  Table
} from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { ComparisonTable } from '@/components/features/products/ComparisonTable';
import { useCartActions } from '@/hooks/useCartActions';
import type { ProductComparisonResponse as ComparisonProduct } from '@/types/product-view';

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const renderStars = (rating: number, size = 'sm') => {
  return (
    <div className={`flex items-center gap-1 ${size === 'sm' ? 'text-xs' : ''}`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`${size === 'sm' ? 'w-3 h-3' : 'w-4 h-4'} ${star <= rating
              ? 'fill-yellow-400 text-yellow-400'
              : 'fill-gray-200 text-gray-200'
            }`}
        />
      ))}
      <span className={`ml-1 font-medium ${size === 'sm' ? 'text-xs' : 'text-sm'}`}>
        {rating.toFixed(1)}
      </span>
    </div>
  );
};

// Product Image component with error handling
function ProductImage({ src, alt, className }: { src: string; alt: string; className?: string }) {
  const [imageSrc, setImageSrc] = useState(src);

  return (
    <Image
      src={imageSrc}
      alt={alt}
      fill
      className={className}
      sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 25vw"
      unoptimized={true}
      onError={() => setImageSrc('/placeholder-product.svg')}
    />
  );
}

const ComparisonSkeleton = ({ count }: { count: number }) => (
  <div className="container mx-auto px-4 py-8">
    <div className="mb-8">
      <Skeleton className="h-8 w-64 mb-4" />
      <Skeleton className="h-4 w-96" />
    </div>

    <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-6">
      {Array.from({ length: count }, (_, i) => (
        <Card key={i} className="h-[800px]">
          <CardHeader className="pb-4">
            <Skeleton className="w-full h-48 rounded-lg" />
            <Skeleton className="h-6 w-full mt-4" />
            <Skeleton className="h-4 w-24" />
          </CardHeader>
          <CardContent className="space-y-3">
            {Array.from({ length: 12 }, (_, j) => (
              <div key={j} className="flex justify-between">
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-4 w-32" />
              </div>
            ))}
          </CardContent>
        </Card>
      ))}
    </div>
  </div>
);

const ProductCard = ({
  product,
  onRemove,
  onBuyNow,
  isHighlighted
}: {
  product: ProductComparisonResponse['products'][0];
  onRemove: (id: number) => void;
  onBuyNow?: (product: ProductComparisonResponse['products'][0]) => void;
  isHighlighted?: boolean;
}) => {
  const discount = product.hasDiscount && product.originalPrice && product.discountedPrice
    ? Math.round((1 - product.discountedPrice / product.originalPrice) * 100)
    : 0;

  const displayPrice = product.discountedPrice || product.originalPrice;

  return (
    <Card className={`relative h-fit ${isHighlighted ? 'ring-2 ring-primary shadow-lg' : ''}`}>
      {/* Remove button */}
      <Button
        variant="ghost"
        size="sm"
        onClick={() => onRemove(product.id)}
        className="absolute top-2 right-2 z-10 h-8 w-8 rounded-full bg-white/80 hover:bg-white shadow-sm"
      >
        <X className="w-4 h-4" />
      </Button>

      {/* Best value badge */}
      {isHighlighted && (
        <div className="absolute -top-3 left-1/2 transform -translate-x-1/2 z-20">
          <Badge className="bg-primary text-primary-foreground px-3 py-1 rounded-full">
            <Award className="w-3 h-3 mr-1" />
            Tốt nhất
          </Badge>
        </div>
      )}

      <CardHeader className="pb-4">
        {/* Product Image */}
        <div className="relative aspect-square rounded-lg overflow-hidden bg-gray-50">
          <ProductImage
            src={product.thumbnailUrl || '/placeholder-product.svg'}
            alt={product.name}
            className="object-cover"
          />
          {discount > 0 && (
            <Badge className="absolute top-2 left-2 bg-red-500 text-white">
              -{discount}%
            </Badge>
          )}
        </div>

        {/* Product Info */}
        <div className="space-y-2 mt-4">
          <h3 className="font-semibold text-lg leading-tight line-clamp-2">
            {product.name}
          </h3>

          <p className="text-sm text-muted-foreground">
            {product.brandName}
          </p>

          {/* Rating */}
          {product.averageRating > 0 && (
            <div className="flex items-center gap-2">
              {renderStars(product.averageRating)}
              <span className="text-xs text-muted-foreground">
                ({product.totalReviews} đánh giá)
              </span>
            </div>
          )}

          {/* Price */}
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-xl font-bold text-primary">
                {formatPrice(displayPrice)}
              </span>
              {product.hasDiscount && product.originalPrice && (
                <span className="text-sm text-muted-foreground line-through">
                  {formatPrice(product.originalPrice)}
                </span>
              )}
            </div>
          </div>

          {/* Stock Status */}
          <div className="flex items-center gap-2">
            <div className={`w-2 h-2 rounded-full ${product.inStock ? 'bg-green-500' : 'bg-red-500'}`} />
            <span className={`text-sm ${product.inStock ? 'text-green-700' : 'text-red-700'}`}>
              {product.inStock ? 'Còn hàng' : 'Hết hàng'}
            </span>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-2 pt-2">
            <Button
              size="sm"
              className="flex-1"
              disabled={!product.inStock}
              onClick={() => onBuyNow?.(product)}
            >
              <ShoppingCart className="w-4 h-4 mr-1" />
              Mua ngay
            </Button>
            <Button variant="outline" size="sm">
              <Heart className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </CardHeader>

      <Separator />

      {/* Specifications */}
      <CardContent className="pt-4">
        <h4 className="font-medium mb-3 flex items-center gap-2">
          <Info className="w-4 h-4" />
          Thông số kỹ thuật
        </h4>

        <div className="space-y-2.5 text-sm">
          {product.specs.screen && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Màn hình</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.screen}</span>
            </div>
          )}

          {product.specs.os && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Hệ điều hành</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.os}</span>
            </div>
          )}

          {product.specs.cpu && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Chip xử lý</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.cpu}</span>
            </div>
          )}

          {product.specs.ram && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">RAM</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.ram}</span>
            </div>
          )}

          {product.specs.internalMemory && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Bộ nhớ</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.internalMemory}</span>
            </div>
          )}

          {product.specs.rearCamera && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Camera sau</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.rearCamera}</span>
            </div>
          )}

          {product.specs.frontCamera && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Camera trước</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.frontCamera}</span>
            </div>
          )}

          {product.specs.battery && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Pin</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.battery}</span>
            </div>
          )}

          {product.specs.charging && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Sạc</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.charging}</span>
            </div>
          )}

          {product.specs.weight && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Trọng lượng</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.weight}</span>
            </div>
          )}

          {product.specs.dimensions && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Kích thước</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.dimensions}</span>
            </div>
          )}

          {product.specs.connectivity && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Kết nối</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.connectivity}</span>
            </div>
          )}

          {product.specs.sim && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">SIM</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.sim}</span>
            </div>
          )}

          {product.specs.materials && (
            <div className="flex justify-between items-start gap-2">
              <span className="text-muted-foreground min-w-0">Chất liệu</span>
              <span className="font-medium text-right flex-shrink-0">{product.specs.materials}</span>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

function ProductCompareContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const productIds = useMemo(() =>
    searchParams.get('ids')?.split(',').map(Number).filter(Boolean) || [],
    [searchParams]
  );

  const [comparisonData, setComparisonData] = useState<ProductComparisonResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'cards' | 'table'>('cards');
  const { buyNowWithDetails } = useCartActions();

  // Handle buy now for comparison products
  const handleBuyNow = (product: ComparisonProduct['products'][0]) => {
    buyNowWithDetails({
      productId: product.id,
      productName: product.name,
      productImage: product.thumbnailUrl || '',
      price: product.discountedPrice || product.originalPrice || 0,
      quantity: 1,
    });
  };

  useEffect(() => {
    const fetchComparisonData = async () => {
      if (productIds.length === 0) {
        setError('Không có sản phẩm nào để so sánh');
        setLoading(false);
        return;
      }

      if (productIds.length < 2) {
        setError('Cần ít nhất 2 sản phẩm để so sánh');
        setLoading(false);
        return;
      }

      if (productIds.length > 4) {
        setError('Chỉ có thể so sánh tối đa 4 sản phẩm');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        const data = await compareProducts(productIds);
        setComparisonData(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Không thể tải dữ liệu so sánh');
        console.error('Compare error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchComparisonData();
  }, [productIds]);

  const removeProduct = (productId: number) => {
    const newIds = productIds.filter(id => id !== productId);
    if (newIds.length < 2) {
      router.push('/products');
    } else {
      router.push(`/products/compare?ids=${newIds.join(',')}`);
    }
  };

  // Find best value product (highest rating or lowest price)
  const findBestValueProduct = (products: ProductComparisonResponse['products']) => {
    if (products.length === 0) return null;

    // First try to find by highest rating
    const highestRated = products.reduce((prev, current) =>
      (current.averageRating || 0) > (prev.averageRating || 0) ? current : prev
    );

    if (highestRated.averageRating && highestRated.averageRating >= 4.5) {
      return highestRated.id;
    }

    // If no high rating, find lowest price
    const lowestPrice = products.reduce((prev, current) => {
      const prevPrice = prev.discountedPrice || prev.originalPrice || 0;
      const currentPrice = current.discountedPrice || current.originalPrice || 0;
      return currentPrice < prevPrice ? current : prev;
    });

    return lowestPrice.id;
  };

  if (loading) {
    return <ComparisonSkeleton count={productIds.length} />;
  }

  if (error || !comparisonData || comparisonData.products.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
        <div className="container mx-auto px-4 py-12">
          <div className="text-center max-w-md mx-auto">
            <div className="w-20 h-20 mx-auto mb-6 rounded-full bg-red-100 flex items-center justify-center">
              <X className="w-10 h-10 text-red-500" />
            </div>
            <h2 className="text-2xl font-bold mb-4">Không thể so sánh sản phẩm</h2>
            <p className="text-muted-foreground mb-6">{error}</p>
            <Button onClick={() => router.push('/products')} size="lg">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Quay lại danh sách
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const bestValueProductId = findBestValueProduct(comparisonData.products);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-4 mb-4">
            <Button
              variant="ghost"
              onClick={() => router.push('/products')}
              className="flex items-center gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Quay lại
            </Button>
            <div className="flex items-center gap-2">
              <TrendingUp className="w-6 h-6 text-primary" />
              <h1 className="text-3xl font-bold">So sánh sản phẩm</h1>
            </div>
          </div>

          <p className="text-muted-foreground text-lg">
            So sánh thông số kỹ thuật và giá cả của {comparisonData.products.length} sản phẩm
          </p>

          {/* Quick stats */}
          <div className="flex flex-wrap gap-4 mt-4">
            <Badge variant="secondary" className="px-3 py-1">
              <Shield className="w-4 h-4 mr-1" />
              {comparisonData.products.length} sản phẩm
            </Badge>
            <Badge variant="secondary" className="px-3 py-1">
              <Zap className="w-4 h-4 mr-1" />
              Cùng danh mục
            </Badge>
            <Badge variant="secondary" className="px-3 py-1">
              <Award className="w-4 h-4 mr-1" />
              Tốt nhất được đánh dấu
            </Badge>
          </div>

          {/* View mode toggle */}
          <div className="flex items-center gap-2 mt-4">
            <span className="text-sm text-muted-foreground mr-2">Chế độ xem:</span>
            <div className="flex bg-muted rounded-lg p-1">
              <Button
                variant={viewMode === 'cards' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setViewMode('cards')}
                className="h-8 px-3"
              >
                <LayoutGrid className="w-4 h-4 mr-1" />
                Cards
              </Button>
              <Button
                variant={viewMode === 'table' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setViewMode('table')}
                className="h-8 px-3"
              >
                <Table className="w-4 h-4 mr-1" />
                Bảng
              </Button>
            </div>
          </div>
        </div>

        {/* Comparison Content */}
        {viewMode === 'cards' ? (
          <div className={`grid gap-6 ${comparisonData.products.length === 2 ? 'grid-cols-1 lg:grid-cols-2' :
              comparisonData.products.length === 3 ? 'grid-cols-1 lg:grid-cols-2 xl:grid-cols-3' :
                'grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4'
            }`}>
            {comparisonData.products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onRemove={removeProduct}
                onBuyNow={handleBuyNow}
                isHighlighted={product.id === bestValueProductId}
              />
            ))}
          </div>
        ) : (
          <ComparisonTable
            products={comparisonData.products}
            onRemoveProduct={removeProduct}
            onBuyNow={handleBuyNow}
          />
        )}

        {/* Add more products */}
        {comparisonData.products.length < 4 && (
          <div className="mt-8 text-center">
            <p className="text-muted-foreground mb-4">
              Bạn có thể so sánh tối đa 4 sản phẩm cùng lúc
            </p>
            <Link href="/products">
              <Button variant="outline" size="lg">
                Thêm sản phẩm khác
              </Button>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}

export default function ProductComparePage() {
  return (
    <Suspense fallback={<ComparisonSkeleton count={2} />}>
      <ProductCompareContent />
    </Suspense>
  );
}