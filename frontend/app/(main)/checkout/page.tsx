/**
 * Checkout Page - Thanh toán đơn hàng
 * Design: Clean, minimal, yellow accent (theo FRONTEND_DESIGN_SYSTEM.md)
 * Layout: Wireframe 3-step tabs (Address, Shipping, Payment) + sticky order summary
 */

"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useCartStore } from "@/store/cartStore";
import { PaymentMethodSelector } from "@/components/features/payment";
import { AddressSelector } from "@/components/features/checkout/AddressSelector";
import { orderAPI, userAPI, cartAPI, getAuthToken } from "@/lib/api";
import type { PaymentMethod, CreateOrderRequest } from "@/types";
import { Button } from "@/components/ui/button";
import { Loader2, Package, ShoppingCart, Check } from "lucide-react";
import { formatPrice } from "@/lib/utils";
import { mapBackendCartItems } from "@/lib/utils/cartMapper";
import { toast } from "sonner";

const getConfiguredShippingFee = (): number => {
  const envValue = process.env.NEXT_PUBLIC_DEFAULT_SHIPPING_FEE;
  const parsedValue = envValue ? Number(envValue) : NaN;
  return !Number.isFinite(parsedValue) || parsedValue < 0 ? 30000 : parsedValue;
};

/**
 * Horizontal Tab Steps (Wireframe style)
 */
function CheckoutTabs({
  currentStep,
  onStepClick,
}: {
  currentStep: number;
  onStepClick: (step: number) => void;
}) {
  const steps = [
    { id: 1, label: "Địa chỉ" },
    { id: 2, label: "Vận chuyển" },
    { id: 3, label: "Thanh toán" },
  ];

  return (
    <div className="border-b border-border">
      <div className="flex -mb-px">
        {steps.map((step) => {
          const isActive = currentStep === step.id;
          const isPast = currentStep > step.id;

          return (
            <button
              key={step.id}
              onClick={() => isPast && onStepClick(step.id)}
              disabled={currentStep < step.id}
              className={`
                flex-1 py-4 px-6 text-sm font-medium border-b-2 transition-colors
                ${isActive
                  ? "border-primary text-foreground"
                  : isPast
                    ? "border-transparent text-primary hover:text-primary/80 cursor-pointer"
                    : "border-transparent text-muted-foreground cursor-not-allowed"
                }
              `}
            >
              {isPast && <Check className="inline h-4 w-4 mr-1" />}
              {step.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

/**
 * Order Summary (Right Sticky Column)
 */
function OrderSummary({
  items,
  totalPrice,
  shippingFee,
  promotionDiscount,
  promotionTitle,
  error,
  onCheckout,
  isProcessing,
  showCheckoutButton,
  processingIds,
}: {
  items: any[];
  totalPrice: number;
  shippingFee: number;
  promotionDiscount?: number;
  promotionTitle?: string;
  error?: string;
  onCheckout?: () => void;
  isProcessing?: boolean;
  showCheckoutButton?: boolean;
  processingIds?: number[];
}) {
  const discount = promotionDiscount || 0;
  const finalAmount = Math.max(0, totalPrice - discount + shippingFee);

  return (
    <div className="bg-card border border-border rounded-lg p-6 sticky top-4">
      <h2 className="text-lg font-semibold mb-4">Đơn hàng của bạn</h2>

      {/* Cart Items */}
      <div className="space-y-3 max-h-80 overflow-y-auto mb-4">
        {items.map((item) => {
          const isItemProcessing =
            Array.isArray(processingIds) && processingIds.includes(item.id);
          return (
            <div
              key={item.id}
              className={`flex gap-3 ${isItemProcessing ? "opacity-60 pointer-events-none" : ""
                }`}
            >
              <div className="relative w-16 h-16 bg-muted rounded flex items-center justify-center flex-shrink-0">
                {item.productImage && item.productImage.length <= 4 ? (
                  <span className="text-2xl">{item.productImage}</span>
                ) : (
                  <Package className="h-6 w-6 text-muted-foreground" />
                )}
                {item.quantity > 1 && (
                  <span className="absolute -top-1 -right-1 bg-primary text-primary-foreground text-xs w-5 h-5 rounded-full flex items-center justify-center font-medium">
                    {item.quantity}
                  </span>
                )}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium line-clamp-2">
                  {item.productName}
                </p>
                <p className="text-sm font-semibold mt-1">
                  {formatPrice(item.price * item.quantity)}
                </p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Price Summary */}
      <div className="border-t border-border pt-4 space-y-2 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">Tạm tính</span>
          <span className="font-medium">{formatPrice(totalPrice)}</span>
        </div>
        {discount > 0 && (
          <div className="flex justify-between text-green-600">
            <span>Giảm giá {promotionTitle && `(${promotionTitle})`}</span>
            <span className="font-medium">-{formatPrice(discount)}</span>
          </div>
        )}
        <div className="flex justify-between">
          <span className="text-muted-foreground">Phí vận chuyển</span>
          <span className="font-medium">{formatPrice(shippingFee)}</span>
        </div>
        <div className="flex justify-between text-base font-bold pt-2 border-t border-border">
          <span>Tổng cộng</span>
          <span className="text-primary">{formatPrice(finalAmount)}</span>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded text-sm text-red-600">
          {error}
        </div>
      )}

      {/* Checkout Button */}
      {showCheckoutButton && (
        <Button
          className="w-full mt-4"
          size="lg"
          onClick={onCheckout}
          disabled={isProcessing}
        >
          {isProcessing ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Đang xử lý...
            </>
          ) : (
            "Đặt hàng"
          )}
        </Button>
      )}
    </div>
  );
}

function CheckoutContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { items, totalPrice, clearCart, removeItems, setItems } =
    useCartStore();

  const selectedParam = searchParams?.get?.("selected") ?? null;
  const selectedIdsFromQuery = selectedParam
    ? selectedParam
      .split(",")
      .map((s) => Number(s))
      .filter(Boolean)
    : null;

  // Validate selected items exist in cart
  const itemsForOrder =
    selectedIdsFromQuery && selectedIdsFromQuery.length > 0
      ? items.filter((it: any) => selectedIdsFromQuery.includes(it.id))
      : items;

  // Show warning if selected items don't exist in cart
  const [hasShownMissingItemsWarning, setHasShownMissingItemsWarning] =
    useState(false);

  useEffect(() => {
    if (
      selectedIdsFromQuery &&
      selectedIdsFromQuery.length > 0 &&
      itemsForOrder.length === 0 &&
      !hasShownMissingItemsWarning
    ) {
      setHasShownMissingItemsWarning(true);
      toast.error("Các sản phẩm đã chọn không còn trong giỏ hàng");
      // Redirect to cart after showing error
      setTimeout(() => router.push("/cart"), 2000);
    } else if (
      selectedIdsFromQuery &&
      selectedIdsFromQuery.length > itemsForOrder.length &&
      !hasShownMissingItemsWarning
    ) {
      setHasShownMissingItemsWarning(true);
      const missingCount = selectedIdsFromQuery.length - itemsForOrder.length;
      toast.warning(
        `${missingCount} sản phẩm đã được loại bỏ vì không còn trong giỏ hàng`
      );
    }
  }, [
    selectedIdsFromQuery,
    itemsForOrder.length,
    hasShownMissingItemsWarning,
    router,
  ]);

  const orderItemsTotalPrice = itemsForOrder.reduce(
    (s: number, it: any) => s + (it.appliedPrice ?? it.price) * it.quantity,
    0
  );

  const [currentStep, setCurrentStep] = useState(1);

  // Form states
  const [email, setEmail] = useState("");
  const [recipientName, setRecipientName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");
  const [province, setProvince] = useState("");
  const [provinceCode, setProvinceCode] = useState("");
  const [ward, setWard] = useState("");
  const [wardCode, setWardCode] = useState("");
  const [note, setNote] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("COD");

  // Promotion state (read from cart page via query params)
  const [selectedPromotionId, setSelectedPromotionId] = useState<string | null>(
    null
  );
  const [selectedFreeshippingId, setSelectedFreeshippingId] = useState<
    string | null
  >(null);
  const [promotionDiscount, setPromotionDiscount] = useState(0);
  const [promotionTitle, setPromotionTitle] = useState<string | null>(null);

  // Parse voucher from URL (passed from cart page)
  useEffect(() => {
    const voucherParam = searchParams?.get?.("voucher") ?? null;
    if (voucherParam) {
      try {
        const voucher = JSON.parse(decodeURIComponent(voucherParam));
        // New format: { discountId, discountCode, freeshipId, freeshipCode, totalDiscount }
        if (voucher) {
          if (voucher.discountId) {
            setSelectedPromotionId(String(voucher.discountId));
          }
          if (voucher.freeshipId) {
            setSelectedFreeshippingId(String(voucher.freeshipId));
          }
          setPromotionDiscount(Number(voucher.totalDiscount) || 0);
          // Display both codes if available
          const codes = [voucher.discountCode, voucher.freeshipCode]
            .filter(Boolean)
            .join(" + ");
          setPromotionTitle(codes || null);
        }
      } catch (e) {
        console.warn("Failed to parse voucher param:", e);
      }
    }
  }, [searchParams]);

  const [isProcessing, setIsProcessing] = useState(false);
  const [orderingItemIds, setOrderingItemIds] = useState<number[]>([]);
  const [isLoadingUser, setIsLoadingUser] = useState(true);
  const [error, setError] = useState("");
  const [orderSuccess, setOrderSuccess] = useState(false); // Prevent empty cart flash

  const shippingFee = getConfiguredShippingFee();
  const shippingUnit = "Giao hàng nhanh";

  // Load user profile (guest OK)
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const response = await userAPI.getMe();
        const user = response.data;
        setEmail(user.email || "");
        setRecipientName(user.fullName || "");
        setPhoneNumber(user.phoneNumber || "");
      } catch (err: any) {
        if (!err.message?.includes("401")) {
          console.error("Error loading user profile:", err);
        }
      } finally {
        setIsLoadingUser(false);
      }
    };
    loadUserProfile();
  }, []);

  // Xử lý thay đổi địa chỉ từ AddressSelector
  const handleAddressChange = (address: {
    recipientName: string;
    phoneNumber: string;
    streetAddress: string;
    province: string;
    provinceCode: string;
    ward: string;
    wardCode: string;
  }) => {
    setRecipientName(address.recipientName);
    setPhoneNumber(address.phoneNumber);
    // Tạo địa chỉ đầy đủ từ các thành phần
    const fullAddress = [
      address.streetAddress,
      address.ward,
      address.province
    ].filter(Boolean).join(', ');
    setShippingAddress(fullAddress);
    setProvince(address.province);
    setProvinceCode(address.provinceCode);
    setWard(address.ward);
    setWardCode(address.wardCode);
  };

  // Validation
  const validateStep1 = (): boolean => {
    setError("");
    if (!email.trim()) {
      setError("Vui lòng nhập email");
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError("Email không hợp lệ");
      return false;
    }
    if (!recipientName.trim()) {
      setError("Vui lòng nhập họ và tên");
      return false;
    }
    if (!phoneNumber.trim()) {
      setError("Vui lòng nhập số điện thoại");
      return false;
    }
    if (!/^0\d{9,10}$/.test(phoneNumber.trim())) {
      setError("Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)");
      return false;
    }
    if (!shippingAddress.trim()) {
      setError("Vui lòng nhập địa chỉ giao hàng");
      return false;
    }
    if (!province || !ward) {
      setError("Vui lòng chọn tỉnh/thành phố và phường/xã");
      return false;
    }
    return true;
  };

  // Navigation
  const handleNext = () => {
    if (currentStep === 1 && !validateStep1()) return;
    setError("");
    setCurrentStep((prev) => Math.min(prev + 1, 3));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleBack = () => {
    setError("");
    setCurrentStep((prev) => Math.max(prev - 1, 1));
  };

  const handleStepClick = (step: number) => {
    setError("");
    setCurrentStep(step);
  };

  // Place Order
  const handlePlaceOrder = async () => {
    setError("");
    if (itemsForOrder.length === 0) {
      setError("Giỏ hàng trống");
      return;
    }

    const token = getAuthToken();
    if (!token) {
      toast.error("Vui lòng đăng nhập để đặt hàng");
      const qs = searchParams?.toString?.() ?? "";
      const redirectPath = `/checkout${qs ? `?${qs}` : ""}`;
      router.push(`/login?redirect=${encodeURIComponent(redirectPath)}`);
      return;
    }

    // mark items as being ordered and notify other tabs immediately
    const orderedLocalIds = itemsForOrder.map((it: any) => it.id);
    try {
      localStorage.setItem("lastOrderPlacedAt", String(Date.now()));
    } catch { }
    setOrderingItemIds(orderedLocalIds);
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
        promotionId: selectedPromotionId || undefined,
        freeshippingPromotionId: selectedFreeshippingId || undefined,
        items: itemsForOrder.map((item: any) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      };

      const orderResponse = await orderAPI.createOrder(orderRequest);
      const orderData = orderResponse.data;

      // After order is created, prefer server state: fetch cart and set local store.
      try {
        const cartResp = await cartAPI.getCurrentCart();
        if (cartResp && cartResp.success && cartResp.data) {
          const backendItems = Array.isArray(cartResp.data.items)
            ? cartResp.data.items
            : [];
          const mappedItems = mapBackendCartItems(backendItems);
          setItems(mappedItems);
        } else {
          // Backend didn't clear ordered items yet — remove only ordered local items as fallback
          removeItems(orderedLocalIds);
        }
      } catch (e) {
        // Network or other error — fall back to removing only ordered local items
        removeItems(orderedLocalIds);
      }

      // Mark order placed so other tabs/pages refresh cart
      try {
        localStorage.setItem("lastOrderPlacedAt", String(Date.now()));
      } catch { }

      // Remove ordered items locally as immediate feedback
      try {
        const orderedLocalIds = itemsForOrder.map((it: any) => it.id);
        removeItems(orderedLocalIds);
      } catch { }

      // Try a final refresh from backend to ensure server state wins
      try {
        const final = await cartAPI.getCurrentCart();
        if (final && final.success && final.data) {
          const backendItems = Array.isArray(final.data.items)
            ? final.data.items
            : [];
          const mappedItems = mapBackendCartItems(backendItems);
          clearCart();
          if (mappedItems.length > 0) setItems(mappedItems);
        }
      } catch (e) {
        console.warn("Failed final cart refresh after order:", e);
      }

      // Mark order as successful before navigation to prevent empty cart flash
      setOrderSuccess(true);

      // Navigate to appropriate page
      if (paymentMethod === "VNPAY" && orderData?.paymentUrl) {
        window.location.href = orderData.paymentUrl;
      } else {
        router.push(`/orders/${orderData?.orderId || ""}`);
      }
    } catch (err: any) {
      console.error("Checkout error:", err);
      setError(err.message || "Đặt hàng thất bại. Vui lòng thử lại.");
    } finally {
      setIsProcessing(false);
      // clear ordering flags so UI stops showing processing state
      setOrderingItemIds([]);
    }
  };

  // Empty cart - but don't show if order was just placed (prevents flash before redirect)
  if (items.length === 0 && !orderSuccess && !isProcessing) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <ShoppingCart className="mx-auto h-16 w-16 text-muted-foreground mb-4" />
        <h2 className="text-2xl font-bold mb-2">Giỏ hàng trống</h2>
        <p className="text-muted-foreground mb-6">
          Thêm sản phẩm vào giỏ hàng để tiếp tục
        </p>
        <Button onClick={() => router.push("/")}>Tiếp tục mua sắm</Button>
      </div>
    );
  }

  // Loading
  if (isLoadingUser) {
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
      <h1 className="text-3xl font-bold mb-6">Checkout</h1>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Left: Form */}
        <div className="lg:col-span-2">
          <div className="bg-card border border-border rounded-lg overflow-hidden">
            {/* Tabs */}
            <CheckoutTabs
              currentStep={currentStep}
              onStepClick={handleStepClick}
            />

            {/* Step Content */}
            <div className="p-6">
              {/* Step 1: Address */}
              {currentStep === 1 && (
                <div className="space-y-4">
                  <h2 className="text-lg font-semibold mb-4">
                    Thông tin nhận hàng
                  </h2>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Email nhận thông báo{" "}
                      <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="email"
                      className="w-full px-4 py-2 border border-border rounded focus:outline-none focus:ring-2 focus:ring-primary"
                      placeholder="email@example.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </div>

                  {/* Address Selector Component */}
                  <AddressSelector
                    onAddressChange={handleAddressChange}
                    defaultValue={{
                      recipientName,
                      phoneNumber,
                    }}
                    error={error && (!recipientName || !phoneNumber || !shippingAddress) ? error : undefined}
                  />

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Ghi chú (Tùy chọn)
                    </label>
                    <textarea
                      className="w-full px-4 py-2 border border-border rounded focus:outline-none focus:ring-2 focus:ring-primary"
                      rows={2}
                      placeholder="Ghi chú thêm cho đơn hàng..."
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                    />
                  </div>

                  {error && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded text-sm text-red-600">
                      {error}
                    </div>
                  )}

                  <div className="flex justify-end pt-4">
                    <Button onClick={handleNext}>Tiếp tục vận chuyển</Button>
                  </div>
                </div>
              )}

              {/* Step 2: Shipping */}
              {currentStep === 2 && (
                <div className="space-y-4">
                  <h2 className="text-lg font-semibold mb-4">
                    Phương thức vận chuyển
                  </h2>

                  <div className="border-2 border-primary rounded p-4 bg-primary/5">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="w-5 h-5 rounded-full bg-primary flex items-center justify-center">
                          <Check className="h-3 w-3 text-primary-foreground" />
                        </div>
                        <div>
                          <p className="font-medium">{shippingUnit}</p>
                          <p className="text-sm text-muted-foreground">
                            Giao hàng trong 3-5 ngày
                          </p>
                        </div>
                      </div>
                      <span className="font-semibold">
                        {formatPrice(shippingFee)}
                      </span>
                    </div>
                  </div>

                  <p className="text-sm text-muted-foreground">
                    Phương thức vận chuyển mặc định. Các tùy chọn khác sẽ có
                    sớm.
                  </p>

                  <div className="flex justify-between pt-4">
                    <Button onClick={handleBack} variant="outline">
                      Quay lại
                    </Button>
                    <Button onClick={handleNext}>Tiếp tục thanh toán</Button>
                  </div>
                </div>
              )}

              {/* Step 3: Payment */}
              {currentStep === 3 && (
                <div className="space-y-4">
                  <h2 className="text-lg font-semibold mb-4">
                    Phương thức thanh toán
                  </h2>

                  <PaymentMethodSelector
                    value={paymentMethod}
                    onChange={setPaymentMethod}
                    disabled={isProcessing}
                  />

                  <div className="flex justify-between pt-4">
                    <Button
                      onClick={handleBack}
                      variant="outline"
                      disabled={isProcessing}
                    >
                      Quay lại
                    </Button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right: Order Summary */}
        <div>
          <OrderSummary
            items={itemsForOrder}
            totalPrice={orderItemsTotalPrice}
            shippingFee={shippingFee}
            promotionDiscount={promotionDiscount}
            promotionTitle={promotionTitle || undefined}
            error={currentStep === 3 ? error : undefined}
            onCheckout={handlePlaceOrder}
            isProcessing={isProcessing}
            showCheckoutButton={currentStep === 3}
            processingIds={orderingItemIds}
          />
        </div>
      </div>
    </div>
  );
}

export default function CheckoutPage() {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-screen">
          <Loader2 className="animate-spin" />
        </div>
      }
    >
      <CheckoutContent />
    </Suspense>
  );
}
