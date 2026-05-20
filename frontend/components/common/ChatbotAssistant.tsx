'use client';

import { useState, useRef, useEffect } from 'react';
import { useChatbotAssistant } from '@/hooks/useChatbotAssistant';
import { ChatbotAssistantUserRequest } from '@/types/chatbot-assistant.d';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, Send, Trash2, Sparkles, Zap, Clock, Target, ChevronLeft, ChevronRight, Star, TrendingUp, Package, Bot, MessageCircle, Hand, Crown, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import { ChatbotMarkdown } from './ChatbotMarkdown';
import { ChatbotProductList } from './ChatbotProductCard';

interface ChatbotAssistantProps {
  className?: string;
}

// Intent display mapping với colors
const INTENT_DISPLAY: Record<string, { label: string; icon: React.ReactNode; color: string }> = {
  FEATURED: { label: 'Nổi bật', icon: <Sparkles className="w-3 h-3" />, color: 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200' },
  BEST_SELLING: { label: 'Bán chạy', icon: <Zap className="w-3 h-3" />, color: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' },
  NEW_ARRIVALS: { label: 'Mới về', icon: <Sparkles className="w-3 h-3" />, color: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' },
  SEARCH: { label: 'Tìm kiếm', icon: <Target className="w-3 h-3" />, color: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' },
  FILTER_RAM: { label: 'Lọc RAM', icon: <Target className="w-3 h-3" />, color: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' },
  FILTER_STORAGE: { label: 'Lọc Storage', icon: <Target className="w-3 h-3" />, color: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200' },
  FILTER_BATTERY: { label: 'Pin trâu', icon: <Target className="w-3 h-3" />, color: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200' },
  FILTER_PRICE: { label: 'Lọc Giá', icon: <Target className="w-3 h-3" />, color: 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200' },
  FILTER_BRAND: { label: 'Theo hãng', icon: <Target className="w-3 h-3" />, color: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900 dark:text-cyan-200' },
  COMPARE: { label: 'So sánh', icon: <Target className="w-3 h-3" />, color: 'bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200' },
  CATEGORY: { label: 'Danh mục', icon: <Target className="w-3 h-3" />, color: 'bg-teal-100 text-teal-800 dark:bg-teal-900 dark:text-teal-200' },
  FILTER_OS: { label: 'Hệ điều hành', icon: <Target className="w-3 h-3" />, color: 'bg-slate-100 text-slate-800 dark:bg-slate-900 dark:text-slate-200' },
  RELATED: { label: 'Liên quan', icon: <Target className="w-3 h-3" />, color: 'bg-violet-100 text-violet-800 dark:bg-violet-900 dark:text-violet-200' },
  FILTER_CAMERA: { label: 'Camera đẹp', icon: <Target className="w-3 h-3" />, color: 'bg-rose-100 text-rose-800 dark:bg-rose-900 dark:text-rose-200' },
  FILTER_GAMING: { label: 'Gaming', icon: <Zap className="w-3 h-3" />, color: 'bg-fuchsia-100 text-fuchsia-800 dark:bg-fuchsia-900 dark:text-fuchsia-200' },
  FILTER_BUDGET: { label: 'Giá rẻ', icon: <Target className="w-3 h-3" />, color: 'bg-lime-100 text-lime-800 dark:bg-lime-900 dark:text-lime-200' },
  FILTER_FLAGSHIP: { label: 'Cao cấp', icon: <Sparkles className="w-3 h-3" />, color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' },
  FILTER_SCREEN: { label: 'Màn hình', icon: <Target className="w-3 h-3" />, color: 'bg-sky-100 text-sky-800 dark:bg-sky-900 dark:text-sky-200' },
  FILTER_RATING: { label: 'Đánh giá', icon: <Sparkles className="w-3 h-3" />, color: 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200' },
};

// Quick action categories for bottom bar - Prompts sát yêu cầu
const QUICK_CATEGORIES = [
  { id: 'featured', label: 'Nổi bật', icon: Star, prompt: 'Xem sản phẩm nổi bật', color: 'from-amber-500 to-orange-500' },
  { id: 'bestselling', label: 'Bán chạy', icon: TrendingUp, prompt: 'Xem sản phẩm bán chạy', color: 'from-red-500 to-pink-500' },
  { id: 'new', label: 'Mới về', icon: Sparkles, prompt: 'Xem sản phẩm mới về', color: 'from-green-500 to-emerald-500' },
  { id: 'budget', label: 'Giá rẻ', icon: Package, prompt: 'Xem điện thoại giá rẻ', color: 'from-blue-500 to-cyan-500' },
  { id: 'flagship', label: 'Cao cấp', icon: Crown, prompt: 'Xem điện thoại cao cấp', color: 'from-amber-500 to-orange-500' },
];


/**
 * Component Chatbot Tư Vấn Sản Phẩm - UI/UX Tối Ưu
 */
export const ChatbotAssistant: React.FC<ChatbotAssistantProps> = ({
  className,
}) => {
  const { messages, loading, error, sendMessage, clearChat } =
    useChatbotAssistant();
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const quickActionsRef = useRef<HTMLDivElement>(null);

  // Auto scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendChatRequest = async (message: string) => {
    if (!message.trim() || loading) return;

    const request: ChatbotAssistantUserRequest = {
      message: message.trim(),
    };

    try {
      await sendMessage(request);
      setInput('');
    } catch (err) {
      console.error('❌ Lỗi gửi message:', err);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    await sendChatRequest(input);
  };

  const handleQuickPrompt = async (prompt: string) => {
    await sendChatRequest(prompt);
  };

  const scrollQuickActions = (direction: 'left' | 'right') => {
    if (quickActionsRef.current) {
      const scrollAmount = 200;
      quickActionsRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth'
      });
    }
  };

  return (
    <div
      className={cn('flex flex-col h-full bg-card rounded-xl border shadow-xl overflow-hidden', className)}
    >
      {/* Header - Compact */}
      <CardHeader className="border-b py-3 px-4 bg-gradient-to-r from-primary/10 via-primary/5 to-transparent">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-lg">
                <Bot className="w-5 h-5 text-primary-foreground" />
              </div>
              <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 rounded-full border-2 border-background"></span>
            </div>
            <div>
              <CardTitle className="text-base font-bold">Trợ lý AI</CardTitle>
              <p className="text-xs text-muted-foreground">Online • Sẵn sàng hỗ trợ</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {messages.length > 0 && (
              <Button
                variant="ghost"
                size="icon"
                onClick={clearChat}
                className="h-8 w-8 hover:bg-destructive/10 hover:text-destructive"
              >
                <Trash2 className="w-4 h-4" />
              </Button>
            )}
          </div>
        </div>
      </CardHeader>

      {/* Chat Area - Tăng kích thước */}
      <div className="flex-1 overflow-y-auto p-4 md:p-6 space-y-4 bg-gradient-to-b from-muted/20 to-muted/40 min-h-[400px]">
        {messages.length === 0 ? (
          <div className="h-full flex items-center justify-center">
            <div className="max-w-md text-center space-y-6 p-4">
              {/* Avatar lớn hơn */}
              <div className="w-24 h-24 mx-auto rounded-2xl bg-gradient-to-br from-primary to-primary/60 flex items-center justify-center shadow-2xl animate-bounce">
                <Bot className="w-12 h-12 text-primary-foreground" />
              </div>
              
              {/* Welcome text */}
              <div className="space-y-2">
                <div className="flex items-center justify-center gap-2">
                  <h3 className="text-xl font-bold">Xin chào!</h3>
                  <Hand className="w-5 h-5 text-primary animate-wave" />
                </div>
                <p className="text-sm text-muted-foreground">
                  Tôi là trợ lý AI của UTE PhoneHub, sẵn sàng tư vấn điện thoại phù hợp với bạn!
                </p>
              </div>
              
              {/* Quick Access Links - thay thế gợi ý câu hỏi */}
             

              {/* Gợi ý nhỏ */}
              <p className="text-xs text-muted-foreground flex items-center justify-center gap-1">
                <MessageCircle className="w-3 h-3" />
                Hoặc nhập câu hỏi bên dưới để được tư vấn!
              </p>
            </div>
          </div>
        ) : (
          messages.map((message) => (
            <div
              key={message.id}
              className={cn(
                'flex gap-3 animate-in fade-in slide-in-from-bottom-2 duration-300',
                message.type === 'user' ? 'justify-end' : 'justify-start'
              )}
            >
              {message.type === 'assistant' && (
                <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-md">
                  <Bot className="w-4 h-4 text-primary-foreground" />
                </div>
              )}

              <div
                className={cn(
                  'max-w-[90%] md:max-w-2xl p-4 rounded-2xl text-sm shadow-sm',
                  message.type === 'user'
                    ? 'bg-primary text-primary-foreground rounded-br-sm'
                    : 'bg-background border rounded-bl-sm'
                )}
              >
                {message.type === 'assistant' ? (
                  <ChatbotMarkdown content={message.content} />
                ) : (
                  <p className="whitespace-pre-line">{message.content}</p>
                )}

                {/* Products */}
                {message.response?.recommendedProducts &&
                  message.response.recommendedProducts.length > 0 && (
                    <div className="mt-3 space-y-2 border-t border-border/50 pt-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold flex items-center gap-1">
                          <Package className="w-3 h-3" />
                          Gợi ý ({message.response.recommendedProducts.length})
                        </span>
                        {message.response.detectedIntent && (
                          <Badge 
                            className={cn(
                              'text-[10px] gap-1 px-2',
                              INTENT_DISPLAY[message.response.detectedIntent]?.color
                            )}
                          >
                            {INTENT_DISPLAY[message.response.detectedIntent]?.icon}
                            {INTENT_DISPLAY[message.response.detectedIntent]?.label}
                          </Badge>
                        )}
                      </div>
                      <ChatbotProductList 
                        products={message.response.recommendedProducts}
                        groupByBrand={message.response.recommendedProducts.length > 3}
                      />
                    </div>
                  )}

                {/* Metadata */}
                {message.response && (
                  <div className="mt-2 pt-2 border-t border-border/30 flex items-center gap-3 text-[10px] text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {message.response.processingTimeMs}ms
                    </span>
                    {message.response.relevanceScore > 0 && (
                      <span className="flex items-center gap-1">
                        <Target className="w-3 h-3" />
                        {(message.response.relevanceScore * 100).toFixed(0)}%
                      </span>
                    )}
                  </div>
                )}
              </div>

              {message.type === 'user' && (
                <div className="flex-shrink-0 w-8 h-8 rounded-full bg-muted flex items-center justify-center border">
                  <User className="w-4 h-4 text-muted-foreground" />
                </div>
              )}
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Error */}
      {error && (
        <div className="px-4 py-2 bg-destructive/10 text-destructive text-xs flex items-center gap-2">
          <span>❌ {error}</span>
        </div>
      )}

      {/* Quick Actions Bar - Scrollable */}
      <div className="border-t bg-muted/30 relative">
        <div className="flex items-center">
          <button
            onClick={() => scrollQuickActions('left')}
            className="absolute left-0 z-10 h-full px-1 bg-gradient-to-r from-muted to-transparent hover:from-muted/80"
          >
            <ChevronLeft className="w-4 h-4 text-muted-foreground" />
          </button>
          
          <div 
            ref={quickActionsRef}
            className="flex gap-2 overflow-x-auto scrollbar-hide py-2 px-8"
            style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
          >
            {QUICK_CATEGORIES.map((cat) => (
              <button
                key={cat.id}
                onClick={() => handleQuickPrompt(cat.prompt)}
                disabled={loading}
                className={cn(
                  "flex-shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium text-white shadow-sm transition-transform hover:scale-105 disabled:opacity-50",
                  `bg-gradient-to-r ${cat.color}`
                )}
              >
                <cat.icon className="w-3.5 h-3.5" />
                {cat.label}
              </button>
            ))}
          </div>

          <button
            onClick={() => scrollQuickActions('right')}
            className="absolute right-0 z-10 h-full px-1 bg-gradient-to-l from-muted to-transparent hover:from-muted/80"
          >
            <ChevronRight className="w-4 h-4 text-muted-foreground" />
          </button>
        </div>
      </div>

      {/* Input */}
      <div className="border-t bg-background p-3">
        <form onSubmit={handleSendMessage} className="flex gap-2">
          <Input
            type="text"
            placeholder="Nhập câu hỏi của bạn..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={loading}
            className="flex-1 rounded-full px-4"
          />
          <Button 
            type="submit" 
            disabled={loading || !input.trim()}
            size="icon"
            className="rounded-full w-10 h-10 bg-primary hover:bg-primary/90"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Send className="w-4 h-4" />
            )}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default ChatbotAssistant;
