/**
 * Category types matching backend DTOs
 */

export interface Category {
  id: number;
  name: string;
  description?: string | null;
  parentId: number | null;
  parentName?: string | null;
  hasChildren?: boolean;
  childrenCount?: number;
  productCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parentId?: number | null;
}

export interface UpdateCategoryRequest {
  name: string;
  description?: string;
  parentId?: number | null;
}

export interface CategoryResponse {
  id: number;
  name: string;
  description?: string | null;
  parentId: number | null;
  parentName?: string | null;
  hasChildren?: boolean;
  childrenCount: number;
  productCount: number;
  children?: CategoryResponse[]; // Nested sub-categories
  createdAt: string;
  updatedAt: string;
}

