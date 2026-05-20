/**
 * Wishlist item interface
 */
export interface WishlistItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  inStock: boolean;
}

/**
 * Wishlist state interface
 */
export interface WishlistState {
  items: WishlistItem[];
  totalItems: number;
  addItem: (item: Omit<WishlistItem, 'id'>) => void;
  removeItem: (id: number) => void;
  clearWishlist: () => void;
  isInWishlist: (productId: number) => boolean;
  toggleItem: (item: Omit<WishlistItem, 'id'>) => void;
}
