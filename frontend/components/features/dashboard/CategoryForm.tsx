/**
 * CategoryForm component - Form for creating/updating categories
 * Following FRONTEND_DESIGN_SYSTEM.md principles:
 * - Form validation with clear error messages
 * - Accessibility: labels, ARIA, focus management
 * - Loading states
 * - Mobile-first responsive
 */

'use client';

import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { adminAPI } from '@/lib/api';
import type { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from '@/types';

interface CategoryFormProps {
  category?: CategoryResponse | null;
  parentCategory?: CategoryResponse | null;
  allCategories: CategoryResponse[];
  onSuccess: () => void;
  onCancel: () => void;
}

export function CategoryForm({ category, parentCategory, allCategories, onSuccess, onCancel }: CategoryFormProps) {
  const isEditMode = !!category;
  const isAddChildMode = !!parentCategory;

  const [formData, setFormData] = useState({
    name: category?.name || '',
    description: '',
    parentId: parentCategory?.id || category?.parentId || null as number | null,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Reset form when category changes
  useEffect(() => {
    if (category) {
      setFormData({
        name: category.name,
        description: '',
        parentId: category.parentId,
      });
    } else if (parentCategory) {
      setFormData({
        name: '',
        description: '',
        parentId: parentCategory.id,
      });
    }
  }, [category, parentCategory]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Name validation
    if (!formData.name.trim()) {
      newErrors.name = 'Tên danh mục không được để trống';
    } else if (formData.name.length < 2) {
      newErrors.name = 'Tên danh mục phải có ít nhất 2 ký tự';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Tên danh mục không được vượt quá 100 ký tự';
    }

    // Check if setting parent to itself (edit mode only)
    if (isEditMode && formData.parentId === category?.id) {
      newErrors.parentId = 'Danh mục không thể là cha của chính nó';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setSubmitError(null);

    try {
      if (isEditMode && category) {
        // Update existing category
        const updateData: UpdateCategoryRequest = {
          name: formData.name.trim(),
          description: formData.description?.trim() || undefined,
          parentId: formData.parentId,
        };

        const response = await adminAPI.updateCategory(category.id, updateData);

        if (response.success) {
          onSuccess();
        } else {
          throw new Error(response.message || 'Lỗi khi cập nhật danh mục');
        }
      } else {
        // Create new category
        const createData: CreateCategoryRequest = {
          name: formData.name.trim(),
          description: formData.description?.trim() || undefined,
          parentId: formData.parentId,
        };

        const response = await adminAPI.createCategory(createData);

        if (response.success) {
          onSuccess();
        } else {
          throw new Error(response.message || 'Lỗi khi tạo danh mục');
        }
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Đã xảy ra lỗi';
      setSubmitError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: string | number | null) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  // Filter out current category and its descendants from parent options (edit mode)
  const getAvailableParents = (): CategoryResponse[] => {
    if (!isEditMode || !category) {
      return allCategories;
    }

    // In edit mode, exclude current category from parent options
    return allCategories.filter(cat => cat.id !== category.id);
  };

  const availableParents = getAvailableParents();

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-card rounded-xl border border-border max-w-lg w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-border sticky top-0 bg-card">
          <h3 className="text-lg font-semibold text-foreground">
            {isEditMode
              ? 'Cập nhật danh mục'
              : isAddChildMode
                ? `Thêm danh mục con cho "${parentCategory.name}"`
                : 'Thêm danh mục mới'
            }
          </h3>
          <button
            onClick={onCancel}
            className="p-1 hover:bg-secondary rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
            aria-label="Đóng"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          {/* Global error */}
          {submitError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {submitError}
            </div>
          )}

          {/* Name field */}
          <div>
            <label htmlFor="category-name" className="block text-sm font-medium text-foreground mb-1">
              Tên danh mục <span className="text-red-500">*</span>
            </label>
            <input
              id="category-name"
              type="text"
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              className={cn(
                "w-full px-3 py-2 border rounded-lg bg-background text-foreground",
                "focus:outline-none focus:ring-2 focus:ring-primary transition-shadow",
                errors.name ? "border-red-500" : "border-border"
              )}
              placeholder="Nhập tên danh mục"
              maxLength={100}
              autoFocus
              aria-invalid={!!errors.name}
              aria-describedby={errors.name ? "name-error" : undefined}
            />
            {errors.name && (
              <p id="name-error" className="mt-1 text-sm text-red-600">
                {errors.name}
              </p>
            )}
            <p className="mt-1 text-xs text-muted-foreground">
              {formData.name.length}/100 ký tự
            </p>
          </div>

          {/* Description field */}
          <div>
            <label htmlFor="category-description" className="block text-sm font-medium text-foreground mb-1">
              Mô tả
            </label>
            <textarea
              id="category-description"
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              className="w-full px-3 py-2 border border-border rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-shadow resize-none"
              placeholder="Nhập mô tả (tùy chọn)"
              rows={3}
              maxLength={500}
            />
            <p className="mt-1 text-xs text-muted-foreground">
              {formData.description.length}/500 ký tự
            </p>
          </div>

          {/* Parent category field */}
          {!isAddChildMode && (
            <div>
              <label htmlFor="category-parent" className="block text-sm font-medium text-foreground mb-1">
                Danh mục cha
              </label>
              <select
                id="category-parent"
                value={formData.parentId || ''}
                onChange={(e) => handleChange('parentId', e.target.value ? Number(e.target.value) : null)}
                className={cn(
                  "w-full px-3 py-2 border rounded-lg bg-background text-foreground",
                  "focus:outline-none focus:ring-2 focus:ring-primary transition-shadow",
                  errors.parentId ? "border-red-500" : "border-border"
                )}
                aria-invalid={!!errors.parentId}
                aria-describedby={errors.parentId ? "parent-error" : undefined}
              >
                <option value="">-- Không có (danh mục gốc) --</option>
                {availableParents.map(cat => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {errors.parentId && (
                <p id="parent-error" className="mt-1 text-sm text-red-600">
                  {errors.parentId}
                </p>
              )}
              <p className="mt-1 text-xs text-muted-foreground">
                Để trống nếu đây là danh mục gốc
              </p>
            </div>
          )}

          {isAddChildMode && (
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-sm text-blue-700">
                <strong>Danh mục cha:</strong> {parentCategory.name}
              </p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              disabled={loading}
              className="flex-1"
            >
              Hủy
            </Button>
            <Button
              type="submit"
              disabled={loading}
              className="flex-1"
            >
              {loading ? (
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Đang xử lý...</span>
                </div>
              ) : (
                <span>{isEditMode ? 'Cập nhật' : 'Thêm mới'}</span>
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

