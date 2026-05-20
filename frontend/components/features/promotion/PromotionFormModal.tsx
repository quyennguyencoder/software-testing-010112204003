/**
 * PromotionFormModal - Create/Edit promotion form modal (Admin)
 */
"use client";

import { useState, useEffect } from "react";
import { X, Plus, Trash2, Calendar } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  SearchableSelect,
  SelectOption,
} from "@/components/ui/searchable-select";
import { templateAPI, productAPI, categoryAPI, brandAPI } from "@/lib/api";
import type {
  PromotionResponse,
  CreatePromotionRequest,
  PromotionTemplateResponse,
} from "@/types";

interface PromotionFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreatePromotionRequest) => Promise<boolean>;
  promotion?: PromotionResponse | null; // For edit mode
}

export function PromotionFormModal({
  isOpen,
  onClose,
  onSubmit,
  promotion,
}: PromotionFormModalProps) {
  const isEditMode = !!promotion;
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [templates, setTemplates] = useState<PromotionTemplateResponse[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState(false);

  // Options for target selection
  const [productOptions, setProductOptions] = useState<SelectOption[]>([]);
  const [categoryOptions, setCategoryOptions] = useState<SelectOption[]>([]);
  const [brandOptions, setBrandOptions] = useState<SelectOption[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(false);

  // Target selection UI state (for adding multiple targets at once)
  const [targetType, setTargetType] = useState<
    "CATEGORY" | "BRAND" | "PRODUCT"
  >("CATEGORY");
  const [selectedTargetIds, setSelectedTargetIds] = useState<string[]>([]);

  // Form state
  const [formData, setFormData] = useState<CreatePromotionRequest>({
    title: "",
    description: "",
    effectiveDate: "",
    expirationDate: "",
    percentDiscount: 0,
    minValueToBeApplied: null,
    status: "ACTIVE",
    templateId: "",
    targets: [],
  });

  // Load templates from API
  useEffect(() => {
    const fetchTemplates = async () => {
      setLoadingTemplates(true);
      try {
        const response = await templateAPI.getAllTemplates();
        if (response.success && response.data) {
          setTemplates(response.data);
        }
      } catch (err) {
        console.error("Failed to load templates:", err);
      } finally {
        setLoadingTemplates(false);
      }
    };

    if (isOpen) {
      fetchTemplates();
      loadProductsAndCategories();
    }
  }, [isOpen]);

  // Load products, categories, and brands for selection
  const loadProductsAndCategories = async () => {
    setLoadingOptions(true);
    try {
      // Load products
      try {
        const productsResponse = await productAPI.getAllProducts({
          page: 0,
          size: 1000,
        });
        if (productsResponse.success && productsResponse.data?.content) {
          const options: SelectOption[] = productsResponse.data.content.map(
            (product: any) => ({
              value: String(product.id),
              label: product.name || product.productName,
              description: `ID: ${product.id} | ${product.brand || "N/A"}`,
            })
          );
          setProductOptions(options);
        }
      } catch (err) {
        console.warn("Failed to load products:", err);
        setProductOptions([]);
      }

      // Load categories
      try {
        const categoriesResponse = await categoryAPI.getRootCategories();
        if (categoriesResponse.success && categoriesResponse.data) {
          const options: SelectOption[] = categoriesResponse.data.map(
            (category: any) => ({
              value: String(category.id),
              label: category.name,
              description: `ID: ${category.id}`,
            })
          );
          setCategoryOptions(options);
        }
      } catch (err) {
        console.warn("Failed to load categories:", err);
        setCategoryOptions([]);
      }

      // Load brands
      try {
        const brandsResponse = await brandAPI.getAll();
        if (brandsResponse.success && brandsResponse.data) {
          const options: SelectOption[] = brandsResponse.data.map(
            (brand: any) => ({
              value: String(brand.id),
              label: brand.name,
              description: `ID: ${brand.id}`,
            })
          );
          setBrandOptions(options);
        }
      } catch (err) {
        console.warn("Failed to load brands:", err);
        setBrandOptions([]);
      }
    } catch (err) {
      console.error("Failed to load options:", err);
    } finally {
      setLoadingOptions(false);
    }
  };

  // Initialize form with promotion data in edit mode
  useEffect(() => {
    if (promotion) {
      setFormData({
        title: promotion.title,
        description: promotion.description,
        effectiveDate: promotion.effectiveDate.split("T")[0],
        expirationDate: promotion.expirationDate.split("T")[0],
        percentDiscount: promotion.percentDiscount,
        minValueToBeApplied: promotion.minValueToBeApplied,
        status: promotion.status === "EXPIRED" ? "INACTIVE" : promotion.status,
        templateId: promotion.templateId,
        targets: promotion.targets.map((t) => ({
          type: t.type,
          applicableObjectId: t.applicableObjectId,
        })),
      });
    } else {
      // Reset form for create mode
      setFormData({
        title: "",
        description: "",
        effectiveDate: "",
        expirationDate: "",
        percentDiscount: 0,
        minValueToBeApplied: null,
        status: "ACTIVE",
        templateId: "",
        targets: [],
      });
    }
  }, [promotion]);

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === "percentDiscount"
          ? value === ""
            ? 0
            : Number(value)
          : name === "minValueToBeApplied"
          ? value === ""
            ? null
            : Number(value)
          : value,
    }));
  };

  const addTarget = () => {
    if (selectedTargetIds.length === 0) {
      setError("Vui lòng chọn ít nhất một mục để thêm");
      return;
    }

    // Add all selected items as separate targets
    const newTargets = selectedTargetIds.map((id) => ({
      type: targetType,
      applicableObjectId: id,
    }));

    setFormData((prev) => ({
      ...prev,
      targets: [...prev.targets, ...newTargets],
    }));

    // Clear selection after adding
    setSelectedTargetIds([]);
    setError(null);
  };

  const removeTarget = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      targets: prev.targets.filter((_, i) => i !== index),
    }));
  };

  const updateTarget = (
    index: number,
    field: "type" | "applicableObjectId",
    value: string
  ) => {
    setFormData((prev) => ({
      ...prev,
      targets: prev.targets.map((target, i) =>
        i === index ? { ...target, [field]: value } : target
      ),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!formData.title.trim()) {
      setError("Vui lòng nhập tiêu đề khuyến mãi");
      return;
    }
    if (!formData.templateId) {
      setError("Vui lòng chọn template");
      return;
    }
    if (formData.percentDiscount <= 0 || formData.percentDiscount > 100) {
      setError("Phần trăm giảm giá phải từ 1-100%");
      return;
    }
    if (!formData.effectiveDate || !formData.expirationDate) {
      setError("Vui lòng chọn ngày hiệu lực và hết hạn");
      return;
    }

    // Validate expiration date is in the future
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const expirationDate = new Date(formData.expirationDate);
    if (expirationDate < today) {
      setError("Ngày hết hạn phải là ngày trong tương lai");
      return;
    }

    // Validate effective date is before expiration date
    const effectiveDate = new Date(formData.effectiveDate);
    if (effectiveDate >= expirationDate) {
      setError("Ngày hiệu lực phải trước ngày hết hạn");
      return;
    }

    // Convert dates to LocalDateTime format (ISO 8601)
    const payload = {
      ...formData,
      effectiveDate: `${formData.effectiveDate}T00:00:00`,
      expirationDate: `${formData.expirationDate}T23:59:59`,
      minValueToBeApplied: formData.minValueToBeApplied || null,
      targets: formData.targets.map((target) => ({
        type: target.type,
        applicableObjectId: target.applicableObjectId, // Keep as string, backend will convert
      })),
    };

    setLoading(true);
    const success = await onSubmit(payload);
    setLoading(false);

    if (success) {
      onClose();
    } else {
      setError("Không thể lưu khuyến mãi. Vui lòng thử lại.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
            {isEditMode ? "Chỉnh sửa khuyến mãi" : "Tạo khuyến mãi mới"}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6">
          {error && (
            <div className="mb-4 p-3 bg-red-100 dark:bg-red-900/20 border border-red-400 dark:border-red-700 rounded text-red-700 dark:text-red-400 text-sm">
              {error}
            </div>
          )}

          <div className="space-y-4">
            {/* Title */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Tiêu đề khuyến mãi <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="title"
                value={formData.title || ""}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                placeholder="Ví dụ: Giảm giá 20% cho tất cả sản phẩm"
                required
              />
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Mô tả
              </label>
              <textarea
                name="description"
                value={formData.description || ""}
                onChange={handleInputChange}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                placeholder="Mô tả chi tiết về chương trình khuyến mãi"
              />
            </div>

            {/* Date Range */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  <Calendar className="w-4 h-4 inline mr-1" />
                  Ngày hiệu lực <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  name="effectiveDate"
                  value={formData.effectiveDate || ""}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  <Calendar className="w-4 h-4 inline mr-1" />
                  Ngày hết hạn <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  name="expirationDate"
                  value={formData.expirationDate || ""}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  required
                />
              </div>
            </div>

            {/* Discount & Min Value */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Phần trăm giảm giá (%) <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  name="percentDiscount"
                  value={formData.percentDiscount || ""}
                  onChange={handleInputChange}
                  min="1"
                  max="100"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Giá trị đơn hàng tối thiểu (đ)
                </label>
                <input
                  type="number"
                  name="minValueToBeApplied"
                  value={formData.minValueToBeApplied || ""}
                  onChange={handleInputChange}
                  min="0"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  placeholder="Không giới hạn"
                />
              </div>
            </div>

            {/* Template Type Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Loại khuyến mãi <span className="text-red-500">*</span>
              </label>
              <select
                name="templateId"
                value={formData.templateId}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                required
                disabled={loadingTemplates}
              >
                <option value="">
                  {loadingTemplates
                    ? "Đang tải..."
                    : "-- Chọn loại khuyến mãi --"}
                </option>
                {templates.map((template) => (
                  <option key={template.id} value={template.id}>
                    {template.code} - {template.type}
                  </option>
                ))}
              </select>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                DISCOUNT áp dụng cho sản phẩm/danh mục, VOUCHER áp dụng cho đơn
                hàng
              </p>
            </div>

            {/* Status */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Trạng thái
              </label>
              <select
                name="status"
                value={formData.status}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              >
                <option value="ACTIVE">ACTIVE - Đang hoạt động</option>
                <option value="INACTIVE">INACTIVE - Chưa kích hoạt</option>
              </select>
            </div>

            {/* Targets */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                Đối tượng áp dụng
              </label>

              {/* Selection Area */}
              <div className="mb-4 p-4 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700/30">
                <div className="flex items-center gap-3 mb-3">
                  <select
                    value={targetType}
                    onChange={(e) => {
                      setTargetType(
                        e.target.value as "CATEGORY" | "BRAND" | "PRODUCT"
                      );
                      setSelectedTargetIds([]); // Clear selection when changing type
                    }}
                    className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="CATEGORY">CATEGORY - Danh mục</option>
                    <option value="BRAND">BRAND - Thương hiệu</option>
                    <option value="PRODUCT">PRODUCT - Sản phẩm</option>
                  </select>
                  <Button
                    type="button"
                    onClick={addTarget}
                    variant="default"
                    size="sm"
                    disabled={selectedTargetIds.length === 0}
                  >
                    <Plus className="w-4 h-4 mr-1" />
                    Thêm{" "}
                    {selectedTargetIds.length > 0 &&
                      `(${selectedTargetIds.length})`}
                  </Button>
                </div>

                {/* Multi-select options */}
                <div className="max-h-48 overflow-y-auto space-y-1">
                  {loadingOptions ? (
                    <p className="text-sm text-gray-500 dark:text-gray-400 py-2">
                      Đang tải...
                    </p>
                  ) : (
                    (() => {
                      const options =
                        targetType === "PRODUCT"
                          ? productOptions
                          : targetType === "CATEGORY"
                          ? categoryOptions
                          : brandOptions;

                      if (options.length === 0) {
                        return (
                          <p className="text-sm text-gray-500 dark:text-gray-400 py-2 italic">
                            Không có dữ liệu
                          </p>
                        );
                      }

                      return options.map((option) => (
                        <label
                          key={option.value}
                          className="flex items-center gap-2 p-2 hover:bg-gray-100 dark:hover:bg-gray-600/50 rounded cursor-pointer"
                        >
                          <input
                            type="checkbox"
                            checked={selectedTargetIds.includes(option.value)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedTargetIds([
                                  ...selectedTargetIds,
                                  option.value,
                                ]);
                              } else {
                                setSelectedTargetIds(
                                  selectedTargetIds.filter(
                                    (id) => id !== option.value
                                  )
                                );
                              }
                            }}
                            className="w-4 h-4 text-blue-600 rounded focus:ring-2 focus:ring-blue-500"
                          />
                          <span className="text-sm text-gray-700 dark:text-gray-300 flex-1">
                            {option.label}
                          </span>
                        </label>
                      ));
                    })()
                  )}
                </div>
              </div>

              {/* Added Targets List */}
              {formData.targets.length === 0 ? (
                <p className="text-sm text-gray-500 dark:text-gray-400 italic">
                  Chưa có đối tượng áp dụng. Chọn các mục ở trên và nhấn
                  &quot;Thêm&quot;.
                </p>
              ) : (
                <div className="space-y-2">
                  <p className="text-xs text-gray-600 dark:text-gray-400 mb-2">
                    Đã thêm {formData.targets.length} đối tượng:
                  </p>
                  {formData.targets.map((target, index) => {
                    const options =
                      target.type === "PRODUCT"
                        ? productOptions
                        : target.type === "CATEGORY"
                        ? categoryOptions
                        : brandOptions;
                    const option = options.find(
                      (opt) => opt.value === target.applicableObjectId
                    );
                    const typeLabel =
                      target.type === "PRODUCT"
                        ? "Sản phẩm"
                        : target.type === "CATEGORY"
                        ? "Danh mục"
                        : "Thương hiệu";

                    return (
                      <div
                        key={index}
                        className="flex items-center justify-between p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700/50"
                      >
                        <div className="flex-1">
                          <span className="text-xs text-gray-500 dark:text-gray-400">
                            {typeLabel}:
                          </span>
                          <span className="ml-2 text-sm text-gray-700 dark:text-gray-300">
                            {option?.label || target.applicableObjectId}
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={() => removeTarget(index)}
                          className="p-1 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded flex-shrink-0"
                          title="Xóa"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </form>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200 dark:border-gray-700">
          <Button type="button" onClick={onClose} variant="outline">
            Hủy
          </Button>
          <Button
            type="submit"
            onClick={handleSubmit}
            disabled={loading}
            className="bg-blue-600 hover:bg-blue-700 text-white"
          >
            {loading ? "Đang lưu..." : isEditMode ? "Cập nhật" : "Tạo mới"}
          </Button>
        </div>
      </div>
    </div>
  );
}
