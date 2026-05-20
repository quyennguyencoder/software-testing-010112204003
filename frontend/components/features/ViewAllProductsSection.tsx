'use client';

import Link from 'next/link';
import { ArrowRight, ShoppingBag } from 'lucide-react';
import { Button } from '@/components/ui/button';

export function ViewAllProductsSection() {
  return (
    <section className="py-12 md:py-16 bg-gradient-to-r from-primary/5 via-primary/10 to-primary/5">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex flex-col items-center text-center">
          <ShoppingBag className="w-12 h-12 text-primary mb-4" />
          <h2 className="text-2xl md:text-3xl font-bold text-foreground mb-2">
            Khám phá thêm nhiều sản phẩm
          </h2>
          <p className="text-muted-foreground mb-6 max-w-md">
            Hàng ngàn sản phẩm đang chờ bạn khám phá. Tìm kiếm điện thoại phù hợp với nhu cầu của bạn.
          </p>
          <Link href="/products">
            <Button size="lg" className="gap-2 px-8 py-6 text-lg font-semibold">
              <span>Xem tất cả sản phẩm</span>
              <ArrowRight className="w-5 h-5" />
            </Button>
          </Link>
        </div>
      </div>
    </section>
  );
}
