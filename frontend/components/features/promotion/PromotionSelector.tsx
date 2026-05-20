/**
 * PromotionSelector - Customer component to select and apply promotions
 * Used in Cart/Checkout pages
 */
"use client";

import { useState } from "react";
import {
  Tag,
  Calendar,
  CheckCircle,
  AlertCircle,
  ChevronDown,
  ChevronUp,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAvailablePromotions } from "@/hooks/useAvailablePromotions";
import type { PromotionResponse } from "@/types/promotion";
import { cn } from "@/lib/utils";

interface PromotionSelectorProps {
  orderTotal: number;
  onPromotionApplied?: (promotionId: string, discountAmount: number) => void;
  onPromotionCleared?: () => void;
}

export function PromotionSelector({
  orderTotal,
  onPromotionApplied,
  onPromotionCleared,
}: PromotionSelectorProps) {
  const {
    promotions,
    loading,
    error,
    selectedPromotion,
    discountAmount,
    calculatingDiscount,
    applyPromotion,
    clearPromotion,
  } = useAvailablePromotions(orderTotal);

  const [isExpanded, setIsExpanded] = useState(false);

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

  const handleApply = async (promotionId: string) => {
    const result = await applyPromotion(promotionId);
    if (result.success && onPromotionApplied) {
      onPromotionApplied(promotionId, result.discount || 0);
    }
    setIsExpanded(false);
  };

  const handleClear = () => {
    clearPromotion();
    if (onPromotionCleared) {
      onPromotionCleared();
    }
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
      {/* Header */}
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full px-4 py-3 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
      >
        <div className="flex items-center gap-2">
          <Tag className="w-5 h-5 text-blue-600 dark:text-blue-400" />
          <span className="font-medium text-gray-900 dark:text-white">
            {selectedPromotion ? "Mã giảm giá đã áp dụng" : "Chọn mã giảm giá"}
          </span>
          {promotions.length > 0 && !selectedPromotion && (
            <span className="px-2 py-0.5 bg-blue-100 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 text-xs font-medium rounded-full">
              {promotions.length} khả dụng
            </span>
          )}
        </div>
        {isExpanded ? (
          <ChevronUp className="w-5 h-5 text-gray-400" />
        ) : (
          <ChevronDown className="w-5 h-5 text-gray-400" />
        )}
      </button>

      {/* Selected Promotion Display */}
      {selectedPromotion && !isExpanded && (
        <div className="px-4 pb-3 border-t border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between pt-3">
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <CheckCircle className="w-4 h-4 text-green-600 dark:text-green-400" />
                <span className="font-medium text-gray-900 dark:text-white">
                  {selectedPromotion.title}
                </span>
              </div>
              <p className="text-sm text-green-600 dark:text-green-400 font-medium">
                Tiết kiệm {formatCurrency(discountAmount)}
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleClear}
              className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20"
            >
              <X className="w-4 h-4" />
            </Button>
          </div>
        </div>
      )}

      {/* Expanded Promotions List */}
      {isExpanded && (
        <div className="border-t border-gray-200 dark:border-gray-700">
          {loading && (
            <div className="p-8 flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
            </div>
          )}

          {error && (
            <div className="p-4 m-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p className="text-red-800 dark:text-red-400 text-sm flex items-center gap-2">
                <AlertCircle className="w-4 h-4" />
                {error}
              </p>
            </div>
          )}

          {!loading && promotions.length === 0 && (
            <div className="p-8 text-center">
              <Tag className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
              <p className="text-gray-500 dark:text-gray-400 text-sm">
                {orderTotal === 0
                  ? "Thêm sản phẩm vào giỏ hàng để xem mã giảm giá"
                  : "Không có mã giảm giá khả dụng cho đơn hàng này"}
              </p>
            </div>
          )}

          {!loading && promotions.length > 0 && (
            <div className="max-h-96 overflow-y-auto">
              <div className="p-4 space-y-3">
                {promotions.map((promotion) => (
                  <PromotionCard
                    key={promotion.id}
                    promotion={promotion}
                    isSelected={selectedPromotion?.id === promotion.id}
                    isApplying={
                      calculatingDiscount &&
                      selectedPromotion?.id === promotion.id
                    }
                    onApply={() => handleApply(promotion.id)}
                    formatCurrency={formatCurrency}
                    formatDate={formatDate}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// Promotion Card Component
interface PromotionCardProps {
  promotion: PromotionResponse;
  isSelected: boolean;
  isApplying: boolean;
  onApply: () => void;
  formatCurrency: (value: number) => string;
  formatDate: (date: string) => string;
}

function PromotionCard({
  promotion,
  isSelected,
  isApplying,
  onApply,
  formatCurrency,
  formatDate,
}: PromotionCardProps) {
  return (
    <div
      className={cn(
        "border rounded-lg p-4 transition-all",
        isSelected
          ? "border-green-500 bg-green-50 dark:bg-green-900/10"
          : "border-gray-200 dark:border-gray-700 hover:border-blue-300 dark:hover:border-blue-600"
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          {/* Title & Badge */}
          <div className="flex items-center gap-2 mb-2">
            <h3 className="font-semibold text-gray-900 dark:text-white truncate">
              {promotion.title}
            </h3>
            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 shrink-0">
              {promotion.percentDiscount}%
            </span>
          </div>

          {/* Description */}
          {promotion.description && (
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
              {promotion.description}
            </p>
          )}

          {/* Info */}
          <div className="space-y-1 text-sm text-gray-500 dark:text-gray-400">
            {promotion.minValueToBeApplied && (
              <div className="flex items-center gap-1.5">
                <span>Đơn tối thiểu:</span>
                <span className="font-medium text-gray-700 dark:text-gray-300">
                  {formatCurrency(promotion.minValueToBeApplied)}
                </span>
              </div>
            )}
            <div className="flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5" />
              <span>HSD: {formatDate(promotion.expirationDate)}</span>
            </div>
          </div>
        </div>

        {/* Apply Button */}
        <Button
          onClick={onApply}
          disabled={isApplying || isSelected}
          size="sm"
          className={cn(
            "shrink-0",
            isSelected &&
              "bg-green-600 hover:bg-green-700 dark:bg-green-600 dark:hover:bg-green-700"
          )}
        >
          {isApplying ? (
            <div className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
          ) : isSelected ? (
            <>
              <CheckCircle className="w-4 h-4 mr-1" />
              Đã áp dụng
            </>
          ) : (
            "Áp dụng"
          )}
        </Button>
      </div>
    </div>
  );
}
