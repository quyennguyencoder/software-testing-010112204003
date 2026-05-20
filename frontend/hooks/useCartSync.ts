"use client";

import { useCallback, useEffect, useRef } from "react";
import { useAuth } from "@/lib/auth-context";
import { useCartStore } from "@/store";
import { cartAPI, guestCartAPI } from "@/lib/api";
import { mapBackendCartItems } from "@/lib/utils/cartMapper";
import { toast } from "sonner";

export function useCartSync() {
  const { user } = useAuth();
  const isAuthenticated = !!user;
  const {
    items,
    clearCart,
    setItems,
    guestCartId,
    setGuestCartId,
    clearGuestCartId,
  } = useCartStore();

  const wasAuthenticatedRef = useRef(isAuthenticated);

  // Use sessionStorage to persist merge status across page reloads
  const getMergeStatus = useCallback(() => {
    if (typeof window === "undefined") return false;
    return sessionStorage.getItem("cart-merged-session") === "true";
  }, []);

  const setMergeStatus = useCallback((status: boolean) => {
    if (typeof window === "undefined") return;
    if (status) {
      sessionStorage.setItem("cart-merged-session", "true");
    } else {
      sessionStorage.removeItem("cart-merged-session");
    }
  }, []);

  const showMergeNotification = useCallback(
    (guestItems: unknown[], backendItems: unknown[]) => {
      toast.info(
        `Đã gộp ${guestItems.length} sản phẩm chưa đăng nhập vào giỏ hàng (${backendItems.length} sản phẩm trước đó)`,
        {
          duration: 5000,
          closeButton: true,
        }
      );
    },
    []
  );

  const mergeGuestCartToBackend = useCallback(
    async (guestItems: unknown[], showSuccessToast: boolean) => {
      try {
        const guestCartItems = (Array.isArray(guestItems) ? guestItems : [])
          .map((it: any) => {
            const qty = Math.max(1, Math.min(10, Number(it.quantity ?? 1)));
            return {
              productId: Number(it.productId ?? 0),
              quantity: qty,
            };
          })
          .filter((it) => it.productId > 0);

        if (guestCartItems.length === 0) {
          // Fetch backend cart as fallback
          const resp = await cartAPI.getCurrentCart();
          if (resp?.success && resp.data?.items) {
            const mapped = mapBackendCartItems(resp.data.items);
            setItems(mapped);
          }
          return;
        }

        try {
          // Prefer Redis-based merge if this tab has a guestCartId; include items as fallback.
          await cartAPI.mergeGuestCart({ guestCartId, guestCartItems });
          if (showSuccessToast) {
            toast.success("Đã gộp giỏ hàng thành công");
          }
        } catch (mergeErr: any) {
          const msg = String(mergeErr?.message || mergeErr || "");

          // If per-product limit error, try adding items one by one
          if (msg.includes("Maximum") || msg.includes("10 items")) {
            await Promise.allSettled(
              guestCartItems.map((it) =>
                cartAPI.addToCart({
                  productId: it.productId,
                  quantity: it.quantity,
                })
              )
            );
            if (showSuccessToast) {
              toast.warning("Đã gộp giỏ hàng (một số sản phẩm đạt giới hạn)");
            }
          } else if (msg.includes("Row was updated") || msg.includes("stale")) {
            // Concurrency error - just refetch
            console.warn("Concurrency error during merge, refetching cart");
          } else {
            throw mergeErr;
          }
        }

        // Always fetch latest cart from backend after merge
        const finalResp = await cartAPI.getCurrentCart();
        if (finalResp?.success && finalResp.data?.items) {
          const mapped = mapBackendCartItems(finalResp.data.items);
          // Clear guest cart from this tab to prevent re-merge
          clearCart();
          clearGuestCartId();
          setItems(mapped);
        }
      } catch (error) {
        console.error("Failed to merge guest cart:", error);
        toast.error("Không thể gộp giỏ hàng, vui lòng thử lại");

        // Fallback: fetch backend cart
        try {
          const resp = await cartAPI.getCurrentCart();
          if (resp?.success && resp.data?.items) {
            const mapped = mapBackendCartItems(resp.data.items);
            clearCart();
            clearGuestCartId();
            setItems(mapped);
          }
        } catch (e) {
          console.error("Failed to fetch cart after merge error:", e);
        }
      }
    },
    [clearCart, clearGuestCartId, guestCartId, setItems]
  );

  const checkAndMergeCart = useCallback(async () => {
    try {
      // ADMIN users don't have carts - skip cart operations
      if (user?.role === "ADMIN") {
        clearCart();
        clearGuestCartId();
        return;
      }

      // Guest cart is in-memory only (no localStorage). If this tab has guest items, merge them.
      const guestItems: any[] =
        Array.isArray(items) && items.length > 0 ? items : [];

      // Fetch backend cart
      const backendResp = await cartAPI.getCurrentCart();
      
      // Debug logging
      if (process.env.NODE_ENV === 'development') {
        console.log('[useCartSync] Backend cart response:', backendResp);
      }
      
      const backendItems =
        backendResp?.success && backendResp.data && Array.isArray(backendResp.data.items)
          ? backendResp.data.items
          : [];
      
      if (process.env.NODE_ENV === 'development') {
        console.log('[useCartSync] Backend items:', backendItems);
        console.log('[useCartSync] Has guest items:', guestItems.length > 0);
        console.log('[useCartSync] Has backend items:', backendItems.length > 0);
      }

      const hasGuestItems = guestItems.length > 0;
      const hasBackendItems = backendItems.length > 0;

      if (!hasGuestItems) {
        // No guest cart, just sync with backend
        if (hasBackendItems) {
          const mapped = mapBackendCartItems(backendItems);
          
          if (process.env.NODE_ENV === 'development') {
            console.log('[useCartSync] Mapped items from backend:', mapped);
          }
          
          // Clear any stale guest cart data in this tab
          clearCart();
          clearGuestCartId();
          setItems(mapped);
          
          if (process.env.NODE_ENV === 'development') {
            console.log('[useCartSync] Cart items after setItems:', useCartStore.getState().items);
          }
        } else {
          // No backend items either, ensure cart is cleared
          if (process.env.NODE_ENV === 'development') {
            console.log('[useCartSync] No backend items, clearing cart');
          }
          clearCart();
          clearGuestCartId();
        }
        return;
      }

      if (!hasBackendItems) {
        // No backend cart, just merge guest cart silently
        await mergeGuestCartToBackend(guestItems, false);
        return;
      }

      // Both have items - show notification and merge
      showMergeNotification(guestItems, backendItems);
      await mergeGuestCartToBackend(guestItems, true);
    } catch (error) {
      console.error("Failed to check and merge cart:", error);
      // On error, still fetch backend cart as fallback
      try {
        const resp = await cartAPI.getCurrentCart();
        if (resp?.success && resp.data?.items) {
          const mapped = mapBackendCartItems(resp.data.items);
          setItems(mapped);
        }
      } catch (e) {
        console.error("Failed to fetch cart on merge error:", e);
      }
    }
  }, [
    clearCart,
    clearGuestCartId,
    items,
    mergeGuestCartToBackend,
    setItems,
    showMergeNotification,
  ]);

  useEffect(() => {
    const alreadyMerged = getMergeStatus();

    if (isAuthenticated && user) {
      // ADMIN users don't have carts
      if (user.role === "ADMIN") {
        clearCart();
        clearGuestCartId();
        return;
      }
      
      if (!alreadyMerged) {
        // First time login in this session - check and merge
        if (process.env.NODE_ENV === 'development') {
          console.log('[useCartSync] First login, checking and merging cart');
        }
        setMergeStatus(true);
        checkAndMergeCart();
      } else {
        // Already merged, but ensure cart is synced from backend
        // This handles case where user navigates to cart page after login
        if (process.env.NODE_ENV === 'development') {
          console.log('[useCartSync] Already merged, syncing cart from backend');
        }
        
        // Fetch and sync cart from backend (without merge)
        (async () => {
          try {
            const resp = await cartAPI.getCurrentCart();
            if (resp?.success && resp.data && Array.isArray(resp.data.items)) {
              const mapped = mapBackendCartItems(resp.data.items);
              setItems(mapped);
              
              if (process.env.NODE_ENV === 'development') {
                console.log('[useCartSync] Synced cart items:', mapped);
              }
            }
          } catch (e) {
            console.error('[useCartSync] Failed to sync cart:', e);
          }
        })();
      }
    } else if (!isAuthenticated) {
      // IMPORTANT: Do NOT clear the cart on every unauthenticated render.
      // Only clear when transitioning from authenticated -> unauthenticated (logout).
      if (wasAuthenticatedRef.current) {
        if (process.env.NODE_ENV === 'development') {
          console.log('[useCartSync] User logged out, clearing cart');
        }
        setMergeStatus(false);
        if ((Array.isArray(items) && items.length > 0) || guestCartId) {
          clearCart();
        }
      } else if (alreadyMerged) {
        // If we are already unauthenticated, ensure merge flag doesn't block future login merge.
        setMergeStatus(false);
      }
    }

    wasAuthenticatedRef.current = isAuthenticated;
  }, [
    isAuthenticated,
    user,
    getMergeStatus,
    setMergeStatus,
    clearCart,
    checkAndMergeCart,
    items,
    guestCartId,
    clearGuestCartId,
    setItems,
  ]);

  // Guest cart Redis sync: keep server-side guest cart updated during the current tab session.
  // IMPORTANT: guestCartId is in-memory only (no localStorage/sessionStorage) so reload/new tab loses it.
  useEffect(() => {
    if (isAuthenticated) return;
    if (typeof window === "undefined") return;

    let cancelled = false;
    const doSync = async () => {
      try {
        const currentItems = Array.isArray(items) ? items : [];

        // If cart is empty, cleanup guest cart id (and best-effort delete in Redis)
        if (currentItems.length === 0) {
          if (guestCartId) {
            try {
              await guestCartAPI.deleteGuestCart(guestCartId);
            } catch {
              // ignore
            }
            if (!cancelled) clearGuestCartId();
          }
          return;
        }

        // Ensure we have a guestCartId
        let id = guestCartId;
        if (!id) {
          const created = await guestCartAPI.createGuestCart();
          if (!created?.success || !created.data?.guestCartId) return;
          id = created.data.guestCartId;
          if (!cancelled) setGuestCartId(id);
        }

        // Replace items in Redis (compact payload: productId + quantity)
        const payloadItems = currentItems
          .map((it: any) => ({
            productId: Number(it.productId ?? 0),
            quantity: Math.max(1, Math.min(10, Number(it.quantity ?? 1))),
          }))
          .filter((it) => it.productId > 0);

        await guestCartAPI.replaceGuestCart(id, { items: payloadItems });
      } catch (e) {
        // Keep silent to avoid spamming toasts; guest flow still works locally.
        console.warn("Guest cart Redis sync failed:", e);
      }
    };

    // Debounce changes to avoid too many network calls
    const t = window.setTimeout(() => {
      if (!cancelled) void doSync();
    }, 250);

    return () => {
      cancelled = true;
      window.clearTimeout(t);
    };
  }, [isAuthenticated, items, guestCartId, setGuestCartId, clearGuestCartId]);

  return { syncLocalCartToBackend: checkAndMergeCart };
}

// A tiny client component that mounts the `useCartSync` hook.
export function CartSyncClient(): null {
  useCartSync();
  return null;
}
