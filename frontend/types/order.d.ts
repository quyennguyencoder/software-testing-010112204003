/**
 * Order types matching backend DTOs
 */

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "SHIPPING"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: number;
  orderCode: string;
  userId?: number;
  email: string;
  recipientName: string;
  phoneNumber: string;
  shippingAddress: string;
  shippingFee?: number;
  shippingUnit?: string;
  note?: string;
  status: OrderStatus;
  paymentMethod: string;
  totalAmount: number;
  promotionId?: string;
  createdAt: string;
  updatedAt: string;
  items?: OrderItem[];
  // Frontend computed fields
  customer?: string;
  total?: number;
  date?: string;
  itemCount?: number;
}

// Admin order list response (mapping từ AdminOrderListResponse backend)
export interface AdminOrderListResponse {
  id: number;
  orderCode: string;
  customerId?: number | null;
  customerName?: string | null;
  customerEmail?: string | null;
  customerPhone?: string | null;
  recipientName: string;
  recipientPhone: string;
  status: OrderStatus;
  statusDisplay: string;
  paymentMethod: PaymentMethod;
  totalAmount: number;
  shippingFee: number;
  shippingAddress: string;
  createdAt: string;
  updatedAt: string;
  itemCount: number;
  note?: string | null;
}

export interface OrderResponse {
  id: number;
  orderCode: string;
  userId?: number;
  email: string;
  recipientName: string;
  phoneNumber: string;
  shippingAddress: string;
  shippingFee?: number;
  shippingUnit?: string;
  note?: string;
  status: OrderStatus;
  paymentMethod: string;
  totalAmount: number;
  promotionId?: string;
  createdAt: string;
  updatedAt: string;
  items?: OrderItem[];
}

export interface RecentOrderResponse {
  orderId: number;
  orderCode: string;
  customerName: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
}

/**
 * Create Order Request & Response types
 */
export type PaymentMethod = "COD" | "BANK_TRANSFER" | "VNPAY";

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  email: string;
  recipientName: string;
  phoneNumber: string;
  shippingAddress: string;
  shippingFee?: number;
  shippingUnit?: string;
  note?: string;
  paymentMethod: PaymentMethod;
  promotionId?: string; // UUID cho DISCOUNT/VOUCHER promotion
  freeshippingPromotionId?: string; // UUID cho FREESHIP promotion
  items: OrderItemRequest[];
}

export interface CreateOrderResponse {
  orderId: number;
  orderCode: string;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  totalAmount: number;
  createdAt: string;
  message?: string;
  paymentUrl?: string; // For VNPay payment
}

// Admin order detail response (mapping từ AdminOrderDetailResponse backend)
export interface AdminOrderItemDto {
  id: number;
  productId: number;
  productName: string;
  productThumbnail?: string;
  quantity: number;
  price: number;
  totalPrice: number;
  createdAt: string;
}

export interface AdminOrderDetailResponse {
  id: number;
  orderCode: string;
  // Customer Information
  customerId?: number | null;
  customerName: string;
  customerEmail: string;
  customerPhone?: string | null;
  // Recipient Information
  recipientName: string;
  recipientPhone: string;
  shippingAddress: string;
  // Order Details
  status: OrderStatus;
  statusDisplay: string;
  paymentMethod: PaymentMethod;
  totalAmount: number;
  shippingFee: number;
  shippingUnit?: string;
  note?: string;
  // Items
  items: AdminOrderItemDto[];
  // Timestamps
  createdAt: string;
  updatedAt: string;
  // Admin specific fields
  availableStatusTransitions: OrderStatus[];
  adminNotes?: string;
}

