'use client';

import Link from 'next/link';
import { ChevronRight } from 'lucide-react';
import { ProductCard } from './products/NewProductCard';
import { useFeaturedProducts } from '@/hooks/useProducts';
import { useWishlistStore } from '@/store';
import { useCartActions } from '@/hooks/useCartActions';
import { toast } from 'sonner';

export function FeaturedProducts() {
  const { data: featuredProducts, isLoading, error } = useFeaturedProducts({ limit: 8 });
  const { addToCart } = useCartActions();
  const { toggleItem: toggleWishlist, isInWishlist } = useWishlistStore();

  const handleAddToCart = (productId: number) => {
    const product = featuredProducts?.find(p => p.id === productId);
    if (product) {
      addToCart(product);
    }
  };

  const handleToggleWishlist = (productId: number) => {
    const product = featuredProducts?.find(p => p.id === productId);
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
    <section className="py-8 md:py-12">
      <div className="max-w-7xl mx-auto px-4">
        <div className="mb-3 flex items-center justify-between">
          <div>
            <h2 className="text-xl md:text-2xl font-bold text-foreground">
              Điện thoại nổi bật
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Lựa chọn được nhiều khách hàng quan tâm trong tuần qua
            </p>
          </div>
          <Link
            href="/products/featured"
            className="text-primary hover:underline flex items-center gap-1 text-sm font-semibold"
          >
            Xem tất cả <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive bg-destructive/10 p-6 text-center text-destructive">
            Có lỗi xảy ra khi tải sản phẩm nổi bật.
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
        ) : featuredProducts && featuredProducts.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
            {featuredProducts.map((product) => (
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
            Chưa có sản phẩm nổi bật.
          </div>
        )}
      </div>
    </section>
  );
}
