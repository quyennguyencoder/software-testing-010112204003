/**
 * TemplateFormModal - Create/Edit Promotion Template
 */
"use client";

import { useState, useEffect } from "react";
import { X } from "lucide-react";
import { Button } from "@/components/ui/button";
import type {
  CreateTemplateRequest,
  UpdateTemplateRequest,
  PromotionTemplateResponse,
} from "@/types";

interface TemplateFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateTemplateRequest | UpdateTemplateRequest) => void;
  template?: PromotionTemplateResponse | null;
  isLoading?: boolean;
}

export function TemplateFormModal({
  isOpen,
  onClose,
  onSubmit,
  template,
  isLoading,
}: TemplateFormModalProps) {
  const [formData, setFormData] = useState<CreateTemplateRequest>({
    code: "",
    type: "DISCOUNT",
  });

  useEffect(() => {
    if (template) {
      setFormData({
        code: template.code,
        type: template.type,
      });
    } else {
      setFormData({
        code: "",
        type: "DISCOUNT",
      });
    }
  }, [template, isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
            {template ? "Chỉnh sửa Template" : "Tạo Template mới"}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Template Code */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Mã Template <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="code"
              value={formData.code}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              placeholder="VD: TPL_DISCOUNT"
              required
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Mã duy nhất để định danh template
            </p>
          </div>

          {/* Template Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Loại Template <span className="text-red-500">*</span>
            </label>
            <select
              name="type"
              value={formData.type}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              required
            >
              <option value="DISCOUNT">DISCOUNT - Giảm giá sản phẩm</option>
              <option value="VOUCHER">VOUCHER - Giảm giá đơn hàng</option>
              <option value="FREESHIP">FREESHIP - Miễn phí vận chuyển</option>
            </select>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              DISCOUNT áp dụng cho sản phẩm/danh mục, VOUCHER áp dụng cho đơn
              hàng
            </p>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
            >
              Hủy
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? "Đang xử lý..." : template ? "Cập nhật" : "Tạo mới"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
