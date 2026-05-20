'use client';

import { Suspense } from 'react';
import { ProductViewPage } from '@/components/features/products/ProductViewPage';
import { Sparkles, Loader2 } from 'lucide-react';
import { getNewArrivalsPaginated } from '@/services/new-product.service';

const SORT_OPTIONS = [
  { value: 'createdAt:desc', label: 'Mới nhất trước', sortBy: 'createdAt', sortDirection: 'desc' as const },
  { value: 'createdAt:asc', label: 'Cũ nhất trước', sortBy: 'createdAt', sortDirection: 'asc' as const },
  { value: 'price:asc', label: 'Giá: Thấp đến cao', sortBy: 'price', sortDirection: 'asc' as const },
  { value: 'price:desc', label: 'Giá: Cao đến thấp', sortBy: 'price', sortDirection: 'desc' as const },
  { value: 'rating:desc', label: 'Đánh giá cao nhất', sortBy: 'rating', sortDirection: 'desc' as const },
  { value: 'name:asc', label: 'Tên: A-Z', sortBy: 'name', sortDirection: 'asc' as const },
  { value: 'name:desc', label: 'Tên: Z-A', sortBy: 'name', sortDirection: 'desc' as const },
];

function NewArrivalsPageContent() {
  return (
    <ProductViewPage
      title="Sản phẩm mới nhất"
      subtitle="Những sản phẩm vừa ra mắt, cập nhật công nghệ mới nhất"
      icon={<Sparkles className="w-6 h-6 text-white" />}
      iconBgClass="bg-gradient-to-br from-blue-500 to-cyan-500"
      headerGradient="bg-gradient-to-r from-blue-500/10 via-cyan-500/10 to-teal-500/10"
      emptyIcon={<Sparkles className="w-12 h-12 text-muted-foreground mb-4 mx-auto" />}
      emptyTitle="Chưa có sản phẩm mới"
      emptyDescription="Các sản phẩm mới sẽ được cập nhật sớm"
      basePath="/products/new-arrivals"
      defaultSort="createdAt:desc"
      sortOptions={SORT_OPTIONS}
      fetchProducts={getNewArrivalsPaginated}
    />
  );
}

export default function NewArrivalsPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    }>
      <NewArrivalsPageContent />
    </Suspense>
  );
}
