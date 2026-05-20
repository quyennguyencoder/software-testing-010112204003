const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// ==================== TYPES ====================

export interface ProductCardResponse {
  id: number;
  name: string;
  thumbnailUrl?: string;
  brandName: string;
  brandId: number;
  categoryName: string;
  categoryId: number;
  
  // Price info
  originalPrice: number;
  minPrice: number;
  maxPrice: number;
  priceRange: string;
  discountedPrice?: number;
  hasDiscount: boolean;
  discountPercentage?: number;
  savingAmount?: number;
  
  // Rating & Reviews
  averageRating: number;
  totalReviews: number;
  ratingDisplay: string;
  
  // Stock info
  inStock: boolean;
  stockQuantity: number;
  stockStatus: string;
  soldCount?: number; // Số lượng đã bán
  
  // Key specs
  ram?: string;
  storage?: string;
  color?: string;
  screenSize?: string;
  operatingSystem?: string;
  processor?: string;
  
  // Extended specs (from ProductMetadata)
  batteryCapacity?: number;
  chargingPower?: number;
  screenResolution?: string;
  screenTechnology?: string;
  refreshRate?: number;
}

export interface ProductSearchRequest {
  keyword?: string;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

export interface ProductFilterRequest {
  categoryIds?: number[];
  brandIds?: number[];
  minPrice?: number;
  maxPrice?: number;
  ramOptions?: string[];
  storageOptions?: string[];
  minBattery?: number;
  maxBattery?: number;
  screenSizeOptions?: string[];
  osOptions?: string[];
  minRating?: number;
  maxRating?: number;
  inStockOnly?: boolean;
  hasDiscountOnly?: boolean;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface ProductComparisonResponse {
  products: ProductCardResponse[];
  comparisonTable: {
    [key: string]: string[];
  };
}

// ==================== API FUNCTIONS ====================

/**
 * Search products by keyword
 * GET /api/v1/products/search
 */
export async function searchProducts(request: ProductSearchRequest = {}): Promise<PageResponse<ProductCardResponse>> {
  try {
    const params = new URLSearchParams();
    if (request.keyword) params.append('keyword', request.keyword);
    if (request.sortBy) params.append('sortBy', request.sortBy);
    if (request.sortDirection) params.append('sortDirection', request.sortDirection);
    if (request.page !== undefined) params.append('page', request.page.toString());
    if (request.size !== undefined) params.append('size', request.size.toString());
    
    const url = `${API_BASE_URL}/products/search?${params}`;
    console.log('🔍 Searching products:', url);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to search products: ${response.statusText}`);
    }
    
    const result: ApiResponse<PageResponse<ProductCardResponse>> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Search products error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 20,
      number: request.page || 0,
    };
  }
}

/**
 * Filter products with multiple criteria
 * POST /api/v1/products/filter
 */
export async function filterProducts(request: ProductFilterRequest): Promise<PageResponse<ProductCardResponse>> {
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
    
    const result: ApiResponse<PageResponse<ProductCardResponse>> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Filter products error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 20,
      number: request.page || 0,
    };
  }
}

/**
 * Get featured products
 * GET /api/v1/products/featured
 */
export async function getFeaturedProducts(limit?: number): Promise<ProductCardResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/featured?${params}`;
    console.log('⭐ Getting featured products');
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get featured products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductCardResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get featured products error:', error);
    return [];
  }
}

/**
 * Get best selling products
 * GET /api/v1/products/best-selling
 */
export async function getBestSellingProducts(limit?: number): Promise<ProductCardResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/best-selling?${params}`;
    console.log('🔥 Getting best selling products');
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get best selling products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductCardResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get best selling products error:', error);
    return [];
  }
}

/**
 * Get new arrivals
 * GET /api/v1/products/new-arrivals
 */
export async function getNewArrivals(limit?: number): Promise<ProductCardResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/new-arrivals?${params}`;
    console.log('🆕 Getting new arrivals');
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get new arrivals: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductCardResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get new arrivals error:', error);
    return [];
  }
}

/**
 * Get products on sale
 * GET /api/v1/products/on-sale
 */
export async function getOnSaleProducts(limit?: number): Promise<ProductCardResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/on-sale?${params}`;
    console.log('🏷️ Getting on-sale products');
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get on-sale products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductCardResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get on-sale products error:', error);
    return [];
  }
}

/**
 * Get products by category
 * GET /api/v1/products/category/{categoryId}
 */
export async function getProductsByCategory(
  categoryId: number, 
  request: ProductFilterRequest = {}
): Promise<{ products: PageResponse<ProductCardResponse>; categoryInfo: any }> {
  try {
    const params = new URLSearchParams();
    if (request.minPrice !== undefined) params.append('minPrice', request.minPrice.toString());
    if (request.maxPrice !== undefined) params.append('maxPrice', request.maxPrice.toString());
    if (request.sortBy) params.append('sortBy', request.sortBy);
    if (request.sortDirection) params.append('sortDirection', request.sortDirection);
    if (request.page !== undefined) params.append('page', request.page.toString());
    if (request.size !== undefined) params.append('size', request.size.toString());
    
    const url = `${API_BASE_URL}/products/category/${categoryId}?${params}`;
    console.log(`📂 Getting products by category ${categoryId}`);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get products by category: ${response.statusText}`);
    }
    
    const result: ApiResponse<any> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get products by category error:', error);
    return {
      products: {
        content: [],
        totalPages: 0,
        totalElements: 0,
        size: request.size || 20,
        number: request.page || 0,
      },
      categoryInfo: null,
    };
  }
}

/**
 * Get product details
 * GET /api/v1/products/{id}
 */
export async function getProductDetails(id: number): Promise<any> {
  try {
    const url = `${API_BASE_URL}/products/${id}`;
    console.log(`📱 Getting product details for ID ${id}`);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get product details: ${response.statusText}`);
    }
    
    const result: ApiResponse<any> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get product details error:', error);
    return null;
  }
}

/**
 * Get related products
 * GET /api/v1/products/{id}/related
 */
export async function getRelatedProducts(id: number, limit?: number): Promise<ProductCardResponse[]> {
  try {
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit.toString());
    
    const url = `${API_BASE_URL}/products/${id}/related?${params}`;
    console.log(`🔗 Getting related products for ID ${id}`);
    
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get related products: ${response.statusText}`);
    }
    
    const result: ApiResponse<ProductCardResponse[]> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Get related products error:', error);
    return [];
  }
}

/**
 * Compare products
 * POST /api/v1/products/compare
 */
export async function compareProducts(productIds: number[]): Promise<ProductComparisonResponse | null> {
  try {
    const url = `${API_BASE_URL}/products/compare`;
    console.log('⚖️ Comparing products:', productIds);
    
    const response = await fetch(url, {
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
    return null;
  }
}

// ==================== PAGINATED API FUNCTIONS ====================

export interface PaginatedRequest {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

/**
 * Get featured products with pagination
 * Sử dụng filter API với sortBy=soldCount để lấy sản phẩm nổi bật
 * Cho phép user override sort options
 */
export async function getFeaturedProductsPaginated(request: PaginatedRequest = {}): Promise<PageResponse<ProductCardResponse>> {
  try {
    const filterRequest: ProductFilterRequest = {
      page: request.page || 0,
      size: request.size || 12,
      // Cho phép override sort, mặc định là soldCount
      sortBy: request.sortBy || 'soldCount',
      sortDirection: request.sortDirection || 'desc',
    };
    
    console.log('⭐ Getting featured products (paginated):', filterRequest);
    return await filterProducts(filterRequest);
  } catch (error) {
    console.error('Get featured products paginated error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 12,
      number: request.page || 0,
    };
  }
}

/**
 * Get new arrivals with pagination
 * Sử dụng filter API với sortBy=createdAt để lấy sản phẩm mới nhất
 * Cho phép user override sort options
 */
export async function getNewArrivalsPaginated(request: PaginatedRequest = {}): Promise<PageResponse<ProductCardResponse>> {
  try {
    const filterRequest: ProductFilterRequest = {
      page: request.page || 0,
      size: request.size || 12,
      // Cho phép override sort, mặc định là createdAt
      sortBy: request.sortBy || 'createdAt',
      sortDirection: request.sortDirection || 'desc',
    };
    
    console.log('🆕 Getting new arrivals (paginated):', filterRequest);
    return await filterProducts(filterRequest);
  } catch (error) {
    console.error('Get new arrivals paginated error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 12,
      number: request.page || 0,
    };
  }
}

/**
 * Get on-sale products with pagination
 * Sử dụng filter API với hasDiscountOnly=true để lấy sản phẩm đang giảm giá
 * Cho phép user override sort options
 */
export async function getOnSaleProductsPaginated(request: PaginatedRequest = {}): Promise<PageResponse<ProductCardResponse>> {
  try {
    const filterRequest: ProductFilterRequest = {
      page: request.page || 0,
      size: request.size || 12,
      hasDiscountOnly: true,
      // Cho phép override sort, mặc định là discountPercentage
      sortBy: request.sortBy || 'discountPercentage',
      sortDirection: request.sortDirection || 'desc',
    };
    
    console.log('🏷️ Getting on-sale products (paginated):', filterRequest);
    return await filterProducts(filterRequest);
  } catch (error) {
    console.error('Get on-sale products paginated error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 12,
      number: request.page || 0,
    };
  }
}

/**
 * Get best selling products with pagination
 * Cho phép user override sort options
 */
export async function getBestSellingProductsPaginated(request: PaginatedRequest = {}): Promise<PageResponse<ProductCardResponse>> {
  try {
    const filterRequest: ProductFilterRequest = {
      page: request.page || 0,
      size: request.size || 12,
      // Cho phép override sort, mặc định là soldCount
      sortBy: request.sortBy || 'soldCount',
      sortDirection: request.sortDirection || 'desc',
    };
    
    console.log('🔥 Getting best selling products (paginated):', filterRequest);
    return await filterProducts(filterRequest);
  } catch (error) {
    console.error('Get best selling products paginated error:', error);
    return {
      content: [],
      totalPages: 0,
      totalElements: 0,
      size: request.size || 12,
      number: request.page || 0,
    };
  }
}