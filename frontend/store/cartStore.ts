import { create } from 'zustand';
import type { CartItem, CartState } from '@/types';
import { calculateCartTotals } from '@/lib/utils/cartMapper';

/**
 * Cart Store using Zustand
 * Guest cart is NOT persisted. (Reload/new tab => guest cart lost)
 */
export const useCartStore = create<CartState>()(
  (set, get) => ({
    items: [],
    totalItems: 0,
    totalPrice: 0,
    guestCartId: undefined,

    setGuestCartId: (guestCartId) => set({ guestCartId }),
    clearGuestCartId: () => set({ guestCartId: undefined }),

    /**
     * Add item to cart or update quantity if exists
     */
    addItem: (item) => {
      const currentItems = get().items;
      const existingItemIndex = currentItems.findIndex(
        (i) => i.productId === item.productId && i.color === item.color && i.storage === item.storage
      );

      let newItems: CartItem[];

      if (existingItemIndex !== -1) {
        // Item exists, update quantity
        newItems = currentItems.map((i, index) =>
          index === existingItemIndex
            ? { ...i, quantity: i.quantity + item.quantity }
            : i
        );
      } else {
        // New item, add to cart
        const newId = currentItems.length > 0 ? Math.max(...currentItems.map(i => i.id)) + 1 : 1;
        newItems = [...currentItems, { ...item, id: newId }];
      }

      const totals = calculateCartTotals(newItems);
      set({
        items: newItems,
        ...totals,
      });
    },

    /**
     * Remove item from cart by id
     */
    removeItem: (id) => {
      const newItems = get().items.filter((item) => item.id !== id);
      const totals = calculateCartTotals(newItems);
      set({
        items: newItems,
        ...totals,
      });
    },

    /**
     * Update quantity of specific item
     */
    updateQuantity: (id, quantity) => {
      if (quantity <= 0) {
        get().removeItem(id);
        return;
      }

      const newItems = get().items.map((item) =>
        item.id === id ? { ...item, quantity } : item
      );

      const totals = calculateCartTotals(newItems);
      set({
        items: newItems,
        ...totals,
      });
    },

    /**
     * Clear all items from cart
     */
    clearCart: () => {
      const state = get();
      const isAlreadyEmpty =
        (state.items?.length ?? 0) === 0 &&
        (state.totalItems ?? 0) === 0 &&
        (state.totalPrice ?? 0) === 0 &&
        state.guestCartId === undefined;

      if (isAlreadyEmpty) return;

      set({
        items: [],
        totalItems: 0,
        totalPrice: 0,
        guestCartId: undefined,
      });
    },

    /**
     * Replace the entire items array (used to sync with backend)
     * Deduplicate by productId + color + storage and combine quantities
     */
    setItems: (items) => {
      // Normalize and dedupe items to avoid duplicates coming from backend
      const dedupeMap = new Map<string, CartItem>();
      const currentItems = get().items || [];

      const nextIdStart = currentItems.length > 0 ? Math.max(...currentItems.map(i => i.id)) + 1 : 1;
      let nextId = nextIdStart;

      for (const raw of items) {
        const item = raw as CartItem;
        const key = `${item.productId}::${item.color ?? ''}::${item.storage ?? ''}`;
        const existing = dedupeMap.get(key);
        if (existing) {
          existing.quantity += item.quantity;
          // prefer explicit appliedPrice if provided
          if (item.appliedPrice !== undefined) {
            existing.appliedPrice = item.appliedPrice;
          }
        } else {
          const idToUse = Number(item.id ?? nextId++);
          dedupeMap.set(key, { ...item, id: idToUse });
        }
      }

      const newItems = Array.from(dedupeMap.values());
      const totals = calculateCartTotals(newItems);

      set({
        items: newItems,
        ...totals,
      });
    },

    /**
     * Remove multiple items by id
     */
    removeItems: (ids) => {
      const newItems = get().items.filter((item) => !ids.includes(item.id));
      const totals = calculateCartTotals(newItems);
      set({
        items: newItems,
        ...totals,
      });
    },

    /**
     * Get item by id
     */
    getItemById: (id) => {
      return get().items.find((item) => item.id === id);
    },
  })
);
