'use client';

import Link from 'next/link';
import { ChevronRight, Flame, TrendingUp } from 'lucide-react';
import { ProductCard, ProductCardSkeleton } from './products/NewProductCard';
import { useBestSellingProducts } from '@/hooks/useProducts';
import { Badge } from '@/components/ui/badge';
import { useWishlistStore } from '@/store';
import { useCartActions } from '@/hooks/useCartActions';
import { toast } from 'sonner';

export function BestSellingSection() {
  const { data: bestSellingProducts, isLoading, error } = useBestSellingProducts({ limit: 8 });
  const { addToCart } = useCartActions();
  const { toggleItem: toggleWishlist, isInWishlist } = useWishlistStore();

  const handleAddToCart = (productId: number) => {
    const product = bestSellingProducts?.find(p => p.id === productId);
    if (product) {
      addToCart(product);
    }
  };

  const handleToggleWishlist = (productId: number) => {
    const product = bestSellingProducts?.find(p => p.id === productId);
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
    <section className="py-8 md:py-12 bg-gradient-to-b from-orange-50/50 to-white">
      <div className="max-w-7xl mx-auto px-4">
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-gradient-to-br from-orange-500 to-red-500 rounded-lg shadow-md">
              <Flame className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl md:text-2xl font-bold text-foreground">
                  Sản phẩm bán chạy
                </h2>
                <Badge variant="outline" className="bg-orange-100 text-orange-700 border-orange-200">
                  <TrendingUp className="w-3 h-3 mr-1" />
                  HOT
                </Badge>
              </div>
              <p className="mt-1 text-sm text-muted-foreground">
                Top sản phẩm được mua nhiều nhất
              </p>
            </div>
          </div>
          <Link
            href="/products/best-selling"
            className="text-primary hover:underline flex items-center gap-1 text-sm font-semibold"
          >
            Xem tất cả <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {error && (
          <div className="rounded-lg border border-destructive bg-destructive/10 p-6 text-center text-destructive">
            Có lỗi xảy ra khi tải sản phẩm bán chạy.
          </div>
        )}

        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
            {Array.from({ length: 8 }).map((_, index) => (
              <ProductCardSkeleton key={index} />
            ))}
          </div>
        ) : bestSellingProducts && bestSellingProducts.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
            {bestSellingProducts.map((product, index) => (
              <div key={product.id} className="relative">
                {/* Ranking badge for top 3 */}
                {index < 3 && (
                  <div className={`absolute -top-2 -left-2 z-10 w-7 h-7 rounded-full flex items-center justify-center text-white font-bold text-xs shadow-lg ${index === 0 ? 'bg-gradient-to-br from-yellow-400 to-yellow-600' :
                      index === 1 ? 'bg-gradient-to-br from-gray-300 to-gray-500' :
                        'bg-gradient-to-br from-orange-400 to-orange-600'
                    }`}>
                    {index + 1}
                  </div>
                )}
                <ProductCard
                  product={product}
                  onAddToCart={handleAddToCart}
                  onToggleWishlist={handleToggleWishlist}
                  isInWishlist={isInWishlist(product.id)}
                />
              </div>
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-dashed border-border bg-secondary/60 p-6 text-center text-muted-foreground">
            <Flame className="w-12 h-12 mx-auto mb-2 text-muted-foreground/50" />
            <p>Chưa có dữ liệu bán chạy. Dữ liệu sẽ cập nhật khi có đơn hàng.</p>
          </div>
        )}
      </div>
    </section>
  );
}
