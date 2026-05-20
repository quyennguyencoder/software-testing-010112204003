'use client';

import { useState, useEffect } from 'react';
import { ProductTable } from './admin/ProductTable';
import { adminAPI } from '@/lib/api';
import type { Product } from '@/types';

// Type Definitions
interface Category {
  id: number;
  name: string;
  description?: string | null;
}

interface Brand {
  id: number;
  name: string;
}

export function ProductManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [filters, setFilters] = useState({});
  const [searchKeyword, setSearchKeyword] = useState('');
  const [sortBy, setSortBy] = useState('');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  const fetchProducts = async (): Promise<void> => {
    try {
      setLoading(true);
      
      const response = await adminAPI.getAllProducts({
        page: currentPage,
        size: 30,
        keyword: searchKeyword,
        sortBy,
        sortDirection,
        ...filters,
      });
      
      console.log('🔍 API Response:', { 
        totalElements: response.data?.totalElements, 
        contentLength: response.data?.content?.length,
        size: 30
      });
      
      if (response.success && response.data) {
        setProducts(response.data.content || []);
        setTotalPages(response.data.totalPages || 0);
        setTotalItems(response.data.totalElements || 0);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Không thể tải danh sách sản phẩm';
      console.error('Error fetching products:', errorMessage);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async (): Promise<void> => {
    try {
      const response = await adminAPI.getAllCategories();
      if (response.success && response.data) {
        setCategories(response.data);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Không thể tải danh mục';
      console.error('Error fetching categories:', errorMessage);
    }
  };

  const fetchBrands = async (): Promise<void> => {
    try {
      const response = await adminAPI.getAllBrands();
      if (response.success && response.data) {
        setBrands(response.data);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Không thể tải thương hiệu';
      console.error('Error fetching brands:', errorMessage);
    }
  };

  useEffect(() => {
    fetchCategories();
    fetchBrands();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, [currentPage, searchKeyword, sortBy, sortDirection, filters]);

  const handleDelete = async (id: number): Promise<void> => {
    try {
      const response = await adminAPI.deleteProduct(id);
      if (response.success) {
        await fetchProducts();
      } else {
        console.error('Delete failed:', response.message);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Không thể xóa sản phẩm';
      console.error('Error deleting product:', errorMessage);
    }
  };

  if (loading && products.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <ProductTable
      products={products}
      categories={categories}
      brands={brands}
      totalPages={totalPages}
      currentPage={currentPage}
      totalItems={totalItems}
      onPageChange={setCurrentPage}
      onSearch={setSearchKeyword}
      onSort={(sortBy, direction) => {
        setSortBy(sortBy);
        setSortDirection(direction);
      }}
      onFilter={setFilters}
      onDelete={handleDelete}
    />
  );
}
