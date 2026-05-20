/**
 * VNPay Payment Return Page
 * Handles callback from VNPay payment gateway
 */

'use client';

import { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { CheckCircle2, XCircle, Loader2, Package } from 'lucide-react';
import { formatPrice } from '@/lib/utils';

function PaymentReturnContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [paymentStatus, setPaymentStatus] = useState<'processing' | 'success' | 'failed'>('processing');
  const [paymentInfo, setPaymentInfo] = useState<any>(null);

  useEffect(() => {
    // Parse VNPay return params
    const vnp_ResponseCode = searchParams.get('vnp_ResponseCode');
    const vnp_TxnRef = searchParams.get('vnp_TxnRef'); // Order code
    const vnp_Amount = searchParams.get('vnp_Amount');
    const vnp_TransactionNo = searchParams.get('vnp_TransactionNo');
    const vnp_BankCode = searchParams.get('vnp_BankCode');

    // VNPay response code: "00" = success, others = failed
    const isSuccess = vnp_ResponseCode === '00';

    setPaymentStatus(isSuccess ? 'success' : 'failed');
    setPaymentInfo({
      responseCode: vnp_ResponseCode,
      orderCode: vnp_TxnRef,
      amount: vnp_Amount ? parseInt(vnp_Amount) / 100 : 0, // VNPay sends amount * 100
      transactionNo: vnp_TransactionNo,
      bankCode: vnp_BankCode,
    });
  }, [searchParams]);

  if (paymentStatus === 'processing') {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </div>
    );
  }

  const isSuccess = paymentStatus === 'success';

  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-2xl mx-auto">
        {/* Icon */}
        <div className="flex justify-center mb-6">
          <div className={`w-20 h-20 rounded-full flex items-center justify-center ${
            isSuccess ? 'bg-green-50' : 'bg-red-50'
          }`}>
            {isSuccess ? (
              <CheckCircle2 className="h-12 w-12 text-green-600" />
            ) : (
              <XCircle className="h-12 w-12 text-red-600" />
            )}
          </div>
        </div>

        {/* Title */}
        <div className="text-center mb-8">
          <h1 className="text-2xl md:text-3xl font-bold mb-2">
            {isSuccess ? 'Thanh toán thành công!' : 'Thanh toán thất bại'}
          </h1>
          <p className="text-muted-foreground">
            {isSuccess 
              ? 'Cảm ơn bạn đã thanh toán. Đơn hàng của bạn đang được xử lý.'
              : 'Giao dịch không thành công. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.'
            }
          </p>
        </div>

        {/* Payment Details Card */}
        <div className="bg-card border border-border rounded-lg p-6 mb-6">
          <div className="flex items-center gap-3 mb-4 pb-4 border-b border-border">
            <Package className="h-5 w-5 text-primary" />
            <div>
              <p className="text-sm text-muted-foreground">Mã đơn hàng</p>
              <p className="font-semibold">#{paymentInfo?.orderCode || 'N/A'}</p>
            </div>
          </div>

          <div className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Trạng thái</span>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                isSuccess 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-red-100 text-red-800'
              }`}>
                {isSuccess ? 'Thành công' : 'Thất bại'}
              </span>
            </div>

            {paymentInfo?.amount > 0 && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">Số tiền</span>
                <span className="font-semibold">{formatPrice(paymentInfo.amount)}</span>
              </div>
            )}

            {paymentInfo?.transactionNo && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">Mã giao dịch</span>
                <span className="font-mono text-xs">{paymentInfo.transactionNo}</span>
              </div>
            )}

            {paymentInfo?.bankCode && (
              <div className="flex justify-between">
                <span className="text-muted-foreground">Ngân hàng</span>
                <span className="font-medium">{paymentInfo.bankCode}</span>
              </div>
            )}

            <div className="flex justify-between">
              <span className="text-muted-foreground">Mã phản hồi</span>
              <span className="font-mono text-xs">{paymentInfo?.responseCode || 'N/A'}</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        {isSuccess ? (
          <>
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <h3 className="font-semibold text-blue-900 mb-2">Bước tiếp theo</h3>
              <ul className="text-sm text-blue-800 space-y-1">
                <li>• Kiểm tra email để xác nhận đơn hàng và thanh toán</li>
                <li>• Chúng tôi sẽ xử lý đơn hàng trong vòng 24h</li>
                <li>• Theo dõi đơn hàng trong tài khoản của bạn</li>
              </ul>
            </div>

            <div className="flex gap-3">
              <Button 
                onClick={() => router.push('/')} 
                variant="outline"
                className="flex-1"
              >
                Tiếp tục mua sắm
              </Button>
              <Button 
                onClick={() => router.push('/account/payments')} 
                className="flex-1"
              >
                Xem đơn hàng
              </Button>
            </div>
          </>
        ) : (
          <>
            <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 mb-6">
              <h3 className="font-semibold text-amber-900 mb-2">Lý do có thể</h3>
              <ul className="text-sm text-amber-800 space-y-1">
                <li>• Số dư tài khoản không đủ</li>
                <li>• Thông tin thẻ không chính xác</li>
                <li>• Giao dịch bị hủy bởi người dùng</li>
                <li>• Lỗi kết nối với ngân hàng</li>
              </ul>
            </div>

            <div className="flex gap-3">
              <Button 
                onClick={() => router.push('/')} 
                variant="outline"
                className="flex-1"
              >
                Về trang chủ
              </Button>
              <Button 
                onClick={() => router.push('/checkout')} 
                className="flex-1"
              >
                Thử lại
              </Button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default function PaymentReturnPage() {
  return (
    <Suspense fallback={
      <div className="container mx-auto px-4 py-16">
        <div className="flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </div>
    }>
      <PaymentReturnContent />
    </Suspense>
  );
}
