/**
 * PromotionsTable - Display and manage promotions (Admin view)
 */
"use client";

import { useState } from "react";
import {
  Edit,
  Trash2,
  Plus,
  AlertCircle,
  CheckCircle,
  XCircle,
  Calendar,
  Eye,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { usePromotions } from "@/hooks";
import type { PromotionResponse } from "@/types";
import { cn } from "@/lib/utils";
import { PromotionFormModal } from "./PromotionFormModal";
import { PromotionDetailModal } from "./PromotionDetailModal";

export function PromotionsTable() {
  const {
    promotions,
    loading,
    error,
    createPromotion,
    updatePromotion,
    disablePromotion,
  } = usePromotions();
  const [selectedPromotion, setSelectedPromotion] =
    useState<PromotionResponse | null>(null);
  const [showFormModal, setShowFormModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [editingPromotion, setEditingPromotion] =
    useState<PromotionResponse | null>(null);

  const getStatusBadge = (status: string) => {
    const styles = {
      ACTIVE:
        "bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400",
      INACTIVE:
        "bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400",
      EXPIRED: "bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400",
    };
    const icons = {
      ACTIVE: <CheckCircle className="w-3 h-3" />,
      INACTIVE: <XCircle className="w-3 h-3" />,
      EXPIRED: <AlertCircle className="w-3 h-3" />,
    };
    return (
      <span
        className={cn(
          "inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium",
          styles[status as keyof typeof styles]
        )}
      >
        {icons[status as keyof typeof icons]}
        {status}
      </span>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const formatCurrency = (value: number | null) => {
    if (value === null) return "N/A";
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  const handleDisable = async (id: string) => {
    if (confirm("Bạn có chắc muốn vô hiệu hóa khuyến mãi này?")) {
      await disablePromotion(id);
    }
  };

  const handleEdit = (promotion: PromotionResponse) => {
    setEditingPromotion(promotion);
    setShowFormModal(true);
  };

  const handleCreate = () => {
    setEditingPromotion(null);
    setShowFormModal(true);
  };

  const handleViewDetail = (promotion: PromotionResponse) => {
    setSelectedPromotion(promotion);
    setShowDetailModal(true);
  };

  const handleFormSubmit = async (data: any) => {
    if (editingPromotion) {
      return await updatePromotion(editingPromotion.id, data);
    } else {
      return await createPromotion(data);
    }
  };

  const handleCloseForm = () => {
    setShowFormModal(false);
    setEditingPromotion(null);
  };

  const handleCloseDetail = () => {
    setShowDetailModal(false);
    setSelectedPromotion(null);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
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

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
            Quản lý Khuyến mãi
          </h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Tạo và quản lý các chương trình khuyến mãi
          </p>
        </div>
        <Button onClick={handleCreate} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Tạo mới
        </Button>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Tên chương trình
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Giảm giá
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Giá trị tối thiểu
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Thời gian
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Trạng thái
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {promotions.length === 0 ? (
                <tr>
                  <td
                    colSpan={6}
                    className="px-6 py-8 text-center text-gray-500 dark:text-gray-400"
                  >
                    Chưa có chương trình khuyến mãi nào
                  </td>
                </tr>
              ) : (
                promotions.map((promotion) => (
                  <tr
                    key={promotion.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
                  >
                    <td className="px-6 py-4">
                      <div>
                        <div className="font-medium text-gray-900 dark:text-white">
                          {promotion.title}
                        </div>
                        <div className="text-sm text-gray-500 dark:text-gray-400 line-clamp-1">
                          {promotion.description}
                        </div>
                        <div className="text-xs text-blue-600 dark:text-blue-400 mt-1">
                          {promotion.templateCode}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-lg font-semibold text-green-600 dark:text-green-400">
                        {promotion.percentDiscount}%
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                      {formatCurrency(promotion.minValueToBeApplied)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
                        <Calendar className="w-4 h-4" />
                        <div>
                          <div>{formatDate(promotion.effectiveDate)}</div>
                          <div className="text-xs">
                            đến {formatDate(promotion.expirationDate)}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(promotion.status)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleViewDetail(promotion)}
                          className="flex items-center gap-1"
                        >
                          <Eye className="w-3 h-3" />
                          Xem
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(promotion)}
                          className="flex items-center gap-1"
                        >
                          <Edit className="w-3 h-3" />
                          Sửa
                        </Button>
                        {promotion.status === "ACTIVE" && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDisable(promotion.id)}
                            className="flex items-center gap-1 text-red-600 hover:text-red-700 border-red-300 hover:border-red-400"
                          >
                            <Trash2 className="w-3 h-3" />
                            Vô hiệu
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modals */}
      <PromotionFormModal
        isOpen={showFormModal}
        onClose={handleCloseForm}
        onSubmit={handleFormSubmit}
        promotion={editingPromotion}
      />
      <PromotionDetailModal
        isOpen={showDetailModal}
        onClose={handleCloseDetail}
        promotion={selectedPromotion}
      />
    </div>
  );
}
