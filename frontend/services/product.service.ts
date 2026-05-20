import { getAuthToken } from '@/lib/api';
import type { Product } from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

interface GetProductsParams {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number;
  brandId?: number;
  minPrice?: number;
  maxPrice?: number;
  status?: boolean;
  includeDeleted?: boolean;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

interface ProductListResponse {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
  thumbnailUrl?: string;
  status: boolean;
  categoryName?: string;
  brandName?: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/**
 * Product API Service
 * All admin product management API calls
 */
class ProductService {
  private getAuthHeaders() {
    const token = getAuthToken();
    if (process.env.NODE_ENV === 'development') {
      console.log('🔐 Token exists:', !!token);
    }
    return {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
  }

  /**
   * Get paginated list of products with filters
   * GET /api/v1/admin/products
   */
  async getProducts(params: GetProductsParams = {}): Promise<PageResponse<ProductListResponse>> {
    try {
      const queryParams = new URLSearchParams();
      
      if (params.page !== undefined) queryParams.append('page', params.page.toString());
      if (params.size !== undefined) queryParams.append('size', params.size.toString());
      if (params.keyword) queryParams.append('keyword', params.keyword);
      if (params.categoryId) queryParams.append('categoryId', params.categoryId.toString());
      if (params.brandId) queryParams.append('brandId', params.brandId.toString());
      if (params.minPrice !== undefined) queryParams.append('minPrice', params.minPrice.toString());
      if (params.maxPrice !== undefined) queryParams.append('maxPrice', params.maxPrice.toString());
      if (params.status !== undefined) queryParams.append('status', params.status.toString());
      if (params.includeDeleted !== undefined) queryParams.append('includeDeleted', params.includeDeleted.toString());
      if (params.sortBy) queryParams.append('sortBy', params.sortBy);
      if (params.sortDirection) queryParams.append('sortDirection', params.sortDirection);

      const url = `${API_BASE_URL}/admin/products?${queryParams}`;
      console.log('Fetching products from:', url);

      const response = await fetch(url, {
        method: 'GET',
        headers: this.getAuthHeaders(),
        credentials: 'include', // Include cookies for authentication
      });

      console.log('Response status:', response.status);

      // If 401, return empty data for now (no authentication yet)
      if (response.status === 401) {
        console.warn('Not authenticated - returning empty data');
        return {
          content: [],
          totalPages: 0,
          totalElements: 0,
          size: params.size || 20,
          number: params.page || 0,
        };
      }

      if (!response.ok) {
        const errorText = await response.text();
        console.error('API Error:', errorText);
        throw new Error(`Failed to fetch products: ${response.statusText}`);
      }

      const result: ApiResponse<PageResponse<ProductListResponse>> = await response.json();
      return result.data;
    } catch (error) {
      console.error('Fetch products error:', error);
      // Return empty data instead of throwing
      return {
        content: [],
        totalPages: 0,
        totalElements: 0,
        size: params.size || 20,
        number: params.page || 0,
      };
    }
  }

  /**
   * Get product by ID
   * GET /api/v1/admin/products/{id}
   */
  async getProductById(id: number): Promise<Product> {
    const response = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'GET',
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch product: ${response.statusText}`);
    }

    const result: ApiResponse<Product> = await response.json();
    return result.data;
  }

  /**
   * Create new product
   * POST /api/v1/admin/products
   */
  async createProduct(data: Partial<Product>): Promise<Product> {
    const response = await fetch(`${API_BASE_URL}/admin/products`, {
      method: 'POST',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create product');
    }

    const result: ApiResponse<Product> = await response.json();
    return result.data;
  }

  /**
   * Update product
   * PUT /api/v1/admin/products/{id}
   */
  async updateProduct(id: number, data: Partial<Product>): Promise<Product> {
    const response = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update product');
    }

    const result: ApiResponse<Product> = await response.json();
    return result.data;
  }

  /**
   * Delete product (soft delete)
   * DELETE /api/v1/admin/products/{id}
   */
  async deleteProduct(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/admin/products/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to delete product');
    }
  }

  /**
   * Restore deleted product
   * POST /api/v1/admin/products/{id}/restore
   */
  async restoreProduct(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/admin/products/${id}/restore`, {
      method: 'POST',
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to restore product');
    }
  }

  /**
   * Get all products (admin) - with API-side filtering
   */
  async getAllProductsAdmin(filters?: {
    keyword?: string;
    categoryId?: number;
    brandId?: number;
    status?: boolean;
    sortBy?: string;
    sortDirection?: 'asc' | 'desc';
    includeDeleted?: boolean;
    page?: number;
    size?: number;
  }): Promise<{ success: boolean; data: ProductListResponse[] }> {
    try {
      const params: GetProductsParams = {
        page: filters?.page || 0,
        size: filters?.size || 100,
        keyword: filters?.keyword,
        categoryId: filters?.categoryId,
        brandId: filters?.brandId,
        sortBy: filters?.sortBy || 'createdAt',
        sortDirection: filters?.sortDirection || 'desc',
        includeDeleted: filters?.includeDeleted,
      };
      
      console.log('🔧 getAllProductsAdmin params:', params);
      console.log('🔧 Input filters:', filters);
      const result = await this.getProducts(params);
      console.log('✅ Result from getProducts:', result.content?.length, 'items');
      return {
        success: true,
        data: result.content,
      };
    } catch (error) {
      console.error('getAllProductsAdmin error:', error);
      return {
        success: false,
        data: [],
      };
    }
  }

  /**
   * Get deleted products only
   * GET /api/v1/admin/products/deleted
   */
  async getDeletedProducts(filters?: {
    keyword?: string;
    categoryId?: number;
    brandId?: number;
    sortBy?: string;
    sortDirection?: 'asc' | 'desc';
  }): Promise<{ success: boolean; data: ProductListResponse[] }> {
    try {
      const queryParams = new URLSearchParams();
      queryParams.append('size', '1000');
      queryParams.append('sortBy', filters?.sortBy || 'deletedAt');
      queryParams.append('sortDirection', filters?.sortDirection || 'desc');
      
      if (filters?.keyword) queryParams.append('keyword', filters.keyword);
      if (filters?.categoryId) queryParams.append('categoryId', filters.categoryId.toString());
      if (filters?.brandId) queryParams.append('brandId', filters.brandId.toString());

      const url = `${API_BASE_URL}/admin/products/deleted?${queryParams}`;
      console.log('🗑️ Fetching deleted products from:', url);

      const response = await fetch(url, {
        method: 'GET',
        headers: this.getAuthHeaders(),
        credentials: 'include',
      });

      if (response.status === 401) {
        console.warn('Not authenticated - returning empty data');
        return {
          success: false,
          data: [],
        };
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      console.log('✅ Deleted products response:', result);
      
      return {
        success: true,
        data: result.data?.content || [],
      };
    } catch (error) {
      console.error('getDeletedProducts error:', error);
      return {
        success: false,
        data: [],
      };
    }
  }
}

export const productService = new ProductService();

// Export standalone functions for backward compatibility
export const getAllProductsAdmin = (filters?: {
  keyword?: string;
  categoryId?: number;
  brandId?: number;
  status?: boolean;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  includeDeleted?: boolean;
}) => productService.getAllProductsAdmin(filters);

export const getDeletedProducts = (filters?: {
  keyword?: string;
  categoryId?: number;
  brandId?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}) => productService.getDeletedProducts(filters);

export const deleteProduct = (id: number) => productService.deleteProduct(id);

export const restoreProduct = (id: number) => productService.restoreProduct(id);
