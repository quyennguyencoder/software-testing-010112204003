'use client';

import useSWR from 'swr';
import {
  searchProducts,
  getBestSellingProducts,
  getFeaturedProducts,
  getNewArrivals,
  getOnSaleProducts,
  getOnSaleProductsPaginated,
  getProductsByCategory,
  getRelatedProducts,
  type ProductCardResponse,
  type PageResponse,
  type ProductSearchRequest,
  type ProductFilterRequest
} from '@/services/new-product.service';

interface UseProductsOptions {
  limit?: number;
  categoryId?: number;
  brandId?: number;
  search?: string;
  enabled?: boolean;
}

interface UseProductsReturn {
  data: ProductCardResponse[] | undefined;
  isLoading: boolean;
  error: any;
  mutate: () => void;
}

/**
 * Hook for fetching products with search/filter capabilities
 * Uses GET /api/v1/products/search for keyword search only
 * Uses GET /api/v1/products/category/{id} for category filtering
 */
export function useProducts(options: UseProductsOptions = {}): UseProductsReturn {
  const { limit = 20, categoryId, search, enabled = true } = options;

  // Determine which API to use based on parameters
  const shouldUseCategory = categoryId !== undefined && !search;

  const searchRequest: ProductSearchRequest = {
    keyword: search,
    size: limit,
    page: 0,
    sortBy: 'created_date',
    sortDirection: 'desc',
  };

  const { data, error, isLoading, mutate } = useSWR(
    enabled ? ['products', categoryId, search, limit] : null,
    async () => {
      if (shouldUseCategory && categoryId) {
        // Use category API
        const response: { products: PageResponse<ProductCardResponse>; categoryInfo: any } = await getProductsByCategory(categoryId, searchRequest);
        return response?.products?.content || [];
      } else {
        // Use search API
        const response: PageResponse<ProductCardResponse> = await searchProducts(searchRequest);
        return response.content;
      }
    },
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching best-selling products
 */
export function useBestSellingProducts(options: { limit?: number } = {}): UseProductsReturn {
  const { limit = 8 } = options;

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'best-selling', limit],
    () => getBestSellingProducts(limit),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching featured products
 */
export function useFeaturedProducts(options: { limit?: number } = {}): UseProductsReturn {
  const { limit = 8 } = options;

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'featured', limit],
    () => getFeaturedProducts(limit),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching new arrival products
 */
export function useNewArrivals(options: { limit?: number } = {}): UseProductsReturn {
  const { limit = 8 } = options;

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'new-arrivals', limit],
    () => getNewArrivals(limit),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}


/**
 * Hook for fetching products on sale (Paginated/Filtered version)
 * Uses POST /api/v1/products/filter with hasDiscountOnly=true
 * This is more reliable than the GET /api/v1/products/on-sale endpoint
 */
export function useProductsOnSalePaginated(options: { limit?: number } = {}): UseProductsReturn {
  const { limit = 8 } = options;

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'on-sale-paginated', limit],
    () => getOnSaleProductsPaginated({ size: limit }).then(res => res.content),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching products on sale
 * Uses GET /api/v1/products/on-sale endpoint
 */
export function useProductsOnSale(options: { limit?: number } = {}): UseProductsReturn {
  const { limit = 8 } = options;

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'on-sale', limit],
    () => getOnSaleProducts(limit),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching products by category
 */
export function useProductsByCategory(
  categoryId: number,
  options: { limit?: number } = {}
): UseProductsReturn {
  const { limit = 20 } = options;

  const request: ProductFilterRequest = {
    page: 0,
    size: limit,
    sortBy: 'created_date',
    sortDirection: 'desc',
  };

  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'category', categoryId, request],
    () => getProductsByCategory(categoryId, request).then((response: { products: PageResponse<ProductCardResponse>; categoryInfo: any }) => response?.products.content || []),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

/**
 * Hook for fetching related products
 */
export function useRelatedProducts(productId: number, limit: number = 4): UseProductsReturn {
  const { data, error, isLoading, mutate } = useSWR(
    ['products', 'related', productId, limit],
    () => getRelatedProducts(productId, limit),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    data,
    isLoading,
    error,
    mutate,
  };
}

// Export all product hooks under a namespace for easier access
export const ProductHooks = {
  useProducts,
  useBestSellingProducts,
  useFeaturedProducts,
  useNewArrivals,
  useProductsOnSale,
  useProductsByCategory,
  useRelatedProducts,
};

