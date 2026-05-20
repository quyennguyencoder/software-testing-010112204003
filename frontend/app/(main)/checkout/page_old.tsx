/**
 * Checkout Page - Thanh toán đơn hàng (Minimalist Design)
 */

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useCartStore } from '@/store/cartStore';
import { PaymentMethodSelector } from '@/components/features/payment';
import { orderAPI, userAPI, cartAPI } from '@/lib/api';
import type { PaymentMethod, CreateOrderRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Loader2, Package, ShoppingCart, Check, ChevronRight, ChevronLeft } from 'lucide-react';
import { formatPrice, cn } from '@/lib/utils';

/**
 * Get configured shipping fee from environment or return default
 */
const getConfiguredShippingFee = (): number => {
  const envValue = process.env.NEXT_PUBLIC_DEFAULT_SHIPPING_FEE;
  const parsedValue = envValue ? Number(envValue) : NaN;

  if (!Number.isFinite(parsedValue) || parsedValue < 0) {
    // Fallback to the original flat-rate shipping fee (30,000 VND)
    return 30000;
  }

  return parsedValue;
};

/**
 * Stepper Component - Minimalist Text Style
 * Matches "Address —— Shipping —— Payment"
 */
function CheckoutStepper({ currentStep }: { currentStep: number }) {
  const steps = [
    { number: 1, title: 'Địa chỉ' },
    { number: 2, title: 'Vận chuyển' },
    { number: 3, title: 'Thanh toán' },
  ];

  return (
    <div className="mb-10 pt-4">
      <div className="flex items-center gap-3 text-sm md:text-base">
        {steps.map((step, index) => {
          const isActive = currentStep === step.number;
          const isCompleted = currentStep > step.number;

          return (
            <div key={step.number} className="flex items-center gap-3">
              <span
                className={cn(
                  "font-medium transition-colors",
                  isActive ? "text-black font-bold" :
                    isCompleted ? "text-black" : "text-gray-400"
                )}
              >
                {step.title}
              </span>

              {/* Separator Line */}
              {index < steps.length - 1 && (
                <div
                  className={cn(
                    "w-12 h-[1px]",
                    (isActive || isCompleted) ? "bg-black" : "bg-gray-200"
                  )}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

/**
 * Order Summary Component
 */
function OrderSummary({
  items,
  totalPrice,
  shippingFee,
  error,
  onCheckout,
  isProcessing = false,
  showCheckoutButton = false,
}: {
  items: any[];
  totalPrice: number;
  shippingFee: number;
  error?: string;
  onCheckout?: () => void;
  isProcessing?: boolean;
  showCheckoutButton?: boolean;
}) {
  const finalAmount = totalPrice + shippingFee;

  return (
    <div className="bg-gray-50 p-6 md:p-8 h-full min-h-screen md:min-h-[auto]">
      <h2 className="text-lg font-medium text-gray-900 mb-6">
        Đơn hàng của bạn
      </h2>

      {/* Cart Items */}
      <div className="space-y-6 mb-8">
        {items.map((item) => (
          <div key={item.id} className="flex gap-4">
            <div className="relative h-20 w-20 bg-white border border-gray-200 flex-shrink-0 flex items-center justify-center">
              {item.productImage && (item.productImage.startsWith('http') || item.productImage.startsWith('/images')) ? (
                <img
                  src={item.productImage}
                  alt={item.productName}
                  className="object-contain w-full h-full p-2"
                  onError={(e) => {
                    (e.target as HTMLImageElement).style.display = 'none';
                    const parent = (e.target as HTMLElement).parentElement;
                    if (parent && !parent.querySelector('.fallback-icon')) {
                      const fallback = document.createElement('div');
                      fallback.className = 'fallback-icon flex items-center justify-center h-full w-full';
                      fallback.innerHTML = '<svg class="h-6 w-6 text-gray-300" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg>';
                      parent.appendChild(fallback);
                    }
                  }}
                />
              ) : (
                <Package className="h-6 w-6 text-gray-300" />
              )}
              <div className="absolute -top-2 -right-2 h-5 w-5 bg-gray-500 text-white text-xs flex items-center justify-center rounded-full">
                {item.quantity}
              </div>
            </div>
            <div className="flex-1 min-w-0 flex flex-col justify-center">
              <h3 className="font-medium text-gray-900 text-sm line-clamp-2">
                {item.productName}
              </h3>
              <p className="text-sm text-gray-500 mt-1">
                {formatPrice(item.price)}
              </p>
            </div>
            <div className="flex items-center">
              <p className="font-medium text-gray-900 text-sm">
                {formatPrice(item.price * item.quantity)}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Styling Divider */}
      <div className="border-t border-gray-200 my-6"></div>

      {/* Price Breakdown */}
      <div className="space-y-3 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-600">Tạm tính</span>
          <span className="font-medium text-gray-900">{formatPrice(totalPrice)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Phí vận chuyển</span>
          <span className="font-medium text-gray-900">{formatPrice(shippingFee)}</span>
        </div>

        <div className="border-t border-gray-200 my-4"></div>

        <div className="flex justify-between items-baseline">
          <span className="text-base font-medium text-gray-900">Tổng cộng</span>
          <div className="flex items-baseline gap-2">
            <span className="text-xs text-gray-500">VND</span>
            <span className="text-xl font-bold text-gray-900">
              {formatPrice(finalAmount).replace('đ', '')}
            </span>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mt-6 p-4 bg-red-50 border border-red-100 text-red-600 text-sm rounded-none">
          {error}
        </div>
      )}

      {/* Place Order Button */}
      {showCheckoutButton && (
        <>
          <Button
            className="w-full mt-8 bg-black hover:bg-neutral-800 text-white rounded-none h-14 text-base font-medium shadow-none transition-all"
            onClick={onCheckout}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Đang xử lý...
              </>
            ) : (
              'Đặt hàng ngay'
            )}
          </Button>

          <p className="text-xs text-gray-400 text-center mt-4">
            Bằng cách đặt hàng, bạn đồng ý với{' '}
            <a href="/terms" className="underline hover:text-black">
              Điều khoản dịch vụ
            </a>
          </p>
        </>
      )}
    </div>
  );
}

export default function CheckoutPage() {
  const router = useRouter();
  const { items, totalPrice } = useCartStore();

  // Step management
  const [currentStep, setCurrentStep] = useState(1);

  // Form states
  const [email, setEmail] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [shippingAddress, setShippingAddress] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [note, setNote] = useState('');
  const [shippingUnit] = useState('Giao hàng nhanh');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('COD');

  // UI states
  const [isProcessing, setIsProcessing] = useState(false);
  const [isLoadingUser, setIsLoadingUser] = useState(true);
  const [error, setError] = useState<string>('');

  const shippingFee = getConfiguredShippingFee();

  // Load user profile
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const response = await userAPI.getMe();
        const user = response.data;
        setEmail(user.email || '');
        setRecipientName(user.fullName || '');
        setPhoneNumber(user.phoneNumber || '');
      } catch (err: any) {
        // Guest checkout: Allow users to checkout without login
        // Only log if it's not a 401 (unauthorized) error
        if (err.message && !err.message.includes('401')) {
          console.error('Error loading user profile:', err);
        }
        // User can still proceed with manual input
      } finally {
        setIsLoadingUser(false);
      }
    };

    loadUserProfile();
  }, []);

  // Validation
  const validateStep1 = (): boolean => {
    setError('');

    if (!email.trim()) {
      setError('Vui lòng nhập Email');
      return false;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError('Email không hợp lệ');
      return false;
    }

    if (!recipientName.trim()) {
      setError('Vui lòng nhập Tên người nhận');
      return false;
    }

    if (!shippingAddress.trim()) {
      setError('Vui lòng nhập Địa chỉ giao hàng');
      return false;
    }

    if (!phoneNumber.trim()) {
      setError('Vui lòng nhập Số điện thoại');
      return false;
    }

    if (!/^0\d{9,10}$/.test(phoneNumber.trim())) {
      setError('Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)');
      return false;
    }

    return true;
  };

  const handleNextStep = () => {
    console.log('handleNextStep called, currentStep:', currentStep);
    
    if (currentStep === 1) {
      const isValid = validateStep1();
      console.log('Step 1 validation result:', isValid);
      if (!isValid) {
        console.log('Validation failed, error:', error);
        return;
      }
    }
    
    setError('');
    const newStep = Math.min(currentStep + 1, 3);
    console.log('Moving to step:', newStep);
    setCurrentStep(newStep);
  };

  const handlePrevStep = () => {
    setError('');
    setCurrentStep((prev) => Math.max(prev - 1, 1));
  };

  const handlePlaceOrder = async () => {
    setError('');
    if (items.length === 0) {
      setError('Giỏ hàng trống');
      return;
    }

    setIsProcessing(true);

    try {
      const orderRequest: CreateOrderRequest = {
        email: email.trim(),
        recipientName: recipientName.trim(),
        phoneNumber: phoneNumber.trim(),
        shippingAddress: shippingAddress.trim(),
        shippingFee,
        shippingUnit,
        note: note.trim() || undefined,
        paymentMethod,
        promotionId: undefined,
        items: items.map((item: any) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      };

      const orderResponse = await orderAPI.createOrder(orderRequest);
      const orderData = orderResponse.data;

      useCartStore.getState().clearCart();

      if (paymentMethod === 'VNPAY' && orderData.paymentUrl) {
        window.location.href = orderData.paymentUrl;
      } else {
        router.push(`/orders/${orderData.orderId}`);
      }
    } catch (err: any) {
      console.error('Checkout error:', err);
      setError(err.message || 'Đặt hàng thất bại. Vui lòng thử lại.');
    } finally {
      setIsProcessing(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-32 text-center">
        <div className="inline-flex h-20 w-20 items-center justify-center rounded-full bg-gray-100 mb-6">
          <ShoppingCart className="h-10 w-10 text-gray-400" />
        </div>
        <h2 className="text-2xl font-bold mb-2">Giỏ hàng của bạn đang trống</h2>
        <p className="text-gray-500 mb-8 max-w-md mx-auto">
          Có vẻ như bạn chưa thêm sản phẩm nào. Hãy quay lại cửa hàng để mua sắm nhé.
        </p>
        <Button onClick={() => router.push('/')} className="bg-black hover:bg-neutral-800 text-white rounded-none px-8 h-12">
          Quay lại cửa hàng
        </Button>
      </div>
    );
  }

  if (isLoadingUser) {
    return (
      <div className="h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-black" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-screen">

        {/* LEFT COLUMN: Main Content */}
        <div className="lg:col-span-7 px-4 md:px-12 py-8 lg:py-12">
          <div className="max-w-xl mx-auto lg:mx-0 lg:ml-auto">
            <h1 className="text-3xl font-bold tracking-tight text-gray-900 mb-2">Thanh toán</h1>
            <p className="text-sm text-gray-500 mb-8">
              <Link href="/" className="hover:underline hover:text-black">Trang chủ</Link>
              <span className="mx-2">/</span>
              Checkout
            </p>

            {/* Stepper */}
            <CheckoutStepper currentStep={currentStep} />

            {/* Step 1: Address */}
            {currentStep === 1 && (
              <div className="space-y-6 animate-in fade-in slide-in-from-right-2 duration-300">
                <div className="grid grid-cols-1 gap-6">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Email nhận thông báo</label>
                    <input
                      type="email"
                      className="w-full px-4 h-12 border border-gray-300 rounded-none focus:outline-none focus:ring-1 focus:ring-black focus:border-black transition-all bg-white"
                      placeholder="email@example.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Họ và tên người nhận</label>
                    <input
                      type="text"
                      className="w-full px-4 h-12 border border-gray-300 rounded-none focus:outline-none focus:ring-1 focus:ring-black focus:border-black transition-all bg-white"
                      placeholder="Nguyễn Văn A"
                      value={recipientName}
                      onChange={(e) => setRecipientName(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Số điện thoại</label>
                    <input
                      type="tel"
                      className="w-full px-4 h-12 border border-gray-300 rounded-none focus:outline-none focus:ring-1 focus:ring-black focus:border-black transition-all bg-white"
                      placeholder="0901234567"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Địa chỉ giao hàng chi tiết</label>
                    <textarea
                      className="w-full px-4 py-3 border border-gray-300 rounded-none focus:outline-none focus:ring-1 focus:ring-black focus:border-black transition-all bg-white"
                      rows={2}
                      placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố"
                      value={shippingAddress}
                      onChange={(e) => setShippingAddress(e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Ghi chú (Tùy chọn)</label>
                    <textarea
                      className="w-full px-4 py-3 border border-gray-300 rounded-none focus:outline-none focus:ring-1 focus:ring-black focus:border-black transition-all bg-white"
                      rows={2}
                      placeholder="Lời nhắn cho shipper..."
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                    />
                  </div>
                </div>

                {/* Error Message Display */}
                {error && (
                  <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
                    <p className="text-sm text-red-600 font-medium">{error}</p>
                  </div>
                )}

                <div className="flex justify-end pt-6">
                  <Button
                    onClick={handleNextStep}
                    className="bg-black hover:bg-neutral-800 text-white rounded-none h-12 px-8 text-base shadow-none"
                  >
                    Tiếp tục vận chuyển
                  </Button>
                </div>
              </div>
            )}

            {/* Step 2: Shipping */}
            {currentStep === 2 && (
              <div className="space-y-6 animate-in fade-in slide-in-from-right-2 duration-300">
                <div className="space-y-4">
                  {/* Selected Option */}
                  <div className="relative flex cursor-pointer rounded-none border-2 border-black p-5 shadow-sm bg-white">
                    <div className="flex w-full items-center justify-between">
                      <div className="flex items-center gap-4">
                        <div className="flex items-center justify-center h-5 w-5 bg-black border border-black text-white">
                          <Check className="h-3 w-3" />
                        </div>
                        <div>
                          <span className="block text-base font-bold text-gray-900">
                            {shippingUnit}
                          </span>
                          <span className="block text-sm text-gray-500 mt-0.5">
                            Giao hàng tiêu chuẩn (3-5 ngày)
                          </span>
                        </div>
                      </div>
                      <span className="font-bold text-gray-900">
                        {formatPrice(shippingFee)}
                      </span>
                    </div>
                  </div>

                  {/* Disabled Option */}
                  <div className="relative flex cursor-not-allowed rounded-none border border-gray-200 p-5 bg-gray-50 opacity-60">
                    <div className="flex w-full items-center justify-between">
                      <div className="flex items-center gap-4">
                        <div className="h-5 w-5 border border-gray-300 bg-white"></div>
                        <div>
                          <span className="block text-base font-medium text-gray-900">
                            Giao hàng Hỏa tốc
                          </span>
                          <span className="block text-sm text-gray-500 mt-0.5">
                            Nhận hàng trong 2h (Coming soon)
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-8">
                  <button
                    onClick={handlePrevStep}
                    className="flex items-center text-sm font-medium text-gray-500 hover:text-black hover:underline transition-colors"
                  >
                    <ChevronLeft className="h-4 w-4 mr-1" />
                    Quay lại
                  </button>
                  <Button
                    onClick={handleNextStep}
                    className="bg-black hover:bg-neutral-800 text-white rounded-none h-12 px-8 text-base shadow-none"
                  >
                    Tiếp tục thanh toán
                  </Button>
                </div>
              </div>
            )}

            {/* Step 3: Payment */}
            {currentStep === 3 && (
              <div className="space-y-8 animate-in fade-in slide-in-from-right-2 duration-300">
                <div className="rounded-none border-2 border-gray-100 p-1">
                  {/* We reuse the PaymentMethodSelector but wrapper needs style adjustment via props preferably, 
                       but standard component is fine if styled neutrally */}
                  <PaymentMethodSelector
                    value={paymentMethod}
                    onChange={setPaymentMethod}
                    disabled={isProcessing}
                  />
                </div>

                {/* Confirm Navigation */}
                <div className="flex items-center justify-between pt-4">
                  <button
                    onClick={handlePrevStep}
                    className="flex items-center text-sm font-medium text-gray-500 hover:text-black hover:underline transition-colors"
                  >
                    <ChevronLeft className="h-4 w-4 mr-1" />
                    Quay lại vận chuyển
                  </button>
                  {/* Button is handled in Order Summary for consistency or here? 
                      In wireframe, button is usually under summary for Desktop, or sticky bottom mobile.
                      Our OrderSummary has the button for Step 3. So no button here needed.
                  */}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* RIGHT COLUMN: Order Summary (Gray Background) */}
        <div className="lg:col-span-5 bg-gray-50 border-l border-gray-200">
          <div className="sticky top-0 h-full">
            <OrderSummary
              items={items}
              totalPrice={totalPrice}
              shippingFee={shippingFee}
              error={currentStep === 3 ? error : undefined}
              onCheckout={handlePlaceOrder}
              isProcessing={isProcessing}
              showCheckoutButton={currentStep === 3}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
