/**
 * Payment Module Type Definitions
 * Định nghĩa các types cho module thanh toán (M06)
 */

/**
 * Phương thức thanh toán
 * - COD: Thanh toán khi nhận hàng
 * - BANK_TRANSFER: Chuyển khoản ngân hàng
 * - VNPAY: Thanh toán qua VNPay
 */
export type PaymentMethod = 'COD' | 'BANK_TRANSFER' | 'VNPAY';

/**
 * Trạng thái thanh toán
 * - PENDING: Đang chờ xử lý
 * - SUCCESS: Thanh toán thành công
 * - FAILED: Thanh toán thất bại
 * - CANCELLED: Đã hủy
 */
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED';

/**
 * Payment Response - Thông tin thanh toán chi tiết
 * Mapping từ backend PaymentResponse.java
 */
export interface PaymentResponse {
  id: number;
  orderId: number;
  paymentMethod: PaymentMethod;
  provider?: string; // 'VNPAY' khi paymentMethod = VNPAY
  transactionId?: string;
  amount: number; // Số tiền thanh toán (VND)
  status: PaymentStatus;
  createdAt: string; // ISO timestamp
}

/**
 * VNPay Payment Response - Response khi tạo URL thanh toán VNPay
 * Mapping từ backend VNPayPaymentResponse.java
 */
export interface VNPayPaymentResponse {
  code: string; // "00" = success, other = error
  message: string;
  paymentUrl: string; // URL để redirect người dùng đến VNPay
}

/**
 * Create Payment Request - Request tạo thanh toán VNPay
 * Mapping từ backend CreatePaymentRequest.java
 */
export interface CreatePaymentRequest {
  orderId: number;
  amount: number; // Số tiền thanh toán (VND), minimum: 10,000
  orderInfo?: string; // Thông tin đơn hàng
  locale?: 'vn' | 'en'; // Ngôn ngữ (mặc định: vn)
}

/**
 * Payment History Response - Lịch sử thanh toán
 * Mapping từ backend PaymentHistoryResponse.java
 */
export interface PaymentHistoryResponse {
  payments: PaymentResponse[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * VNPay Callback Parameters
 * Các parameters VNPay trả về sau khi thanh toán
 */
export interface VNPayCallbackParams {
  vnp_TmnCode: string; // Mã website
  vnp_Amount: string; // Số tiền (x100)
  vnp_BankCode: string; // Mã ngân hàng
  vnp_BankTranNo?: string; // Mã giao dịch tại ngân hàng
  vnp_CardType?: string; // Loại thẻ
  vnp_OrderInfo: string; // Thông tin đơn hàng
  vnp_PayDate: string; // Thời gian thanh toán (yyyyMMddHHmmss)
  vnp_ResponseCode: string; // Mã phản hồi (00 = success)
  vnp_TxnRef: string; // Mã tham chiếu giao dịch
  vnp_TransactionNo?: string; // Mã giao dịch tại VNPay
  vnp_TransactionStatus?: string; // Trạng thái giao dịch
  vnp_SecureHash: string; // Chữ ký bảo mật
}

/**
 * Payment Method Option - Lựa chọn phương thức thanh toán
 */
export interface PaymentMethodOption {
  value: PaymentMethod;
  label: string;
  icon?: string;
  description?: string;
  disabled?: boolean;
}
