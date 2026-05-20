'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  searchProducts,
  filterProducts,
  getFeaturedProducts,
  getBestSellingProducts,
  getNewArrivals,
  getOnSaleProducts,
  compareProducts,
  type ProductCardResponse,
  type ProductSearchRequest,
  type ProductFilterRequest,
  type PageResponse,
  type ProductComparisonResponse,
} from '@/services/new-product.service';

interface UseProductDiscoveryReturn {
  // Discovery products (loaded on mount)
  featuredProducts: ProductCardResponse[];
  bestSellingProducts: ProductCardResponse[];
  newArrivals: ProductCardResponse[];
  onSaleProducts: ProductCardResponse[];
  discoveryLoading: boolean;
  
  // Search results (from GET /products/search)
  searchResults: PageResponse<ProductCardResponse> | null;
  searchLoading: boolean;
  searchError: string | null;
  
  // Filter results (from POST /products/filter)
  filterResults: PageResponse<ProductCardResponse> | null;
  filterLoading: boolean;
  filterError: string | null;
  
  // Comparison results (from POST /products/compare)
  comparisonResults: ProductComparisonResponse | null;
  comparisonLoading: boolean;
  comparisonError: string | null;
  
  // Actions
  performSearch: (request: ProductSearchRequest) => Promise<void>;
  performFilter: (request: ProductFilterRequest) => Promise<void>;
  performComparison: (productIds: number[]) => Promise<void>;
  clearResults: () => void;
}

/**
 * Hook for product discovery with proper API integration
 */
export function useProductDiscovery(): UseProductDiscoveryReturn {
  // Discovery products state
  const [featuredProducts, setFeaturedProducts] = useState<ProductCardResponse[]>([]);
  const [bestSellingProducts, setBestSellingProducts] = useState<ProductCardResponse[]>([]);
  const [newArrivals, setNewArrivals] = useState<ProductCardResponse[]>([]);
  const [onSaleProducts, setOnSaleProducts] = useState<ProductCardResponse[]>([]);
  const [discoveryLoading, setDiscoveryLoading] = useState(false);
  
  // Search state
  const [searchResults, setSearchResults] = useState<PageResponse<ProductCardResponse> | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  
  // Filter state
  const [filterResults, setFilterResults] = useState<PageResponse<ProductCardResponse> | null>(null);
  const [filterLoading, setFilterLoading] = useState(false);
  const [filterError, setFilterError] = useState<string | null>(null);
  
  // Comparison state
  const [comparisonResults, setComparisonResults] = useState<ProductComparisonResponse | null>(null);
  const [comparisonLoading, setComparisonLoading] = useState(false);
  const [comparisonError, setComparisonError] = useState<string | null>(null);

  // Load discovery products on mount
  useEffect(() => {
    const loadDiscoveryProducts = async () => {
      setDiscoveryLoading(true);
      try {
        const [featured, bestSelling, newArrivalsList, onSale] = await Promise.all([
          getFeaturedProducts(8),
          getBestSellingProducts(8), 
          getNewArrivals(8),
          getOnSaleProducts(8),
        ]);
        
        setFeaturedProducts(featured);
        setBestSellingProducts(bestSelling);
        setNewArrivals(newArrivalsList);
        setOnSaleProducts(onSale);
      } catch (error) {
        console.error('Failed to load discovery products:', error);
      } finally {
        setDiscoveryLoading(false);
      }
    };

    loadDiscoveryProducts();
  }, []);

  // Search function
  const performSearch = useCallback(async (request: ProductSearchRequest) => {
    setSearchLoading(true);
    setSearchError(null);
    setFilterResults(null); // Clear filter results when searching
    
    try {
      const results = await searchProducts(request);
      setSearchResults(results);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to search products';
      setSearchError(errorMessage);
      setSearchResults(null);
    } finally {
      setSearchLoading(false);
    }
  }, []);

  // Filter function
  const performFilter = useCallback(async (request: ProductFilterRequest) => {
    setFilterLoading(true);
    setFilterError(null);
    setSearchResults(null); // Clear search results when filtering
    
    try {
      const results = await filterProducts(request);
      setFilterResults(results);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to filter products';
      setFilterError(errorMessage);
      setFilterResults(null);
    } finally {
      setFilterLoading(false);
    }
  }, []);

  // Comparison function
  const performComparison = useCallback(async (productIds: number[]) => {
    if (productIds.length < 2) {
      setComparisonError('Cần ít nhất 2 sản phẩm để so sánh');
      return;
    }
    
    if (productIds.length > 4) {
      setComparisonError('Chỉ có thể so sánh tối đa 4 sản phẩm');
      return;
    }
    
    setComparisonLoading(true);
    setComparisonError(null);
    
    try {
      const results = await compareProducts(productIds);
      setComparisonResults(results);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to compare products';
      setComparisonError(errorMessage);
      setComparisonResults(null);
    } finally {
      setComparisonLoading(false);
    }
  }, []);

  // Clear all results
  const clearResults = useCallback(() => {
    setSearchResults(null);
    setSearchError(null);
    setFilterResults(null);
    setFilterError(null);
    setComparisonResults(null);
    setComparisonError(null);
  }, []);

  return {
    // Discovery products
    featuredProducts,
    bestSellingProducts,
    newArrivals,
    onSaleProducts,
    discoveryLoading,
    
    // Search
    searchResults,
    searchLoading,
    searchError,
    
    // Filter
    filterResults,
    filterLoading,
    filterError,
    
    // Comparison
    comparisonResults,
    comparisonLoading,
    comparisonError,
    
    // Actions
    performSearch,
    performFilter,
    performComparison,
    clearResults,
  };
}

// Additional hook for categories if needed
interface UseCategoryProductsReturn {
  products: PageResponse<ProductCardResponse> | null;
  categoryInfo: any;
  loading: boolean;
  error: string | null;
  loadProducts: (categoryId: number, request?: ProductFilterRequest) => Promise<void>;
}

export function useCategoryProducts(): UseCategoryProductsReturn {
  const [products, setProducts] = useState<PageResponse<ProductCardResponse> | null>(null);
  const [categoryInfo, setCategoryInfo] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadProducts = useCallback(async (categoryId: number, request: ProductFilterRequest = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const { getProductsByCategory } = await import('@/services/new-product.service');
      const results = await getProductsByCategory(categoryId, request);
      setProducts(results.products);
      setCategoryInfo(results.categoryInfo);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load category products';
      setError(errorMessage);
      setProducts(null);
      setCategoryInfo(null);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    products,
    categoryInfo,
    loading,
    error,
    loadProducts,
  };
}