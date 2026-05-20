/**
 * Validation utilities for forms
 */

export interface ValidationResult {
  isValid: boolean;
  error?: string;
}

/**
 * Validate email format
 */
export const validateEmail = (email: string): ValidationResult => {
  if (!email) {
    return { isValid: false, error: 'Email là bắt buộc' };
  }
  
  // Trim whitespace
  const trimmedEmail = email.trim();
  
  if (!trimmedEmail) {
    return { isValid: false, error: 'Email là bắt buộc' };
  }
  
  // Improved email regex: more permissive but still valid
  // Allows: user@domain.com, user.name@domain.co.uk, user+tag@domain.com
  const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
  
  if (!emailRegex.test(trimmedEmail)) {
    return { isValid: false, error: 'Email không hợp lệ' };
  }
  
  // Additional check: must have at least one dot after @
  const parts = trimmedEmail.split('@');
  if (parts.length !== 2 || !parts[1].includes('.')) {
    return { isValid: false, error: 'Email không hợp lệ' };
  }
  
  return { isValid: true };
};

/**
 * Validate password strength
 */
export const validatePassword = (password: string): ValidationResult => {
  if (!password) {
    return { isValid: false, error: 'Mật khẩu là bắt buộc' };
  }
  
  if (password.length < 8) {
    return { isValid: false, error: 'Mật khẩu phải có ít nhất 8 ký tự' };
  }
  
  if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) {
    return { isValid: false, error: 'Mật khẩu phải chứa chữ hoa, chữ thường và số' };
  }
  
  return { isValid: true };
};

/**
 * Check if a URL is a remote image (http/https)
 */
export const isRemoteImageUrl = (value: unknown): boolean => {
  if (typeof value !== 'string') return false;
  const trimmed = value.trim();
  return /^(https?:)?\.\/\//i.test(trimmed);
};

/**
 * Validate username format
 */
export const validateUsername = (username: string): ValidationResult => {
  if (!username) {
    return { isValid: false, error: 'Tên đăng nhập là bắt buộc' };
  }
  
  if (username.length < 3) {
    return { isValid: false, error: 'Tên đăng nhập phải có ít nhất 3 ký tự' };
  }
  
  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    return { isValid: false, error: 'Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới' };
  }
  
  return { isValid: true };
};

/**
 * Validate phone number (Vietnamese format)
 */
export const validatePhoneNumber = (phone: string): ValidationResult => {
  if (!phone) {
    return { isValid: false, error: 'Số điện thoại là bắt buộc' };
  }
  
  // Support Vietnamese phone numbers: 10-11 digits starting with 0
  // Covers both mobile (10 digits) and landline (10-11 digits)
  const phoneRegex = /^0\d{9,10}$/;
  if (!phoneRegex.test(phone.replace(/\s/g, ''))) {
    return { isValid: false, error: 'Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)' };
  }
  
  return { isValid: true };
};

/**
 * Validate required field
 */
export const validateRequired = (value: string, fieldName: string): ValidationResult => {
  if (!value || value.trim() === '') {
    return { isValid: false, error: `${fieldName} là bắt buộc` };
  }
  
  return { isValid: true };
};

/**
 * Validate password confirmation
 */
export const validatePasswordMatch = (password: string, confirmPassword: string): ValidationResult => {
  if (password !== confirmPassword) {
    return { isValid: false, error: 'Mật khẩu xác nhận không khớp' };
  }
  
  return { isValid: true };
};
