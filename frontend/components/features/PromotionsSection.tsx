/**
 * PromotionsSection component - Display active promotions on homepage
 */

"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Tag, ArrowRight, Calendar, Package } from "lucide-react";
import { promotionAPI } from "@/lib/api";
import type { PromotionResponse } from "@/types";

export function PromotionsSection() {
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPromotions = async () => {
      try {
        const response = await promotionAPI.getAllActivePromotions();
        // Backend returns ApiResponse<PromotionResponse[]>, extract data array
        const promotionsList = Array.isArray(response)
          ? response
          : response.data || [];
        // Show only first 3 promotions
        setPromotions(promotionsList.slice(0, 3));
      } catch (error) {
        if (process.env.NODE_ENV !== "production") {
          console.warn(
            "[PromotionsSection] Failed to load promotions:",
            error instanceof Error ? error.message : error
          );
        } else {
          console.warn(
            "[PromotionsSection] Promotions are temporarily unavailable."
          );
        }
        // Silently fail - don't show error to user, just hide section
        setPromotions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPromotions();
  }, []);

  if (loading) {
    return (
      <section className="py-8 bg-background">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-center gap-2 mb-6">
            <Tag className="w-6 h-6 text-primary" />
            <h2 className="text-2xl font-bold">Khuyến Mãi Đặc Biệt</h2>
          </div>
          <div className="grid md:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-32 bg-white rounded-lg animate-pulse" />
            ))}
          </div>
        </div>
      </section>
    );
  }

  if (promotions.length === 0) {
    return null;
  }

  const formatDate = (date: string) => {
    return new Date(date).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const getPromotionTypeLabel = (type: string) => {
    switch (type) {
      case "DISCOUNT_PERCENTAGE":
        return "Giảm %";
      case "DISCOUNT_FIXED":
        return "Giảm giá";
      case "FREE_SHIPPING":
        return "Miễn phí vận chuyển";
      default:
        return type;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  };

  return (
  <section className="py-12 bg-background">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <Tag className="w-7 h-7 text-primary" />
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
              Khuyến Mãi Đặc Biệt
            </h2>
          </div>
          <Link href="/promotions">
            <Button
              variant="outline"
              className="gap-2 hover:bg-orange-50 dark:hover:bg-orange-950/30"
            >
              Xem tất cả
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>

        {/* Promotions Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {promotions.map((promotion) => (
            <div
              key={promotion.id}
              className="bg-white dark:bg-gray-800 rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 hover:-translate-y-1 p-6 border-l-4 border-orange-500 dark:border-orange-600"
            >
              {/* Promotion Header */}
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <span className="inline-block px-3 py-1 bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400 text-xs font-semibold rounded-full mb-3">
                    {getPromotionTypeLabel(promotion.templateType)}
                  </span>
                  <h3 className="font-bold text-xl text-gray-900 dark:text-white line-clamp-2 leading-tight">
                    {promotion.title}
                  </h3>
                </div>
              </div>

              {/* Discount Info */}
              <div className="bg-secondary rounded-xl p-4 mb-4 shadow-md">
                <div className="text-center">
                  {promotion.templateType !== "FREE_SHIPPING" &&
                    promotion.percentDiscount > 0 && (
                      <div className="text-foreground">
                        <span className="text-4xl font-extrabold drop-shadow-md">
                          {promotion.percentDiscount}%
                        </span>
                        <span className="text-sm font-medium block mt-2">
                          Giảm giá
                        </span>
                      </div>
                    )}
                  {promotion.templateType === "FREE_SHIPPING" && (
                    <div className="text-foreground">
                      <Package className="w-10 h-10 mx-auto mb-2 drop-shadow-md" />
                      <span className="text-sm font-medium">
                        Miễn phí vận chuyển
                      </span>
                    </div>
                  )}
                </div>
              </div>

              {/* Conditions */}
              <div className="space-y-2 text-sm text-gray-600 dark:text-gray-400 mb-4">
                {promotion.minValueToBeApplied !== null &&
                  promotion.minValueToBeApplied > 0 && (
                    <div className="flex items-center gap-2">
                      <Package className="w-4 h-4 text-gray-400" />
                      <span>
                        Đơn tối thiểu:{" "}
                        {formatCurrency(promotion.minValueToBeApplied)}
                      </span>
                    </div>
                  )}
              </div>

              {/* Date Range */}
              <div className="flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400 pt-3 border-t border-gray-200 dark:border-gray-700">
                <Calendar className="w-4 h-4 flex-shrink-0" />
                <span className="truncate">
                  {formatDate(promotion.effectiveDate)} -{" "}
                  {formatDate(promotion.expirationDate)}
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* View All Link (Mobile) */}
        <div className="mt-6 text-center md:hidden">
          <Link href="/promotions">
            <Button className="gap-2">
              Xem tất cả khuyến mãi
              <ArrowRight className="w-4 h-4" />
            </Button>
          </Link>
        </div>
      </div>
    </section>
  );
}
