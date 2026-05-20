/**
 * PaymentHistoryTable component - Display payment history in table format
 */

'use client';

import { Eye, CreditCard } from 'lucide-react';
import { formatPrice, formatDateTime, cn } from '@/lib/utils';
import { getPaymentStatus, getPaymentMethodLabel } from '@/lib/constants';
import type { PaymentResponse } from '@/types';

interface PaymentHistoryTableProps {
  payments: PaymentResponse[];
  onViewDetail?: (payment: PaymentResponse) => void;
}

export function PaymentHistoryTable({
  payments,
  onViewDetail,
}: PaymentHistoryTableProps) {
  if (payments.length === 0) {
    return (
      <div className="bg-card rounded-xl border border-border p-8 text-center">
        <CreditCard className="mx-auto h-12 w-12 text-muted-foreground/50 mb-3" />
        <p className="text-muted-foreground text-sm">
          Chưa có lịch sử thanh toán
        </p>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-xl border border-border overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-secondary/50">
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                ID Giao dịch
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                Mã đơn hàng
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">
                Phương thức
              </th>
              <th className="text-right py-3 px-4 font-semibold text-muted-foreground">
                Số tiền
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                Trạng thái
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                Ngày thanh toán
              </th>
              {onViewDetail && (
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">
                  Thao tác
                </th>
              )}
            </tr>
          </thead>
          <tbody>
            {payments.map((payment) => {
              const statusConfig = getPaymentStatus(payment.status);
              const methodLabel = getPaymentMethodLabel(payment.paymentMethod);

              return (
                <tr
                  key={payment.id}
                  className="border-b border-border hover:bg-secondary/50 transition-colors"
                >
                  <td className="py-3 px-4 font-medium">
                    {payment.transactionId || `#${payment.id}`}
                  </td>
                  <td className="py-3 px-4 font-medium text-primary">
                    #{payment.orderId}
                  </td>
                  <td className="py-3 px-4 text-muted-foreground hidden sm:table-cell">
                    <div className="flex items-center gap-2">
                      <CreditCard className="w-4 h-4" />
                      <span>{methodLabel}</span>
                    </div>
                  </td>
                  <td className="py-3 px-4 font-semibold text-right">
                    {formatPrice(payment.amount)}
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={cn(
                        'px-2 py-1 rounded-full text-xs font-semibold whitespace-nowrap',
                        statusConfig.class
                      )}
                    >
                      {statusConfig.label}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-muted-foreground hidden md:table-cell">
                    {formatDateTime(payment.createdAt)}
                  </td>
                  {onViewDetail && (
                    <td className="py-3 px-4">
                      <button
                        onClick={() => onViewDetail(payment)}
                        className="rounded-lg p-2 text-blue-600 hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                        aria-label={`Xem chi tiết thanh toán ${payment.id}`}
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
