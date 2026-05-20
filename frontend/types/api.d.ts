/**
 * Generic API Response wrapper
 * Matches Backend's ApiResponse<T> structure
 */
export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
  timestamp?: string;
}

/**
 * API Error structure
 */
export interface ApiError {
  code: number;
  message: string;
  details?: string;
}
