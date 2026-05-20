/**
 * Brand types for UTE Phone Hub
 * Matching backend DTOs: BrandResponse, CreateBrandRequest, UpdateBrandRequest
 */

export interface BrandResponse {
  id: number;
  name: string;
  description: string | null;
  logoUrl: string | null;
  productCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBrandRequest {
  name: string;
  description?: string;
  logoUrl?: string;
}

export interface UpdateBrandRequest {
  name: string;
  description?: string;
  logoUrl?: string;
}

// Alias for simpler imports
export type Brand = BrandResponse;

