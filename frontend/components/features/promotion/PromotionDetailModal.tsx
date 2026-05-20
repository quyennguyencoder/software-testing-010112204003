/**
 * PromotionDetailModal - View promotion details (Admin)
 */
"use client";

import { X, Calendar, Percent, DollarSign, Tag, Target } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { PromotionResponse } from "@/types";
import { cn } from "@/lib/utils";

interface PromotionDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  promotion: PromotionResponse | null;
}

export function PromotionDetailModal({
  isOpen,
  onClose,
  promotion,
}: PromotionDetailModalProps) {
  if (!isOpen || !promotion) return null;

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  const getStatusBadge = (status: string) => {
    const styles = {
      ACTIVE:
        "bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400",
      INACTIVE:
        "bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400",
      EXPIRED: "bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400",
    };
    return (
      <span
        className={cn(
          "inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium",
          styles[status as keyof typeof styles]
        )}
      >
        {status}
      </span>
    );
  };

  const getTemplateTypeBadge = (type: string) => {
    const styles = {
      DISCOUNT_PERCENTAGE:
        "bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400",
      DISCOUNT_FIXED:
        "bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400",
      FREE_SHIPPING:
        "bg-orange-100 text-orange-800 dark:bg-orange-900/20 dark:text-orange-400",
    };
    const labels = {
      DISCOUNT_PERCENTAGE: "Giảm theo %",
      DISCOUNT_FIXED: "Giảm cố định",
      FREE_SHIPPING: "Miễn phí vận chuyển",
    };
    return (
      <span
        className={cn(
          "inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-medium",
          styles[type as keyof typeof styles]
        )}
      >
        {labels[type as keyof typeof labels] || type}
      </span>
    );
  };

  const getTargetTypeLabel = (type: string) => {
    const labels = {
      CATEGORY: "Danh mục",
      BRAND: "Thương hiệu",
      PRODUCT: "Sản phẩm",
    };
    return labels[type as keyof typeof labels] || type;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
            Chi tiết khuyến mãi
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-6">
            {/* Title & Status */}
            <div>
              <div className="flex items-start justify-between mb-2">
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                  {promotion.title}
                </h3>
                {getStatusBadge(promotion.status)}
              </div>
              {promotion.description && (
                <p className="text-gray-600 dark:text-gray-300">
                  {promotion.description}
                </p>
              )}
            </div>

            {/* Date Range */}
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 mb-1">
                  <Calendar className="w-4 h-4" />
                  Ngày hiệu lực
                </div>
                <p className="text-base font-medium text-gray-900 dark:text-white">
                  {formatDate(promotion.effectiveDate)}
                </p>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 mb-1">
                  <Calendar className="w-4 h-4" />
                  Ngày hết hạn
                </div>
                <p className="text-base font-medium text-gray-900 dark:text-white">
                  {formatDate(promotion.expirationDate)}
                </p>
              </div>
            </div>

            {/* Discount Info */}
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 bg-green-50 dark:bg-green-900/10 rounded-lg border border-green-200 dark:border-green-800">
                <div className="flex items-center gap-2 text-sm text-green-600 dark:text-green-400 mb-1">
                  <Percent className="w-4 h-4" />
                  Phần trăm giảm giá
                </div>
                <p className="text-2xl font-bold text-green-700 dark:text-green-300">
                  {promotion.percentDiscount}%
                </p>
              </div>
              <div className="p-4 bg-blue-50 dark:bg-blue-900/10 rounded-lg border border-blue-200 dark:border-blue-800">
                <div className="flex items-center gap-2 text-sm text-blue-600 dark:text-blue-400 mb-1">
                  <DollarSign className="w-4 h-4" />
                  Giá trị tối thiểu
                </div>
                <p className="text-base font-bold text-blue-700 dark:text-blue-300">
                  {promotion.minValueToBeApplied
                    ? formatCurrency(promotion.minValueToBeApplied)
                    : "Không giới hạn"}
                </p>
              </div>
            </div>

            {/* Template Info */}
            <div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
              <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
                <Tag className="w-4 h-4" />
                Thông tin Template
              </div>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-300">
                    ID:{" "}
                    <span className="font-mono">{promotion.templateId}</span>
                  </p>
                  <p className="text-sm text-gray-600 dark:text-gray-300">
                    Code:{" "}
                    <span className="font-mono">{promotion.templateCode}</span>
                  </p>
                </div>
                {getTemplateTypeBadge(promotion.templateType)}
              </div>
            </div>

            {/* Targets */}
            <div>
              <div className="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                <Target className="w-4 h-4" />
                Đối tượng áp dụng
              </div>
              {promotion.targets.length === 0 ? (
                <p className="text-sm text-gray-500 dark:text-gray-400 italic">
                  Áp dụng cho tất cả sản phẩm
                </p>
              ) : (
                <div className="space-y-2">
                  {promotion.targets.map((target) => (
                    <div
                      key={target.id}
                      className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg border border-gray-200 dark:border-gray-600"
                    >
                      <div>
                        <p className="text-sm font-medium text-gray-900 dark:text-white">
                          {getTargetTypeLabel(target.type)}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-400 font-mono">
                          ID: {target.applicableObjectId}
                        </p>
                      </div>
                      <span className="px-2 py-1 bg-gray-200 dark:bg-gray-600 rounded text-xs font-medium text-gray-700 dark:text-gray-300">
                        {target.type}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Promotion ID */}
            <div className="p-3 bg-gray-100 dark:bg-gray-700 rounded border border-gray-300 dark:border-gray-600">
              <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">
                Promotion ID
              </p>
              <p className="text-sm font-mono text-gray-900 dark:text-white">
                {promotion.id}
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 dark:border-gray-700">
          <Button onClick={onClose} variant="outline">
            Đóng
          </Button>
        </div>
      </div>
    </div>
  );
}
