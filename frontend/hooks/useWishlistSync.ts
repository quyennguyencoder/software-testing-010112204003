"use client";

import { useEffect, useRef } from "react";
import { useAuth } from "@/lib/auth-context";
import { useWishlistStore } from "@/store/wishlistStore";

/**
 * Hook to sync wishlist when user logs in or logs out
 * This ensures each user has their own separate wishlist
 */
export function useWishlistSync() {
  const { user } = useAuth();
  const userId = user?.id;
  
  const { loadUserWishlist } = useWishlistStore();
  const prevUserIdRef = useRef<number | string | undefined>(undefined);
  const initializedRef = useRef(false);

  useEffect(() => {
    // Determine current identifier
    const currentIdentifier = userId ? `user-${userId}` : 'guest';
    
    // Skip if same identifier and already initialized
    if (prevUserIdRef.current === currentIdentifier && initializedRef.current) {
      return;
    }

    // Update refs
    prevUserIdRef.current = currentIdentifier;
    initializedRef.current = true;

    // Load wishlist for current user/guest
    loadUserWishlist();
  }, [userId, loadUserWishlist]);

  // Handle storage events for cross-tab sync
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key?.startsWith('wishlist-')) {
        // Reload wishlist if it's the current user's key
        loadUserWishlist();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [loadUserWishlist]);
}

/**
 * Client component to mount the wishlist sync hook
 */
export function WishlistSyncClient() {
  useWishlistSync();
  return null;
}
