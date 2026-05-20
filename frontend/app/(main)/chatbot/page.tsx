"use client";

import { useEffect } from "react";
import Link from "next/link";
import { ChatbotAssistant } from "@/components/common/ChatbotAssistant";
import { FlashSaleSection, BestSellingSection } from "@/components/features";
import { 
  Star, 
  TrendingUp, 
  Sparkles, 
  Tag, 
  Crown,
  ArrowRight,
  Smartphone,
  Zap
} from "lucide-react";

// Quick Actions - Truy cập nhanh tính năng
const QUICK_ACTIONS = [
  { 
    id: 'featured', 
    label: 'Nổi bật', 
    icon: Star, 
    href: '/products/featured',
    color: 'bg-amber-500 hover:bg-amber-600',
  },
  { 
    id: 'bestselling', 
    label: 'Bán chạy', 
    icon: TrendingUp, 
    href: '/products/best-selling',
    color: 'bg-red-500 hover:bg-red-600',
  },
  { 
    id: 'new', 
    label: 'Mới về', 
    icon: Sparkles, 
    href: '/products/new-arrivals',
    color: 'bg-green-500 hover:bg-green-600',
  },
  { 
    id: 'sale', 
    label: 'Đang giảm', 
    icon: Tag, 
    href: '/products/flash-sale',
    color: 'bg-orange-500 hover:bg-orange-600',
  },
  { 
    id: 'flagship', 
    label: 'Cao cấp', 
    icon: Crown, 
    href: '/products?minPrice=20000000',
    color: 'bg-amber-500 hover:bg-amber-600',
  },
];

/**
 * Trang chatbot tư vấn sản phẩm - Tối ưu UI/UX
 * Route: /chatbot
 */
export default function ChatbotPage() {
  // Scroll lên đầu khi vào trang
  useEffect(() => {
    if (typeof window !== "undefined") {
      window.scrollTo({ top: 0, behavior: "instant" as ScrollBehavior });
    }
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted/30">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border-b">
        <div className="max-w-7xl mx-auto px-4 py-6 md:py-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center">
                <Smartphone className="w-5 h-5 text-primary-foreground" />
              </div>
              <h1 className="text-2xl md:text-3xl font-bold tracking-tight">
                Trợ lý tư vấn AI
              </h1>
            </div>
          </div>
        </div>
      </section>

      {/* Quick Actions Bar */}
      

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 py-6">
        {/* Chatbot - Full Width, Taller */}
        <div className="mb-8">
          <ChatbotAssistant className="h-[600px] md:h-[650px]" />
        </div>

        {/* Divider */}
        <div className="flex items-center gap-4 my-8">
          <div className="flex-1 h-px bg-border"></div>
          <span className="text-sm font-large text-muted-foreground px-4 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary" />
            Khám phá thêm sản phẩm
          </span>
          <div className="flex-1 h-px bg-border"></div>
        </div>
        {/* Featured Products Section */}
        <div className="space-y-8">
          {/* Flash Sale */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold flex items-center gap-2">
                <Zap className="w-5 h-5 text-orange-500" />
                Flash Sale
              </h2>
              <Link 
                href="/products/flash-sale" 
                className="text-sm text-primary hover:underline flex items-center gap-1"
              >
                Xem tất cả <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
            <FlashSaleSection />
          </section>

          {/* Best Selling */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold flex items-center gap-2">
                <TrendingUp className="w-5 h-5 text-red-500" />
                Sản phẩm bán chạy
              </h2>
              <Link 
                href="/products/best-selling" 
                className="text-sm text-primary hover:underline flex items-center gap-1"
              >
                Xem tất cả <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
            <BestSellingSection />
          </section>
        </div>

        {/* CTA Section */}
        <section className="mt-12 text-center py-8 px-4 rounded-2xl bg-gradient-to-r from-primary/10 via-primary/5 to-primary/10 border">
          <h3 className="text-lg font-bold mb-2">Không tìm thấy điện thoại ưng ý?</h3>
          <p className="text-sm text-muted-foreground mb-4">
            Khám phá toàn bộ danh mục sản phẩm của chúng tôi
          </p>
          <Link
            href="/products"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-primary text-primary-foreground font-medium hover:bg-primary/90 transition-colors"
          >
            <Smartphone className="w-4 h-4" />
            Xem tất cả sản phẩm
            <ArrowRight className="w-4 h-4" />
          </Link>
        </section>
      </div>
    </div>
  );
}


