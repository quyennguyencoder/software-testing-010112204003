'use client';

/**
 * ProductTable Component - Admin Product Management
 * Hiển thị danh sách sản phẩm với actions: Edit, Delete, Restore
 */

import { useState, useEffect, useCallback } from 'react';
import { Product } from '@/types';
import type { CategoryResponse } from '@/types/category';
import type { BrandResponse } from '@/types/brand';
import { 
  getAllProductsAdmin, 
  deleteProduct, 
  restoreProduct 
} from '@/services/product.service';
import { adminAPI, productAPI } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import ConfirmDialog from '@/components/common/ConfirmDialog';

// Constants
const EXCLUDED_CATEGORY = 'Phụ kiện';

interface ProductTableProps {
  filters?: {
    keyword?: string;
    categoryId?: number;
    brandId?: number;
    deletedStatus?: 'all' | 'active' | 'deleted';
    includeDeleted?: boolean;
  };
  onEdit?: (product: Product) => void;
  onRefresh?: () => void;
}

export function ProductTable({ filters, onEdit, onRefresh }: ProductTableProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Confirm dialog state
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    description: string;
    onConfirm: () => void;
  }>({ open: false, title: '', description: '', onConfirm: () => {} });
  
  // State cho inline editing
  const [editingProductId, setEditingProductId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({
    name: '',
    description: '',
    categoryId: 0,
    brandId: 0,
    price: 0,
    stockQuantity: 0,
    thumbnailUrl: ''
  });
  
  // Local filter states for UI
  const [searchInput, setSearchInput] = useState(''); // For input value
  const [searchKeyword, setSearchKeyword] = useState(''); // For API call
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>();
  const [selectedBrand, setSelectedBrand] = useState<number | undefined>();
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'stockQuantity' | 'createdAt'>('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  
  // Categories and brands from API
  const [categories, setCategories] = useState<Array<{ id: number; name: string }>>([]);
  const [brands, setBrands] = useState<Array<{ id: number; name: string }>>([]);

  // Load categories and brands on mount
  useEffect(() => {
    const loadFilters = async () => {
      try {
        const [catsRes, brandsRes] = await Promise.all([
          adminAPI.getAllCategories(),
          adminAPI.getAllBrands()
        ]);
        if (catsRes.data) {
          // Filter out excluded category
          const filteredCategories = catsRes.data.filter((c: CategoryResponse) => c.name !== EXCLUDED_CATEGORY);
          setCategories(filteredCategories.map((c: CategoryResponse) => ({ id: c.id, name: c.name })));
        }
        if (brandsRes.data) {
          setBrands(brandsRes.data.map((b: BrandResponse) => ({ id: b.id, name: b.name })));
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Không thể tải bộ lọc';
        console.error('Failed to load filters:', errorMessage);
      }
    };
    
    loadFilters();
  }, []);

  const loadProducts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;

      // Use different endpoint for deleted products (Trash tab)
      if (filters?.deletedStatus === 'deleted') {
        const { getDeletedProducts } = await import('@/services/product.service');
        
        // Only send keyword if it has at least 2 characters
        const validKeyword = (searchKeyword || filters.keyword)?.trim();
        const keyword = validKeyword && validKeyword.length >= 2 ? validKeyword : undefined;
        
        response = await getDeletedProducts({
          keyword,
          categoryId: selectedCategory || filters.categoryId,
          brandId: selectedBrand || filters.brandId,
        });
      } else {
        // Only send keyword if it has at least 2 characters
        const validKeyword = (searchKeyword || filters?.keyword)?.trim();
        const keyword = validKeyword && validKeyword.length >= 2 ? validKeyword : undefined;
        
        // Build API filters - pass all params to API for server-side filtering
        const apiFilters = {
          keyword,
          categoryId: selectedCategory || filters?.categoryId,
          brandId: selectedBrand || filters?.brandId,
          sortBy,
          sortDirection,
          includeDeleted: filters?.includeDeleted,
        };
        
        response = await getAllProductsAdmin(apiFilters);
      }
      
      if (response.success && response.data) {
        setProducts(response.data as unknown as Product[]);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Lỗi khi tải danh sách sản phẩm';
      setError(errorMessage);
      console.error('Error loading products:', errorMessage);
    } finally {
      setLoading(false);
    }
  }, [filters, searchKeyword, selectedCategory, selectedBrand, sortBy, sortDirection]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  // Helper functions
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const handleDelete = async (id: number, name: string) => {
    setConfirmDialog({
      open: true,
      title: 'Ngưng bán sản phẩm',
      description: `Bạn có chắc muốn ngưng bán sản phẩm "${name}"? (Có thể khôi phục sau)`,
      onConfirm: async () => {
        try {
          await deleteProduct(id);
          toast.success('Ngưng bán sản phẩm thành công', {
            description: `Sản phẩm "${name}" đã được chuyển vào thùng rác`,
          });
          await loadProducts();
          if (onRefresh) onRefresh();
        } catch (err) {
          toast.error('Lỗi khi ngưng bán sản phẩm', {
            description: err instanceof Error ? err.message : 'Vui lòng thử lại',
          });
        } finally {
          setConfirmDialog({ ...confirmDialog, open: false });
        }
      },
    });
  };

  const handleRestore = async (id: number, name: string) => {
    setConfirmDialog({
      open: true,
      title: 'Khôi phục sản phẩm',
      description: `Bạn có chắc muốn khôi phục sản phẩm "${name}"?`,
      onConfirm: async () => {
        try {
          await restoreProduct(id);
          toast.success('Khôi phục sản phẩm thành công', {
            description: `Sản phẩm "${name}" đã được khôi phục`,
          });
          await loadProducts();
          if (onRefresh) onRefresh();
        } catch (err) {
          toast.error('Lỗi khi khôi phục sản phẩm', {
            description: err instanceof Error ? err.message : 'Vui lòng thử lại',
          });
        } finally {
          setConfirmDialog({ ...confirmDialog, open: false });
        }
      },
    });
  };

  const handleEditClick = (product: Product) => {
    setEditingProductId(product.id);
    
    // Use direct properties if available, otherwise use first template
    const price = product.price ?? product.templates?.[0]?.price ?? 0;
    const stockQuantity = product.stockQuantity ?? product.templates?.reduce((sum, t) => sum + t.stockQuantity, 0) ?? 0;
    
    setEditForm({
      name: product.name,
      description: product.description || '',
      categoryId: product.categoryId || categories[0]?.id || 1,
      brandId: product.brandId || brands[0]?.id || 1,
      price,
      stockQuantity,
      thumbnailUrl: product.thumbnailUrl || ''
    });
  };

  const handleCancelEdit = () => {
    setEditingProductId(null);
  };

  const handleUpdateProduct = async (productId: number) => {
    try {
      const response = await productAPI.update(productId, editForm);
      
      if (response.success) {
        toast.success('Cập nhật sản phẩm thành công!', {
          description: `Thay đổi đã được lưu`,
        });
        setEditingProductId(null);
        await loadProducts();
        if (onRefresh) onRefresh();
      } else {
        throw new Error(response.message || 'Lỗi khi cập nhật');
      }
    } catch (err) {
      toast.error('Lỗi khi cập nhật sản phẩm', {
        description: err instanceof Error ? err.message : 'Vui lòng thử lại',
      });
    }
  };

  // Apply client-side sorting if needed (when API sort doesn't work properly)
  const filteredProducts = [...products].sort((a, b) => {
    let comparison = 0;
    switch (sortBy) {
      case 'name':
        comparison = a.name.localeCompare(b.name);
        break;
      case 'price':
        const aPrice = (a as any).price || 0;
        const bPrice = (b as any).price || 0;
        comparison = aPrice - bPrice;
        break;
      case 'stockQuantity':
        comparison = ((a as any).stockQuantity || 0) - ((b as any).stockQuantity || 0);
        break;
      case 'createdAt':
        comparison = new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime();
        break;
    }
    return sortDirection === 'asc' ? comparison : -comparison;
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-muted-foreground">Đang tải...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <div className="text-destructive">{error}</div>
        <Button onClick={loadProducts}>Thử lại</Button>
      </div>
    );
  }

  if (products.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-muted-foreground">Chưa có sản phẩm nào</div>
      </div>
    );
  }

  return (
    <div className="w-full space-y-4">
      {/* Search and Filter Bar */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        {/* Search */}
        <div className="flex-1 max-w-md">
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm (Enter để tìm)..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                setSearchKeyword(searchInput);
              }
            }}
            onBlur={() => setSearchKeyword(searchInput)}
            className="w-full px-4 py-2 border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-2">
          {/* Category Filter */}
          <select
            value={selectedCategory || ''}
            onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : undefined)}
            className="px-3 py-2 border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Tất cả danh mục</option>
            {categories.map(cat => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>

          {/* Brand Filter */}
          <select
            value={selectedBrand || ''}
            onChange={(e) => setSelectedBrand(e.target.value ? Number(e.target.value) : undefined)}
            className="px-3 py-2 border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Tất cả thương hiệu</option>
            {brands.map(brand => (
              <option key={brand.id} value={brand.id}>{brand.name}</option>
            ))}
          </select>

          {/* Sort */}
          <select
            value={`${sortBy}-${sortDirection}`}
            onChange={(e) => {
              const [field, direction] = e.target.value.split('-');
              setSortBy(field as any);
              setSortDirection(direction as 'asc' | 'desc');
            }}
            className="px-3 py-2 border border-border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="createdAt-desc">Mới nhất</option>
            <option value="createdAt-asc">Cũ nhất</option>
            <option value="name-asc">Tên A-Z</option>
            <option value="name-desc">Tên Z-A</option>
            <option value="price-asc">Giá thấp - cao</option>
            <option value="price-desc">Giá cao - thấp</option>
            <option value="stockQuantity-asc">Tồn kho thấp - cao</option>
            <option value="stockQuantity-desc">Tồn kho cao - thấp</option>
          </select>
        </div>
      </div>

      {/* Product Count */}
      <div className="text-sm text-muted-foreground">
        Hiển thị {filteredProducts.length} / {products.length} sản phẩm
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full border-collapse">
          <thead>
            <tr className="border-b border-border bg-muted/50">
              <th className="px-4 py-3 text-left text-sm font-medium">ID</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Tên sản phẩm</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Danh mục</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Thương hiệu</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Giá (VND)</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Tồn kho</th>
              <th className="px-4 py-3 text-left text-sm font-medium">Trạng thái</th>
              <th className="px-4 py-3 text-center text-sm font-medium">Thao tác</th>
            </tr>
          </thead>
        <tbody>
          {filteredProducts.map((product) => (
            editingProductId === product.id ? (
              // Form chỉnh sửa inline
              <tr key={product.id} className="border-b border-border bg-blue-50">
                <td className="px-4 py-3 text-sm">{product.id}</td>
                <td colSpan={6} className="px-4 py-3">
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium mb-1">Tên sản phẩm</label>
                      <input
                        type="text"
                        value={editForm.name}
                        onChange={(e) => setEditForm({...editForm, name: e.target.value})}
                        className="w-full px-3 py-2 border rounded-md"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-1">Mô tả</label>
                      <textarea
                        value={editForm.description}
                        onChange={(e) => setEditForm({...editForm, description: e.target.value})}
                        className="w-full px-3 py-2 border rounded-md"
                        rows={2}
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-1">Thumbnail URL</label>
                      <input
                        type="text"
                        value={editForm.thumbnailUrl || ''}
                        onChange={(e) => setEditForm({...editForm, thumbnailUrl: e.target.value})}
                        className="w-full px-3 py-2 border rounded-md"
                        placeholder="https://example.com/image.jpg"
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-sm font-medium mb-1">Danh mục</label>
                        <select
                          value={editForm.categoryId}
                          onChange={(e) => setEditForm({...editForm, categoryId: Number(e.target.value)})}
                          className="w-full px-3 py-2 border rounded-md"
                        >
                          {categories.map(cat => (
                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-1">Thương hiệu</label>
                        <select
                          value={editForm.brandId}
                          onChange={(e) => setEditForm({...editForm, brandId: Number(e.target.value)})}
                          className="w-full px-3 py-2 border rounded-md"
                        >
                          {brands.map(brand => (
                            <option key={brand.id} value={brand.id}>{brand.name}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-sm font-medium mb-1">Giá (VNĐ)</label>
                        <input
                          type="number"
                          value={editForm.price}
                          onChange={(e) => setEditForm({...editForm, price: Number(e.target.value)})}
                          className="w-full px-3 py-2 border rounded-md"
                          min="0"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-1">Số lượng tồn kho</label>
                        <input
                          type="number"
                          value={editForm.stockQuantity}
                          onChange={(e) => setEditForm({...editForm, stockQuantity: Number(e.target.value)})}
                          className="w-full px-3 py-2 border rounded-md"
                          min="0"
                        />
                      </div>
                    </div>
                    <div className="flex gap-2 justify-end">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={handleCancelEdit}
                      >
                        Hủy
                      </Button>
                      <Button
                        size="sm"
                        onClick={() => handleUpdateProduct(product.id)}
                      >
                        Cập nhật
                      </Button>
                    </div>
                  </div>
                </td>
              </tr>
            ) : (
            <tr
              key={product.id}
              className={`border-b border-border hover:bg-muted/30 ${
                product.isDeleted ? 'opacity-50' : ''
              }`}
            >
              <td className="px-4 py-3 text-sm">{product.id}</td>
              <td className="px-4 py-3 text-sm font-medium">{product.name}</td>
              <td className="px-4 py-3 text-sm">{product.categoryName || '—'}</td>
              <td className="px-4 py-3 text-sm">{product.brandName || '—'}</td>
              <td className="px-4 py-3 text-sm">
                {(product.price ?? product.templates?.[0]?.price) ? formatPrice(product.price ?? product.templates![0].price) : '—'}
              </td>
              <td className="px-4 py-3 text-sm">
                {product.stockQuantity ?? product.templates?.reduce((sum: number, t: any) => sum + t.stockQuantity, 0) ?? 0}
              </td>
              <td className="px-4 py-3 text-sm">
                {product.isDeleted || filters?.deletedStatus === 'deleted' ? (
                  <span className="text-orange-600 font-medium">Ngưng bán</span>
                ) : (
                  <span className="text-green-600 font-medium">Hoạt động</span>
                )}
              </td>
              <td className="px-4 py-3 text-sm">
                <div className="flex items-center justify-center gap-2">
                  {filters?.deletedStatus === 'deleted' || product.isDeleted ? (
                    <Button
                      size="sm"
                      variant="default"
                      onClick={() => handleRestore(product.id, product.name)}
                    >
                      Khôi phục
                    </Button>
                  ) : (
                    <>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleEditClick(product)}
                      >
                        Sửa
                      </Button>
                      <Button
                        size="sm"
                        variant="destructive"
                        onClick={() => handleDelete(product.id, product.name)}
                      >
                        Ngưng bán
                      </Button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          )
        ))}
        </tbody>
      </table>
      </div>

      <ConfirmDialog
        open={confirmDialog.open}
        title={confirmDialog.title}
        description={confirmDialog.description}
        onConfirm={confirmDialog.onConfirm}
        onClose={() => setConfirmDialog({ ...confirmDialog, open: false })}
        intent="danger"
        confirmLabel="Xác nhận"
        cancelLabel="Hủy"
      />
    </div>
  );
}
