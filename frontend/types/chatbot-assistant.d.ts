/**
 * Types cho Chatbot Tư Vấn Sản Phẩm
 */

/**
 * Request để gửi câu hỏi tới chatbot
 */
export interface ChatbotAssistantUserRequest {
  /**
   * Câu hỏi/yêu cầu từ khách hàng
   */
  message: string;

  /**
   * ID sản phẩm để lấy sản phẩm liên quan (tùy chọn)
   */
  productId?: number;

  /**
   * ID danh mục (tùy chọn)
   */
  categoryId?: number;

  /**
   * Giá min (tùy chọn)
   */
  minPrice?: number;

  /**
   * Giá max (tùy chọn)
   */
  maxPrice?: number;

  /**
   * Sắp xếp theo: RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST, BEST_SELLING
   */
  sortBy?: string;
}

/**
 * Sản phẩm được gợi ý - đầy đủ thông tin cho Product Card
 */
export interface RecommendedProductDTO {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  imageUrl: string;
  categoryName: string;
  matchScore?: number;
  reason?: string;
  productUrl?: string;
  
  // Technical specs
  ram?: string;
  storage?: string;
  batteryCapacity?: number;
  operatingSystem?: string;
  brandName?: string;
  
  // Discount info
  discountPercent?: number;
  hasDiscount?: boolean;
  
  // Sales info
  soldCount?: number;
  inStock?: boolean;
}

/**
 * Response từ chatbot
 */
export interface ChatbotAssistantUserResponse {
  /**
   * Phản hồi lời tư vấn từ AI
   */
  aiResponse: string;

  /**
   * Danh sách sản phẩm được gợi ý (max 5)
   */
  recommendedProducts: RecommendedProductDTO[];

  /**
   * Intent phát hiện: FEATURED, BEST_SELLING, NEW_ARRIVALS, SEARCH, CATEGORY, COMPARE
   */
  detectedIntent: string;

  /**
   * Điểm độ phù hợp (0-1)
   */
  relevanceScore: number;

  /**
   * Thời gian xử lý (ms)
   */
  processingTimeMs: number;
}

/**
 * Message trong chat history
 */
export interface ChatMessage {
  id: string;
  type: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  response?: ChatbotAssistantUserResponse;
}

// ==================== ADMIN TYPES ====================

/**
 * Request để bật/tắt chatbot (Admin only)
 */
export interface ChatbotToggleRequest {
  /**
   * Trạng thái mới của chatbot (true = bật, false = tắt)
   */
  enabled: boolean;
  
  /**
   * Lý do thay đổi (tùy chọn)
   */
  reason?: string;
}

/**
 * Response trạng thái chatbot
 */
export interface ChatbotStatusResponse {
  /**
   * Chatbot đang bật hay tắt
   */
  enabled: boolean;
  
  /**
   * Trạng thái hiển thị (BẬT/TẮT)
   */
  status: string;
  
  /**
   * Admin đã cập nhật lần cuối
   */
  updatedBy?: string;
  
  /**
   * Thời gian cập nhật lần cuối
   */
  updatedAt?: string;
  
  /**
   * Mô tả
   */
  description?: string;
  
  /**
   * Thông báo cho admin
   */
  message?: string;
}
