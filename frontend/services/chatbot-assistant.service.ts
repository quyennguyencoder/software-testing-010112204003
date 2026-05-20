import fetchAPI from '@/lib/api';
import {
  ChatbotAssistantUserRequest,
  ChatbotAssistantUserResponse,
  ChatbotToggleRequest,
  ChatbotStatusResponse,
} from '@/types/chatbot-assistant.d';

/**
 * Service gọi API chatbot tư vấn sản phẩm
 */
export const chatbotAssistantService = {
  /**
   * Gửi câu hỏi tư vấn sản phẩm
   */
  async chat(
    request: ChatbotAssistantUserRequest
  ): Promise<ChatbotAssistantUserResponse> {
    try {
      const response = await fetchAPI<ChatbotAssistantUserResponse>(
        '/chatbot-assistant/chat',
        {
          method: 'POST',
          body: JSON.stringify(request),
        }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi gọi chatbot API:', error);
      throw error;
    }
  },

  /**
   * Xóa cache (admin only)
   */
  async clearCache(): Promise<string> {
    try {
      const response = await fetchAPI<string>(
        '/chatbot-assistant/clear-cache',
        {
          method: 'POST',
        }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi xóa cache:', error);
      throw error;
    }
  },

  // ==================== ADMIN APIs ====================

  /**
   * Lấy trạng thái chatbot (Admin only)
   */
  async getStatus(): Promise<ChatbotStatusResponse> {
    try {
      const response = await fetchAPI<ChatbotStatusResponse>(
        '/admin/chatbot/status',
        { method: 'GET' }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi lấy trạng thái chatbot:', error);
      throw error;
    }
  },

  /**
   * Bật chatbot (Admin only)
   */
  async enable(): Promise<ChatbotStatusResponse> {
    try {
      const response = await fetchAPI<ChatbotStatusResponse>(
        '/admin/chatbot/enable',
        { method: 'POST' }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi bật chatbot:', error);
      throw error;
    }
  },

  /**
   * Tắt chatbot (Admin only)
   */
  async disable(): Promise<ChatbotStatusResponse> {
    try {
      const response = await fetchAPI<ChatbotStatusResponse>(
        '/admin/chatbot/disable',
        { method: 'POST' }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi tắt chatbot:', error);
      throw error;
    }
  },

  /**
   * Toggle chatbot (Admin only)
   */
  async toggle(): Promise<ChatbotStatusResponse> {
    try {
      const response = await fetchAPI<ChatbotStatusResponse>(
        '/admin/chatbot/toggle',
        { method: 'POST' }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi toggle chatbot:', error);
      throw error;
    }
  },

  /**
   * Cập nhật cấu hình chatbot (Admin only)
   */
  async updateConfig(request: ChatbotToggleRequest): Promise<ChatbotStatusResponse> {
    try {
      const response = await fetchAPI<ChatbotStatusResponse>(
        '/admin/chatbot/config',
        {
          method: 'PUT',
          body: JSON.stringify(request),
        }
      );
      return response.data;
    } catch (error) {
      console.error('❌ Lỗi cập nhật cấu hình chatbot:', error);
      throw error;
    }
  },
};
