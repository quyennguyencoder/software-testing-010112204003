/**
 * VNPay error code mappings
 * Reference: VNPay API Documentation
 */
export const VNPAY_ERROR_MESSAGES: Record<string, string> = {
  '07': 'Giao dịch bị nghi ngờ gian lận',
  '09': 'Thẻ chưa đăng ký Internet Banking',
  '10': 'Xác thực thông tin thẻ không chính xác',
  '11': 'Thẻ hết hạn hoặc chưa đến hạn sử dụng',
  '12': 'Thẻ bị khóa',
  '13': 'Sai mật khẩu xác thực giao dịch',
  '24': 'Giao dịch bị hủy',
  '51': 'Tài khoản không đủ số dư',
  '65': 'Tài khoản đã vượt quá hạn mức giao dịch',
  '75': 'Ngân hàng thanh toán đang bảo trì',
  '79': 'Giao dịch vượt quá số lần nhập sai mật khẩu',
  '99': 'Lỗi không xác định',
};

/**
 * Get VNPay error message by response code
 * @param code - VNPay response code
 * @returns Localized error message
 */
export function getVNPayErrorMessage(code: string | null): string {
  return VNPAY_ERROR_MESSAGES[code || ''] || 'Đã có lỗi xảy ra trong quá trình thanh toán';
}
