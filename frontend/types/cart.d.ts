/**
 * Cart item interface
 */
export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  /** Percentage discount applied to this item (e.g. 10 for 10%) */
  discountPercent?: number;
  /** Final unit price after discount; if present used for totals */
  appliedPrice?: number;
  quantity: number;
  color?: string;
  storage?: string;
}

/**
 * Cart state interface
 */
export interface CartState {
  items: CartItem[];
  totalItems: number;
  totalPrice: number;

  /**
   * In-memory guest cart id used to sync guest cart to Redis.
   * Not persisted (reload/new tab => lost).
   */
  guestCartId?: string;

  setGuestCartId: (guestCartId: string) => void;
  clearGuestCartId: () => void;

  addItem: (item: Omit<CartItem, 'id'>) => void;
  removeItem: (id: number) => void;
  updateQuantity: (id: number, quantity: number) => void;
  clearCart: () => void;
  getItemById: (id: number) => CartItem | undefined;
  /**
   * Replace the entire cart items (used when syncing with backend)
   */
  setItems: (items: CartItem[]) => void;
  /**
   * Remove multiple items by id
   */
  removeItems: (ids: number[]) => void;
}
