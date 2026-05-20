/**
 * CategoriesTable component - Display categories in hierarchical table format for admin
 * Following FRONTEND_DESIGN_SYSTEM.md principles:
 * - Server Component by default (but this uses 'use client' for interactivity)
 * - Accessibility: semantic HTML, focus states, ARIA labels
 * - Mobile-first responsive design
 * - Consistent spacing (4/8px grid)
 * - Color system: primary yellow, semantic states
 */

'use client';

import React, { useState, useEffect, useRef } from 'react';
import { Edit, Trash2, ChevronRight, ChevronDown, Folder, FolderOpen } from 'lucide-react';
import { cn } from '@/lib/utils';
import { adminAPI } from '@/lib/api';
import type { CategoryResponse } from '@/types';

interface CategoriesTableProps {
  categories: CategoryResponse[];
  onEdit: (category: CategoryResponse) => void;
  onDelete: (category: CategoryResponse) => void;
  onAddChild: (parentCategory: CategoryResponse) => void;
  searchQuery?: string;
  searchMatches?: Set<number>;
}

export function CategoriesTable({ categories, onEdit, onDelete, onAddChild, searchQuery, searchMatches }: CategoriesTableProps) {
  const [expandedCategories, setExpandedCategories] = useState<Set<number>>(new Set());
  const [childrenCache, setChildrenCache] = useState<Map<number, CategoryResponse[]>>(new Map());
  const [loadingChildren, setLoadingChildren] = useState<Set<number>>(new Set());
  const previousSearchQuery = useRef<string>('');

  // Auto-expand parent categories when search has results
  useEffect(() => {
    // Only run when search query actually changes
    if (previousSearchQuery.current === searchQuery) {
      return;
    }
    previousSearchQuery.current = searchQuery || '';

    if (searchQuery && searchMatches && searchMatches.size > 0) {
      const parentsToExpand = new Set<number>();
      
      // Find parent categories that have matching children
      categories.forEach(cat => {
        if (cat.hasChildren) {
          // Check if any children match (not the parent itself)
          const hasMatchingChild = Array.from(searchMatches).some(matchId => {
            // Find if this match is a child of current category
            return matchId !== cat.id; // Exclude parent itself
          });
          
          if (hasMatchingChild) {
            parentsToExpand.add(cat.id);
          }
        }
      });

      // Auto-expand and load children for parents with matching results
      if (parentsToExpand.size > 0) {
        parentsToExpand.forEach(async (catId) => {
          if (!childrenCache.has(catId)) {
            try {
              const response = await adminAPI.getAllCategories(catId);
              if (response.success && response.data) {
                setChildrenCache(prev => new Map(prev).set(catId, response.data));
              }
            } catch (err) {
              console.error('Error loading children:', err);
            }
          }
        });

        setExpandedCategories(parentsToExpand);
      }
    } else if (!searchQuery) {
      // Collapse all when search is cleared
      setExpandedCategories(new Set());
    }
  }, [searchQuery, searchMatches, categories, childrenCache]);

  const toggleExpand = async (categoryId: number) => {
    const isExpanded = expandedCategories.has(categoryId);

    if (isExpanded) {
      // Collapse
      const newExpanded = new Set(expandedCategories);
      newExpanded.delete(categoryId);
      setExpandedCategories(newExpanded);
    } else {
      if (!childrenCache.has(categoryId)) {
        setLoadingChildren(prev => new Set(prev).add(categoryId));
        try {
          const response = await adminAPI.getAllCategories(categoryId);
          if (response.success && response.data) {
            setChildrenCache(prev => new Map(prev).set(categoryId, response.data));
          }
        } finally {
          setLoadingChildren(prev => {
            const newSet = new Set(prev);
            newSet.delete(categoryId);
            return newSet;
          });
        }
      }

      const newExpanded = new Set(expandedCategories);
      newExpanded.add(categoryId);
      setExpandedCategories(newExpanded);
    }
  };

  const renderCategoryRow = (category: CategoryResponse, level: number = 0): React.ReactElement => {
    const isExpanded = expandedCategories.has(category.id);
    const isLoading = loadingChildren.has(category.id);
    const children = childrenCache.get(category.id) || [];
    const hasChildren = category.hasChildren;

    return (
      <>
        {/* Category Row */}
        <tr
          key={category.id}
          className={cn(
            "border-b border-border hover:bg-secondary/50 transition-colors",
            level > 0 && "bg-secondary/20"
          )}
        >
          {/* Category Name with indent and expand/collapse */}
          <td className="py-3 px-4">
            <div className="flex items-center gap-2">
              {/* Indent for hierarchy */}
              <div style={{ width: `${level * 24}px` }} />

              {/* Expand/Collapse button */}
              {hasChildren ? (
                <button
                  onClick={() => toggleExpand(category.id)}
                  className="p-1 hover:bg-secondary rounded transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                  aria-label={isExpanded ? 'Thu gọn' : 'Mở rộng'}
                >
                  {isLoading ? (
                    <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                  ) : isExpanded ? (
                    <ChevronDown className="w-4 h-4 text-muted-foreground" />
                  ) : (
                    <ChevronRight className="w-4 h-4 text-muted-foreground" />
                  )}
                </button>
              ) : (
                <div className="w-6" />
              )}

              {/* Folder icon */}
              {isExpanded ? (
                <FolderOpen className="w-4 h-4 text-primary flex-shrink-0" />
              ) : (
                <Folder className="w-4 h-4 text-muted-foreground flex-shrink-0" />
              )}

              {/* Category name */}
              <span className={cn(
                "font-medium text-sm md:text-base truncate",
                searchMatches && searchMatches.has(category.id) && "bg-yellow-100 text-yellow-900 px-1 rounded"
              )}>
                {category.name}
              </span>
            </div>
          </td>

          {/* Children Count Column */}
          <td className="py-3 px-4 hidden md:table-cell text-center">
            <span className="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-700 text-xs font-semibold rounded-full">
              {category.childrenCount || 0}
            </span>
          </td>

          {/* Product Count Column */}
          <td className="py-3 px-4 hidden lg:table-cell text-center">
            <span className={cn(
              "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium",
              (category.productCount || 0) > 0 
                ? "bg-blue-50 text-blue-700" 
                : "bg-gray-100 text-gray-600"
            )}>
              {category.productCount || 0}
            </span>
          </td>

          {/* Actions */}
          <td className="py-3 px-4">
            <div className="flex gap-1">
              {/* Add child button - CHỈ hiển thị cho root categories (level === 0) */}
              {level === 0 && (
                <button
                  onClick={() => onAddChild(category)}
                  className="px-3 py-1.5 text-xs font-medium text-green-700 bg-green-50 hover:bg-green-100 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary whitespace-nowrap"
                  title="Thêm danh mục con"
                >
                  Thêm danh mục con
                </button>
              )}
              {/* Edit button */}
              <button
                onClick={() => onEdit(category)}
                className="p-2 hover:bg-secondary rounded-lg text-blue-600 transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                aria-label="Chỉnh sửa"
                title="Chỉnh sửa"
              >
                <Edit className="w-4 h-4" />
              </button>

              {/* Delete button */}
              <button
                onClick={() => onDelete(category)}
                className="p-2 hover:bg-secondary rounded-lg text-red-600 transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                aria-label="Xóa"
                title="Xóa"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </td>
        </tr>

        {/* Children rows (recursive) */}
        {isExpanded && children.length > 0 &&
          children.map(child => (
            <React.Fragment key={child.id}>
              {renderCategoryRow(child, level + 1)}
            </React.Fragment>
          ))
        }
      </>
    );
  };

  return (
    <div className="space-y-4">
      {/* Table */}
      <div className="bg-card rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-secondary/50">
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                  Tên danh mục
                </th>
                <th className="text-center py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                  Danh mục con
                </th>
                <th className="text-center py-3 px-4 font-semibold text-muted-foreground hidden lg:table-cell">
                  Sản phẩm
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody>
              {categories.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-12 text-center">
                    <Folder className="w-12 h-12 text-muted-foreground mx-auto mb-3 opacity-50" />
                    <p className="text-muted-foreground">Chưa có danh mục nào</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      Nhấn &quot;Thêm danh mục&quot; để tạo danh mục mới
                    </p>
                  </td>
                </tr>
              ) : (
                categories.map(category => (
                  <React.Fragment key={category.id}>
                    {renderCategoryRow(category, 0)}
                  </React.Fragment>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

