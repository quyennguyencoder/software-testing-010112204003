/**
 * Product-related TypeScript definitions
 */

// Product Template (Variants)
export interface ProductTemplate {
  id?: number;
  sku: string;
  color?: string;
  storage?: string;
  ram?: string;
  price: number;
  stockQuantity: number;
  status: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Product Metadata - Dynamic fields based on category
export interface ProductMetadata {
  id?: number;
  [key: string]: any; // Allow dynamic fields for different product categories
}

// Product Image
export interface ProductImage {
  id?: number;
  imageUrl: string;
  altText?: string;
  imageOrder: number;
  isPrimary: boolean;
}

// Main Product interface
export interface Product {
  id: number;
  name: string;
  description?: string;
  thumbnailUrl?: string;
  categoryId: number;
  brandId: number;
  status?: boolean;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
  templates?: ProductTemplate[];
  metadata?: ProductMetadata;
  images?: ProductImage[];
  
  // Optional computed fields (from API)
  price?: number;
  stockQuantity?: number;
  categoryName?: string;
  brandName?: string;
  minPrice?: number;
  maxPrice?: number;
  totalStock?: number;
  rating?: number;
  reviews?: number;
}

// Request DTOs for API calls
export interface CreateProductRequest {
  name: string;
  description?: string;
  thumbnailUrl?: string;
  categoryId: number;
  brandId: number;
  status?: boolean;
  templates: Omit<ProductTemplate, 'id' | 'createdAt' | 'updatedAt'>[];
  metadata?: Omit<ProductMetadata, 'id'>;
  images?: Omit<ProductImage, 'id'>[];
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  categoryId?: number;
  brandId?: number;
}

// Response DTO
export type ProductResponse = Product;
