'use client';

import Link from 'next/link';
import { ChevronRight, Sparkles } from 'lucide-react';
import { ProductCard } from './products/NewProductCard';
import { useNewArrivals } from '@/hooks/useProducts';
import { useWishlistStore } from '@/store';
import { useCartActions } from '@/hooks/useCartActions';
import { toast } from 'sonner';

export function NewArrivalsSection() {
  const { data: newProducts, isLoading, error } = useNewArrivals({ limit: 8 });
  const { addToCart } = useCartActions();
  const { toggleItem: toggleWishlist, isInWishlist } = useWishlistStore();

  const handleAddToCart = (productId: number) => {
    const product = newProducts?.find(p => p.id === productId);
    if (product) {
      addToCart(product);
    }
  };

  const handleToggleWishlist = (productId: number) => {
    const product = newProducts?.find(p => p.id === productId);
    if (product) {
      const wasInWishlist = isInWishlist(productId);
      toggleWishlist({
        productId: product.id,
        productName: product.name,
        price: product.discountedPrice || product.originalPrice,
        productImage: product.thumbnailUrl || '',
        inStock: product.inStock,
      });
      toast.success(wasInWishlist ? 'Đã xóa khỏi yêu thích' : 'Đã thêm vào yêu thích');
    }
  };

  return (
    <section className="py-8 md:py-12 bg-gradient-to-b from-background to-muted/30">
      <div className="max-w-7xl mx-auto px-4">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Sparkles className="w-5 h-5 text-blue-500" />
              <h2 className="text-xl md:text-2xl font-bold text-foreground">
                Sản phẩm mới nhất
              </h2>
            </div>
            <p className="text-sm text-muted-foreground">
              Những sản phẩm vừa ra mắt, cập nhật công nghệ mới nhất
            </p>
          </div>
          <Link
            href="/products/new-arrivals"
            className="text-primary hover:underline flex items-center gap-1 text-sm font-semibold"
          >
            Xem tất cả <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive bg-destructive/10 p-6 text-center text-destructive">
            Có lỗi xảy ra khi tải sản phẩm mới.
          </div>
        )}

        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
            {Array.from({ length: 8 }).map((_, index) => (
              <div key={index} className="animate-pulse">
                <div className="aspect-square bg-secondary rounded-lg mb-2"></div>
                <div className="h-4 bg-secondary rounded mb-1"></div>
                <div className="h-3 bg-secondary rounded w-3/4"></div>
              </div>
            ))}
          </div>
        ) : newProducts && newProducts.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
            {newProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={handleAddToCart}
                onToggleWishlist={handleToggleWishlist}
                isInWishlist={isInWishlist(product.id)}
              />
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-dashed border-border bg-secondary/60 p-6 text-center text-muted-foreground">
            Chưa có sản phẩm mới.
          </div>
        )}
      </div>
    </section>
  );
}
