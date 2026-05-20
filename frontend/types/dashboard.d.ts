/**
 * Dashboard Module Types
 * Module M10.2 - View Dashboard
 * 
 * Định nghĩa các interface cho Dashboard statistics, charts và tables
 */

import type { LucideIcon } from 'lucide-react';

// ==================== OVERVIEW STATS ====================
export interface DashboardOverview {
  totalRevenue: number;
  totalOrders: number;
  totalProducts: number;
  totalUsers: number;
}

// Legacy type (keep for backward compatibility)
export interface DashboardOverviewResponse {
  totalRevenue: number;
  totalOrders: number;
  totalProducts: number;
  totalUsers: number;
}

// UI Helper type
export interface DashboardStats {
  label: string;
  value: string;
  change: string;
  colorClass: string;
  icon: LucideIcon;
}

// ==================== REVENUE CHART ====================
export interface RevenueChartData {
  labels: string[];
  values: number[];
  total: number;
  averagePerDay: number;
  period: string;
}

export type DashboardPeriod = 'SEVEN_DAYS' | 'THIRTY_DAYS' | 'THREE_MONTHS';


// ==================== ORDER STATUS CHART ====================
export interface OrderStatusChartData {
  labels: string[];
  values: number[];
  percentages: number[];
  totalOrders: number;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

// ==================== USER REGISTRATION CHART ====================
export interface UserRegistrationChartData {
  labels: string[];
  values: number[];
  total: number;
  totalUsers: number; // ✅ Added for summary stats
  period: string;
}

// ✅ Match Backend enum: WEEKLY | MONTHLY
export type RegistrationPeriod = 'WEEKLY' | 'MONTHLY';

// ==================== TOP PRODUCTS ====================
export interface TopProduct {
  productId: number;
  productName: string;
  imageUrl?: string;        // ✅ Match backend response
  totalSold: number;        // ✅ Changed from soldQuantity to match backend
  revenue: number;          // ✅ Correct
}

// Legacy type for backward compatibility with backend TopProductResponse
export interface TopProductResponse {
  productId: number;
  productName: string;
  imageUrl?: string;
  totalSold: number;
  revenue: number;
}

// ==================== RECENT ORDERS ====================
export interface RecentOrder {
  orderId: number;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: OrderStatus;
  statusLabel: string;
  createdAt: string;
}

// ==================== LOW STOCK PRODUCTS ====================
// ==================== LOW STOCK PRODUCTS ====================
export interface LowStockProduct {
  productId: number;
  productName: string;
  imageUrl: string; // ✅ Changed from imagePath
  stockQuantity: number;
  categoryName: string; // ✅ Added
  brandName: string; // ✅ Added
  status: boolean; // ✅ Added
}
