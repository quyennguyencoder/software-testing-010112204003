/**
 * useBrands hook - Fetch and manage brands data
 * Following FRONTEND_DESIGN_SYSTEM.md:
 * - Proper loading/error states
 * - Type-safe API integration
 * - Reusable hook pattern
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import { adminAPI } from '@/lib/api';
import type { BrandResponse } from '@/types';

interface UseBrandsReturn {
  brands: BrandResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useBrands(): UseBrandsReturn {
  const [brands, setBrands] = useState<BrandResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchBrands = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await adminAPI.getAllBrands();

      if (response.success && response.data) {
        setBrands(response.data);
      } else {
        throw new Error(response.message || 'Lỗi khi tải danh sách thương hiệu');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Đã xảy ra lỗi khi tải dữ liệu';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBrands();
  }, [fetchBrands]);

  return {
    brands,
    loading,
    error,
    refetch: fetchBrands,
  };
}

