import { User } from './user';

/**
 * Login request payload
 */
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

/**
 * Login response from Backend
 */
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

/**
 * Register request payload
 */
export interface RegisterRequest {
  username: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  dateOfBirth?: string;
  password: string;
  confirmPassword: string;
}

/**
 * Forgot password request
 */
export interface ForgotPasswordRequest {
  email: string;
}

/**
 * Verify OTP and reset password request
 */
export interface VerifyOtpRequest {
  email: string;
  otp: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Refresh token request
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}
