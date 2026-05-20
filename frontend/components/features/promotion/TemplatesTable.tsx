/**
 * TemplatesTable - Display and manage promotion templates (Admin view)
 */
"use client";

import { useState, useEffect } from "react";
import { Edit, Trash2, Plus, Calendar } from "lucide-react";
import { Button } from "@/components/ui/button";
import { templateAPI } from "@/lib/api";
import type {
  PromotionTemplateResponse,
  CreateTemplateRequest,
  UpdateTemplateRequest,
} from "@/types";
import { TemplateFormModal } from "./TemplateFormModal";

export function TemplatesTable() {
  const [templates, setTemplates] = useState<PromotionTemplateResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showFormModal, setShowFormModal] = useState(false);
  const [editingTemplate, setEditingTemplate] =
    useState<PromotionTemplateResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadTemplates();
  }, []);

  const loadTemplates = async () => {
    try {
      setLoading(true);
      const response = await templateAPI.getAllTemplates();
      if (response.success && response.data) {
        setTemplates(response.data);
      } else {
        setError(response.message || "Không thể tải danh sách template");
      }
    } catch (err) {
      setError("Có lỗi xảy ra khi tải danh sách template");
      console.error("Load templates error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingTemplate(null);
    setShowFormModal(true);
  };

  const handleEdit = (template: PromotionTemplateResponse) => {
    setEditingTemplate(template);
    setShowFormModal(true);
  };

  const handleSubmit = async (
    data: CreateTemplateRequest | UpdateTemplateRequest
  ) => {
    try {
      setSubmitting(true);
      let response;

      if (editingTemplate) {
        // Update existing template
        response = await templateAPI.updateTemplate(editingTemplate.id, data);
      } else {
        // Create new template
        response = await templateAPI.createTemplate(data);
      }

      if (response.success) {
        alert(
          editingTemplate
            ? "Cập nhật template thành công!"
            : "Tạo template thành công!"
        );
        setShowFormModal(false);
        loadTemplates();
      } else {
        alert(response.message || "Có lỗi xảy ra");
      }
    } catch (err) {
      console.error("Submit template error:", err);
      alert("Có lỗi xảy ra khi lưu template");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (
      !confirm(
        "Bạn có chắc muốn xóa template này? Template đang được sử dụng sẽ không thể xóa."
      )
    ) {
      return;
    }

    try {
      const response = await templateAPI.deleteTemplate(id);
      if (response.success) {
        alert("Xóa template thành công!");
        loadTemplates();
      } else {
        alert(
          response.message ||
            "Không thể xóa template. Template có thể đang được sử dụng."
        );
      }
    } catch (err) {
      console.error("Delete template error:", err);
      // Extract error message from Error object
      const errorMessage =
        err instanceof Error ? err.message : "Có lỗi xảy ra khi xóa template";
      alert(errorMessage);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      DISCOUNT: "Giảm giá sản phẩm",
      VOUCHER: "Giảm giá đơn hàng",
      FREESHIP: "Miễn phí vận chuyển",
    };
    return labels[type] || type;
  };

  const getTypeBadge = (type: string) => {
    const styles: Record<string, string> = {
      DISCOUNT:
        "bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400",
      VOUCHER:
        "bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400",
      FREESHIP:
        "bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400",
    };
    return (
      <span
        className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
          styles[type] || ""
        }`}
      >
        {type}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-4 rounded-lg">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
            Quản lý Template Khuyến Mãi
          </h2>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Tạo và quản lý các template cho khuyến mãi
          </p>
        </div>
        <Button onClick={handleCreate} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Tạo Template
        </Button>
      </div>

      {/* Table */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Mã Template
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Loại
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Mô tả
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Ngày tạo
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {templates.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    className="px-6 py-8 text-center text-gray-500 dark:text-gray-400"
                  >
                    Chưa có template nào. Hãy tạo template đầu tiên!
                  </td>
                </tr>
              ) : (
                templates.map((template) => (
                  <tr
                    key={template.id}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700"
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        {template.code}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getTypeBadge(template.type)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-600 dark:text-gray-400">
                        {getTypeLabel(template.type)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center text-sm text-gray-600 dark:text-gray-400">
                        <Calendar className="w-4 h-4 mr-1" />
                        {formatDate(template.createdAt)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleEdit(template)}
                          className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                          title="Chỉnh sửa"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(template.id)}
                          className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                          title="Xóa"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Form Modal */}
      <TemplateFormModal
        isOpen={showFormModal}
        onClose={() => setShowFormModal(false)}
        onSubmit={handleSubmit}
        template={editingTemplate}
        isLoading={submitting}
      />
    </div>
  );
}
