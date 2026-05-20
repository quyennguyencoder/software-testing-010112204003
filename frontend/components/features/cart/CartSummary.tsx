import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Shield, CreditCard } from "lucide-react";
import { VoucherSelector } from "./VoucherSelector";
import { useState } from "react";
import type { Promotion } from "@/types/api-cart";
import type { CartItem } from "@/types";
import { calculateCartTotals } from "@/lib/utils/cartMapper";

interface CartSummaryProps {
  items: CartItem[];
  onCheckout: () => void;
  selectedIds?: number[];
  onBuySelected?: () => void;
  selectedDiscountVoucher?: Promotion | null;
  selectedFreeshipVoucher?: Promotion | null;
  voucherDiscount?: number;
  onVoucherChange?: (
    discountVoucher: Promotion | null,
    freeshipVoucher: Promotion | null,
    totalDiscount: number
  ) => void;
}

export function CartSummary({
  items,
  onCheckout,
  selectedIds = [],
  onBuySelected,
  selectedDiscountVoucher: externalDiscountVoucher,
  selectedFreeshipVoucher: externalFreeshipVoucher,
  voucherDiscount: externalDiscount = 0,
  onVoucherChange,
}: CartSummaryProps) {
  const [internalDiscountVoucher, setInternalDiscountVoucher] =
    useState<Promotion | null>(null);
  const [internalFreeshipVoucher, setInternalFreeshipVoucher] =
    useState<Promotion | null>(null);
  const [internalDiscount, setInternalDiscount] = useState(0);

  // Use external voucher if controlled, otherwise use internal state
  const selectedDiscountVoucher =
    externalDiscountVoucher !== undefined
      ? externalDiscountVoucher
      : internalDiscountVoucher;
  const selectedFreeshipVoucher =
    externalFreeshipVoucher !== undefined
      ? externalFreeshipVoucher
      : internalFreeshipVoucher;
  const voucherDiscount =
    externalDiscount !== undefined ? externalDiscount : internalDiscount;

  const handleVoucherChange = (
    discountVoucher: Promotion | null,
    freeshipVoucher: Promotion | null,
    totalDiscount: number
  ) => {
    if (onVoucherChange) {
      onVoucherChange(discountVoucher, freeshipVoucher, totalDiscount);
    } else {
      setInternalDiscountVoucher(discountVoucher);
      setInternalFreeshipVoucher(freeshipVoucher);
      setInternalDiscount(totalDiscount);
    }
  };

  const selectedItems =
    Array.isArray(items) && selectedIds.length > 0
      ? items.filter((it) => selectedIds.includes(it.id))
      : [];
  const hasSelection = selectedItems.length > 0;
  const { totalItems: selectedTotalItems, totalPrice: selectedTotalPrice } =
    calculateCartTotals(selectedItems);

  // Requirement: totals only calculated when selecting products.
  const subtotal = hasSelection ? selectedTotalPrice : 0;
  const finalTotal = hasSelection ? Math.max(0, subtotal - voucherDiscount) : 0;

  const mainButtonLabel = hasSelection
    ? `Mua đã chọn (${selectedIds.length})`
    : 'Mua đã chọn';

  const mainButtonOnClick = onBuySelected ?? onCheckout;
  const mainButtonDisabled = !hasSelection;

  return (
    <div className="space-y-6">
      {/* Order Summary */}
      <Card className="shadow-sm">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center gap-2 text-xl">
            <CreditCard className="h-5 w-5" />
            Tóm tắt đơn hàng
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {!hasSelection ? (
            <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-5">
              <div className="flex items-center justify-between">
                <div className="text-base font-semibold text-gray-900">
                  Chưa chọn sản phẩm
                </div>
                <Badge variant="secondary" className="text-sm">
                  0
                </Badge>
              </div>
            </div>
          ) : (
            <div className="rounded-2xl border border-primary/20 bg-white p-5 ring-1 ring-primary/10">
              <div className="flex items-center justify-between">
                <div className="text-base font-semibold text-gray-900">
                  Sản phẩm đã chọn
                </div>
                <Badge variant="secondary" className="text-sm">
                  {selectedIds.length}
                </Badge>
              </div>
              <div className="mt-4 max-h-56 space-y-3 overflow-auto pr-1">
                {selectedItems.map((it) => (
                  <div
                    key={it.id}
                    className="rounded-xl border border-gray-100 bg-white p-3 shadow-sm"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <div className="h-2 w-2 rounded-full bg-primary/60" />
                          <div className="text-base font-medium text-gray-900 line-clamp-2">
                            {it.productName}
                          </div>
                        </div>
                        {(it.color || it.storage) && (
                          <div className="mt-1 inline-flex items-center gap-2 rounded-full bg-gray-50 px-3 py-1 text-sm text-gray-700">
                            {[it.color, it.storage].filter(Boolean).join(" • ")}
                          </div>
                        )}
                      </div>
                      <div className="inline-flex h-8 min-w-8 items-center justify-center rounded-md border border-primary/20 bg-primary/5 px-2 text-base font-bold text-primary whitespace-nowrap">
                        ×{it.quantity}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              <div className="mt-4 flex items-center justify-between rounded-xl bg-primary/5 px-4 py-3">
                <span className="text-base font-medium text-gray-800">
                  Tổng số lượng
                </span>
                <span className="text-base font-bold text-gray-900">
                  {selectedTotalItems}
                </span>
              </div>
            </div>
          )}

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-base text-gray-700">Tạm tính</span>
              <span className="text-base font-semibold text-gray-900">
                {subtotal.toLocaleString("vi-VN")}₫
              </span>
            </div>
          </div>

          {/* Voucher Selector */}
          <div className="space-y-2">
            <label className="text-base font-medium text-gray-800">
              Mã giảm giá
            </label>
            <VoucherSelector
              orderTotal={subtotal}
              onApplyVoucher={handleVoucherChange}
              currentDiscountVoucher={selectedDiscountVoucher}
              currentFreeshipVoucher={selectedFreeshipVoucher}
              disabled={!hasSelection}
            />
          </div>

          {hasSelection && voucherDiscount > 0 && (
            <div className="flex justify-between items-center text-green-600">
              <span className="text-base">Giảm giá</span>
              <span className="text-base font-semibold">
                -{voucherDiscount.toLocaleString("vi-VN")}₫
              </span>
            </div>
          )}

          <Separator />

          <div className="flex items-center justify-between rounded-2xl border border-primary/15 bg-primary/5 px-5 py-4">
            <div>
              <div className="text-base font-semibold text-gray-900">
                Tổng cộng
              </div>
            </div>
            <div className="text-2xl font-bold text-primary">
              {finalTotal.toLocaleString("vi-VN")}₫
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <Button
              onClick={mainButtonOnClick}
              disabled={mainButtonDisabled}
              className="w-full"
              size="lg"
            >
              {mainButtonLabel}
            </Button>

            <Button
              onClick={onCheckout}
              disabled={!Array.isArray(items) || items.length === 0}
              className="w-full"
              size="lg"
              variant="outline"
            >
              Mua tất cả
            </Button>
          </div>

          {hasSelection && (
            <p className="text-xs text-center text-muted-foreground"></p>
          )}
        </CardContent>
      </Card>

      {/* Benefits */}
      <Card className="shadow-sm">
        <CardContent className="pt-6">
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <div className="p-2 bg-blue-50 rounded-lg">
                <Shield className="h-5 w-5 text-blue-600" />
              </div>
              <div>
                <div className="font-medium text-sm">Bảo hành chính hãng</div>
                <div className="text-xs text-gray-600">
                  Bảo hành 12 tháng tại các trung tâm bảo hành ủy quyền
                </div>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="p-2 bg-green-50 rounded-lg">
                <svg
                  className="h-5 w-5 text-green-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <div>
                <div className="font-medium text-sm">Hàng chính hãng 100%</div>
                <div className="text-xs text-gray-600">
                  Cam kết sản phẩm chính hãng, hoàn tiền nếu phát hiện hàng giả
                </div>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="p-2 bg-purple-50 rounded-lg">
                <svg
                  className="h-5 w-5 text-purple-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"
                  />
                </svg>
              </div>
              <div>
                <div className="font-medium text-sm">Đổi trả linh hoạt</div>
                <div className="text-xs text-gray-600">
                  Đổi trả trong 7 ngày nếu sản phẩm lỗi do nhà sản xuất
                </div>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="p-2 bg-orange-50 rounded-lg">
                <svg
                  className="h-5 w-5 text-orange-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <div>
                <div className="font-medium text-sm">Giao hàng nhanh chóng</div>
                <div className="text-xs text-gray-600">
                  Giao hàng nội thành trong 2h, miễn phí với đơn hàng từ 500K
                </div>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="p-2 bg-red-50 rounded-lg">
                <svg
                  className="h-5 w-5 text-red-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
                  />
                </svg>
              </div>
              <div>
                <div className="font-medium text-sm">Thanh toán an toàn</div>
                <div className="text-xs text-gray-600">
                  Hỗ trợ nhiều phương thức thanh toán, bảo mật thông tin
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
