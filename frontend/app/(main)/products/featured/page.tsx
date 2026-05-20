'use client';

import { Suspense } from 'react';
import { ProductViewPage } from '@/components/features/products/ProductViewPage';
import { TrendingUp, Loader2 } from 'lucide-react';
import { getFeaturedProductsPaginated } from '@/services/new-product.service';

const SORT_OPTIONS = [
  { value: 'soldCount:desc', label: 'Bán chạy nhất', sortBy: 'soldCount', sortDirection: 'desc' as const },
  { value: 'soldCount:asc', label: 'Bán ít nhất', sortBy: 'soldCount', sortDirection: 'asc' as const },
  { value: 'price:asc', label: 'Giá: Thấp đến cao', sortBy: 'price', sortDirection: 'asc' as const },
  { value: 'price:desc', label: 'Giá: Cao đến thấp', sortBy: 'price', sortDirection: 'desc' as const },
  { value: 'rating:desc', label: 'Đánh giá cao nhất', sortBy: 'rating', sortDirection: 'desc' as const },
  { value: 'name:asc', label: 'Tên: A-Z', sortBy: 'name', sortDirection: 'asc' as const },
  { value: 'name:desc', label: 'Tên: Z-A', sortBy: 'name', sortDirection: 'desc' as const },
];

function FeaturedProductsPageContent() {
  return (
    <ProductViewPage
      title="Sản phẩm nổi bật"
      subtitle="Những sản phẩm được khách hàng quan tâm và mua nhiều nhất"
      icon={<TrendingUp className="w-6 h-6 text-white" />}
      iconBgClass="bg-gradient-to-br from-yellow-500 to-orange-500"
      headerGradient="bg-gradient-to-r from-yellow-500/10 via-orange-500/10 to-red-500/10"
      emptyIcon={<TrendingUp className="w-12 h-12 text-muted-foreground mb-4 mx-auto" />}
      emptyTitle="Chưa có sản phẩm nổi bật"
      emptyDescription="Hãy quay lại sau để xem các sản phẩm hot nhất"
      basePath="/products/featured"
      defaultSort="soldCount:desc"
      sortOptions={SORT_OPTIONS}
      fetchProducts={getFeaturedProductsPaginated}
    />
  );
}

export default function FeaturedProductsPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    }>
      <FeaturedProductsPageContent />
    </Suspense>
  );
}
