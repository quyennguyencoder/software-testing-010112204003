/**
 * OrdersTable component - Display orders in table format
 */

'use client';

import { useRouter } from 'next/navigation';
import { Eye } from 'lucide-react';
import { formatPrice } from '@/lib/utils';
import { getOrderStatus } from '@/lib/constants';
import { cn } from '@/lib/utils';
import type { Order } from '@/types';

interface OrdersTableProps {
  orders: Order[];
  isAdmin?: boolean;
  onViewDetail?: (orderId: number) => void;
}

export function OrdersTable({ orders, isAdmin = false, onViewDetail }: OrdersTableProps) {
  const router = useRouter();

  const handleViewOrder = (order: Order) => {
    if (isAdmin && onViewDetail) {
      // Admin mode: open modal
      onViewDetail(order.id);
    } else {
      // Customer mode: navigate to order page
      router.push(`/orders/${order.id}`);
    }
  };

  // Empty state
  if (orders.length === 0) {
    return (
      <div className="bg-card rounded-xl border border-border p-12 text-center">
        <p className="text-muted-foreground">Không có đơn hàng nào</p>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-xl border border-border overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-secondary/50">
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Mã đơn</th>
              {isAdmin && (
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">
                  Khách hàng
                </th>
              )}
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Tổng tiền</th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Trạng thái</th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                Ngày đặt
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const statusConfig = getOrderStatus(order.status);
              return (
                <tr key={order.id} className="border-b border-border hover:bg-secondary/50">
                  <td className="py-3 px-4 font-medium">#{order.id}</td>
                  {isAdmin && (
                    <td className="py-3 px-4 hidden sm:table-cell">
                      {order.customer || order.recipientName || 'N/A'}
                    </td>
                  )}
                  <td className="py-3 px-4 font-semibold">
                    {formatPrice(order.total || order.totalAmount)}
                  </td>
                  <td className="py-3 px-4">
                    <span className={cn("px-2 py-1 rounded-full text-xs font-semibold", statusConfig.class)}>
                      {statusConfig.label}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-muted-foreground hidden md:table-cell">
                    {order.date || new Date(order.createdAt).toLocaleDateString('vi-VN')}
                  </td>
                  <td className="py-3 px-4">
                    <button
                      className="rounded-lg p-2 text-blue-600 hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                      aria-label={`Xem chi tiết đơn hàng ${order.id}`}
                      onClick={() => handleViewOrder(order)}
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
