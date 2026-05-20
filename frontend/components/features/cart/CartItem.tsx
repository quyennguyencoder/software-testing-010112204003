import { useState, useCallback } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Trash2, Plus, Minus, Loader2 } from 'lucide-react';
import ConfirmDialog from '@/components/common/ConfirmDialog';
import Image from 'next/image';
import { cartAPI } from '@/lib/api';
import { toast } from 'sonner';
import { scheduleDelete, undoDelete } from '@/lib/undo';
import { useCartStore } from '@/store';
import { useAuth } from '@/hooks';
import type { CartItem as CartItemType } from '@/types';
import { mapBackendCartItems, getItemSubtotal } from '@/lib/utils/cartMapper';
import { formatPrice, isRemoteImageUrl } from '@/lib/utils';

interface CartItemProps {
  item: CartItemType;
  onUpdateQuantity: (id: number, quantity: number) => void;
  onRemove: (id: number) => void;
  selected?: boolean;
  onSelectChange?: (id: number, selected: boolean) => void;
}

export function CartItem({ item, onUpdateQuantity, onRemove, selected, onSelectChange }: CartItemProps) {
  const [isUpdating, setIsUpdating] = useState(false);
  const [showRemoveConfirm, setShowRemoveConfirm] = useState(false);
  const [imageError, setImageError] = useState(false);
  const { isAuthenticated } = useAuth();

  const isRemoteImage = isRemoteImageUrl(item.productImage);

  const isValidImageSrc = (src: unknown) => {
    if (!src || typeof src !== 'string') return false;
    const s = src.trim();
    const hasAllowedPrefix = /^(https?:\/\/|\/\/|\/|data:|blob:)/i.test(s);
    if (!hasAllowedPrefix) return false;
    // Reject non-ASCII (emoji, full-width)
    const isAscii = /^[\x00-\x7F]+$/.test(s);
    return isAscii;
  };

  const getErrorInfo = (e: unknown): { status?: number; message?: string; data?: unknown } => {
    if (!e || typeof e !== 'object') return { message: String(e) };
    const obj = e as Record<string, unknown>;
    return {
      status: typeof obj.status === 'number' ? obj.status : undefined,
      message: typeof obj.message === 'string' ? obj.message : undefined,
      data: obj.data,
    };
  };

  const refetchAndSyncCart = useCallback(async () => {
    try {
      const resp = await cartAPI.getCurrentCart();
      if (resp?.success && resp.data?.items) {
        const backendItems = Array.isArray(resp.data.items) ? resp.data.items : [];
        const mappedItems = mapBackendCartItems(backendItems);
        useCartStore.getState().setItems(mappedItems);
      }
    } catch (e) {
      console.error('Failed to refetch cart:', e);
    }
  }, []);

  const handleQuantityChange = async (newQuantity: number) => {
    if (newQuantity < 1 || isUpdating) return;

    const previousQuantity = item.quantity;
    setIsUpdating(true);
    
    // Optimistic update
    onUpdateQuantity(item.id, newQuantity);

    if (!isAuthenticated) {
      toast.success(
        <div className="flex items-center gap-2">
          <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M20 6L9 17l-5-5" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          <span>Cập nhật số lượng thành công</span>
        </div>
      );
      setIsUpdating(false);
      return;
    }

    try {
      const response = await cartAPI.updateCartItem(item.id, newQuantity);
      if (response.success) {
        toast.success(
          <div className="flex items-center gap-2">
            <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M20 6L9 17l-5-5" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <span>Cập nhật số lượng thành công</span>
          </div>
        );
      } else {
        // Revert on failure
        onUpdateQuantity(item.id, previousQuantity);
        toast.error('Không thể cập nhật số lượng');
      }
    } catch (error: unknown) {
      console.error('Failed to update cart item:', error);
      const info = getErrorInfo(error);
      const message = (info.message || '').toLowerCase();

      // Revert optimistic update
      onUpdateQuantity(item.id, previousQuantity);

      // Handle specific errors
      if (
        info.status === 404 ||
        message.includes('không tồn tại') ||
        message.includes('not found') ||
        message.includes('does not exist')
      ) {
        useCartStore.getState().removeItem(item.id);
        toast.error('Sản phẩm không còn tồn tại — đã xóa khỏi giỏ hàng');
        setIsUpdating(false);
        return;
      }

      // Handle insufficient stock
      const availableStock = (info.data && typeof (info.data as Record<string, unknown>).availableStock === 'number') 
        ? (info.data as Record<string, unknown>).availableStock as number 
        : undefined;

      if (typeof availableStock === 'number') {
        const qty = Math.max(0, availableStock);
        if (qty === 0) {
          useCartStore.getState().removeItem(item.id);
          toast.error('Sản phẩm đã hết hàng — đã loại khỏi giỏ');
        } else {
          onUpdateQuantity(item.id, qty);
          toast.error(`Chỉ còn ${qty} sản phẩm trong kho`);
        }
        setIsUpdating(false);
        return;
      }

      // Handle conflict (optimistic locking)
      if (info.status === 409) {
        await refetchAndSyncCart();
        toast.error('Giỏ hàng đã thay đổi — đã tải lại trạng thái');
        setIsUpdating(false);
        return;
      }

      toast.error(info.message || 'Lỗi khi cập nhật số lượng');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleRemove = useCallback(() => {
    if (isUpdating) return;
    
    // Optimistic remove
    onRemove(item.id);

    // Schedule finalize
    scheduleDelete(
      item.id,
      async () => {
        if (!isAuthenticated) return;
        try {
          const response = await cartAPI.removeCartItem(item.id);
          if (!response?.success) {
            throw new Error(response?.message || 'Không thể xóa sản phẩm');
          }
        } catch (e: unknown) {
          const info = getErrorInfo(e);

          // If conflict, refetch
          if (info.status === 409) {
            await refetchAndSyncCart();
            toast.error('Giỏ hàng đã thay đổi — đã tải lại trạng thái');
          }

          throw e;
        }
      },
      // Restore function
      () => {
        const current = useCartStore.getState().items;
        useCartStore.getState().setItems([item, ...current]);
      }
    );

    // Show undo toast
    toast.success(
      <div className="flex items-center gap-3">
        <span>Đã xóa sản phẩm</span>
        <button className="underline ml-2 text-sm" onClick={() => undoDelete(item.id)}>
          Hoàn tác
        </button>
      </div>
    );
  }, [item, isUpdating, isAuthenticated, onRemove, refetchAndSyncCart]);

  return (
    <Card className="mb-4 shadow-sm hover:shadow-md transition-all duration-200 border-l-4 border-l-transparent hover:border-l-primary">
      <CardContent className="p-6 bg-gradient-to-r from-white to-gray-50/30">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:gap-6">
          <div className="flex items-center gap-4 sm:gap-6">
            <div>
              <input
                type="checkbox"
                checked={!!(typeof selected !== 'undefined' ? selected : false)}
                onChange={(e) => onSelectChange && onSelectChange(item.id, e.target.checked)}
                className="h-4 w-4 text-primary border-gray-300 rounded"
                aria-label={`Chọn sản phẩm ${item.productName}`}
              />
            </div>

            <div className="relative w-24 h-24 sm:w-28 sm:h-28 rounded-xl overflow-hidden bg-gradient-to-br from-gray-100 to-gray-200 flex-shrink-0 flex items-center justify-center shadow-sm border border-gray-200 p-2">
            {isValidImageSrc(item.productImage) && !imageError ? (
              <Image
                src={item.productImage}
                alt={item.productName}
                fill
                className="object-contain transition-transform hover:scale-105 duration-200"
                onError={() => setImageError(true)}
                loading="lazy"
                sizes="(max-width: 640px) 96px, 112px"
              />
            ) : (
              <div className="w-full h-full bg-gradient-to-br from-gray-200 to-gray-300 flex items-center justify-center">
                <svg className="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                </svg>
              </div>
            )}
            </div>
          </div>

          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-lg text-gray-900 mb-2 line-clamp-2 hover:text-primary transition-colors cursor-pointer">
              {item.productName}
            </h3>

            <div className="flex flex-wrap gap-2 mb-4">
              {item.color && (
                <Badge variant="secondary" className="text-xs bg-gradient-to-r from-blue-50 to-blue-100 text-blue-700 border-blue-200">
                  <span className="inline-block w-3 h-3 rounded-full border-2 border-blue-400 mr-1.5" style={{backgroundColor: item.color.toLowerCase()}}></span>
                  {item.color}
                </Badge>
              )}
              {item.storage && (
                <Badge variant="secondary" className="text-xs bg-gradient-to-r from-purple-50 to-purple-100 text-purple-700 border-purple-200">
                  <svg className="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                  </svg>
                  {item.storage}
                </Badge>
              )}
            </div>

            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center justify-between sm:justify-start sm:gap-3">
                <div className="flex items-center border-2 border-gray-200 rounded-lg shadow-sm hover:border-primary transition-colors">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      if (isUpdating) return;
                      if (item.quantity <= 1) {
                        // show confirm to remove item instead of decrementing to 0
                        setShowRemoveConfirm(true);
                        return;
                      }
                      handleQuantityChange(item.quantity - 1);
                    }}
                    disabled={isUpdating}
                    className="h-9 w-9 p-0 hover:bg-primary/10 hover:text-primary transition-colors"
                  >
                    <Minus className="h-4 w-4" />
                  </Button>
                  <span className="w-14 text-center text-sm font-semibold">
                    {isUpdating ? (
                      <Loader2 className="h-4 w-4 animate-spin mx-auto" />
                    ) : (
                      item.quantity
                    )}
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleQuantityChange(item.quantity + 1)}
                    disabled={isUpdating}
                    className="h-9 w-9 p-0 hover:bg-primary/10 hover:text-primary transition-colors"
                  >
                    <Plus className="h-4 w-4" />
                  </Button>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleRemove}
                  disabled={isUpdating}
                  className="text-red-500 hover:text-red-700 hover:bg-red-50 h-9 w-9 p-0 rounded-lg transition-colors"
                >
                  {isUpdating ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Trash2 className="h-4 w-4" />
                  )}
                </Button>
              </div>

              <div className="text-right self-end sm:self-auto">
                <div className="text-xl font-bold text-primary">
                  {getItemSubtotal(item).toLocaleString('vi-VN')}₫
                </div>
                {item.appliedPrice && item.appliedPrice !== item.price ? (
                  <div className="text-sm text-gray-500 space-y-0.5">
                    <div className="line-through text-xs text-gray-400">{item.price.toLocaleString('vi-VN')}₫</div>
                    <div className="text-xs text-red-600 font-medium bg-red-50 px-2 py-0.5 rounded inline-block">
                      {item.appliedPrice.toLocaleString('vi-VN')}₫ × {item.quantity}
                    </div>
                  </div>
                ) : item.quantity > 1 ? (
                  <div className="text-sm text-gray-500 mt-1">
                    {item.price.toLocaleString('vi-VN')}₫ × {item.quantity}
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </div>
      </CardContent>
      {/* Confirm dialog for removing when decrementing from 1 */}
      <ConfirmDialog
        open={showRemoveConfirm}
        title="Xóa sản phẩm"
        description="Bạn chắc chắn muốn xóa sản phẩm này?"
        confirmLabel="Xóa"
        cancelLabel="Hủy"
        intent="danger"
        onConfirm={() => {
          setShowRemoveConfirm(false);
          handleRemove();
        }}
        onClose={() => setShowRemoveConfirm(false)}
      />
    </Card>
  );
}