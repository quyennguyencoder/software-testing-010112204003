'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { useWishlistStore } from '@/store/wishlistStore';
import { useCartStore } from '@/store/cartStore';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { 
  Heart, 
  ShoppingCart, 
  Trash2, 
  ArrowLeft,
  Sparkles,
  Package
} from 'lucide-react';
import { toast } from 'sonner';

export default function WishlistPage() {
  const { items, removeItem, clearWishlist } = useWishlistStore();
  const { addItem: addToCart } = useCartStore();

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const handleAddToCart = (item: typeof items[0]) => {
    addToCart({
      productId: item.productId,
      productName: item.productName,
      price: item.price,
      quantity: 1,
      productImage: item.productImage,
    });
    toast.success('Đã thêm vào giỏ hàng!');
  };

  const handleRemoveItem = (id: number, name: string) => {
    removeItem(id);
    toast.success(`Đã xóa "${name}" khỏi danh sách yêu thích`);
  };

  if (items.length === 0) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center px-4">
        <div className="text-center max-w-md">
          <div className="w-24 h-24 mx-auto mb-6 rounded-full bg-red-50 flex items-center justify-center">
            <Heart className="w-12 h-12 text-red-300" />
          </div>
          <h1 className="text-2xl font-bold text-foreground mb-2">
            Danh sách yêu thích trống
          </h1>
          <p className="text-muted-foreground mb-6">
            Bạn chưa thêm sản phẩm nào vào danh sách yêu thích. 
            Khám phá các sản phẩm và thêm vào để theo dõi!
          </p>
          <div className="flex flex-col sm:flex-row gap-3 justify-center">
            <Link href="/products">
              <Button size="lg" className="gap-2">
                <Package className="w-5 h-5" />
                Xem sản phẩm
              </Button>
            </Link>
            <Link href="/products/flash-sale">
              <Button size="lg" variant="outline" className="gap-2">
                <Sparkles className="w-5 h-5" />
                Xem khuyến mãi
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-6 md:py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div className="flex items-center gap-3">
          <Link href="/products" className="p-2 rounded-full hover:bg-muted transition-colors">
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold flex items-center gap-2">
              <Heart className="w-6 h-6 text-red-500 fill-red-500" />
              Sản phẩm yêu thích
            </h1>
            <p className="text-sm text-muted-foreground">
              {items.length} sản phẩm trong danh sách
            </p>
          </div>
        </div>
        
        {items.length > 0 && (
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => {
              clearWishlist();
              toast.success('Đã xóa tất cả sản phẩm yêu thích');
            }}
            className="gap-2 text-destructive hover:text-destructive"
          >
            <Trash2 className="w-4 h-4" />
            Xóa tất cả
          </Button>
        )}
      </div>

      {/* Product Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 md:gap-6">
        {items.map((item) => (
          <Card key={item.id} className="overflow-hidden group hover:shadow-lg transition-shadow">
            <Link href={`/products/${item.productId}`}>
              <div className="relative aspect-square bg-gray-100">
                <Image
                  src={item.productImage || '/placeholder-product.png'}
                  alt={item.productName}
                  fill
                  className="object-cover group-hover:scale-105 transition-transform duration-300"
                />
                {/* Remove button */}
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    handleRemoveItem(item.id, item.productName);
                  }}
                  className="absolute top-3 right-3 p-2 rounded-full bg-white shadow-md hover:bg-red-50 transition-colors"
                >
                  <Heart className="w-5 h-5 fill-red-500 text-red-500" />
                </button>
              </div>
            </Link>
            
            <CardContent className="p-4">
              <Link href={`/products/${item.productId}`}>
                <h3 className="font-semibold text-sm line-clamp-2 mb-2 hover:text-primary transition-colors">
                  {item.productName}
                </h3>
              </Link>
              
              <div className="flex items-center justify-between gap-2">
                <span className="text-lg font-bold text-primary">
                  {formatPrice(item.price)}
                </span>
                <Button
                  size="sm"
                  onClick={() => handleAddToCart(item)}
                  className="gap-1"
                >
                  <ShoppingCart className="w-4 h-4" />
                  <span className="hidden sm:inline">Thêm giỏ</span>
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="mt-8 p-6 rounded-xl bg-gradient-to-r from-primary/5 to-primary/10 border">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            <h3 className="font-semibold text-lg">Tiếp tục mua sắm?</h3>
            <p className="text-sm text-muted-foreground">
              Khám phá thêm nhiều sản phẩm hấp dẫn khác
            </p>
          </div>
          <div className="flex gap-3">
            <Link href="/products/flash-sale">
              <Button variant="outline" className="gap-2">
                <Sparkles className="w-4 h-4" />
                Flash Sale
              </Button>
            </Link>
            <Link href="/products">
              <Button className="gap-2">
                <Package className="w-4 h-4" />
                Tất cả sản phẩm
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
