/**
 * AvailablePromotionsList - Display available promotions for customer
 */
"use client";

import { useState } from "react";
import { Ticket, Calendar, Tag, AlertCircle, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAvailablePromotions } from "@/hooks";
import type { PromotionResponse } from "@/types";
import { cn } from "@/lib/utils";

interface AvailablePromotionsListProps {
  orderTotal: number;
  onApplyPromotion?: (promotion: PromotionResponse) => void;
  selectedPromotionId?: string;
}

export function AvailablePromotionsList({
  orderTotal,
  onApplyPromotion,
  selectedPromotionId,
}: AvailablePromotionsListProps) {
  const { promotions, loading, error } = useAvailablePromotions(orderTotal);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        <span className="ml-3 text-gray-600 dark:text-gray-400">
          Đang tải khuyến mãi...
        </span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
        <p className="text-red-800 dark:text-red-400 flex items-center gap-2">
          <AlertCircle className="w-4 h-4" />
          {error}
        </p>
      </div>
    );
  }

  if (promotions.length === 0) {
    return (
      <div className="text-center p-8 bg-gray-50 dark:bg-gray-800 rounded-lg">
        <Ticket className="w-12 h-12 text-gray-400 mx-auto mb-3" />
        <p className="text-gray-600 dark:text-gray-400">
          {orderTotal > 0
            ? "Không có khuyến mãi phù hợp với đơn hàng của bạn"
            : "Thêm sản phẩm vào giỏ hàng để xem khuyến mãi"}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
        <Tag className="w-5 h-5 text-blue-600" />
        Khuyến mãi có thể áp dụng ({promotions.length})
      </h3>

      <div className="space-y-3">
        {promotions.map((promotion) => {
          const isSelected = selectedPromotionId === promotion.id;
          const canApply =
            !promotion.minValueToBeApplied ||
            orderTotal >= promotion.minValueToBeApplied;

          return (
            <div
              key={promotion.id}
              className={cn(
                "relative p-4 rounded-lg border-2 transition-all",
                isSelected
                  ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20"
                  : "border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-blue-300 dark:hover:border-blue-700"
              )}
            >
              {/* Discount Badge */}
              <div className="absolute top-3 right-3">
                <div className="bg-gradient-to-r from-red-500 to-pink-500 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                  -{promotion.percentDiscount}%
                </div>
              </div>

              <div className="pr-20">
                {/* Title */}
                <h4 className="font-bold text-gray-900 dark:text-white mb-1">
                  {promotion.title}
                </h4>

                {/* Description */}
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
                  {promotion.description}
                </p>

                {/* Details */}
                <div className="space-y-2 text-sm">
                  {promotion.minValueToBeApplied && (
                    <div className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                      <span className="font-medium">Đơn tối thiểu:</span>
                      <span className="text-blue-600 dark:text-blue-400 font-semibold">
                        {formatCurrency(promotion.minValueToBeApplied)}
                      </span>
                    </div>
                  )}

                  <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                    <Calendar className="w-4 h-4" />
                    <span>HSD: {formatDate(promotion.expirationDate)}</span>
                  </div>
                </div>

                {/* Action Button */}
                <div className="mt-4">
                  {isSelected ? (
                    <div className="flex items-center gap-2 text-green-600 dark:text-green-400 font-medium">
                      <Check className="w-5 h-5" />
                      <span>Đã áp dụng</span>
                    </div>
                  ) : (
                    <Button
                      onClick={() => onApplyPromotion?.(promotion)}
                      disabled={!canApply}
                      size="sm"
                      className={cn(
                        "w-full",
                        !canApply && "opacity-50 cursor-not-allowed"
                      )}
                    >
                      {canApply ? "Áp dụng" : "Không đủ điều kiện"}
                    </Button>
                  )}
                </div>

                {/* Not eligible message */}
                {!canApply && promotion.minValueToBeApplied && (
                  <p className="text-xs text-orange-600 dark:text-orange-400 mt-2">
                    Cần thêm{" "}
                    {formatCurrency(promotion.minValueToBeApplied - orderTotal)}{" "}
                    để áp dụng
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
