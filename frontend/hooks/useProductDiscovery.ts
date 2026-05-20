'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  searchProducts,
  filterProducts,
  getFeaturedProducts,
  getBestSellingProducts,
  getNewArrivals,
  getOnSaleProducts,
} from '@/services/product-view.service';
import type {
  ProductViewResponse,
  ProductSearchFilterRequest,
  ProductFilterRequest,
  PageResponse,
} from '@/types/product-view';

interface UseProductDiscoveryOptions {
  autoLoad?: boolean;
}

interface UseProductDiscoveryReturn {
  // Discovery data
  featuredProducts: ProductViewResponse[];
  bestSellingProducts: ProductViewResponse[];
  newArrivals: ProductViewResponse[];
  onSaleProducts: ProductViewResponse[];
  discoveryLoading: boolean;
  discoveryError: string | null;

  // Search results
  searchResults: PageResponse<ProductViewResponse> | null;
  searchLoading: boolean;
  searchError: string | null;

  // Filter results
  filterResults: PageResponse<ProductViewResponse> | null;
  filterLoading: boolean;
  filterError: string | null;

  // Actions
  performSearch: (searchRequest: ProductSearchFilterRequest) => Promise<void>;
  performFilter: (filterRequest: ProductFilterRequest) => Promise<void>;
  loadDiscoveryProducts: () => Promise<void>;
  clearResults: () => void;
}

/**
 * Hook for managing product discovery, search, and filtering
 * Uses correct backend APIs:
 * - GET /products/search for keyword search
 * - POST /products/filter for advanced filtering
 * - GET /products/featured, /products/best-selling, etc. for discovery
 */
export function useProductDiscovery(options: UseProductDiscoveryOptions = {}): UseProductDiscoveryReturn {
  const { autoLoad = true } = options;

  // Discovery products state
  const [featuredProducts, setFeaturedProducts] = useState<ProductViewResponse[]>([]);
  const [bestSellingProducts, setBestSellingProducts] = useState<ProductViewResponse[]>([]);
  const [newArrivals, setNewArrivals] = useState<ProductViewResponse[]>([]);
  const [onSaleProducts, setOnSaleProducts] = useState<ProductViewResponse[]>([]);
  const [discoveryLoading, setDiscoveryLoading] = useState(false);
  const [discoveryError, setDiscoveryError] = useState<string | null>(null);

  // Search state
  const [searchResults, setSearchResults] = useState<PageResponse<ProductViewResponse> | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  // Filter state
  const [filterResults, setFilterResults] = useState<PageResponse<ProductViewResponse> | null>(null);
  const [filterLoading, setFilterLoading] = useState(false);
  const [filterError, setFilterError] = useState<string | null>(null);

  /**
   * Load discovery products (featured, best-selling, new arrivals, on-sale)
   */
  const loadDiscoveryProducts = useCallback(async () => {
    setDiscoveryLoading(true);
    setDiscoveryError(null);

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
      const errorMessage = error instanceof Error ? error.message : 'Failed to load discovery products';
      setDiscoveryError(errorMessage);
      console.error('Discovery products error:', error);
    } finally {
      setDiscoveryLoading(false);
    }
  }, []);

  /**
   * Perform keyword search using GET /products/search
   */
  const performSearch = useCallback(async (searchRequest: ProductSearchFilterRequest) => {
    setSearchLoading(true);
    setSearchError(null);
    setFilterResults(null); // Clear filter results when searching

    try {
      console.log('🔍 Performing search:', searchRequest);
      const results = await searchProducts(searchRequest);
      setSearchResults(results);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Search failed';
      setSearchError(errorMessage);
      console.error('Search error:', error);
    } finally {
      setSearchLoading(false);
    }
  }, []);

  /**
   * Perform advanced filtering using POST /products/filter
   */
  const performFilter = useCallback(async (filterRequest: ProductFilterRequest) => {
    setFilterLoading(true);
    setFilterError(null);
    setSearchResults(null); // Clear search results when filtering

    try {
      console.log('🔎 Performing filter:', filterRequest);
      const results = await filterProducts(filterRequest);
      setFilterResults(results);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Filter failed';
      setFilterError(errorMessage);
      console.error('Filter error:', error);
    } finally {
      setFilterLoading(false);
    }
  }, []);

  /**
   * Clear all results
   */
  const clearResults = useCallback(() => {
    setSearchResults(null);
    setFilterResults(null);
    setSearchError(null);
    setFilterError(null);
  }, []);

  // Auto-load discovery products on mount
  useEffect(() => {
    if (autoLoad) {
      loadDiscoveryProducts();
    }
  }, [autoLoad, loadDiscoveryProducts]);

  return {
    // Discovery data
    featuredProducts,
    bestSellingProducts,
    newArrivals,
    onSaleProducts,
    discoveryLoading,
    discoveryError,

    // Search results
    searchResults,
    searchLoading,
    searchError,

    // Filter results
    filterResults,
    filterLoading,
    filterError,

    // Actions
    performSearch,
    performFilter,
    loadDiscoveryProducts,
    clearResults,
  };
}