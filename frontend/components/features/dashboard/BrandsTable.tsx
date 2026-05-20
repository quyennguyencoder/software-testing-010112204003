/**
 * BrandsTable component - Display brands in table format for admin
 * Following FRONTEND_DESIGN_SYSTEM.md principles:
 * - Accessibility: semantic HTML, focus states, ARIA labels
 * - Mobile-first responsive design
 * - Consistent spacing (4/8px grid)
 * - Color system: primary yellow, semantic states
 * - Consistent with CategoriesTable and UsersTable styles
 */

'use client';

import { Edit, Trash2, Tag, Image as ImageIcon } from 'lucide-react';
import type { BrandResponse } from '@/types';
import { cn } from '@/lib/utils';

interface BrandsTableProps {
  brands: BrandResponse[];
  onEdit: (brand: BrandResponse) => void;
  onDelete: (brand: BrandResponse) => void;
}

export function BrandsTable({ brands, onEdit, onDelete }: BrandsTableProps) {
  // Format date to readable string
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  return (
    <div className="space-y-4">
      {/* Table */}
      <div className="bg-card rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-secondary/30 border-b border-border">
              <tr>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                  Thương hiệu
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                  Mô tả
                </th>
                <th className="text-center py-3 px-4 font-semibold text-muted-foreground hidden lg:table-cell">
                  Logo
                </th>
                <th className="text-center py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">
                  Sản phẩm
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden xl:table-cell">
                  Ngày tạo
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody>
              {brands.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center">
                    <Tag className="w-12 h-12 text-muted-foreground mx-auto mb-3 opacity-50" />
                    <p className="text-muted-foreground">Chưa có thương hiệu nào</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      Nhấn &quot;Thêm thương hiệu&quot; để tạo thương hiệu mới
                    </p>
                  </td>
                </tr>
              ) : (
                brands.map((brand) => (
                  <tr
                    key={brand.id}
                    className="border-b border-border hover:bg-secondary/50 transition-colors"
                  >
                    {/* Brand Name */}
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <Tag className="w-4 h-4 text-primary flex-shrink-0" />
                        <span className="font-medium text-sm md:text-base truncate">
                          {brand.name}
                        </span>
                      </div>
                    </td>

                    {/* Description */}
                    <td className="py-3 px-4 hidden md:table-cell">
                      <span className="text-sm text-muted-foreground line-clamp-2">
                        {brand.description || (
                          <span className="italic text-muted-foreground/60">Chưa có mô tả</span>
                        )}
                      </span>
                    </td>

                    {/* Logo Image/Preview */}
                    <td className="py-3 px-4 hidden lg:table-cell">
                      {brand.logoUrl ? (
                        <div className="flex items-center justify-center">
                          <div className="relative w-12 h-12 flex-shrink-0">
                            <img 
                              src={brand.logoUrl} 
                              alt={`${brand.name} logo`}
                              className="w-full h-full object-contain rounded border border-border bg-white p-1"
                              onError={(e) => {
                                // Fallback to placeholder icon if image fails to load
                                const target = e.currentTarget;
                                target.style.display = 'none';
                                const fallback = target.nextElementSibling as HTMLElement;
                                if (fallback) fallback.style.display = 'flex';
                              }}
                            />
                            <div 
                              className="w-full h-full rounded border border-dashed border-gray-300 bg-gray-50 flex items-center justify-center"
                              style={{ display: 'none' }}
                            >
                              <ImageIcon className="w-6 h-6 text-gray-400" />
                            </div>
                          </div>
                        </div>
                      ) : (
                        <div className="flex items-center justify-center">
                          <div className="w-12 h-12 rounded border border-dashed border-gray-300 bg-gray-50 flex items-center justify-center flex-shrink-0">
                            <ImageIcon className="w-6 h-6 text-gray-400" />
                          </div>
                        </div>
                      )}
                    </td>

                    {/* Product Count */}
                    <td className="py-3 px-4 hidden sm:table-cell text-center">
                      <span className={cn(
                        "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium",
                        brand.productCount > 0 
                          ? "bg-blue-50 text-blue-700" 
                          : "bg-gray-100 text-gray-600"
                      )}>
                        {brand.productCount}
                      </span>
                    </td>

                    {/* Created Date */}
                    <td className="py-3 px-4 hidden xl:table-cell">
                      <span className="text-sm text-muted-foreground">
                        {formatDate(brand.createdAt)}
                      </span>
                    </td>

                    {/* Actions */}
                    <td className="py-3 px-4">
                      <div className="flex gap-1">
                        {/* Edit button */}
                        <button
                          onClick={() => onEdit(brand)}
                          className="p-2 hover:bg-secondary rounded-lg text-blue-600 transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                          aria-label="Chỉnh sửa"
                          title="Chỉnh sửa"
                        >
                          <Edit className="w-4 h-4" />
                        </button>

                        {/* Delete button */}
                        <button
                          onClick={() => onDelete(brand)}
                          className="p-2 hover:bg-secondary rounded-lg text-red-600 transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                          aria-label="Xóa"
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
    </div>
  );
}

