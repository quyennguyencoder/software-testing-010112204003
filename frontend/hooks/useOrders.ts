'use client';

import { useState, useEffect } from "react";
import { orderAPI, adminAPI } from "@/lib/api";
import type {
  Order,
  OrderResponse,
  RecentOrderResponse,
  AdminOrderListResponse,
} from "@/types";

export function useOrders(isAdmin: boolean = false) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        setError(null);

        if (isAdmin) {
          // Admin: dùng API quản lý đơn hàng: GET /api/v1/admin/orders
          const response = await adminAPI.getOrders({
            page: 0,
            size: 50,
            sortBy: "createdAt",
            sortDirection: "desc",
          });

          if (response.success && response.data) {
            const pageData = response.data;
            // Handle both paginated and non-paginated responses
            const ordersArray = pageData.content || (Array.isArray(pageData) ? pageData : []);

            const transformedOrders: Order[] = ordersArray.map((item: AdminOrderListResponse) => ({
              id: item.id,
              orderCode: item.orderCode,
              customer: item.customerName || item.recipientName,
              total: item.totalAmount,
              totalAmount: item.totalAmount,
              status: item.status,
              date: new Date(item.createdAt).toLocaleDateString("vi-VN"),
              createdAt: item.createdAt,
              updatedAt: item.updatedAt,
              email: item.customerEmail || "",
              recipientName: item.recipientName,
              phoneNumber: item.recipientPhone || "",
              shippingAddress: item.shippingAddress,
              paymentMethod: item.paymentMethod,
              items: [],
              itemCount: item.itemCount,
            }));
            setOrders(transformedOrders);
          } else {
            setOrders([]);
          }
        } else {
          // Customer: dùng API /api/v1/orders/my-orders
          const response = await orderAPI.getMyOrders();
          if (response.success && Array.isArray(response.data)) {
            const transformedOrders: Order[] = response.data.map(
              (item: OrderResponse) => ({
                id: item.id,
                orderCode: item.orderCode,
                email: item.email,
                recipientName: item.recipientName,
                phoneNumber: item.phoneNumber,
                shippingAddress: item.shippingAddress,
                shippingFee: item.shippingFee,
                shippingUnit: item.shippingUnit,
                note: item.note,
                status: item.status,
                paymentMethod: item.paymentMethod,
                totalAmount: item.totalAmount,
                promotionId: item.promotionId,
                createdAt: item.createdAt,
                updatedAt: item.updatedAt,
                items: item.items,
                customer: item.recipientName,
                total: item.totalAmount,
                date: new Date(item.createdAt).toLocaleDateString("vi-VN"),
                itemCount: item.items?.length ?? 0,
              })
            );
            setOrders(transformedOrders);
          } else {
            setOrders([]);
          }
        }
      } catch (err) {
        console.error('Error fetching orders:', err);
        setError(err instanceof Error ? err.message : 'Failed to fetch orders');
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [isAdmin]);

  const refetch = () => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        setError(null);

        if (isAdmin) {
          const response = await adminAPI.getOrders({
            page: 0,
            size: 20,
            sortBy: "createdAt",
            sortDirection: "desc",
          });
          if (response.success && response.data) {
            const pageData = response.data;
            const transformedOrders: Order[] = (pageData.content ||
              []).map((item: AdminOrderListResponse) => ({
                id: item.id,
                orderCode: item.orderCode,
                customer: item.customerName || item.recipientName,
                total: item.totalAmount,
                totalAmount: item.totalAmount,
                status: item.status,
                date: new Date(item.createdAt).toLocaleDateString("vi-VN"),
                createdAt: item.createdAt,
                updatedAt: item.updatedAt,
                email: item.customerEmail || "",
                recipientName: item.recipientName,
                phoneNumber: item.recipientPhone || "",
                shippingAddress: item.shippingAddress,
                paymentMethod: item.paymentMethod,
                items: [],
                itemCount: item.itemCount,
              }));
            setOrders(transformedOrders);
          }
        } else {
          const response = await orderAPI.getMyOrders();
          if (response.success && Array.isArray(response.data)) {
            const transformedOrders: Order[] = response.data.map(
              (item: OrderResponse) => ({
                id: item.id,
                orderCode: item.orderCode,
                email: item.email,
                recipientName: item.recipientName,
                phoneNumber: item.phoneNumber,
                shippingAddress: item.shippingAddress,
                shippingFee: item.shippingFee,
                shippingUnit: item.shippingUnit,
                note: item.note,
                status: item.status,
                paymentMethod: item.paymentMethod,
                totalAmount: item.totalAmount,
                promotionId: item.promotionId,
                createdAt: item.createdAt,
                updatedAt: item.updatedAt,
                items: item.items,
                customer: item.recipientName,
                total: item.totalAmount,
                date: new Date(item.createdAt).toLocaleDateString("vi-VN"),
                itemCount: item.items?.length ?? 0,
              })
            );
            setOrders(transformedOrders);
          } else {
            setOrders([]);
          }
        }
      } catch (err) {
        console.error('Error fetching orders:', err);
        setError(err instanceof Error ? err.message : 'Failed to fetch orders');
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  };

  return { orders, loading, error, refetch };
}

