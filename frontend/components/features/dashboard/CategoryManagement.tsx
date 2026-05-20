/**
 * CategoryManagement component - Main component for category CRUD operations
 * Following FRONTEND_DESIGN_SYSTEM.md principles:
 * - Server Component by default (but uses 'use client' for state management)
 * - Loading, empty, error states
 * - Confirmation dialogs
 * - Success feedback with auto-refresh
 */

'use client';

import { useState, useCallback, useMemo, useEffect } from 'react';
import { Plus, Search, FolderTree, Package } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { CategoriesTable } from './CategoriesTable';
import { CategoryForm } from './CategoryForm';
import { useCategories } from '@/hooks';
import { adminAPI } from '@/lib/api';
import type { CategoryResponse } from '@/types';
import { cn } from '@/lib/utils';

type FormMode = 'create' | 'edit' | 'add-child' | null;

interface ToastState {
  show: boolean;
  message: string;
  type: 'success' | 'error';
}

export function CategoryManagement() {
  const { categories, loading, error, refetch } = useCategories({ parentId: null });
  const [formMode, setFormMode] = useState<FormMode>(null);
  const [selectedCategory, setSelectedCategory] = useState<CategoryResponse | null>(null);
  const [parentCategory, setParentCategory] = useState<CategoryResponse | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<CategoryResponse | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [toast, setToast] = useState<ToastState>({ show: false, message: '', type: 'success' });
  const [allCategoriesFlat, setAllCategoriesFlat] = useState<CategoryResponse[]>([]);
  const [searchMatches, setSearchMatches] = useState<Set<number>>(new Set());

  // Toast notification helper
  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ show: true, message, type });
    setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
  };

  // Fetch all categories recursively for search
  const fetchAllCategories = useCallback(async () => {
    const allCats: CategoryResponse[] = [];
    
    const fetchRecursive = async (parentId: number | null) => {
      try {
        const response = await adminAPI.getAllCategories(parentId);
        if (response.success && response.data) {
          for (const cat of response.data) {
            allCats.push(cat);
            if (cat.hasChildren) {
              await fetchRecursive(cat.id);
            }
          }
        }
      } catch (err) {
        console.error('Error fetching categories:', err);
      }
    };

    await fetchRecursive(null);
    setAllCategoriesFlat(allCats);
  }, []);

  // Load all categories on mount
  useEffect(() => {
    fetchAllCategories();
  }, [fetchAllCategories]);

  // Refresh all categories when data changes
  useEffect(() => {
    if (categories.length > 0) {
      fetchAllCategories();
    }
  }, [categories, fetchAllCategories]);

  // Filter categories by search query (includes children)
  const filteredCategories = useMemo(() => {
    if (!searchQuery.trim()) {
      setSearchMatches(new Set());
      return categories;
    }
    
    const query = searchQuery.toLowerCase();
    const matches = new Set<number>();
    const parentIds = new Set<number>();

    // Find all matching categories (including children)
    allCategoriesFlat.forEach(cat => {
      if (cat.name.toLowerCase().includes(query)) {
        matches.add(cat.id);
        // Add parent to show hierarchy
        if (cat.parentId) {
          parentIds.add(cat.parentId);
        }
      }
    });

    setSearchMatches(matches);

    // Return root categories that match or have matching children
    return categories.filter(cat => 
      matches.has(cat.id) || parentIds.has(cat.id)
    );
  }, [categories, searchQuery, allCategoriesFlat]);

  // Calculate stats
  const totalCategories = categories.length;
  const totalResults = searchMatches.size;

  const handleAddNew = useCallback(() => {
    setFormMode('create');
    setSelectedCategory(null);
    setParentCategory(null);
  }, []);

  const handleEdit = useCallback((category: CategoryResponse) => {
    setFormMode('edit');
    setSelectedCategory(category);
    setParentCategory(null);
  }, []);

  const handleAddChild = useCallback((category: CategoryResponse) => {
    setFormMode('add-child');
    setSelectedCategory(null);
    setParentCategory(category);
  }, []);

  const handleDelete = useCallback((category: CategoryResponse) => {
    setDeleteConfirm(category);
  }, []);

  const confirmDelete = async () => {
    if (!deleteConfirm) return;

    setDeleting(true);
    try {
      const response = await adminAPI.deleteCategory(deleteConfirm.id);

      if (response.success) {
        setDeleteConfirm(null);
        showToast('Xóa danh mục thành công!', 'success');
        await refetch();
      } else {
        throw new Error(response.message || 'Lỗi khi xóa danh mục');
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Đã xảy ra lỗi khi xóa';
      showToast(errorMessage, 'error');
    } finally {
      setDeleting(false);
    }
  };

  const handleFormSuccess = async () => {
    const message = formMode === 'edit'
      ? 'Cập nhật danh mục thành công!'
      : 'Thêm danh mục thành công!';

    setFormMode(null);
    setSelectedCategory(null);
    setParentCategory(null);

    showToast(message, 'success');
    await refetch();
  };

  const handleFormCancel = () => {
    setFormMode(null);
    setSelectedCategory(null);
    setParentCategory(null);
  };

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {toast.show && (
        <div className="fixed top-4 right-4 z-50 animate-in slide-in-from-top-2">
          <div className={cn(
            "px-4 py-3 rounded-lg shadow-lg border flex items-center gap-3 min-w-[300px]",
            toast.type === 'success'
              ? "bg-green-50 border-green-200 text-green-800"
              : "bg-red-50 border-red-200 text-red-800"
          )}>
            <div className={cn(
              "w-2 h-2 rounded-full flex-shrink-0",
              toast.type === 'success' ? "bg-green-500" : "bg-red-500"
            )} />
            <p className="text-sm font-medium">{toast.message}</p>
          </div>
        </div>
      )}

      {/* Header - Single title with stats */}
      <div className="flex flex-col gap-4">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <FolderTree className="w-5 h-5 text-primary" />
            </div>
            <div>
              <h2 className="text-xl font-semibold text-foreground">Quản lý danh mục</h2>
              <div className="flex items-center gap-2 mt-1">
                <span className="inline-flex items-center gap-1.5 px-2 py-0.5 bg-blue-50 text-blue-700 text-xs font-medium rounded-full">
                  <Package className="w-3 h-3" />
                  {totalCategories} danh mục
                </span>
              </div>
            </div>
          </div>
          <Button onClick={handleAddNew} className="gap-2">
            <Plus className="w-4 h-4" />
            Thêm danh mục
          </Button>
        </div>

        {/* Search bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Tìm kiếm danh mục theo tên..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-border rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-shadow"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              <span className="text-lg">×</span>
            </button>
          )}
        </div>

        {/* Search results info */}
        {searchQuery && (
          <p className="text-sm text-muted-foreground">
            Tìm thấy <strong>{totalResults}</strong> kết quả cho &quot;{searchQuery}&quot;
          </p>
        )}
      </div>

      {/* Error state */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm text-red-700">{error}</p>
          <Button
            onClick={() => refetch()}
            variant="outline"
            size="sm"
            className="mt-2"
          >
            Thử lại
          </Button>
        </div>
      )}

      {/* Loading state */}
      {loading && !error && (
        <div className="bg-card rounded-xl border border-border p-12">
          <div className="flex flex-col items-center justify-center">
            <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-4" />
            <p className="text-muted-foreground">Đang tải danh mục...</p>
          </div>
        </div>
      )}

      {/* Table */}
      {!loading && !error && (
        <CategoriesTable
          categories={filteredCategories}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onAddChild={handleAddChild}
          searchQuery={searchQuery}
          searchMatches={searchMatches}
        />
      )}

      {/* Form modal */}
      {formMode && (
        <CategoryForm
          category={selectedCategory}
          parentCategory={parentCategory}
          allCategories={categories}
          onSuccess={handleFormSuccess}
          onCancel={handleFormCancel}
        />
      )}

      {/* Delete confirmation dialog */}
      {deleteConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-card rounded-xl border border-border max-w-md w-full p-6">
            <h3 className="text-lg font-semibold text-foreground mb-2">
              Xác nhận xóa danh mục
            </h3>
            <p className="text-muted-foreground mb-4">
              Bạn có chắc chắn muốn xóa danh mục <strong>{deleteConfirm.name}</strong>?
            </p>
            
            {deleteConfirm.childrenCount > 0 ? (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg mb-4">
                <p className="text-sm text-red-700 font-medium flex items-center gap-2">
                  ⚠️ Không thể xóa!
                </p>
                <p className="text-sm text-red-600 mt-1">
                  Danh mục này đang có <strong>{deleteConfirm.childrenCount}</strong> danh mục con. 
                  Vui lòng xóa các danh mục con trước.
                </p>
              </div>
            ) : deleteConfirm.productCount > 0 ? (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg mb-4">
                <p className="text-sm text-red-700 font-medium flex items-center gap-2">
                  ⚠️ Không thể xóa!
                </p>
                <p className="text-sm text-red-600 mt-1">
                  Danh mục này đang có <strong>{deleteConfirm.productCount}</strong> sản phẩm liên kết. 
                  Vui lòng xóa hoặc chuyển sản phẩm sang danh mục khác trước.
                </p>
              </div>
            ) : (
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg mb-4">
                <p className="text-sm text-amber-700">
                  Hành động này không thể hoàn tác.
                </p>
              </div>
            )}

            <div className="flex gap-3">
              <Button
                variant="outline"
                onClick={() => setDeleteConfirm(null)}
                disabled={deleting}
                className="flex-1"
              >
                Hủy
              </Button>
              <Button
                variant="destructive"
                onClick={confirmDelete}
                disabled={deleting || deleteConfirm.childrenCount > 0 || deleteConfirm.productCount > 0}
                className="flex-1"
              >
                {deleting ? (
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Đang xóa...</span>
                  </div>
                ) : (
                  <span>Xóa</span>
                )}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

