/**
 * Payment History Page - Lịch sử thanh toán
 * Design: Clean, minimal, yellow accent (theo FRONTEND_DESIGN_SYSTEM.md)
 */

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { paymentAPI } from '@/lib/api';
import type { PaymentHistoryResponse, PaymentResponse, PaymentStatus } from '@/types';
import { Button } from '@/components/ui/button';
import { 
  Loader2, 
  Receipt, 
  Calendar, 
  CreditCard, 
  CheckCircle2, 
  XCircle, 
  Clock,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { formatPrice } from '@/lib/utils';

/**
 * Payment Status Badge Component
 */
function PaymentStatusBadge({ status }: { status: PaymentStatus }) {
  const config = {
    SUCCESS: {
      label: 'Thành công',
      className: 'bg-green-100 text-green-800 border-green-200',
      icon: CheckCircle2,
    },
    PENDING: {
      label: 'Đang xử lý',
      className: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      icon: Clock,
    },
    FAILED: {
      label: 'Thất bại',
      className: 'bg-red-100 text-red-800 border-red-200',
      icon: XCircle,
    },
    CANCELLED: {
      label: 'Đã hủy',
      className: 'bg-gray-100 text-gray-800 border-gray-200',
      icon: XCircle,
    },
  };

  const { label, className, icon: Icon } = config[status] || config.PENDING;

  return (
    <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium border ${className}`}>
      <Icon className="h-3.5 w-3.5" />
      {label}
    </span>
  );
}

/**
 * Payment Method Badge
 */
function PaymentMethodBadge({ method, provider }: { method: string; provider?: string }) {
  const config = {
    COD: { label: 'COD', color: 'bg-blue-50 text-blue-700 border-blue-200' },
    BANK_TRANSFER: { label: 'Chuyển khoản', color: 'bg-purple-50 text-purple-700 border-purple-200' },
    VNPAY: { label: provider || 'VNPay', color: 'bg-primary/10 text-primary border-primary/20' },
  };

  const { label, color } = config[method as keyof typeof config] || config.COD;

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-medium border ${color}`}>
      <CreditCard className="h-3 w-3 mr-1" />
      {label}
    </span>
  );
}

/**
 * Payment Item Card
 */
function PaymentCard({ payment }: { payment: PaymentResponse }) {
  const router = useRouter();
  const date = new Date(payment.createdAt);

  return (
    <div className="bg-card border border-border rounded-lg p-5 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center flex-shrink-0">
            <Receipt className="h-5 w-5 text-primary" />
          </div>
          <div>
            <div className="flex items-center gap-2 mb-1">
              <h3 className="font-semibold text-base">Đơn hàng #{payment.orderId}</h3>
              <PaymentStatusBadge status={payment.status} />
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar className="h-3.5 w-3.5" />
              <span>{date.toLocaleDateString('vi-VN', { 
                day: '2-digit',
                month: '2-digit', 
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
              })}</span>
            </div>
          </div>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-primary">{formatPrice(payment.amount)}</p>
          <PaymentMethodBadge method={payment.paymentMethod} provider={payment.provider} />
        </div>
      </div>

      {payment.transactionId && (
        <div className="pt-3 border-t border-border">
          <p className="text-xs text-muted-foreground">
            Mã giao dịch: <span className="font-mono">{payment.transactionId}</span>
          </p>
        </div>
      )}

      <div className="mt-4 flex gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => router.push(`/orders/${payment.orderId}`)}
          className="flex-1"
        >
          Xem đơn hàng
        </Button>
      </div>
    </div>
  );
}

/**
 * Pagination Component
 */
function Pagination({
  currentPage,
  totalPages,
  hasNext,
  hasPrevious,
  onPageChange,
}: {
  currentPage: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="flex items-center justify-between mt-6">
      <p className="text-sm text-muted-foreground">
        Trang {currentPage + 1} / {totalPages}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          disabled={!hasPrevious}
          onClick={() => onPageChange(currentPage - 1)}
        >
          <ChevronLeft className="h-4 w-4 mr-1" />
          Trước
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={!hasNext}
          onClick={() => onPageChange(currentPage + 1)}
        >
          Sau
          <ChevronRight className="h-4 w-4 ml-1" />
        </Button>
      </div>
    </div>
  );
}

/**
 * Main Payment History Page
 */
export default function PaymentHistoryPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [paymentHistory, setPaymentHistory] = useState<PaymentHistoryResponse | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Load payment history
  const loadPaymentHistory = async (page: number) => {
    setIsLoading(true);
    setError('');

    try {
      const response = await paymentAPI.getPaymentHistory(page, pageSize);
      setPaymentHistory(response.data);
      setCurrentPage(page);
    } catch (err: any) {
      console.error('Error loading payment history:', err);
      if (err.message?.includes('401')) {
        setError('Bạn cần đăng nhập để xem lịch sử thanh toán');
      } else {
        setError(err.message || 'Không thể tải lịch sử thanh toán');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadPaymentHistory(0);
  }, []);

  // Loading state
  if (isLoading && !paymentHistory) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">Lịch sử thanh toán</h1>
        <p className="text-muted-foreground">
          Xem lại tất cả các giao dịch thanh toán của bạn
        </p>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div className="flex items-start gap-3">
            <XCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="text-sm font-medium text-red-800">{error}</p>
              {error.includes('đăng nhập') && (
                <Button
                  size="sm"
                  variant="outline"
                  className="mt-2"
                  onClick={() => router.push('/login')}
                >
                  Đăng nhập ngay
                </Button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Payment List */}
      {paymentHistory && paymentHistory.payments.length > 0 ? (
        <>
          {/* Summary Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="bg-card border border-border rounded-lg p-4">
              <p className="text-sm text-muted-foreground mb-1">Tổng giao dịch</p>
              <p className="text-2xl font-bold">{paymentHistory.totalElements}</p>
            </div>
            <div className="bg-card border border-border rounded-lg p-4">
              <p className="text-sm text-muted-foreground mb-1">Thành công</p>
              <p className="text-2xl font-bold text-green-600">
                {paymentHistory.payments.filter(p => p.status === 'SUCCESS').length}
              </p>
            </div>
            <div className="bg-card border border-border rounded-lg p-4">
              <p className="text-sm text-muted-foreground mb-1">Đang xử lý</p>
              <p className="text-2xl font-bold text-yellow-600">
                {paymentHistory.payments.filter(p => p.status === 'PENDING').length}
              </p>
            </div>
          </div>

          {/* Payment Cards */}
          <div className="space-y-4">
            {paymentHistory.payments.map((payment) => (
              <PaymentCard key={payment.id} payment={payment} />
            ))}
          </div>

          {/* Pagination */}
          {paymentHistory.totalPages > 1 && (
            <Pagination
              currentPage={paymentHistory.currentPage}
              totalPages={paymentHistory.totalPages}
              hasNext={paymentHistory.hasNext}
              hasPrevious={paymentHistory.hasPrevious}
              onPageChange={loadPaymentHistory}
            />
          )}
        </>
      ) : (
        !error && (
          /* Empty State */
          <div className="text-center py-16">
            <div className="inline-flex w-16 h-16 items-center justify-center rounded-full bg-muted mb-4">
              <Receipt className="h-8 w-8 text-muted-foreground" />
            </div>
            <h2 className="text-xl font-semibold mb-2">Chưa có giao dịch nào</h2>
            <p className="text-muted-foreground mb-6">
              Bạn chưa thực hiện giao dịch thanh toán nào
            </p>
            <Button onClick={() => router.push('/')}>
              Mua sắm ngay
            </Button>
          </div>
        )
      )}
    </div>
  );
}
