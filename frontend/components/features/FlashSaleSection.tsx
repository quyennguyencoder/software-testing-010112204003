'use client';

import Link from 'next/link';
import { ChevronRight, Zap, Flame, Tag } from 'lucide-react';
import { ProductCard } from './products/NewProductCard';
import { useProductsOnSalePaginated } from '@/hooks/useProducts';
import { useWishlistStore } from '@/store';
import { useCartActions } from '@/hooks/useCartActions';
import { toast } from 'sonner';

export function FlashSaleSection() {
  const { data: saleProducts, isLoading, error } = useProductsOnSalePaginated({ limit: 8 });
  const { addToCart } = useCartActions();
  const { toggleItem: toggleWishlist, isInWishlist } = useWishlistStore();

  const handleAddToCart = (productId: number) => {
    const product = saleProducts?.find(p => p.id === productId);
    if (product) {
      addToCart(product);
    }
  };

  const handleToggleWishlist = (productId: number) => {
    const product = saleProducts?.find(p => p.id === productId);
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
    <section className="relative py-10 md:py-14 bg-gradient-to-r from-red-500/10 via-orange-500/10 to-yellow-500/10 overflow-hidden">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute inset-0" style={{
          backgroundImage: `radial-gradient(circle at 2px 2px, currentColor 1px, transparent 0)`,
          backgroundSize: '40px 40px'
        }} />
      </div>

      <div className="relative max-w-7xl mx-auto px-4">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <span className="inline-flex items-center gap-2 rounded-full bg-gradient-to-r from-red-500 to-orange-500 text-white px-5 py-2 text-sm font-bold tracking-wide uppercase shadow-xl animate-pulse">
              <Zap className="h-5 w-5" />
              <span>Flash Sale</span>
              <Flame className="h-5 w-5" />
            </span>
            <div className="hidden sm:flex items-center gap-2 rounded-full bg-white/90 backdrop-blur-sm px-4 py-2 text-sm font-semibold shadow-lg border border-orange-200">
              <Tag className="h-4 w-4 text-red-500" />
              <span className="font-bold text-red-600">
                Giảm đến 50%
              </span>
            </div>
          </div>
          <Link
            href="/products/flash-sale"
            className="flex items-center gap-1 text-sm font-semibold text-primary hover:text-primary/80 transition-colors group"
          >
            Xem tất cả <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </Link>
        </div>

        {error && (
          <div className="rounded-2xl border border-destructive bg-destructive/10 p-6 text-center text-destructive">
            Có lỗi xảy ra khi tải sản phẩm khuyến mãi.
          </div>
        )}

        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
            {Array.from({ length: 8 }).map((_, index) => (
              <div key={index} className="animate-pulse">
                <div className="aspect-square bg-secondary rounded-2xl mb-2"></div>
                <div className="h-4 bg-secondary rounded mb-1"></div>
                <div className="h-3 bg-secondary rounded w-3/4"></div>
              </div>
            ))}
          </div>
        ) : saleProducts && saleProducts.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
            {saleProducts.map((product) => (
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
          <div className="rounded-2xl border border-dashed border-border bg-secondary/60 p-8 text-center text-muted-foreground">
            Hiện chưa có sản phẩm đang Flash Sale.
          </div>
        )}
      </div>
    </section>
  );
}
