/**
 * Promotion Selector Demo Page
 * Test customer promotion selection flow
 */
"use client";

import { useState } from "react";
import { PromotionSelector } from "@/components/features/promotion";
import { ShoppingCart } from "lucide-react";

export default function PromotionSelectorDemoPage() {
  const [orderTotal, setOrderTotal] = useState(500000);
  const [appliedPromotionId, setAppliedPromotionId] = useState<string | null>(
    null
  );
  const [discountAmount, setDiscountAmount] = useState(0);

  const handlePromotionApplied = (promotionId: string, discount: number) => {
    setAppliedPromotionId(promotionId);
    setDiscountAmount(discount);
    console.log("Promotion Applied:", { promotionId, discount });
  };

  const handlePromotionCleared = () => {
    setAppliedPromotionId(null);
    setDiscountAmount(0);
    console.log("Promotion Cleared");
  };

  const finalTotal = orderTotal - discountAmount;

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
      <div className="container mx-auto px-4 max-w-2xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            Promotion Selector Demo
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Test customer promotion selection and application flow
          </p>
        </div>

        {/* Order Total Control */}
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm mb-6">
          <div className="flex items-center gap-3 mb-4">
            <ShoppingCart className="w-5 h-5 text-gray-600 dark:text-gray-400" />
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
              Giỏ hàng mẫu
            </h2>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Tổng giá trị đơn hàng (đ)
              </label>
              <input
                type="number"
                value={orderTotal}
                onChange={(e) =>
                  setOrderTotal(Math.max(0, Number(e.target.value)))
                }
                min="0"
                step="10000"
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setOrderTotal(100000)}
                className="px-3 py-1 text-sm bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md text-gray-700 dark:text-gray-300"
              >
                100k
              </button>
              <button
                onClick={() => setOrderTotal(500000)}
                className="px-3 py-1 text-sm bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md text-gray-700 dark:text-gray-300"
              >
                500k
              </button>
              <button
                onClick={() => setOrderTotal(1000000)}
                className="px-3 py-1 text-sm bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md text-gray-700 dark:text-gray-300"
              >
                1M
              </button>
              <button
                onClick={() => setOrderTotal(5000000)}
                className="px-3 py-1 text-sm bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-md text-gray-700 dark:text-gray-300"
              >
                5M
              </button>
            </div>
          </div>
        </div>

        {/* Promotion Selector */}
        <div className="mb-6">
          <PromotionSelector
            orderTotal={orderTotal}
            onPromotionApplied={handlePromotionApplied}
            onPromotionCleared={handlePromotionCleared}
          />
        </div>

        {/* Order Summary */}
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Tổng kết đơn hàng
          </h2>

          <div className="space-y-3">
            <div className="flex justify-between text-gray-600 dark:text-gray-400">
              <span>Tạm tính:</span>
              <span>{formatCurrency(orderTotal)}</span>
            </div>

            {discountAmount > 0 && (
              <div className="flex justify-between text-green-600 dark:text-green-400 font-medium">
                <span>Giảm giá:</span>
                <span>-{formatCurrency(discountAmount)}</span>
              </div>
            )}

            <div className="border-t border-gray-200 dark:border-gray-700 pt-3 flex justify-between text-lg font-bold text-gray-900 dark:text-white">
              <span>Tổng cộng:</span>
              <span className="text-blue-600 dark:text-blue-400">
                {formatCurrency(finalTotal)}
              </span>
            </div>

            {appliedPromotionId && (
              <div className="pt-3 border-t border-gray-200 dark:border-gray-700">
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Promotion ID:{" "}
                  <code className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded">
                    {appliedPromotionId}
                  </code>
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Instructions */}
        <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
          <h3 className="font-semibold text-blue-900 dark:text-blue-300 mb-2">
            💡 Hướng dẫn test
          </h3>
          <ul className="text-sm text-blue-800 dark:text-blue-400 space-y-1">
            <li>
              • Thay đổi tổng giá trị đơn hàng để xem các mã khuyến mãi khả dụng
            </li>
            <li>• Click vào "Chọn mã giảm giá" để mở danh sách</li>
            <li>• Áp dụng một mã để xem số tiền được giảm</li>
            <li>• Bạn có thể xóa mã đã áp dụng và chọn mã khác</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
