/**
 * RecentOrdersTable component - Display recent orders list
 * Module M10.2 - View Dashboard
 */

'use client';

import { RecentOrder, OrderStatus } from '@/types';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useRouter } from 'next/navigation';

interface RecentOrdersTableProps {
  data: RecentOrder[];
  loading?: boolean;
  currentPage?: number;
  hasNext?: boolean;
  onPageChange?: (page: number) => void;
}

// Status badge configuration
const getStatusConfig = (status: OrderStatus) => {
  const configs = {
    PENDING: {
      label: 'Chờ xử lý',
      className: 'bg-yellow-100 text-yellow-800 border-yellow-300',
    },
    CONFIRMED: {
      label: 'Đã xác nhận',
      className: 'bg-blue-100 text-blue-800 border-blue-300',
    },
    SHIPPING: {
      label: 'Đang giao',
      className: 'bg-purple-100 text-purple-800 border-purple-300',
    },
    DELIVERED: {
      label: 'Đã giao',
      className: 'bg-green-100 text-green-800 border-green-300',
    },
    CANCELLED: {
      label: 'Đã hủy',
      className: 'bg-red-100 text-red-800 border-red-300',
    },
  };

  return configs[status] || configs.PENDING;
};

export function RecentOrdersTable({ 
  data, 
  loading = false,
  currentPage = 0,
  hasNext = false,
  onPageChange,
}: RecentOrdersTableProps) {
  const router = useRouter();

  // Handle view order detail
  const handleViewDetail = (orderId: number) => {
    router.push(`/manage?tab=orders&orderId=${orderId}`);
  };

  if (loading) {
    return (
      <div className="bg-card rounded-xl border border-border p-4 md:p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-secondary rounded w-48 mb-4"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-secondary rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="bg-card rounded-xl border border-border p-4 md:p-6">
        <h3 className="text-lg font-semibold text-foreground mb-4">
          Đơn hàng gần đây
        </h3>
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <p className="text-muted-foreground">Chưa có đơn hàng nào</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header */}
      <div className="mb-4">
        <h3 className="text-lg font-semibold text-foreground">
          Đơn hàng gần đây
        </h3>
        <p className="text-sm text-muted-foreground mt-1">
          Hiển thị {data.length} đơn hàng (Trang {currentPage + 1})
        </p>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border">
              <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                Mã đơn
              </th>
              <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                Khách hàng
              </th>
              <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                Tổng tiền
              </th>
              <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                Trạng thái
              </th>
              <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                Ngày tạo
              </th>
            </tr>
          </thead>
          <tbody>
            {data.map((order) => {
              const statusConfig = getStatusConfig(order.status);
              const formattedDate = new Date(order.createdAt).toLocaleDateString('vi-VN', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
              });
              const formattedTime = new Date(order.createdAt).toLocaleTimeString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
              });

              return (
                <tr
                  key={order.orderId}
                  className="border-b border-border hover:bg-secondary/50 transition-colors cursor-pointer"
                  onClick={() => handleViewDetail(order.orderId)}
                  title="Click để xem chi tiết đơn hàng"
                >
                  {/* Order ID */}
                  <td className="py-3 px-4">
                    <p className="text-sm font-medium text-foreground">
                      #{order.orderId.toString().padStart(4, '0')}
                    </p>
                  </td>

                  {/* Customer */}
                  <td className="py-3 px-4">
                    <div className="flex flex-col">
                      <p className="text-sm font-medium text-foreground">
                        {order.customerName}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {order.customerEmail}
                      </p>
                    </div>
                  </td>

                  {/* Total Amount */}
                  <td className="py-3 px-4 text-right">
                    <p className="text-sm font-semibold text-primary">
                      {order.totalAmount.toLocaleString('vi-VN')}đ
                    </p>
                  </td>

                  {/* Status */}
                  <td className="py-3 px-4">
                    <div className="flex justify-center">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${statusConfig.className}`}
                      >
                        {order.statusLabel || statusConfig.label}
                      </span>
                    </div>
                  </td>

                  {/* Created Date */}
                  <td className="py-3 px-4">
                    <div className="flex flex-col items-center">
                      <p className="text-xs text-foreground">{formattedDate}</p>
                      <p className="text-xs text-muted-foreground">{formattedTime}</p>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Footer Summary */}
      {data.length > 0 && (
        <div className="mt-4 pt-4 border-t border-border flex justify-between items-center">
          {/* Left: View all link */}
          <button
            onClick={() => router.push('/manage?tab=orders')}
            className="text-sm text-primary hover:underline font-medium"
          >
            Xem tất cả →
          </button>

          {/* Right: Pagination buttons */}
          <div className="flex items-center gap-2">
            {/* Previous button - only show if not on first page */}
            {currentPage > 0 && onPageChange && (
              <button
                onClick={() => onPageChange(currentPage - 1)}
                className="flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-foreground bg-secondary hover:bg-secondary/80 rounded-lg transition-colors"
              >
                <ChevronLeft className="w-4 h-4" />
                Trước
              </button>
            )}

            {/* Current page indicator */}
            <span className="text-sm text-muted-foreground px-2">
              Trang {currentPage + 1}
            </span>

            {/* Next button - only show if hasNext */}
            {hasNext && onPageChange && (
              <button
                onClick={() => onPageChange(currentPage + 1)}
                className="flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-foreground bg-secondary hover:bg-secondary/80 rounded-lg transition-colors"
              >
                Sau
                <ChevronRight className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
