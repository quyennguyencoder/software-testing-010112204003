/**
 * CustomerWishlist component - Wishlist management for customers
 * Uses Zustand wishlistStore for real data
 */

'use client';

import { useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Heart, ShoppingCart, Package, Sparkles } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { formatPrice } from '@/lib/utils';
import { useWishlistStore } from '@/store/wishlistStore';
import { useCartStore } from '@/store/cartStore';
import { toast } from 'sonner';

export function CustomerWishlist() {
  const { items, removeItem, loadUserWishlist } = useWishlistStore();
  const { addItem: addToCart } = useCartStore();

  useEffect(() => {
    loadUserWishlist();
  }, [loadUserWishlist]);

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
      <div className="flex flex-col items-center justify-center py-12 px-4">
        <div className="text-center max-w-md">
          <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-red-50 flex items-center justify-center">
            <Heart className="w-10 h-10 text-red-300" />
          </div>
          <h3 className="text-lg font-semibold text-foreground mb-2">
            Danh sách yêu thích trống
          </h3>
          <p className="text-muted-foreground mb-4 text-sm">
            Bạn chưa thêm sản phẩm nào vào danh sách yêu thích.
          </p>
          <div className="flex flex-col sm:flex-row gap-2 justify-center">
            <Link href="/products">
              <Button size="sm" className="gap-2">
                <Package className="w-4 h-4" />
                Xem sản phẩm
              </Button>
            </Link>
            <Link href="/products/flash-sale">
              <Button size="sm" variant="outline" className="gap-2">
                <Sparkles className="w-4 h-4" />
                Xem khuyến mãi
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-foreground">
          Sản phẩm yêu thích ({items.length})
        </h3>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {items.map((item) => (
          <div
            key={item.id}
            className="group overflow-hidden rounded-xl border border-border bg-card shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-lg"
          >
            <Link href={`/products/${item.productId}`}>
              <div className="relative">
                <div className="flex h-32 items-center justify-center bg-secondary md:h-40 overflow-hidden">
                  {item.productImage ? (
                    <Image
                      src={item.productImage}
                      alt={item.productName}
                      width={160}
                      height={160}
                      className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300"
                    />
                  ) : (
                    <Package className="w-12 h-12 text-muted-foreground" />
                  )}
                </div>
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    handleRemoveItem(item.id, item.productName);
                  }}
                  className="absolute top-2 right-2 rounded-full bg-card p-2 shadow-md transition-all hover:bg-red-50"
                  aria-label="Bỏ khỏi yêu thích"
                >
                  <Heart className="w-4 h-4 text-red-500 fill-red-500" />
                </button>
              </div>
            </Link>
            <div className="p-3">
              <Link href={`/products/${item.productId}`}>
                <h4 className="font-medium text-foreground text-sm line-clamp-2 mb-2 hover:text-primary transition-colors">
                  {item.productName}
                </h4>
              </Link>
              <p className="text-primary font-bold">{formatPrice(item.price)}</p>
              <Button
                size="sm"
                className="w-full mt-2"
                onClick={() => handleAddToCart(item)}
              >
                <ShoppingCart className="w-4 h-4 mr-1" />
                Thêm giỏ
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
