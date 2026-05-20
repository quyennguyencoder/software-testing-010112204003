import { useState, useCallback } from 'react';
import { chatbotAssistantService } from '@/services/chatbot-assistant.service';
import {
  ChatbotAssistantUserRequest,
  ChatbotAssistantUserResponse,
  ChatMessage,
} from '@/types/chatbot-assistant.d';

/**
 * Hook quản lý chatbot tư vấn sản phẩm
 * State: lịch sử chat, loading, error
 */
export const useChatbotAssistant = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Gửi câu hỏi tới chatbot
   */
  const sendMessage = useCallback(
    async (request: ChatbotAssistantUserRequest) => {
      setLoading(true);
      setError(null);

      try {
        // Thêm message người dùng vào lịch sử
        const userMessage: ChatMessage = {
          id: Date.now().toString(),
          type: 'user',
          content: request.message,
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, userMessage]);

        // Gọi API
        const response = await chatbotAssistantService.chat(request);

        // Thêm phản hồi của chatbot
        const assistantMessage: ChatMessage = {
          id: (Date.now() + 1).toString(),
          type: 'assistant',
          content: response.aiResponse,
          timestamp: new Date(),
          response,
        };
        setMessages((prev) => [...prev, assistantMessage]);

        return response;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Lỗi không xác định';
        setError(errorMessage);
        console.error('❌ Lỗi trong useChatbotAssistant:', err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  /**
   * Xóa lịch sử chat
   */
  const clearChat = useCallback(() => {
    setMessages([]);
    setError(null);
  }, []);

  /**
   * Xóa cache (admin)
   */
  const clearCache = useCallback(async () => {
    try {
      await chatbotAssistantService.clearCache();
      console.log('✅ Cache đã xóa');
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Lỗi xóa cache';
      setError(errorMessage);
      console.error('❌ Lỗi xóa cache:', err);
    }
  }, []);

  return {
    messages,
    loading,
    error,
    sendMessage,
    clearChat,
    clearCache,
  };
};
