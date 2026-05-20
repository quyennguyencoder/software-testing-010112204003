'use client';

import { useState, useEffect } from 'react';
import { adminAPI, orderAPI } from '@/lib/api';
import type { DashboardStats, DashboardOverviewResponse, RecentOrderResponse } from '@/types';
import { DollarSign, ShoppingCart, Users, Package } from 'lucide-react';

export function useDashboard() {
  const [stats, setStats] = useState<DashboardStats[]>([]);
  const [recentOrders, setRecentOrders] = useState<RecentOrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch overview stats
      const overviewResponse = await adminAPI.getDashboardOverview();
      const ordersResponse = await orderAPI.getRecentOrders(5);
      
      if (overviewResponse.success && overviewResponse.data) {
        const overview = overviewResponse.data;
        
        // Transform to DashboardStats format
        const transformedStats: DashboardStats[] = [
          {
            label: 'Doanh thu',
            value: formatCurrency(overview.totalRevenue),
            change: '+20.1%', // This would come from backend in real implementation
            colorClass: 'text-green-500',
            icon: DollarSign,
          },
          {
            label: 'Đơn hàng',
            value: overview.totalOrders.toLocaleString('vi-VN'),
            change: '+15.3%',
            colorClass: 'text-blue-500',
            icon: ShoppingCart,
          },
          {
            label: 'Người dùng',
            value: overview.totalUsers.toLocaleString('vi-VN'),
            change: '+8.2%',
            colorClass: 'text-purple-500',
            icon: Users,
          },
          {
            label: 'Sản phẩm',
            value: overview.totalProducts.toLocaleString('vi-VN'),
            change: '+4.3%',
            colorClass: 'text-orange-500',
            icon: Package,
          },
        ];
        
        setStats(transformedStats);
      }
      
      if (ordersResponse.success && ordersResponse.data) {
        setRecentOrders(ordersResponse.data);
      }
    } catch (err) {
      console.error('Error fetching dashboard:', err);
      setError(err instanceof Error ? err.message : 'Failed to fetch dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  return { stats, recentOrders, loading, error, refetch: fetchDashboard };
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
  }).format(amount);
}

