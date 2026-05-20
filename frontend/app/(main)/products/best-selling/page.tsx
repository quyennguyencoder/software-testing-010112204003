'use client';

import { Suspense } from 'react';
import { ProductViewPage } from '@/components/features/products/ProductViewPage';
import { Flame, Loader2 } from 'lucide-react';
import { getBestSellingProductsPaginated } from '@/services/new-product.service';

const SORT_OPTIONS = [
  { value: 'soldCount:desc', label: 'Bán chạy nhất', sortBy: 'soldCount', sortDirection: 'desc' as const },
  { value: 'soldCount:asc', label: 'Bán ít nhất', sortBy: 'soldCount', sortDirection: 'asc' as const },
  { value: 'price:asc', label: 'Giá: Thấp đến cao', sortBy: 'price', sortDirection: 'asc' as const },
  { value: 'price:desc', label: 'Giá: Cao đến thấp', sortBy: 'price', sortDirection: 'desc' as const },
  { value: 'rating:desc', label: 'Đánh giá cao nhất', sortBy: 'rating', sortDirection: 'desc' as const },
  { value: 'name:asc', label: 'Tên: A-Z', sortBy: 'name', sortDirection: 'asc' as const },
  { value: 'name:desc', label: 'Tên: Z-A', sortBy: 'name', sortDirection: 'desc' as const },
];

function BestSellingPageContent() {
  return (
    <ProductViewPage
      title="Sản phẩm bán chạy"
      subtitle="Những sản phẩm được yêu thích và mua nhiều nhất tại cửa hàng"
      icon={<Flame className="w-6 h-6 text-white" />}
      iconBgClass="bg-gradient-to-br from-orange-500 to-red-500"
      headerGradient="bg-gradient-to-r from-orange-500/10 via-red-500/10 to-pink-500/10"
      emptyIcon={<Flame className="w-12 h-12 text-muted-foreground mb-4 mx-auto" />}
      emptyTitle="Chưa có dữ liệu bán chạy"
      emptyDescription="Dữ liệu sẽ được cập nhật khi có đơn hàng"
      basePath="/products/best-selling"
      defaultSort="soldCount:desc"
      sortOptions={SORT_OPTIONS}
      fetchProducts={getBestSellingProductsPaginated}
    />
  );
}

export default function BestSellingPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    }>
      <BestSellingPageContent />
    </Suspense>
  );
}

