/**
 * Types for Cart API responses
 */
export interface CartItemResponse {
  id: number;
  productId: number;
  productName?: string;
  productImage?: string;
  price?: number;
  quantity: number;
  color?: string;
  storage?: string;
  [key: string]: any;
}

export interface CartResponseData {
  items: CartItemResponse[];
  totalItems?: number;
  totalPrice?: number;
  [key: string]: any;
}

/**
 * Types for Promotion API
 */
export interface Promotion {
  id?: string | number;
  code?: string;
  templateCode?: string;
  title?: string;
  name?: string;
  description?: string;
  status?: string;
  effectiveDate?: string;
  expirationDate?: string;
  percent_discount?: number;
  percentDiscount?: number;
  fixed_amount?: number;
  fixedAmount?: number;
  max_discount?: number;
  maxDiscount?: number;
  min_value_to_be_applied?: number;
  minValueToBeApplied?: number;
  template?: any;
  [key: string]: any;
}
