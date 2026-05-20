/**
 * useCategories hook - Fetch and manage categories data
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import { adminAPI } from '@/lib/api';
import type { CategoryResponse } from '@/types';

interface UseCategoriesOptions {
  parentId?: number | null;
  autoFetch?: boolean;
}

export function useCategories(options: UseCategoriesOptions = {}) {
  const { parentId, autoFetch = true } = options;
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async (fetchParentId?: number | null) => {
    try {
      setLoading(true);
      setError(null);

      const pid = fetchParentId !== undefined ? fetchParentId : parentId;
      const response = await adminAPI.getAllCategories(pid);

      if (response.success && response.data) {
        setCategories(response.data);
      } else {
        throw new Error(response.message || 'Lỗi khi tải danh mục');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Lỗi khi tải danh mục';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [parentId]);

  useEffect(() => {
    if (autoFetch) {
      fetchCategories();
    }
  }, [autoFetch, fetchCategories]);

  return {
    categories,
    loading,
    error,
    refetch: fetchCategories,
  };
}

