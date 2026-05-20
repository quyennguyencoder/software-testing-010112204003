'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { searchProducts } from '@/services/product-view.service';
import * as productViewService from '@/services/product-view.service';
import type {
  ProductViewResponse,
  ProductSearchFilterRequest,
  PageResponse,
} from '@/types/product-view';

interface UseProductViewOptions {
  initialFilters?: ProductSearchFilterRequest;
  autoLoad?: boolean;
}

interface UseProductViewReturn {
  products: ProductViewResponse[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  totalElements: number;
  currentPage: number;
  filters: ProductSearchFilterRequest;
  
  // Actions
  searchProducts: (newFilters?: Partial<ProductSearchFilterRequest>) => Promise<void>;
  setPage: (page: number) => void;
  setSortBy: (sortBy: string, sortDirection?: 'asc' | 'desc') => void;
  setKeyword: (keyword: string) => void;
  setCategoryId: (categoryId: number | undefined) => void;
  setBrandIds: (brandIds: number[]) => void;
  setPriceRange: (minPrice?: number, maxPrice?: number) => void;
  setRating: (minRating?: number) => void;
  setInStockOnly: (inStockOnly: boolean) => void;
  setOnSaleOnly: (onSaleOnly: boolean) => void;
  setRamOptions: (ramOptions: string[]) => void;
  setStorageOptions: (storageOptions: string[]) => void;
  setOsOptions: (osOptions: string[]) => void;
  clearFilters: () => void;
  refetch: () => Promise<void>;
}

const DEFAULT_FILTERS: ProductSearchFilterRequest = {
  page: 0,
  size: 20,
  sortBy: 'created_date',
  sortDirection: 'desc',
};

/**
 * Hook to manage product listing with filters, sorting, and pagination
 */
export function useProductView(options: UseProductViewOptions = {}): UseProductViewReturn {
  const { initialFilters = DEFAULT_FILTERS, autoLoad = true } = options;
  
  const [products, setProducts] = useState<ProductViewResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [filters, setFilters] = useState<ProductSearchFilterRequest>(initialFilters);
  
  // Track if component has mounted and initial load is done
  const hasMounted = useRef(false);
  const hasInitialLoaded = useRef(false);

  /**
   * Main search function
   */
  const searchProducts = useCallback(async (newFilters?: Partial<ProductSearchFilterRequest>) => {
    try {
      setLoading(true);
      setError(null);
      
      // Use functional update to avoid stale closure
      let searchFilters: ProductSearchFilterRequest;
      
      if (newFilters) {
        searchFilters = { ...filters, ...newFilters };
        setFilters(searchFilters);
      } else {
        searchFilters = filters;
      }
      
      console.log('🔍 Searching with filters:', searchFilters);
      
      const response: PageResponse<ProductViewResponse> = await productViewService.searchProducts(searchFilters);
      
      console.log('📦 Search results:', {
        totalElements: response.totalElements,
        totalPages: response.totalPages,
        productsCount: response.content.length
      });
      
      setProducts(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to search products';
      setError(errorMessage);
      console.error('❌ Search error:', err);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  /**
   * Set page number
   */
  const setPage = useCallback((page: number) => {
    setFilters(prev => ({ ...prev, page }));
  }, []);

  /**
   * Set sort options
   */
  const setSortBy = useCallback((sortBy: string, sortDirection: 'asc' | 'desc' = 'desc') => {
    setFilters(prev => ({ 
      ...prev,
      sortBy: sortBy as ProductSearchFilterRequest['sortBy'], 
      sortDirection,
      page: 0 // Reset to first page when sorting
    }));
  }, []);

  /**
   * Set keyword
   */
  const setKeyword = useCallback((keyword: string) => {
    setFilters(prev => ({ ...prev, keyword, page: 0 }));
  }, []);

  /**
   * Set category filter
   */
  const setCategoryId = useCallback((categoryId: number | undefined) => {
    setFilters(prev => ({ ...prev, categoryId, page: 0 }));
  }, []);

  /**
   * Set brand filters
   */
  const setBrandIds = useCallback((brandIds: number[]) => {
    setFilters(prev => ({ ...prev, brandIds, page: 0 }));
  }, []);

  /**
   * Set price range
   */
  const setPriceRange = useCallback((minPrice?: number, maxPrice?: number) => {
    setFilters(prev => ({ ...prev, minPrice, maxPrice, page: 0 }));
  }, []);

  /**
   * Set rating filter
   */
  const setRating = useCallback((minRating?: number) => {
    setFilters(prev => ({ ...prev, minRating, page: 0 }));
  }, []);

  /**
   * Set in stock only filter
   */
  const setInStockOnly = useCallback((inStockOnly: boolean) => {
    setFilters(prev => ({ ...prev, inStockOnly, page: 0 }));
  }, []);

  /**
   * Set on sale only filter
   */
  const setOnSaleOnly = useCallback((onSaleOnly: boolean) => {
    setFilters(prev => ({ ...prev, onSaleOnly, page: 0 }));
  }, []);

  /**
   * Set RAM options
   */
  const setRamOptions = useCallback((ramOptions: string[]) => {
    setFilters(prev => ({ ...prev, ramOptions, page: 0 }));
  }, []);

  /**
   * Set storage options
   */
  const setStorageOptions = useCallback((storageOptions: string[]) => {
    setFilters(prev => ({ ...prev, storageOptions, page: 0 }));
  }, []);

  /**
   * Set OS options
   */
  const setOsOptions = useCallback((osOptions: string[]) => {
    setFilters(prev => ({ ...prev, osOptions, page: 0 }));
  }, []);

  /**
   * Clear all filters
   */
  const clearFilters = useCallback(() => {
    console.log('🧹 Clearing all filters');
    setFilters(DEFAULT_FILTERS);
  }, []);

  /**
   * Refetch with current filters
   */
  const refetch = useCallback(() => {
    return searchProducts();
  }, [searchProducts]);

  // Auto-load on mount if enabled
  useEffect(() => {
    if (autoLoad && !hasInitialLoaded.current) {
      console.log('🚀 Initial load with filters:', filters);
      hasInitialLoaded.current = true;
      searchProducts();
    }
    hasMounted.current = true;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run once on mount

  // Trigger search when filters change (after initial mount)
  useEffect(() => {
    // Skip if not mounted yet or if this is the initial load
    if (!hasMounted.current || !hasInitialLoaded.current) {
      return;
    }
    
    console.log('🔄 Filters changed, triggering search:', filters);
    searchProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  return {
    products,
    loading,
    error,
    totalPages,
    totalElements,
    currentPage: filters.page || 0,
    filters,
    
    // Actions
    searchProducts,
    setPage,
    setSortBy,
    setKeyword,
    setCategoryId,
    setBrandIds,
    setPriceRange,
    setRating,
    setInStockOnly,
    setOnSaleOnly,
    setRamOptions,
    setStorageOptions,
    setOsOptions,
    clearFilters,
    refetch,
  };
}

/**
 * Hook for getting featured products
 */
export function useFeaturedProductsView(limit: number = 10) {
  const [products, setProducts] = useState<ProductViewResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchFeatured = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productViewService.getFeaturedProducts(limit);
      setProducts(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch featured products';
      setError(errorMessage);
      console.error('Fetch featured error:', err);
    } finally {
      setLoading(false);
    }
  }, [limit]);

  useEffect(() => {
    fetchFeatured();
  }, [fetchFeatured]);

  return { products, loading, error, refetch: fetchFeatured };
}

/**
 * Hook for getting best selling products
 */
export function useBestSellingProductsView(limit: number = 10) {
  const [products, setProducts] = useState<ProductViewResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchBestSelling = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productViewService.getBestSellingProducts(limit);
      setProducts(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch best selling products';
      setError(errorMessage);
      console.error('Fetch best selling error:', err);
    } finally {
      setLoading(false);
    }
  }, [limit]);

  useEffect(() => {
    fetchBestSelling();
  }, [fetchBestSelling]);

  return { products, loading, error, refetch: fetchBestSelling };
}

/**
 * Hook for getting new arrival products
 */
export function useNewArrivalsView(limit: number = 10) {
  const [products, setProducts] = useState<ProductViewResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchNewArrivals = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productViewService.getNewArrivals(limit);
      setProducts(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch new arrivals';
      setError(errorMessage);
      console.error('Fetch new arrivals error:', err);
    } finally {
      setLoading(false);
    }
  }, [limit]);

  useEffect(() => {
    fetchNewArrivals();
  }, [fetchNewArrivals]);

  return { products, loading, error, refetch: fetchNewArrivals };
}
