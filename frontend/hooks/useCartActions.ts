"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import { useCartStore } from "@/store";
import { cartAPI } from "@/lib/api";
import { mapBackendCartItems } from "@/lib/utils/cartMapper";
import { toast } from "sonner";
import type { ProductCardResponse } from "@/services/new-product.service";

/** Cart item details for custom add (used by product detail page) */
export interface CartItemDetails {
    productId: number;
    productName: string;
    productImage: string;
    price: number;
    discountPercent?: number;
    quantity: number;
    color?: string;
    storage?: string;
}

/**
 * A centralized hook to handle "Add to Cart" actions.
 * - If logged in: calls cartAPI.addToCart and syncs the cart from backend.
 * - If guest: adds to local store only (guest cart sync handled by useCartSync).
 */
export function useCartActions() {
    const router = useRouter();
    const { user } = useAuth();
    const isAuthenticated = !!user;
    const { addItem, setItems } = useCartStore();

    /**
     * Common helper to sync cart from backend after API call
     */
    const syncCartFromBackend = useCallback(async () => {
        try {
            const cartResp = await cartAPI.getCurrentCart();
            if (cartResp?.data?.items) {
                const mapped = mapBackendCartItems(cartResp.data.items);
                setItems(mapped);
            }
        } catch (syncErr) {
            console.error("[useCartActions] Failed to sync cart:", syncErr);
        }
    }, [setItems]);

    /**
     * Add product to cart (simple - uses product card data)
     */
    const addToCart = useCallback(
        async (product: ProductCardResponse) => {
            const productId = product.id;
            const productName = product.name;
            const productImage = product.thumbnailUrl || "";
            const price = product.originalPrice || 0;
            const appliedPrice = product.discountedPrice || price;

            if (!isAuthenticated) {
                addItem({
                    productId,
                    productName,
                    productImage,
                    price,
                    appliedPrice,
                    quantity: 1,
                } as any);
                toast.success("Đã thêm vào giỏ (khách) — đăng nhập để đồng bộ");
                return true;
            }

            try {
                const resp = await cartAPI.addToCart({ productId, quantity: 1 });
                if (!resp || resp.success === false) {
                    throw new Error(resp?.message || "Không thể thêm vào giỏ hàng");
                }
                await syncCartFromBackend();
                toast.success("Đã thêm vào giỏ hàng");
                return true;
            } catch (e: any) {
                console.error("[useCartActions] Add to cart failed:", e);
                toast.error(e?.message || "Lỗi khi thêm vào giỏ");
                return false;
            }
        },
        [isAuthenticated, addItem, syncCartFromBackend]
    );

    /**
     * Add product with custom details (for product detail page with variants)
     */
    const addToCartWithDetails = useCallback(
        async (details: CartItemDetails) => {
            if (!isAuthenticated) {
                addItem({
                    productId: details.productId,
                    productName: details.productName,
                    productImage: details.productImage,
                    price: details.price,
                    discountPercent: details.discountPercent,
                    quantity: details.quantity,
                    color: details.color,
                    storage: details.storage,
                } as any);
                toast.success("Đã thêm vào giỏ (khách) — đăng nhập để đồng bộ");
                return true;
            }

            try {
                const resp = await cartAPI.addToCart({
                    productId: details.productId,
                    quantity: details.quantity,
                    color: details.color,
                    storage: details.storage,
                });
                if (!resp || resp.success === false) {
                    throw new Error(resp?.message || "Không thể thêm vào giỏ hàng");
                }
                await syncCartFromBackend();
                toast.success("Đã thêm vào giỏ hàng", {
                    description: `${details.productName} x${details.quantity}`,
                });
                return true;
            } catch (e: any) {
                console.error("[useCartActions] Add to cart failed:", e);
                toast.error(e?.message || "Lỗi khi thêm vào giỏ");
                return false;
            }
        },
        [isAuthenticated, addItem, syncCartFromBackend]
    );

    /**
     * Buy Now: Add to cart and redirect to checkout
     */
    const buyNow = useCallback(
        async (product: ProductCardResponse) => {
            const success = await addToCart(product);
            if (success) {
                router.push("/checkout");
            }
        },
        [addToCart, router]
    );

    /**
     * Buy Now with custom details (for product detail page)
     */
    const buyNowWithDetails = useCallback(
        async (details: CartItemDetails) => {
            const success = await addToCartWithDetails(details);
            if (success) {
                router.push("/checkout");
            }
        },
        [addToCartWithDetails, router]
    );

    return { addToCart, addToCartWithDetails, buyNow, buyNowWithDetails };
}

