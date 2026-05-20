/**
 * User entity matching Backend User entity
 */
export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phoneNumber: string | null;
  gender: 'MALE' | 'FEMALE' | 'OTHER' | null;
  dateOfBirth: string | null;
  role: 'CUSTOMER' | 'ADMIN';
  status: 'ACTIVE' | 'LOCKED' | 'EMAIL_VERIFIED';
  createdAt?: string;
  updatedAt?: string;
}

/**
 * User profile update request
 */
export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  dateOfBirth?: string;
}

/**
 * Change password request
 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * Admin User Management Types
 */
export interface UsersPageResponse {
  users: User[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}

export interface CreateUserRequest {
  username?: string;
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  dateOfBirth?: string;
  role: 'CUSTOMER' | 'ADMIN';
}

export interface UserFilters {
  role?: 'CUSTOMER' | 'ADMIN' | 'ALL';
  status?: 'ACTIVE' | 'LOCKED' | 'EMAIL_VERIFIED' | 'ALL';
  keyword?: string;
}
