/**
 * Product View Service
 * Handles all public product viewing API calls
 * Based on ProductViewController endpoints (/api/v1/products)
 */

import type {
  ProductViewResponse,
  ProductDetailViewResponse,
  ProductSearchFilterRequest,
  ProductFilterRequest,
  CategoryProductsResponse,
  ProductComparisonResponse,
  PageResponse,
} from '@/types/product-view';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/**
 * Build query params from filter request
 */
function buildQueryParams(request: ProductSearchFilterRequest): URLSearchParams {
  const params = new URLSearchParams();
  
  if (request.keyword) params.append('keyword', request.keyword);
  if (request.sortBy) params.append('sortBy', request.sortBy);
  if (request.sortDirection) params.append('sortDirection', request.sortDirection);
  if (request.page !== undefined) params.append('page', request.page.toString());
  if (request.size !== undefined) params.append('size', request.size.toString());
  
  return params;
}

/**
 * Search and filter products
 * GET /api/v1/products/search
 */
export async function searchProducts(request: ProductSearchFilterRequest = {}): Promise<PageResponse<ProductViewResponse>> {
  try {
    const params = buildQueryParams(request);
    const url = `${API_BASE_URL}/products/search?${params}`;
    
    console.log('🔍 Searching products:', url);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to search products: ${response.statusText}`);
    }
    
    const result: ApiResponse<PageResponse<ProductViewResponse>> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Search products error:', error);
    throw error;
  }
}

/**
 * Filter products with advanced criteria
 * POST /api/v1/products/filter
 */
export async function filterProducts(request: ProductFilterRequest): Promise<PageResponse<ProductViewResponse>> {
  try {
    const url = `${API_BASE_URL}/products/filter`;
    console.log('🎯 Filtering products:', request);
    
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    
    if (!response.ok) {
      throw new Error(`Failed to filter products: ${response.statusText}`);
    }
    
    const result: ApiResponse<PageResponse<ProductViewResponse>> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Filter products error:', error);
    throw error;
  }
}

/**
 * Get product detail by ID
 * GET /api/v1/products/{id}
 */
export async function getProductById(id: number): Promise<ProductDetailViewResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/products/${id}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get product: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductDetailViewResponse> = await response.json();
    console.log('📦 Raw API response from /products/{id}:', result.data);
    
    return result.data;
  } catch (error) {
    console.error('❌ Get product error:', error);
    throw error;
  }
}

/**
 * Get products by category
 * GET /api/v1/products/category/{categoryId}
 */
export async function getProductsByCategory(
  categoryId: number,
  request: ProductSearchFilterRequest = {}
): Promise<CategoryProductsResponse> {
  try {
    const params = new URLSearchParams();
    if (request.page !== undefined) params.append('page', request.page.toString());
    if (request.size !== undefined) params.append('size', request.size.toString());
    if (request.sortBy) params.append('sortBy', request.sortBy);
    if (request.sortDirection) params.append('sortDirection', request.sortDirection);
    
    const url = `${API_BASE_URL}/products/category/${categoryId}?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get category products: ${response.statusText}`);
    }
    
    const result: ApiResponse<CategoryProductsResponse> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get category products error:', error);
    throw error;
  }
}

/**
 * Get related products
 * GET /api/v1/products/{id}/related
 */
export async function getRelatedProducts(id: number, limit?: number): Promise<ProductViewResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/${id}/related?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get related products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductViewResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get related products error:', error);
    return [];
  }
}

/**
 * Get best selling products
 * GET /api/v1/products/best-selling
 */
export async function getBestSellingProducts(limit?: number): Promise<ProductViewResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/best-selling?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get best selling products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductViewResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get best selling products error:', error);
    return [];
  }
}

/**
 * Get new arrival products
 * GET /api/v1/products/new-arrivals
 */
export async function getNewArrivals(limit?: number): Promise<ProductViewResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/new-arrivals?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get new arrivals: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductViewResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get new arrivals error:', error);
    return [];
  }
}

/**
 * Get featured products
 * GET /api/v1/products/featured
 */
export async function getFeaturedProducts(limit: number = 10): Promise<ProductViewResponse[]> {
  try {
    const params = new URLSearchParams();
    params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/featured?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get featured products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductViewResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get featured products error:', error);
    return [];
  }
}

/**
 * Get products on sale
 * GET /api/v1/products/on-sale
 */
export async function getOnSaleProducts(limit?: number): Promise<ProductViewResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/on-sale?${params}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      cache: 'no-store',
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get on-sale products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductViewResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get on-sale products error:', error);
    return [];
  }
}

/**
 * Compare products
 * POST /api/v1/products/compare
 */
export async function compareProducts(productIds: number[]): Promise<ProductComparisonResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/products/compare`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(productIds),
    });
    
    if (!response.ok) {
      throw new Error(`Failed to compare products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductComparisonResponse> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Compare products error:', error);
    throw error;
  }
}

// Legacy service object for backward compatibility
export const productViewService = {
  searchProducts,
  getProductById,
  getProductsByCategory,
  getRelatedProducts,
  getBestSellingProducts,
  getNewArrivals,
  getFeaturedProducts,
  getOnSaleProducts,
  filterProducts,
  compareProducts,
};
