import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { WishlistItem, WishlistState } from '@/types';

// Generate or get existing guest ID (unique per browser)
const getOrCreateGuestId = (): string => {
  if (typeof window === 'undefined') return 'ssr-guest';
  
  const GUEST_ID_KEY = 'wishlist-guest-id';
  let guestId = localStorage.getItem(GUEST_ID_KEY);
  
  if (!guestId) {
    // Generate a unique ID for this browser/device
    guestId = `guest-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
    localStorage.setItem(GUEST_ID_KEY, guestId);
  }
  
  return guestId;
};

// Helper to get current user ID from localStorage
const getCurrentUserId = (): string => {
  if (typeof window === 'undefined') return 'ssr-guest';
  
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      if (user && user.id) {
        return `user-${user.id}`;
      }
    }
  } catch {
    // Ignore parsing errors
  }
  
  // Return unique guest ID for this browser
  return getOrCreateGuestId();
};

// Get storage key based on user
const getStorageKey = (): string => {
  const identifier = getCurrentUserId();
  return `wishlist-${identifier}`;
};

// Extended state interface with user management
interface WishlistStateExtended extends WishlistState {
  currentIdentifier: string;
  loadUserWishlist: () => void;
}

/**
 * Wishlist Store using Zustand
 * - Each user (guest or logged in) has their own wishlist
 * - Guest users are identified by a unique browser ID
 * - Logged in users are identified by their user ID
 * - All data stored in localStorage (client-side only, no server load)
 */
export const useWishlistStore = create<WishlistStateExtended>()(
  persist(
    (set, get) => ({
      items: [],
      totalItems: 0,
      currentIdentifier: '',

      /**
       * Load wishlist for current user/guest
       */
      loadUserWishlist: () => {
        const identifier = getCurrentUserId();
        const storageKey = `wishlist-${identifier}`;
        
        // If same identifier, no need to reload
        if (get().currentIdentifier === identifier) {
          return;
        }
        
        try {
          const stored = localStorage.getItem(storageKey);
          if (stored) {
            const data = JSON.parse(stored);
            if (data && data.state && Array.isArray(data.state.items)) {
              set({
                items: data.state.items,
                totalItems: data.state.items.length,
                currentIdentifier: identifier,
              });
              return;
            }
          }
        } catch {
          // Ignore parsing errors
        }
        
        // No stored wishlist, start fresh
        set({
          items: [],
          totalItems: 0,
          currentIdentifier: identifier,
        });
      },

      /**
       * Add item to wishlist
       */
      addItem: (item) => {
        const currentItems = get().items;
        const exists = currentItems.some((i) => i.productId === item.productId);

        if (!exists) {
          const newId = currentItems.length > 0 ? Math.max(...currentItems.map(i => i.id)) + 1 : 1;
          const newItems = [...currentItems, { ...item, id: newId }];
          
          set({
            items: newItems,
            totalItems: newItems.length,
          });
        }
      },

      /**
       * Remove item from wishlist by id
       */
      removeItem: (id) => {
        const newItems = get().items.filter((item) => item.id !== id);
        set({
          items: newItems,
          totalItems: newItems.length,
        });
      },

      /**
       * Clear all items from wishlist
       */
      clearWishlist: () => {
        set({
          items: [],
          totalItems: 0,
        });
      },

      /**
       * Check if product is in wishlist
       */
      isInWishlist: (productId) => {
        return get().items.some((item) => item.productId === productId);
      },

      /**
       * Toggle item in wishlist (add if not exists, remove if exists)
       */
      toggleItem: (item) => {
        const currentItems = get().items;
        const existingItem = currentItems.find((i) => i.productId === item.productId);

        if (existingItem) {
          // Remove if exists
          get().removeItem(existingItem.id);
        } else {
          // Add if not exists
          get().addItem(item);
        }
      },
    }),
    {
      name: 'wishlist-default', // Default key, will be updated dynamically
      storage: createJSONStorage(() => ({
        getItem: () => {
          if (typeof window === 'undefined') return null;
          const key = getStorageKey();
          return localStorage.getItem(key);
        },
        setItem: (_name: string, value: string) => {
          if (typeof window === 'undefined') return;
          const key = getStorageKey();
          localStorage.setItem(key, value);
        },
        removeItem: () => {
          if (typeof window === 'undefined') return;
          const key = getStorageKey();
          localStorage.removeItem(key);
        },
      })),
    }
  )
);
