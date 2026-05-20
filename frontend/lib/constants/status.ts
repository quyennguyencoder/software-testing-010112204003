/**
 * Status configurations for orders
 */

export type OrderStatus = 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';

export interface StatusConfig {
  label: string;
  class: string;
}

export const ORDER_STATUS: Record<OrderStatus, StatusConfig> = {
  pending: {
    label: 'Chờ xác nhận',
    class: 'bg-secondary text-foreground/80 dark:bg-secondary text-foreground/80',
  },
  processing: {
    label: 'Đang xử lý',
    class: 'bg-primary/15 text-primary dark:bg-primary/20',
  },
  shipped: {
    label: 'Đang giao',
    class: 'bg-accent text-foreground dark:bg-accent/60',
  },
  delivered: {
    label: 'Đã giao',
    class: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  },
  cancelled: {
    label: 'Đã hủy',
    class: 'bg-destructive/15 text-destructive dark:bg-destructive/20',
  },
} as const;

/**
 * Status configurations for users
 */

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED';

export const USER_STATUS: Record<UserStatus, StatusConfig> = {
  ACTIVE: {
    label: 'Hoạt động',
    class: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  },
  INACTIVE: {
    label: 'Không hoạt động',
    class: 'bg-secondary text-foreground/80 dark:bg-secondary text-foreground/80',
  },
  BANNED: {
    label: 'Bị khóa',
    class: 'bg-destructive/15 text-destructive dark:bg-destructive/20',
  },
} as const;

/**
 * Helper function to get order status configuration
 * Handles both uppercase (backend) and lowercase (mock) status formats
 */
export const getOrderStatus = (status: string | OrderStatus): StatusConfig => {
  // Normalize status to lowercase for lookup
  const normalizedStatus = status.toLowerCase() as OrderStatus;
  
  // Map backend statuses to frontend statuses
  const statusMap: Record<string, OrderStatus> = {
    'pending': 'pending',
    'confirmed': 'processing',
    'shipping': 'shipped',
    'delivered': 'delivered',
    'cancelled': 'cancelled',
  };
  
  const mappedStatus = statusMap[normalizedStatus] || normalizedStatus;
  
  // Fallback to pending if status not found
  return ORDER_STATUS[mappedStatus] || ORDER_STATUS.pending;
};

/**
 * Helper function to get user status configuration
 */
export const getUserStatus = (status: UserStatus): StatusConfig => {
  return USER_STATUS[status];
};

/**
 * Status configurations for payments
 */

export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';

export const PAYMENT_STATUS: Record<PaymentStatus, StatusConfig> = {
  PENDING: {
    label: 'Chờ thanh toán',
    class: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300',
  },
  SUCCESS: {
    label: 'Thành công',
    class: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  },
  FAILED: {
    label: 'Thất bại',
    class: 'bg-destructive/15 text-destructive dark:bg-destructive/20',
  },
  CANCELLED: {
    label: 'Đã hủy',
    class: 'bg-secondary text-foreground/80 dark:bg-secondary text-foreground/80',
  },
} as const;

/**
 * Helper function to get payment status configuration
 */
export const getPaymentStatus = (status: string | PaymentStatus): StatusConfig => {
  const normalizedStatus = status.toUpperCase() as PaymentStatus;
  return PAYMENT_STATUS[normalizedStatus] || PAYMENT_STATUS.PENDING;
};

/**
 * Payment method labels
 */
export const PAYMENT_METHOD_LABELS: Record<string, string> = {
  COD: 'Tiền mặt',
  VNPAY: 'VNPay',
  BANK_TRANSFER: 'Chuyển khoản',
} as const;

/**
 * Helper function to get payment method label
 */
export const getPaymentMethodLabel = (method: string): string => {
  return PAYMENT_METHOD_LABELS[method] || method;
};
