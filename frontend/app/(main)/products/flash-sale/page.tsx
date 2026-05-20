'use client';

import React, { Suspense, useState, useEffect } from 'react';
import { ProductViewPage } from '@/components/features/products/ProductViewPage';
import { Zap, Clock, Loader2 } from 'lucide-react';
import { getOnSaleProductsPaginated } from '@/services/new-product.service';

const SORT_OPTIONS = [
  { value: 'discountPercentage:desc', label: 'Giảm giá nhiều nhất', sortBy: 'discountPercentage', sortDirection: 'desc' as const },
  { value: 'discountPercentage:asc', label: 'Giảm giá ít nhất', sortBy: 'discountPercentage', sortDirection: 'asc' as const },
  { value: 'price:asc', label: 'Giá: Thấp đến cao', sortBy: 'price', sortDirection: 'asc' as const },
  { value: 'price:desc', label: 'Giá: Cao đến thấp', sortBy: 'price', sortDirection: 'desc' as const },
  { value: 'soldCount:desc', label: 'Bán chạy nhất', sortBy: 'soldCount', sortDirection: 'desc' as const },
  { value: 'rating:desc', label: 'Đánh giá cao nhất', sortBy: 'rating', sortDirection: 'desc' as const },
];

// Countdown Timer Component
function CountdownTimer() {
  const [timeLeft, setTimeLeft] = useState({ hours: 2, minutes: 45, seconds: 30 });

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        let { hours, minutes, seconds } = prev;
        if (seconds > 0) {
          seconds--;
        } else if (minutes > 0) {
          minutes--;
          seconds = 59;
        } else if (hours > 0) {
          hours--;
          minutes = 59;
          seconds = 59;
        } else {
          hours = 2; minutes = 45; seconds = 30; // Reset
        }
        return { hours, minutes, seconds };
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (num: number) => String(num).padStart(2, '0');

  return (
    <div className="flex items-center gap-2 bg-gradient-to-r from-red-500 to-orange-500 text-white rounded-xl px-4 py-3 shadow-lg animate-pulse-slow">
      <Clock className="w-5 h-5" />
      <span className="text-sm font-medium mr-2 hidden sm:inline">Kết thúc sau:</span>
      <div className="flex items-center gap-1 font-mono font-bold text-lg">
        <span className="bg-white/20 rounded px-2 py-1">{formatTime(timeLeft.hours)}</span>
        <span>:</span>
        <span className="bg-white/20 rounded px-2 py-1">{formatTime(timeLeft.minutes)}</span>
        <span>:</span>
        <span className="bg-white/20 rounded px-2 py-1">{formatTime(timeLeft.seconds)}</span>
      </div>
    </div>
  );
}

function FlashSalePageContent() {
  return (
    <ProductViewPage
      title="Flash Sale ⚡"
      subtitle="Giảm giá sốc - Số lượng có hạn! Nhanh tay kẻo lỡ"
      icon={<Zap className="w-6 h-6 text-white" />}
      iconBgClass="bg-gradient-to-br from-red-500 to-orange-500 animate-pulse"
      headerGradient="bg-gradient-to-r from-red-500/15 via-orange-500/15 to-yellow-500/15"
      emptyIcon={<Zap className="w-12 h-12 text-muted-foreground mb-4 mx-auto" />}
      emptyTitle="Chưa có sản phẩm Flash Sale"
      emptyDescription="Các sản phẩm giảm giá sẽ được cập nhật sớm"
      basePath="/products/flash-sale"
      defaultSort="discountPercentage:desc"
      sortOptions={SORT_OPTIONS}
      customHeaderContent={<CountdownTimer />}
      fetchProducts={getOnSaleProductsPaginated}
    />
  );
}

export default function FlashSalePage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    }>
      <FlashSalePageContent />
    </Suspense>
  );
}
