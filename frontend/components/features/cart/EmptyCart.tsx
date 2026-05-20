import { Button } from '@/components/ui/button';
import { ShoppingBag, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export function EmptyCart() {
  return (
    <div className="text-center py-20">
      <div className="relative mb-8">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-100 to-purple-100 rounded-full w-32 h-32 mx-auto opacity-50"></div>
        <ShoppingBag className="relative mx-auto h-32 w-32 text-gray-300" />
      </div>

      <h1 className="text-3xl font-bold text-gray-900 mb-4">Giỏ hàng của bạn đang trống</h1>
      <p className="text-gray-600 mb-8 max-w-md mx-auto">
        Hãy khám phá các sản phẩm tuyệt vời của chúng tôi và thêm vào giỏ hàng để bắt đầu mua sắm!
      </p>

      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <Button asChild size="lg" className="px-8">
          <Link href="/products" className="flex items-center gap-2">
            Tiếp tục mua sắm
            <ArrowRight className="h-4 w-4" />
          </Link>
        </Button>

        <Button asChild variant="outline" size="lg" className="px-8">
          <Link href="/products?featured=true">
            Xem sản phẩm nổi bật
          </Link>
        </Button>
      </div>
    </div>
  );
}