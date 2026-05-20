/**
 * Custom hook for managing promotions (Customer & Admin)
 */
"use client";

import { useState, useEffect } from "react";
import { promotionAPI } from "@/lib/api";
import type {
  PromotionResponse,
  CreatePromotionRequest,
  UpdatePromotionRequest,
} from "@/types";

/**
 * Hook for customer to get available promotions based on order total
 */
export function useAvailablePromotions(orderTotal: number | null) {
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (orderTotal === null || orderTotal <= 0) {
      setPromotions([]);
      return;
    }

    const fetchPromotions = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await promotionAPI.getAvailablePromotions(orderTotal);
        if (response.success && response.data) {
          setPromotions(response.data);
        } else {
          setError(response.message || "Failed to load promotions");
        }
      } catch (err) {
        setError("Failed to fetch promotions");
        console.error("Error fetching promotions:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchPromotions();
  }, [orderTotal]);

  return { promotions, loading, error };
}

/**
 * Hook for admin to manage all promotions
 */
export function usePromotions() {
  const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPromotions = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await promotionAPI.getAllPromotions();
      if (response.success && response.data) {
        setPromotions(response.data);
      } else {
        setError(response.message || "Failed to load promotions");
      }
    } catch (err) {
      setError("Failed to fetch promotions");
      console.error("Error fetching promotions:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPromotions();
  }, []);

  const createPromotion = async (
    data: CreatePromotionRequest
  ): Promise<boolean> => {
    try {
      console.log("Creating promotion with data:", data);
      const response = await promotionAPI.createPromotion(data);
      console.log("Create promotion response:", response);
      if (
        response.success &&
        (response.status === 200 || response.status === 201)
      ) {
        await fetchPromotions(); // Refresh list
        return true;
      }
      const errorMsg = response.message || "Failed to create promotion";
      setError(errorMsg);
      console.error("Create promotion failed:", errorMsg, response);
      return false;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Failed to create promotion";
      setError(errorMessage);
      console.error("Error creating promotion:", err);
      return false;
    }
  };

  const updatePromotion = async (
    id: string,
    data: UpdatePromotionRequest
  ): Promise<boolean> => {
    try {
      const response = await promotionAPI.updatePromotion(id, data);
      if (response.success) {
        await fetchPromotions(); // Refresh list
        return true;
      }
      setError(response.message || "Failed to update promotion");
      return false;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Failed to update promotion";
      setError(errorMessage);
      console.error("Error updating promotion:", err);
      return false;
    }
  };

  const disablePromotion = async (id: string): Promise<boolean> => {
    try {
      const response = await promotionAPI.disablePromotion(id);
      if (response.success) {
        await fetchPromotions(); // Refresh list
        return true;
      }
      setError(response.message || "Failed to disable promotion");
      return false;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Failed to disable promotion";
      setError(errorMessage);
      console.error("Error disabling promotion:", err);
      return false;
    }
  };

  const calculateDiscount = async (
    promotionId: string,
    orderTotal: number
  ): Promise<number | null> => {
    try {
      const response = await promotionAPI.calculateDiscount(
        promotionId,
        orderTotal
      );
      if (response.success && response.data !== null) {
        return response.data;
      }
      return null;
    } catch (err) {
      console.error("Error calculating discount:", err);
      return null;
    }
  };

  return {
    promotions,
    loading,
    error,
    createPromotion,
    updatePromotion,
    disablePromotion,
    calculateDiscount,
    refetch: fetchPromotions,
  };
}
