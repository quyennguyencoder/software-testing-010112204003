/**
 * Test file để verify Dashboard API
 * Chạy file này để test các endpoints đã tạo
 * 
 * Cách sử dụng:
 * 1. Đảm bảo backend đang chạy tại http://localhost:8081
 * 2. Import và gọi các hàm test trong component hoặc console
 */

import { dashboardAPI } from './api';

// Test function để gọi tất cả endpoints
export async function testAllDashboardEndpoints() {
  console.group('🧪 Testing Dashboard API Endpoints');

  try {
    // 1. Test Overview
    console.log('\n1️⃣ Testing getOverview...');
    const overviewRes = await dashboardAPI.getOverview();
    console.log('✅ Overview:', overviewRes.data);

    // 2. Test Revenue Chart
    console.log('\n2️⃣ Testing getRevenueChart (THIRTY_DAYS)...');
    const revenueRes = await dashboardAPI.getRevenueChart('THIRTY_DAYS');
    console.log('✅ Revenue Chart:', revenueRes.data);

    // 3. Test Order Status Chart
    console.log('\n3️⃣ Testing getOrderStatusChart...');
    const orderStatusRes = await dashboardAPI.getOrderStatusChart();
    console.log('✅ Order Status Chart:', orderStatusRes.data);

    // 4. Test User Registration Chart
    console.log('\n4️⃣ Testing getUserRegistrationChart (WEEKLY)...');
    const userRegRes = await dashboardAPI.getUserRegistrationChart('WEEKLY');
    console.log('✅ User Registration Chart:', userRegRes.data);

    // 5. Test Top Products
    console.log('\n5️⃣ Testing getTopProducts (limit=5)...');
    const topProductsRes = await dashboardAPI.getTopProducts(5);
    console.log('✅ Top Products:', topProductsRes.data);

    // 6. Test Recent Orders
    console.log('\n6️⃣ Testing getRecentOrders (limit=10)...');
    const recentOrdersRes = await dashboardAPI.getRecentOrders(10);
    console.log('✅ Recent Orders:', recentOrdersRes.data);

    // 7. Test Low Stock Products
    console.log('\n7️⃣ Testing getLowStockProducts (threshold=10)...');
    const lowStockRes = await dashboardAPI.getLowStockProducts(10);
    console.log('✅ Low Stock Products:', lowStockRes.data);

    console.log('\n✅ All endpoints tested successfully!');
    return true;
  } catch (error) {
    console.error('❌ Error testing endpoints:', error);
    return false;
  } finally {
    console.groupEnd();
  }
}

// Test individual endpoints
export const dashboardAPITests = {
  testOverview: async () => {
    try {
      const res = await dashboardAPI.getOverview();
      console.log('✅ Overview API works:', res.data);
      return res;
    } catch (error) {
      console.error('❌ Overview API failed:', error);
      throw error;
    }
  },

  testRevenueChart: async (period: 'SEVEN_DAYS' | 'THIRTY_DAYS' | 'THREE_MONTHS' = 'THIRTY_DAYS') => {
    try {
      const res = await dashboardAPI.getRevenueChart(period);
      console.log(`✅ Revenue Chart (${period}) API works:`, res.data);
      return res;
    } catch (error) {
      console.error(`❌ Revenue Chart (${period}) API failed:`, error);
      throw error;
    }
  },

  testOrderStatusChart: async () => {
    try {
      const res = await dashboardAPI.getOrderStatusChart();
      console.log('✅ Order Status Chart API works:', res.data);
      return res;
    } catch (error) {
      console.error('❌ Order Status Chart API failed:', error);
      throw error;
    }
  },

  testUserRegistrationChart: async (period: 'WEEKLY' | 'MONTHLY' = 'WEEKLY') => {
    try {
      const res = await dashboardAPI.getUserRegistrationChart(period as any);
      console.log(`✅ User Registration Chart (${period}) API works:`, res.data);
      return res;
    } catch (error) {
      console.error(`❌ User Registration Chart (${period}) API failed:`, error);
      throw error;
    }
  },

  testTopProducts: async (limit: number = 5) => {
    try {
      const res = await dashboardAPI.getTopProducts(limit);
      console.log(`✅ Top Products (limit=${limit}) API works:`, res.data);
      return res;
    } catch (error) {
      console.error(`❌ Top Products (limit=${limit}) API failed:`, error);
      throw error;
    }
  },

  testRecentOrders: async (limit: number = 10) => {
    try {
      const res = await dashboardAPI.getRecentOrders(limit);
      console.log(`✅ Recent Orders (limit=${limit}) API works:`, res.data);
      return res;
    } catch (error) {
      console.error(`❌ Recent Orders (limit=${limit}) API failed:`, error);
      throw error;
    }
  },

  testLowStockProducts: async (threshold: number = 10) => {
    try {
      const res = await dashboardAPI.getLowStockProducts(threshold);
      console.log(`✅ Low Stock Products (threshold=${threshold}) API works:`, res.data);
      return res;
    } catch (error) {
      console.error(`❌ Low Stock Products (threshold=${threshold}) API failed:`, error);
      throw error;
    }
  },
};

// Export để sử dụng trong components
export { dashboardAPI } from './api';
