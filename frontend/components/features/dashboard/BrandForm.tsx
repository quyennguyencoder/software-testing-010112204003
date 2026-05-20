/**
 * BrandForm component - Form for creating/updating brands
 * Following FRONTEND_DESIGN_SYSTEM.md principles:
 * - Form validation with clear error messages
 * - Accessibility: labels, ARIA, focus management
 * - Loading states
 * - Mobile-first responsive
 * - Consistent with CategoryForm style
 */

'use client';

import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { adminAPI } from '@/lib/api';
import type { BrandResponse, CreateBrandRequest, UpdateBrandRequest } from '@/types';

interface BrandFormProps {
  brand?: BrandResponse | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function BrandForm({ brand, onSuccess, onCancel }: BrandFormProps) {
  const isEditMode = !!brand;

  const [formData, setFormData] = useState({
    name: brand?.name || '',
    description: brand?.description || '',
    logoUrl: brand?.logoUrl || '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Reset form when brand changes
  useEffect(() => {
    if (brand) {
      setFormData({
        name: brand.name,
        description: brand.description || '',
        logoUrl: brand.logoUrl || '',
      });
    } else {
      // Reset to empty for create mode
      setFormData({
        name: '',
        description: '',
        logoUrl: '',
      });
    }
  }, [brand]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Name validation (matching backend: 2-100 characters)
    if (!formData.name.trim()) {
      newErrors.name = 'Tên thương hiệu không được để trống';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Tên thương hiệu phải có ít nhất 2 ký tự';
    } else if (formData.name.trim().length > 100) {
      newErrors.name = 'Tên thương hiệu không được vượt quá 100 ký tự';
    }

    // Description validation (matching backend: max 500 characters)
    if (formData.description && formData.description.length > 500) {
      newErrors.description = 'Mô tả không được vượt quá 500 ký tự';
    }

    // Logo URL validation (matching backend: max 255 characters)
    if (formData.logoUrl && formData.logoUrl.length > 255) {
      newErrors.logoUrl = 'URL logo không được vượt quá 255 ký tự';
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
      const requestData = {
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        logoUrl: formData.logoUrl.trim() || undefined,
      };

      if (isEditMode && brand) {
        // Update existing brand
        const updateData: UpdateBrandRequest = requestData;
        const response = await adminAPI.updateBrand(brand.id, updateData);

        if (response.success) {
          onSuccess();
        } else {
          throw new Error(response.message || 'Lỗi khi cập nhật thương hiệu');
        }
      } else {
        // Create new brand
        const createData: CreateBrandRequest = requestData;
        const response = await adminAPI.createBrand(createData);

        if (response.success) {
          onSuccess();
        } else {
          throw new Error(response.message || 'Lỗi khi tạo thương hiệu');
        }
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Đã xảy ra lỗi';
      setSubmitError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: string) => {
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

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-card rounded-xl border border-border max-w-lg w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-border sticky top-0 bg-card">
          <h3 className="text-lg font-semibold text-foreground">
            {isEditMode ? 'Cập nhật thương hiệu' : 'Thêm thương hiệu mới'}
          </h3>
          <button
            onClick={onCancel}
            className="p-2 hover:bg-secondary rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
            aria-label="Đóng"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          {/* Error message */}
          {submitError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-sm text-red-700">{submitError}</p>
            </div>
          )}

          {/* Name field */}
          <div>
            <label htmlFor="brand-name" className="block text-sm font-medium text-foreground mb-1.5">
              Tên thương hiệu <span className="text-red-500">*</span>
            </label>
            <input
              id="brand-name"
              type="text"
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              className={cn(
                "w-full px-3 py-2 border rounded-lg bg-background text-foreground",
                "placeholder:text-muted-foreground",
                "focus:outline-none focus:ring-2 focus:ring-primary transition-shadow",
                errors.name ? "border-red-500" : "border-border"
              )}
              placeholder="Ví dụ: Apple, Samsung, Xiaomi..."
              disabled={loading}
              aria-invalid={!!errors.name}
              aria-describedby={errors.name ? "name-error" : undefined}
            />
            {errors.name && (
              <p id="name-error" className="text-sm text-red-600 mt-1">
                {errors.name}
              </p>
            )}
          </div>

          {/* Description field */}
          <div>
            <label htmlFor="brand-description" className="block text-sm font-medium text-foreground mb-1.5">
              Mô tả
            </label>
            <textarea
              id="brand-description"
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              rows={3}
              className={cn(
                "w-full px-3 py-2 border rounded-lg bg-background text-foreground",
                "placeholder:text-muted-foreground resize-none",
                "focus:outline-none focus:ring-2 focus:ring-primary transition-shadow",
                errors.description ? "border-red-500" : "border-border"
              )}
              placeholder="Mô tả ngắn về thương hiệu (tùy chọn)"
              disabled={loading}
              aria-invalid={!!errors.description}
              aria-describedby={errors.description ? "description-error" : undefined}
            />
            {errors.description && (
              <p id="description-error" className="text-sm text-red-600 mt-1">
                {errors.description}
              </p>
            )}
            <p className="text-xs text-muted-foreground mt-1">
              {formData.description.length}/500 ký tự
            </p>
          </div>

          {/* Logo URL field */}
          <div>
            <label htmlFor="brand-logo-url" className="block text-sm font-medium text-foreground mb-1.5">
              URL Logo
            </label>
            <input
              id="brand-logo-url"
              type="url"
              value={formData.logoUrl}
              onChange={(e) => handleChange('logoUrl', e.target.value)}
              className={cn(
                "w-full px-3 py-2 border rounded-lg bg-background text-foreground",
                "placeholder:text-muted-foreground",
                "focus:outline-none focus:ring-2 focus:ring-primary transition-shadow",
                errors.logoUrl ? "border-red-500" : "border-border"
              )}
              placeholder="https://example.com/logo.png (tùy chọn)"
              disabled={loading}
              aria-invalid={!!errors.logoUrl}
              aria-describedby={errors.logoUrl ? "logo-url-error" : undefined}
            />
            {errors.logoUrl && (
              <p id="logo-url-error" className="text-sm text-red-600 mt-1">
                {errors.logoUrl}
              </p>
            )}
          </div>

          {/* Form Actions */}
          <div className="flex gap-3 pt-4 border-t border-border">
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
                  <span>{isEditMode ? 'Đang cập nhật...' : 'Đang tạo...'}</span>
                </div>
              ) : (
                <span>{isEditMode ? 'Cập nhật' : 'Tạo mới'}</span>
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}

