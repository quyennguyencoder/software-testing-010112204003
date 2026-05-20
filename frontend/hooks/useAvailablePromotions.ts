/**
 * useAvailablePromotions - Hook for customer to get and apply promotions
 */
"use client";

import { useState, useEffect } from "react";
import { promotionAPI } from "@/lib/api";
import type { PromotionResponse } from "@/types/promotion";

export function useAvailablePromotions(orderTotal: number) {
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedPromotion, setSelectedPromotion] =
    useState<PromotionResponse | null>(null);
  const [discountAmount, setDiscountAmount] = useState<number>(0);
  const [calculatingDiscount, setCalculatingDiscount] = useState(false);

  // Fetch available promotions based on order total
  useEffect(() => {
    const fetchPromotions = async () => {
      if (orderTotal <= 0) {
        setPromotions([]);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const response = await promotionAPI.getAvailablePromotions(orderTotal);
        setPromotions(response.data || []);
      } catch (err: any) {
        setError(err.response?.data?.message || "Không thể tải khuyến mãi");
        setPromotions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPromotions();
  }, [orderTotal]);

  // Apply promotion (calculate discount)
  const applyPromotion = async (promotionId: string) => {
    setCalculatingDiscount(true);
    setError(null);

    try {
      const response = await promotionAPI.calculateDiscount(
        promotionId,
        orderTotal
      );
      const discount = response.data;
      const promotion = promotions.find((p) => p.id === promotionId);

      if (promotion) {
        setSelectedPromotion(promotion);
        setDiscountAmount(discount);
        return { success: true, discount };
      }

      return { success: false, discount: 0 };
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message || "Không thể áp dụng mã giảm giá này";
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setCalculatingDiscount(false);
    }
  };

  // Clear applied promotion
  const clearPromotion = () => {
    setSelectedPromotion(null);
    setDiscountAmount(0);
    setError(null);
  };

  // Calculate final total after discount
  const finalTotal = orderTotal - discountAmount;

  return {
    promotions,
    loading,
    error,
    selectedPromotion,
    discountAmount,
    calculatingDiscount,
    finalTotal,
    applyPromotion,
    clearPromotion,
  };
}
