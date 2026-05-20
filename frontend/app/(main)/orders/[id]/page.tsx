/**
 * Order Detail Page - Chi tiết đơn hàng
 */

'use client';

import { useEffect, useState, use } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { CheckCircle2, Package, Loader2, MapPin, Phone, Mail, User, CreditCard, Truck, Tag, Calendar, ArrowLeft } from 'lucide-react';
import { formatPrice } from '@/lib/utils';
import { orderAPI } from '@/lib/api';
import type { OrderResponse, OrderItem, OrderStatus } from '@/types';

interface OrderDetailPageProps {
  params: Promise<{
    id: string;
  }>;
}

// Map trạng thái đơn hàng
const statusConfig: Record<OrderStatus, { label: string; color: string; bgColor: string }> = {
  PENDING: { label: 'Đang xử lý', color: 'text-yellow-800', bgColor: 'bg-yellow-100' },
  CONFIRMED: { label: 'Đã xác nhận', color: 'text-blue-800', bgColor: 'bg-blue-100' },
  SHIPPING: { label: 'Đang giao hàng', color: 'text-purple-800', bgColor: 'bg-purple-100' },
  DELIVERED: { label: 'Đã giao hàng', color: 'text-green-800', bgColor: 'bg-green-100' },
  CANCELLED: { label: 'Đã hủy', color: 'text-red-800', bgColor: 'bg-red-100' },
};

// Map phương thức thanh toán
const paymentMethodLabels: Record<string, string> = {
  COD: 'Thanh toán khi nhận hàng (COD)',
  BANK_TRANSFER: 'Chuyển khoản ngân hàng',
  VNPAY: 'VNPay',
};

export default function OrderDetailPage(props: OrderDetailPageProps) {
  const params = use(props.params);
  const router = useRouter();
  const orderId = params.id;

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await orderAPI.getById(Number(orderId));

        if (response.success && response.data) {
          setOrder(response.data);
        } else {
          setError(response.message || 'Không thể tải thông tin đơn hàng');
        }
      } catch (err: any) {
        console.error('Error fetching order:', err);
        setError(err.message || 'Đã có lỗi xảy ra khi tải đơn hàng');
      } finally {
        setIsLoading(false);
      }
    };

    if (orderId) {
      fetchOrder();
    }
  }, [orderId]);

  // Loading state
  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="flex items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </div>
    );
  }

  // Error state
  if (error || !order) {
    return (
      <div className="container mx-auto px-4 py-16">
        <div className="max-w-md mx-auto text-center">
          <Package className="mx-auto h-16 w-16 text-muted-foreground mb-4" />
          <h2 className="text-2xl font-bold mb-2">Không tìm thấy đơn hàng</h2>
          <p className="text-muted-foreground mb-6">
            {error || 'Đơn hàng không tồn tại hoặc bạn không có quyền truy cập.'}
          </p>
          <Button onClick={() => router.push('/')}>Về trang chủ</Button>
        </div>
      </div>
    );
  }

  const statusInfo = statusConfig[order.status] || statusConfig.PENDING;
  const paymentLabel = paymentMethodLabels[order.paymentMethod] || order.paymentMethod;

  // Tính tổng tiền sản phẩm
  const subtotal = order.items?.reduce((sum, item) => sum + item.price * item.quantity, 0) || 0;
  const shippingFee = order.shippingFee || 0;
  // Tính giảm giá từ promotion nếu có (totalAmount = subtotal + shipping - discount)
  const discountAmount = subtotal + shippingFee - order.totalAmount;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Back button */}
      <Button
        variant="ghost"
        onClick={() => router.back()}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        Quay lại
      </Button>

      <div className="max-w-4xl mx-auto">
        {/* Success Header */}
        <div className="flex items-center gap-4 mb-8">
          <div className="w-16 h-16 rounded-full bg-green-50 flex items-center justify-center flex-shrink-0">
            <CheckCircle2 className="h-10 w-10 text-green-600" />
          </div>
          <div>
            <h1 className="text-2xl md:text-3xl font-bold">
              Đặt hàng thành công!
            </h1>
            <p className="text-muted-foreground">
              Cảm ơn bạn đã đặt hàng. Chúng tôi sẽ liên hệ với bạn sớm nhất.
            </p>
          </div>
        </div>

        <div className="grid md:grid-cols-3 gap-6">
          {/* Left: Order Info & Items */}
          <div className="md:col-span-2 space-y-6">
            {/* Order Info Card */}
            <div className="bg-card border border-border rounded-lg p-6">
              <div className="flex items-center justify-between mb-4 pb-4 border-b border-border">
                <div className="flex items-center gap-3">
                  <Package className="h-5 w-5 text-primary" />
                  <div>
                    <p className="text-sm text-muted-foreground">Mã đơn hàng</p>
                    <p className="font-semibold text-lg">#{order.orderCode || order.id}</p>
                  </div>
                </div>
                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${statusInfo.bgColor} ${statusInfo.color}`}>
                  {statusInfo.label}
                </span>
              </div>

              {/* Order date */}
              <div className="flex items-center gap-2 text-sm text-muted-foreground mb-4">
                <Calendar className="h-4 w-4" />
                <span>Ngày đặt: {new Date(order.createdAt).toLocaleDateString('vi-VN', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}</span>
              </div>

              {/* Payment method */}
              <div className="flex items-center gap-2 text-sm">
                <CreditCard className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">Phương thức thanh toán:</span>
                <span className="font-medium">{paymentLabel}</span>
              </div>
            </div>

            {/* Products List */}
            <div className="bg-card border border-border rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <Package className="h-5 w-5" />
                Sản phẩm đã đặt
              </h2>

              {order.items && order.items.length > 0 ? (
                <div className="space-y-4">
                  {order.items.map((item: OrderItem, index: number) => (
                    <div
                      key={item.id || index}
                      className="flex items-center gap-4 pb-4 border-b border-border last:border-b-0 last:pb-0"
                    >
                      {/* Product image placeholder */}
                      <div className="w-16 h-16 bg-muted rounded flex items-center justify-center flex-shrink-0">
                        <Package className="h-6 w-6 text-muted-foreground" />
                      </div>

                      {/* Product info */}
                      <div className="flex-1 min-w-0">
                        <p className="font-medium line-clamp-2">{item.productName}</p>
                        <p className="text-sm text-muted-foreground">Số lượng: {item.quantity}</p>
                      </div>

                      {/* Price */}
                      <div className="text-right flex-shrink-0">
                        <p className="font-semibold">{formatPrice(item.price * item.quantity)}</p>
                        {item.quantity > 1 && (
                          <p className="text-sm text-muted-foreground">{formatPrice(item.price)}/sản phẩm</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted-foreground text-center py-4">
                  Không có thông tin sản phẩm
                </p>
              )}
            </div>

            {/* Customer & Shipping Info */}
            <div className="bg-card border border-border rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                <Truck className="h-5 w-5" />
                Thông tin giao hàng
              </h2>

              <div className="grid sm:grid-cols-2 gap-4">
                <div className="space-y-3">
                  <div className="flex items-start gap-2">
                    <User className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <div>
                      <p className="text-sm text-muted-foreground">Người nhận</p>
                      <p className="font-medium">{order.recipientName}</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-2">
                    <Phone className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <div>
                      <p className="text-sm text-muted-foreground">Số điện thoại</p>
                      <p className="font-medium">{order.phoneNumber}</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-2">
                    <Mail className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <div>
                      <p className="text-sm text-muted-foreground">Email</p>
                      <p className="font-medium">{order.email}</p>
                    </div>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex items-start gap-2">
                    <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <div>
                      <p className="text-sm text-muted-foreground">Địa chỉ giao hàng</p>
                      <p className="font-medium">{order.shippingAddress}</p>
                    </div>
                  </div>

                  {order.shippingUnit && (
                    <div className="flex items-start gap-2">
                      <Truck className="h-4 w-4 text-muted-foreground mt-0.5" />
                      <div>
                        <p className="text-sm text-muted-foreground">Đơn vị vận chuyển</p>
                        <p className="font-medium">{order.shippingUnit}</p>
                      </div>
                    </div>
                  )}

                  {order.note && (
                    <div className="flex items-start gap-2">
                      <Tag className="h-4 w-4 text-muted-foreground mt-0.5" />
                      <div>
                        <p className="text-sm text-muted-foreground">Ghi chú</p>
                        <p className="font-medium">{order.note}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Right: Order Summary */}
          <div className="md:col-span-1">
            <div className="bg-card border border-border rounded-lg p-6 sticky top-4">
              <h2 className="text-lg font-semibold mb-4">Tóm tắt đơn hàng</h2>

              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Tạm tính ({order.items?.length || 0} sản phẩm)</span>
                  <span className="font-medium">{formatPrice(subtotal)}</span>
                </div>

                <div className="flex justify-between">
                  <span className="text-muted-foreground">Phí vận chuyển</span>
                  <span className="font-medium">{formatPrice(shippingFee)}</span>
                </div>

                {discountAmount > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span className="flex items-center gap-1">
                      <Tag className="h-3.5 w-3.5" />
                      Giảm giá
                      {order.promotionId && <span className="text-xs">(Mã: {order.promotionId.slice(0, 8)}...)</span>}
                    </span>
                    <span className="font-medium">-{formatPrice(discountAmount)}</span>
                  </div>
                )}

                <div className="flex justify-between text-base font-bold pt-3 border-t border-border">
                  <span>Tổng cộng</span>
                  <span className="text-primary">{formatPrice(order.totalAmount)}</span>
                </div>
              </div>

              {/* Next Steps */}
              <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <h3 className="font-semibold text-blue-900 mb-2">Bước tiếp theo</h3>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>• Kiểm tra email để xác nhận đơn hàng</li>
                  <li>• Chúng tôi sẽ liên hệ trong vòng 24h</li>
                  <li>• Theo dõi đơn hàng trong tài khoản của bạn</li>
                </ul>
              </div>

              {/* Actions */}
              <div className="mt-6 space-y-3">
                <Button
                  onClick={() => router.push('/manage?tab=orders')}
                  className="w-full"
                >
                  Quản lý đơn hàng
                </Button>
                <Button
                  onClick={() => router.push('/')}
                  variant="outline"
                  className="w-full"
                >
                  Tiếp tục mua sắm
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
