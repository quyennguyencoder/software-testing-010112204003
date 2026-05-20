"use client";

import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components/ui/button';
import { X, ChevronRight, Ticket, Sparkles } from 'lucide-react';
import { promotionAPI } from '@/lib/api';
import { formatPrice } from '@/lib/utils';
import type { Promotion } from '@/types/api-cart';
import { toast } from 'sonner';

interface VoucherSelectorProps {
  orderTotal: number;
  onApplyVoucher: (
    discountVoucher: Promotion | null,
    freeshipVoucher: Promotion | null,
    totalDiscount: number
  ) => void;
  currentDiscountVoucher: Promotion | null;
  currentFreeshipVoucher: Promotion | null;
  disabled?: boolean;
}

export function VoucherSelector({
  orderTotal,
  onApplyVoucher,
  currentDiscountVoucher,
  currentFreeshipVoucher,
  disabled,
}: VoucherSelectorProps) {
  const [showVoucherList, setShowVoucherList] = useState(false);
  const [availableVouchers, setAvailableVouchers] = useState<Promotion[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [voucherCode, setVoucherCode] = useState("");

  // Separate available vouchers by type
  const discountVouchers = availableVouchers.filter((v) => {
    const type = v.templateType || v.template_type || "";
    return (
      type === "DISCOUNT" ||
      type === "VOUCHER" ||
      type === "DISCOUNT_PERCENTAGE" ||
      type === "DISCOUNT_FIXED"
    );
  });

  const freeshipVouchers = availableVouchers.filter((v) => {
    const type = v.templateType || v.template_type || "";
    return type === "FREESHIP" || type === "FREE_SHIPPING";
  });

  const lastCalculatedKeyRef = useRef<string>('');

  useEffect(() => {
    if (showVoucherList && availableVouchers.length === 0) {
      loadAvailableVouchers();
    }
  }, [showVoucherList]);

  const loadAvailableVouchers = async () => {
    setIsLoading(true);
    try {
      const resp = await promotionAPI.getAvailablePromotions(orderTotal);
      if (resp.success && Array.isArray(resp.data)) {
        setAvailableVouchers(
          resp.data.map((v: any) => ({
            ...v,
            // Normalize nullable values from PromotionResponse
            minValueToBeApplied: v.minValueToBeApplied ?? undefined,
          }))
        );
      }
    } catch (error) {
      console.error("Failed to load vouchers:", error);
      toast.error("Không thể tải danh sách voucher");
    } finally {
      setIsLoading(false);
    }
  };

  const calculateDiscount = async (voucher: Promotion): Promise<number> => {
    try {
      const promotionId = String(
        voucher.id || voucher.code || voucher.templateCode || ""
      );
      const resp = await promotionAPI.calculateDiscount(
        promotionId,
        orderTotal
      );
      if (resp.success && typeof resp.data === "number") {
        return resp.data;
      }
      return 0;
    } catch (error) {
      console.error("Failed to calculate discount:", error);
      return 0;
    }
  };

  // Auto re-calculate discount when order total changes while a voucher is applied.
  // This prevents the user from having to remove + re-apply the voucher when they add/remove selected items.
  useEffect(() => {
    const discountVoucher = currentDiscountVoucher;
    const freeshipVoucher = currentFreeshipVoucher;

    if (!discountVoucher && !freeshipVoucher) return;

    const discountId = discountVoucher
      ? String(
          discountVoucher.id ||
            discountVoucher.code ||
            discountVoucher.templateCode ||
            ''
        )
      : '';
    const freeshipId = freeshipVoucher
      ? String(
          freeshipVoucher.id ||
            freeshipVoucher.code ||
            freeshipVoucher.templateCode ||
            ''
        )
      : '';

    // If there's nothing selected, keep vouchers but discount becomes 0.
    if (!Number.isFinite(orderTotal) || orderTotal <= 0) {
      const key = `${discountId}|${freeshipId}:0`;
      if (lastCalculatedKeyRef.current !== key) {
        lastCalculatedKeyRef.current = key;
        onApplyVoucher(discountVoucher, freeshipVoucher, 0);
      }
      return;
    }

    const key = `${discountId}|${freeshipId}:${orderTotal}`;
    if (lastCalculatedKeyRef.current === key) return;

    const abortController = new AbortController();
    const t = setTimeout(async () => {
      try {
        const [discountResp, freeshipResp] = await Promise.all([
          discountId
            ? promotionAPI.calculateDiscount(discountId, orderTotal)
            : Promise.resolve({ success: true, data: 0 } as any),
          freeshipId
            ? promotionAPI.calculateDiscount(freeshipId, orderTotal)
            : Promise.resolve({ success: true, data: 0 } as any),
        ]);

        if (abortController.signal.aborted) return;

        const discountAmount =
          discountResp?.success && typeof discountResp.data === 'number'
            ? discountResp.data
            : 0;
        const freeshipAmount =
          freeshipResp?.success && typeof freeshipResp.data === 'number'
            ? freeshipResp.data
            : 0;

        lastCalculatedKeyRef.current = key;
        onApplyVoucher(discountVoucher, freeshipVoucher, discountAmount + freeshipAmount);
      } catch (error) {
        if (abortController.signal.aborted) return;
        console.error('Failed to auto-recalculate discount:', error);
      }
    }, 200);

    return () => {
      abortController.abort();
      clearTimeout(t);
    };
  }, [currentDiscountVoucher, currentFreeshipVoucher, orderTotal, onApplyVoucher]);

  const canApplyVoucher = (voucher: Promotion): boolean => {
    const minValue =
      voucher.min_value_to_be_applied ?? voucher.minValueToBeApplied ?? 0;
    return orderTotal >= minValue;
  };

  const handleApplyVoucher = async (
    voucher: Promotion,
    voucherType: "discount" | "freeship"
  ) => {
    if (!canApplyVoucher(voucher)) {
      const minValue =
        voucher.min_value_to_be_applied ?? voucher.minValueToBeApplied ?? 0;
      toast.error(
        `Đơn hàng tối thiểu ${formatPrice(minValue)} để áp dụng voucher này`
      );
      return;
    }

    const discount = await calculateDiscount(voucher);
    if (discount > 0) {
      if (voucherType === "discount") {
        // Calculate total discount with both vouchers
        const freeshipDiscount = currentFreeshipVoucher
          ? await calculateDiscount(currentFreeshipVoucher)
          : 0;
        const totalDiscount = discount + freeshipDiscount;
        onApplyVoucher(voucher, currentFreeshipVoucher, totalDiscount);
        toast.success(
          `Đã áp dụng mã giảm giá: ${
            voucher.code || voucher.templateCode || voucher.id
          }`
        );
      } else {
        // Calculate total discount with both vouchers
        const discountAmount = currentDiscountVoucher
          ? await calculateDiscount(currentDiscountVoucher)
          : 0;
        const totalDiscount = discountAmount + discount;
        onApplyVoucher(currentDiscountVoucher, voucher, totalDiscount);
        toast.success(
          `Đã áp dụng mã freeship: ${
            voucher.code || voucher.templateCode || voucher.id
          }`
        );
      }
    } else {
      toast.error("Không thể tính giảm giá cho voucher này");
    }
  };

  const handleRemoveVoucher = async (voucherType: "discount" | "freeship") => {
    if (voucherType === "discount") {
      const freeshipDiscount = currentFreeshipVoucher
        ? await calculateDiscount(currentFreeshipVoucher)
        : 0;
      onApplyVoucher(null, currentFreeshipVoucher, freeshipDiscount);
      toast.info("Đã hủy mã giảm giá");
    } else {
      const discountAmount = currentDiscountVoucher
        ? await calculateDiscount(currentDiscountVoucher)
        : 0;
      onApplyVoucher(currentDiscountVoucher, null, discountAmount);
      toast.info("Đã hủy mã freeship");
    }
  };

  const handleCodeSubmit = async () => {
    const trimmed = voucherCode.trim().toUpperCase();
    if (!trimmed) {
      toast.error("Vui lòng nhập mã giảm giá");
      return;
    }

    // Find voucher by code
    let found = availableVouchers.find(
      (v) =>
        String(v.code || "").toUpperCase() === trimmed ||
        String(v.templateCode || "").toUpperCase() === trimmed ||
        String(v.id).toUpperCase() === trimmed
    );

    // If not in list, try to fetch available vouchers first
    if (!found && availableVouchers.length === 0) {
      await loadAvailableVouchers();
      found = availableVouchers.find(
        (v) =>
          String(v.code || "").toUpperCase() === trimmed ||
          String(v.templateCode || "").toUpperCase() === trimmed ||
          String(v.id).toUpperCase() === trimmed
      );
    }

    if (found) {
      const type = found.templateType || found.template_type || "";
      const isFreeship = type === "FREESHIP" || type === "FREE_SHIPPING";
      await handleApplyVoucher(found, isFreeship ? "freeship" : "discount");
      setVoucherCode("");
    } else {
      toast.error("Mã giảm giá không hợp lệ hoặc không khả dụng");
    }
  };

  return (
    <div className="space-y-4">
      {/* Discount Voucher Section */}
      {currentDiscountVoucher ? (
        <div className="flex items-center justify-between p-4 bg-gradient-to-r from-yellow-50 to-orange-50 border-2 border-yellow-200 rounded-lg shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-yellow-100 rounded-lg">
              <Ticket className="h-5 w-5 text-yellow-600" />
            </div>
            <div>
              <p className="text-sm font-bold text-yellow-800">
                {currentDiscountVoucher.code ||
                  currentDiscountVoucher.templateCode ||
                  currentDiscountVoucher.id}
              </p>
              <p className="text-xs text-yellow-600">
                {currentDiscountVoucher.title ||
                  currentDiscountVoucher.name ||
                  "Mã giảm giá"}
              </p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleRemoveVoucher("discount")}
            className="text-yellow-700 hover:text-yellow-900 hover:bg-yellow-100"
            disabled={disabled}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : null}

      {/* Freeship Voucher Section */}
      {currentFreeshipVoucher ? (
        <div className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-cyan-50 border-2 border-blue-200 rounded-lg shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-100 rounded-lg">
              <Sparkles className="h-5 w-5 text-blue-600" />
            </div>
            <div>
              <p className="text-sm font-bold text-blue-800">
                {currentFreeshipVoucher.code ||
                  currentFreeshipVoucher.templateCode ||
                  currentFreeshipVoucher.id}
              </p>
              <p className="text-xs text-blue-600">
                {currentFreeshipVoucher.title ||
                  currentFreeshipVoucher.name ||
                  "Miễn phí vận chuyển"}
              </p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleRemoveVoucher("freeship")}
            className="text-blue-700 hover:text-blue-900 hover:bg-blue-100"
            disabled={disabled}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : null}

      {/* Show "Chọn mã" button if both vouchers are not selected */}
      {(!currentDiscountVoucher || !currentFreeshipVoucher) && (
        <>
          <Button
            variant="outline"
            className="w-full justify-center border-2 border-dashed border-primary/30 hover:border-primary hover:bg-primary/5 transition-colors group relative"
            onClick={() => setShowVoucherList(!showVoucherList)}
            disabled={disabled}
          >
            <span className="font-medium">
              {!currentDiscountVoucher && !currentFreeshipVoucher
                ? "Chọn hoặc nhập mã giảm giá"
                : !currentDiscountVoucher
                ? "Thêm mã giảm giá"
                : "Thêm mã freeship"}
            </span>
            <ChevronRight
              className={`h-4 w-4 transition-transform absolute right-4 ${
                showVoucherList ? "rotate-90" : ""
              }`}
            />
          </Button>

          {showVoucherList && (
            <div className="mt-3 border-2 border-border rounded-lg overflow-hidden shadow-lg animate-in slide-in-from-top-2 duration-200">
              {/* Voucher Input */}
              <div className="p-4 bg-gradient-to-r from-primary/5 to-primary/10 border-b border-border">
                <div className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Nhập mã giảm giá..."
                    value={voucherCode}
                    onChange={(e) =>
                      setVoucherCode(e.target.value.toUpperCase())
                    }
                    onKeyDown={(e) => e.key === "Enter" && handleCodeSubmit()}
                    className="flex-1 px-4 py-2.5 border-2 border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent text-sm font-medium"
                    disabled={disabled}
                  />
                  <Button
                    onClick={handleCodeSubmit}
                    disabled={!voucherCode.trim() || disabled}
                    className="px-6"
                  >
                    Áp dụng
                  </Button>
                </div>
              </div>

              {/* Available Vouchers by Type */}
              <div className="max-h-96 overflow-y-auto bg-white">
                {isLoading ? (
                  <div className="p-8 text-center text-sm text-muted-foreground">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
                    Đang tải voucher...
                  </div>
                ) : (
                  <div>
                    {/* Discount Vouchers */}
                    {!currentDiscountVoucher && discountVouchers.length > 0 && (
                      <div>
                        <div className="px-4 py-2 bg-yellow-50 border-b border-border">
                          <h3 className="text-sm font-bold text-yellow-800">
                            Mã giảm giá
                          </h3>
                        </div>
                        <div className="divide-y divide-border">
                          {discountVouchers.map((voucher) => {
                            const applicable = canApplyVoucher(voucher);
                            return (
                              <VoucherItem
                                key={String(voucher.id)}
                                voucher={voucher}
                                applicable={applicable}
                                disabled={disabled}
                                onApply={() =>
                                  handleApplyVoucher(voucher, "discount")
                                }
                              />
                            );
                          })}
                        </div>
                      </div>
                    )}

                    {/* Freeship Vouchers */}
                    {!currentFreeshipVoucher && freeshipVouchers.length > 0 && (
                      <div>
                        <div className="px-4 py-2 bg-blue-50 border-b border-border">
                          <h3 className="text-sm font-bold text-blue-800">
                            Miễn phí vận chuyển
                          </h3>
                        </div>
                        <div className="divide-y divide-border">
                          {freeshipVouchers.map((voucher) => {
                            const applicable = canApplyVoucher(voucher);
                            return (
                              <VoucherItem
                                key={String(voucher.id)}
                                voucher={voucher}
                                applicable={applicable}
                                disabled={disabled}
                                onApply={() =>
                                  handleApplyVoucher(voucher, "freeship")
                                }
                              />
                            );
                          })}
                        </div>
                      </div>
                    )}

                    {/* No vouchers available */}
                    {discountVouchers.length === 0 &&
                      freeshipVouchers.length === 0 && (
                        <div className="p-8 text-center">
                          <Ticket className="h-12 w-12 text-muted-foreground mx-auto mb-2 opacity-50" />
                          <p className="text-sm text-muted-foreground">
                            Không có mã giảm giá khả dụng
                          </p>
                        </div>
                      )}
                  </div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

// Extracted VoucherItem component for reusability
function VoucherItem({
  voucher,
  applicable,
  disabled,
  onApply,
}: {
  voucher: Promotion;
  applicable: boolean;
  disabled?: boolean;
  onApply: () => void;
}) {
  return (
    <div
      className={`p-4 hover:bg-muted/50 transition-colors ${
        !applicable ? "opacity-50" : ""
      }`}
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <div className="px-3 py-1.5 bg-gradient-to-r from-primary/10 to-primary/20 rounded-md">
              <span className="text-xs font-mono font-bold text-primary">
                {voucher.code || voucher.templateCode || voucher.id}
              </span>
            </div>
            {voucher.status === "ACTIVE" && (
              <div className="px-2 py-0.5 bg-green-100 text-green-700 rounded text-xs font-medium">
                Khả dụng
              </div>
            )}
          </div>

          <p className="text-sm font-semibold mb-1">
            {voucher.title || voucher.name || "Mã giảm giá"}
          </p>

          {voucher.description && (
            <p className="text-xs text-muted-foreground mb-2">
              {voucher.description}
            </p>
          )}

          <div className="flex items-center gap-3 text-xs text-muted-foreground">
            {(voucher.percent_discount ?? voucher.percentDiscount) && (
              <span className="font-semibold text-orange-600">
                Giảm {voucher.percent_discount ?? voucher.percentDiscount}%
                {(() => {
                  const maxDiscount =
                    voucher.max_discount ?? voucher.maxDiscount;
                  return typeof maxDiscount === "number" && maxDiscount > 0
                    ? ` (tối đa ${formatPrice(maxDiscount)})`
                    : null;
                })()}
              </span>
            )}
            {(() => {
              const fixedAmount = voucher.fixed_amount ?? voucher.fixedAmount;
              return typeof fixedAmount === "number" && fixedAmount > 0 ? (
                <span className="font-semibold text-orange-600">
                  Giảm {formatPrice(fixedAmount)}
                </span>
              ) : null;
            })()}
            {(() => {
              const minValue =
                voucher.min_value_to_be_applied ?? voucher.minValueToBeApplied;
              return typeof minValue === "number" && minValue > 0 ? (
                <span>• Đơn tối thiểu {formatPrice(minValue)}</span>
              ) : null;
            })()}
          </div>

          {!applicable && (
            <p className="text-xs text-red-500 mt-2 font-medium">
              Đơn hàng chưa đủ điều kiện áp dụng
            </p>
          )}
        </div>

        <Button
          size="sm"
          onClick={onApply}
          disabled={!applicable || disabled}
          className="shrink-0 min-w-[80px]"
        >
          {applicable ? "Áp dụng" : "Không đủ"}
        </Button>
      </div>
    </div>
  );
}
