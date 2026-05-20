"use client";

import {
  HeroBanner,
  FlashSaleSection,
  FeaturedProducts,
  BestSellingSection,
  NewArrivalsSection,
  ViewAllProductsSection,
  QuickLinksBar,
} from "@/components/features";

export default function HomePage() {
  return (
    <>
      {/* Hero Banner - Samsung Galaxy Z Fold6 */}
      <HeroBanner
        productId={26}
        productName="Samsung Galaxy Z Fold6 5G"
        productImage="https://cdn.tgdd.vn/Products/Images/42/320721/samsung-galaxy-z-fold6-thumb-1-600x600.jpg"
        description="Thiết kế mỏng nhẹ đột phá, màn hình gập đỉnh cao cùng sức mạnh AI vượt trội với Galaxy AI."
        salePrice={29990000}
        originalPrice={43990000}
        badge="SIÊU PHẨM AI"
      />

      {/* Quick Navigation Bar - Sticky */}
      <section className="border-b bg-background/95 sticky top-16 z-40 backdrop-blur-sm shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <QuickLinksBar />
        </div>
      </section>

      {/* Flash Sale - Ưu tiên cao vì urgency */}
      <FlashSaleSection />

      {/* Sản phẩm nổi bật */}
      <FeaturedProducts />

      {/* Sản phẩm bán chạy */}
      <BestSellingSection />

      {/* Sản phẩm mới nhất */}
      <NewArrivalsSection />

      {/* Nút xem tất cả sản phẩm */}
      <ViewAllProductsSection />
    </>
  );
}
