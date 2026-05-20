/**
 * Utility functions for formatting data
 */

/**
 * Format price to Vietnamese currency format
 * @param price - Price in VND
 * @returns Formatted price string (e.g., "1.234.567₫")
 */
export const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('vi-VN').format(price) + '₫';
};

/**
 * Format date to Vietnamese format
 * @param date - Date string or Date object
 * @returns Formatted date string (e.g., "28/11/2025")
 */
export const formatDate = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return new Intl.DateTimeFormat('vi-VN').format(dateObj);
};

/**
 * Format date with time
 * @param date - Date string or Date object
 * @returns Formatted datetime string (e.g., "28/11/2025 14:30")
 */
export const formatDateTime = (date: string | Date): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(dateObj);
};

/**
 * Format number with thousand separators
 * @param num - Number to format
 * @returns Formatted number string (e.g., "1.234.567")
 */
export const formatNumber = (num: number): string => {
  return new Intl.NumberFormat('vi-VN').format(num);
};

/**
 * Truncate text with ellipsis
 * @param text - Text to truncate
 * @param maxLength - Maximum length before truncation
 * @returns Truncated text
 */
export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
};
