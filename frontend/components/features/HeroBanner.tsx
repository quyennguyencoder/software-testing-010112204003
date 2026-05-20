'use client';

import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { ChevronRight, Bot, Sparkles, Zap, MessageCircle, Flame, Smartphone } from 'lucide-react';
import { formatPrice } from '@/lib/utils';
import { useCartActions } from '@/hooks/useCartActions';

interface HeroBannerProps {
  productId?: number;
  productName?: string;
  productImage?: string;
  description?: string;
  salePrice?: number;
  originalPrice?: number;
  badge?: string;
}

export function HeroBanner({
  productId = 1,
  productName = 'iPhone 15 Pro Max',
  productImage = '',
  description = 'Titan. Siêu nhẹ. Siêu bền. Giảm đến 2 triệu khi thu cũ đổi mới.',
  salePrice = 32990000,
  originalPrice = 34990000,
  badge = 'HOT DEAL',
}: HeroBannerProps = {}) {
  const { buyNowWithDetails } = useCartActions();

  // Handle buy now with product details
  const handleBuyNow = () => {
    buyNowWithDetails({
      productId,
      productName,
      productImage: productImage || '',
      price: salePrice,
      quantity: 1,
    });
  };

  return (
    <section className="relative bg-gradient-to-br from-[#0f172a] via-[#1e293b] to-[#0f172a] text-white overflow-hidden">
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary/10 rounded-full blur-3xl animate-pulse" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-primary/10 rounded-full blur-3xl animate-pulse delay-1000" />
      </div>

      <div className="relative max-w-7xl mx-auto px-4 py-8 md:py-12">
        {/* AI Chatbot Highlight Banner - Glassmorphism */}
        <div className="mb-8 p-5 md:p-7 rounded-3xl bg-white/5 backdrop-blur-xl border border-white/10 shadow-2xl">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="relative">
                <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary via-amber-500 to-orange-500 flex items-center justify-center shadow-lg shadow-primary/50 animate-pulse">
                  <Bot className="w-8 h-8 text-white" />
                </div>
                <span className="absolute -top-1 -right-1 flex h-4 w-4">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-4 w-4 bg-green-500 border-2 border-white"></span>
                </span>
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h3 className="text-lg md:text-xl font-bold text-white">Trợ lý AI thông minh</h3>
                  <span className="px-2.5 py-1 rounded-full bg-green-500/20 text-green-400 text-xs font-semibold border border-green-500/30 backdrop-blur-sm">
                    Online
                  </span>
                </div>
                <p className="text-sm text-gray-300 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-yellow-400 animate-pulse" />
                  Tư vấn điện thoại phù hợp với bạn chỉ trong vài giây!
                </p>
              </div>
            </div>
            <Link href="/chatbot">
              <Button
                size="lg"
                className="bg-gradient-to-r from-primary to-amber-500 hover:from-primary/90 hover:to-amber-600 text-white shadow-lg shadow-primary/50 gap-2 group hover:scale-105 transition-all duration-300"
              >
                <MessageCircle className="w-5 h-5 group-hover:animate-bounce" />
                Chat ngay
                <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
          </div>

          {/* Quick prompts - Improved */}
          <div className="mt-4 flex flex-wrap gap-2">
            <span className="text-xs text-gray-400">Thử hỏi:</span>
            {['Điện thoại chụp hình đẹp', 'iPhone giá tốt', 'Samsung pin trâu'].map((prompt, i) => (
              <Link
                key={i}
                href="/chatbot"
                className="px-3 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-xs text-gray-300 hover:text-white transition-all duration-300 border border-white/10 hover:border-white/20 hover:scale-105 backdrop-blur-sm"
              >
                &quot;{prompt}&quot;
              </Link>
            ))}
          </div>
        </div>

        {/* Main Hero Content */}
        <div className="grid md:grid-cols-2 gap-8 items-center">
          <div className="space-y-6">
            <span className="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full text-sm font-bold shadow-lg shadow-orange-500/50 animate-pulse">
              <Flame className="w-4 h-4" />
              {badge}
            </span>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold leading-tight bg-gradient-to-r from-white via-gray-100 to-white bg-clip-text text-transparent">
              {productName}
            </h1>
            <p className="text-lg md:text-xl text-gray-300 leading-relaxed">
              {description}
            </p>
            <div className="flex items-center gap-4">
              <span className="text-4xl md:text-5xl font-extrabold bg-gradient-to-r from-green-400 via-emerald-400 to-green-500 bg-clip-text text-transparent">
                {formatPrice(salePrice)}
              </span>
              <span className="text-xl text-gray-500 line-through">
                {formatPrice(originalPrice)}
              </span>
              <span className="px-3 py-1.5 bg-red-500/20 text-red-400 rounded-xl text-sm font-bold border border-red-500/30 backdrop-blur-sm">
                -{Math.round((1 - salePrice / originalPrice) * 100)}%
              </span>
            </div>
            <div className="flex gap-3">
              <Button
                size="lg"
                className="gap-2 shadow-xl bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 hover:scale-105 transition-all duration-300"
                onClick={handleBuyNow}
              >
                Mua ngay
                <ChevronRight className="w-4 h-4" />
              </Button>
              <Button
                size="lg"
                variant="outline"
                className="border-2 border-white/50 text-white bg-white/10 hover:bg-white hover:text-[#0f172a] transition-all hover:scale-105"
                asChild
              >
                <Link href={`/products/${productId}`}>
                  Xem chi tiết
                </Link>
              </Button>
            </div>
          </div>

          {/* Product Image - Enhanced */}
          <div className="flex justify-center">
            <div className="relative">
              {/* Glow effect - Enhanced */}
              <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-amber-500/20 to-primary/20 rounded-full blur-3xl animate-pulse"></div>
              <div className="relative w-64 h-64 md:w-80 md:h-80 bg-gradient-to-br from-white/10 to-white/5 rounded-3xl flex items-center justify-center shadow-2xl border border-white/20 backdrop-blur-xl">
                {productImage && (productImage.startsWith('/') || productImage.startsWith('http')) ? (
                  <img
                    src={productImage}
                    alt={productName}
                    className="w-full h-full object-contain p-8 drop-shadow-2xl"
                  />
                ) : (
                  <Smartphone className="w-32 h-32 md:w-40 md:h-40 text-white/80 drop-shadow-2xl" />
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
