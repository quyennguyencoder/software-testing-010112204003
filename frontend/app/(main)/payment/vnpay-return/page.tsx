/**
 * VNPay Return Page - Xử lý kết quả thanh toán từ VNPay
 */

'use client';

import { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { CheckCircle2, XCircle, Loader2, ArrowRight } from 'lucide-react';
import { paymentAPI } from '@/lib/api';
import type { PaymentResponse } from '@/types';
import { formatPrice, formatDateTime } from '@/lib/utils';
import { getPaymentStatus } from '@/lib/constants';
import { getVNPayErrorMessage } from '@/lib/constants/vnpay';

function VNPayReturnContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // VNPay response codes
  const vnpResponseCode = searchParams.get('vnp_ResponseCode');
  const vnpTxnRef = searchParams.get('vnp_TxnRef'); // Order ID
  const vnpAmount = searchParams.get('vnp_Amount');
  const vnpBankCode = searchParams.get('vnp_BankCode');
  const vnpTransactionNo = searchParams.get('vnp_TransactionNo');

  const isSuccess = vnpResponseCode === '00';

  useEffect(() => {
    const checkPaymentStatus = async () => {
      if (!vnpResponseCode || !vnpTxnRef) {
        setError('Thiếu thông tin thanh toán');
        setIsLoading(false);
        return;
      }

      try {


        // Construct payment object from VNPay return parameters
        // The actual payment status is updated by backend via VNPay IPN callback
        // Here we just display the return result to the user
        // Use placeholder ID 0, we will display vnpTxnRef (Order Code) in UI instead
        setPayment({
          id: 0,
          orderId: 0,
          paymentMethod: 'VNPAY',
          provider: 'VNPAY',
          transactionId: vnpTransactionNo || '',
          amount: vnpAmount ? parseInt(vnpAmount) / 100 : 0,
          status: vnpResponseCode === '00' ? 'SUCCESS' : 'FAILED',
          createdAt: new Date().toISOString(),
        });
      } catch (err: any) {
        console.error('Payment status check error:', err);
        setError(err.message || 'Xử lý thanh toán thất bại');
      } finally {
        setIsLoading(false);
      }
    };

    checkPaymentStatus();
  }, [vnpResponseCode, vnpTxnRef, vnpAmount, vnpTransactionNo]);

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="max-w-md mx-auto bg-card rounded-xl border border-border p-8">
          <div className="flex flex-col items-center gap-4 py-8">
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
            <p className="text-lg font-medium">Đang xử lý thanh toán...</p>
            <p className="text-sm text-muted-foreground text-center">
              Vui lòng không đóng trang này
            </p>
          </div>
        </div>
      </div>
    );
  }

  const statusConfig = payment ? getPaymentStatus(payment.status) : null;

  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-md mx-auto bg-card rounded-xl border border-border overflow-hidden">
        {/* Header */}
        <div className="bg-secondary/50 border-b border-border px-6 py-4">
          <h1 className="text-xl font-bold text-center">
            {isSuccess ? 'Thanh toán thành công' : 'Thanh toán thất bại'}
          </h1>
        </div>

        {/* Content */}
        <div className="p-6">
          <div className="flex flex-col items-center gap-6 py-4">
            {/* Status Icon */}
            {isSuccess ? (
              <CheckCircle2 className="h-20 w-20 text-green-500" />
            ) : (
              <XCircle className="h-20 w-20 text-red-500" />
            )}

            {/* Status Message */}
            <div className="text-center space-y-2">
              {error ? (
                <p className="text-red-600 font-medium">{error}</p>
              ) : isSuccess ? (
                <>
                  <p className="text-lg font-semibold text-green-600">
                    Giao dịch thành công!
                  </p>
                  <p className="text-sm text-muted-foreground">
                    Đơn hàng của bạn đã được xác nhận và đang được xử lý
                  </p>
                </>
              ) : (
                <>
                  <p className="text-lg font-semibold text-red-600">
                    Giao dịch không thành công
                  </p>
                  <p className="text-sm text-muted-foreground">
                    {getVNPayErrorMessage(vnpResponseCode)}
                  </p>
                </>
              )}
            </div>

            {/* Payment Details */}
            {payment && (
              <div className="w-full space-y-3 bg-secondary/50 rounded-lg p-4">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Mã đơn hàng:</span>
                  <span className="font-medium text-primary uppercase">{vnpTxnRef || `#${payment.orderId}`}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Số tiền:</span>
                  <span className="font-medium text-primary">
                    {formatPrice(payment.amount)}
                  </span>
                </div>
                {vnpBankCode && (
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Ngân hàng:</span>
                    <span className="font-medium uppercase">
                      {vnpBankCode}
                    </span>
                  </div>
                )}
                {vnpTransactionNo && (
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Mã GD:</span>
                    <span className="font-medium text-xs">
                      {vnpTransactionNo}
                    </span>
                  </div>
                )}
                {payment.transactionId && (
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Mã thanh toán:</span>
                    <span className="font-medium">
                      {payment.transactionId}
                    </span>
                  </div>
                )}
                {statusConfig && (
                  <div className="flex justify-between text-sm items-center">
                    <span className="text-muted-foreground">Trạng thái:</span>
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${statusConfig.class}`}>
                      {statusConfig.label}
                    </span>
                  </div>
                )}
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Thời gian:</span>
                  <span className="font-medium text-xs">
                    {formatDateTime(payment.createdAt)}
                  </span>
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex flex-col w-full gap-3 mt-4">
              {isSuccess && vnpTxnRef && (
                <Button
                  size="lg"
                  className="w-full"
                  onClick={() => router.push('/account/payments')}
                >
                  Xem lịch sử thanh toán
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              )}

              <Button
                variant={isSuccess ? 'outline' : 'default'}
                size="lg"
                className="w-full"
                onClick={() => router.push(isSuccess ? '/' : '/cart')}
              >
                {isSuccess ? 'Về trang chủ' : 'Quay lại giỏ hàng'}
              </Button>
            </div>

            {/* Support Link */}
            {!isSuccess && (
              <p className="text-xs text-muted-foreground text-center mt-4">
                Cần hỗ trợ?{' '}
                <a href="/contact" className="underline hover:text-primary">
                  Liên hệ với chúng tôi
                </a>
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function VNPayReturnPage() {
  return (
    <Suspense
      fallback={
        <div className="container mx-auto px-4 py-16">
          <div className="max-w-md mx-auto bg-card rounded-xl border border-border p-8">
            <div className="flex flex-col items-center gap-4 py-8">
              <Loader2 className="h-12 w-12 animate-spin text-primary" />
              <p className="text-lg font-medium">Đang tải...</p>
            </div>
          </div>
        </div>
      }
    >
      <VNPayReturnContent />
    </Suspense>
  );
}
