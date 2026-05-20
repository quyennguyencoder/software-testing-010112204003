/**
 * Promotions Page - Display all active promotions for customers
 * Module M09 - Promotion Module
 */

"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { promotionAPI } from "@/lib/api";
import { PromotionResponse } from "@/types";
import { Button } from "@/components/ui/button";
import { Tag, Calendar, TrendingUp, Percent, ArrowLeft } from "lucide-react";
import { formatPrice } from "@/lib/utils/formatters";

export default function PromotionsPage() {
  const router = useRouter();
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPromotions = async () => {
      try {
        setLoading(true);
        const response = await promotionAPI.getAllActivePromotions();
        if (response.success && response.data) {
          setPromotions(response.data);
        } else {
          throw new Error(response.message || "Không thể tải khuyến mãi");
        }
      } catch (err: any) {
        console.error("Error fetching promotions:", err);
        setError(err.message || "Có lỗi xảy ra");
      } finally {
        setLoading(false);
      }
    };

    fetchPromotions();
  }, []);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN");
  };

  const getPromotionTypeLabel = (type: PromotionResponse["templateType"]) => {
    switch (type) {
      case "DISCOUNT_PERCENTAGE":
        return "Giảm theo %";
      case "DISCOUNT_FIXED":
        return "Giảm cố định";
      case "FREE_SHIPPING":
        return "Miễn phí ship";
      default:
        return type;
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="flex items-center justify-center">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-muted-foreground">Đang tải khuyến mãi...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="text-center">
          <p className="text-red-500 mb-4">{error}</p>
          <Button onClick={() => window.location.reload()}>Thử lại</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <Button
          variant="outline"
          onClick={() => router.push("/")}
          className="mb-4"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Quay lại trang chủ
        </Button>
        <div className="flex items-center gap-3 mb-2">
          <Tag className="w-8 h-8 text-primary" />
          <h1 className="text-3xl font-bold text-foreground">
            Khuyến mãi đang diễn ra
          </h1>
        </div>
        <p className="text-muted-foreground">
          Khám phá các chương trình khuyến mãi hấp dẫn dành cho bạn
        </p>
      </div>

      {/* Promotions Grid */}
      {promotions.length === 0 ? (
        <div className="text-center py-16 bg-card rounded-lg border border-border">
          <Tag className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-foreground mb-2">
            Chưa có khuyến mãi nào
          </h3>
          <p className="text-muted-foreground">
            Hiện tại không có chương trình khuyến mãi nào. Hãy quay lại sau nhé!
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {promotions.map((promo) => (
            <div
              key={promo.id}
              className="bg-card rounded-xl border border-border p-6 hover:shadow-lg transition-all duration-200 hover:-translate-y-1"
            >
              {/* Header */}
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <h3 className="text-xl font-bold text-foreground mb-1">
                    {promo.title}
                  </h3>
                  <span className="inline-block px-3 py-1 bg-primary/10 text-primary text-xs font-semibold rounded-full">
                    {getPromotionTypeLabel(promo.templateType)}
                  </span>
                </div>
                <div className="flex items-center justify-center w-12 h-12 bg-primary/10 rounded-full flex-shrink-0">
                  <Percent className="w-6 h-6 text-primary" />
                </div>
              </div>

              {/* Description */}
              {promo.description && (
                <p className="text-sm text-muted-foreground mb-4 line-clamp-2">
                  {promo.description}
                </p>
              )}

              {/* Promotion Details */}
              <div className="space-y-3 mb-4">
                {/* Discount Value */}
                <div className="flex items-center gap-2 text-sm">
                  <TrendingUp className="w-4 h-4 text-primary flex-shrink-0" />
                  <span className="text-foreground font-semibold">
                    {promo.templateType === "DISCOUNT_PERCENTAGE"
                      ? `Giảm ${promo.percentDiscount}%`
                      : promo.templateType === "DISCOUNT_FIXED"
                      ? `Giảm ${formatPrice(promo.percentDiscount)}`
                      : "Miễn phí vận chuyển"}
                  </span>
                </div>

                {/* Min Order Value */}
                {promo.minValueToBeApplied != null && promo.minValueToBeApplied > 0 && (
                  <div className="flex items-center gap-2 text-sm">
                    <Tag className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                    <span className="text-muted-foreground">
                      Đơn tối thiểu: {formatPrice(promo.minValueToBeApplied)}
                    </span>
                  </div>
                )}

                {/* Date Range */}
                <div className="flex items-center gap-2 text-sm">
                  <Calendar className="w-4 h-4 text-muted-foreground flex-shrink-0" />
                  <span className="text-muted-foreground">
                    {formatDate(promo.effectiveDate)} -{" "}
                    {formatDate(promo.expirationDate)}
                  </span>
                </div>
              </div>

              {/* Action Button */}
              <Button
                onClick={() => router.push("/")}
                className="w-full"
                variant="default"
              >
                Mua sắm ngay
              </Button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
