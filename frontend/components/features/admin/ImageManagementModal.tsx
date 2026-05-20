'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { X, Trash2, Loader2, Star, Upload, Plus, Image as ImageIcon } from 'lucide-react';
import { Product, ProductImage } from '@/types/product';
import { productAPI } from '@/lib/api';
import { toast } from 'sonner';
import ConfirmDialog from '@/components/common/ConfirmDialog';

interface ImageManagementModalProps {
  product: Product;
  onClose: () => void;
}

export function ImageManagementModal({ product, onClose }: ImageManagementModalProps) {
  const [images, setImages] = useState<ProductImage[]>([]);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState<number | null>(null);
  const [uploading, setUploading] = useState(false);
  
  // Confirm dialog state
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    description: string;
    onConfirm: () => void;
  }>({ open: false, title: '', description: '', onConfirm: () => {} });
  
  // Form state for adding new image
  const [showAddForm, setShowAddForm] = useState(false);
  const [newImageUrl, setNewImageUrl] = useState('');
  const [newImageAlt, setNewImageAlt] = useState('');
  const [newImageOrder, setNewImageOrder] = useState(0);
  const [isPrimary, setIsPrimary] = useState(false);

  useEffect(() => {
    // Use images from product prop (already loaded from API with @EntityGraph)
    if (product.images && Array.isArray(product.images)) {
      console.log('✅ Using images from product prop:', product.images.length);
      setImages(product.images as any[]);
      setNewImageOrder(product.images.length);
      setIsPrimary(product.images.length === 0);
    } else {
      console.log('ℹ️ No images in product prop');
      setImages([]);
      setNewImageOrder(0);
      setIsPrimary(true);
    }
    setLoading(false);
  }, [product]);

  const handleDeleteImage = async (imageId: number) => {
    setConfirmDialog({
      open: true,
      title: 'Xóa hình ảnh',
      description: 'Bạn có chắc muốn xóa hình ảnh này?',
      onConfirm: async () => {
        try {
          setDeleting(imageId);
          console.log('🗑️ Deleting image:', imageId, 'from product:', product.id);
          
          const response = await productAPI.deleteImage(product.id, imageId);
          console.log('📥 Delete response:', response);
          
          if (response.success) {
            toast.success('Xóa hình ảnh thành công');
            setImages(images.filter(img => img.id !== imageId));
          } else {
            toast.error('Không thể xóa hình ảnh', {
              description: response.message || 'Vui lòng thử lại',
            });
          }
        } catch (error) {
          console.error('❌ Error deleting image:', error);
          toast.error('Lỗi khi xóa hình ảnh', {
            description: error instanceof Error ? error.message : 'Vui lòng thử lại',
          });
        } finally {
          setDeleting(null);
          setConfirmDialog({ ...confirmDialog, open: false });
        }
      },
    });
  };

  const handleAddImage = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newImageUrl.trim()) {
      toast.error('Vui lòng nhập URL hình ảnh');
      return;
    }

    try {
      setUploading(true);
      console.log('📤 Adding new image to product:', product.id);
      console.log('📦 Current images:', images);
      console.log('📦 Has primary image?', images.some(img => img.isPrimary));
      
      const newImage = {
        imageUrl: newImageUrl.trim(),
        altText: newImageAlt.trim() || undefined,
        imageOrder: newImageOrder,
        isPrimary: isPrimary,
      };
      
      console.log('📤 New image data:', newImage);
      
      // Backend validates: request must have exactly 1 isPrimary=true
      // So we need to send ALL images (existing + new)
      const allImages = [
        ...images.map(img => ({
          imageUrl: img.imageUrl,
          altText: img.altText,
          imageOrder: img.imageOrder,
          isPrimary: img.isPrimary,
        })),
        newImage
      ];
      
      console.log('📤 Sending all images (old + new):', allImages.length);
      console.log('📤 Primary images count:', allImages.filter(img => img.isPrimary).length);
      
      const requestBody = {
        images: allImages
      };
      
      console.log('📤 Request body:', JSON.stringify(requestBody, null, 2));
      
      const response = await productAPI.uploadImage(product.id, requestBody);
      console.log('📥 Upload response:', response);
      console.log('📥 Response success:', response.success);
      console.log('📥 Response data:', response.data);
      
      if (response.success) {
        toast.success('Thêm hình ảnh thành công!', {
          description: 'Modal sẽ đóng để tải lại dữ liệu.',
        });
        // Backend không trả data về, cần reload để lấy data mới
        onClose(); // Đóng modal → ProductImagesTable sẽ reload
      } else {
        console.error('❌ Upload failed:', response.message);
        toast.error('Không thể thêm hình ảnh', {
          description: response.message || 'Vui lòng thử lại',
        });
      }
    } catch (error) {
      console.error('❌ Error adding image:', error);
      toast.error('Lỗi khi thêm hình ảnh', {
        description: error instanceof Error ? error.message : 'Vui lòng thử lại',
      });
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h2 className="text-xl font-semibold">Quản lý hình ảnh sản phẩm</h2>
            <p className="text-sm text-muted-foreground mt-1">
              {product.name} (ID: {product.id})
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-muted rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {/* Thumbnail Section */}
          <div className="mb-6 p-4 border rounded-lg bg-blue-50 dark:bg-blue-950">
            <h3 className="font-semibold text-sm mb-3 flex items-center gap-2">
              <ImageIcon className="w-4 h-4" />
              Ảnh đại diện sản phẩm (Thumbnail)
            </h3>
            <div className="flex items-center gap-4">
                {'thumbnailUrl' in product && (product as any).thumbnailUrl ? (
                <>
                  <img
                        src={String((product as any).thumbnailUrl)}
                    alt={product.name}
                    className="w-24 h-24 object-cover rounded border-2 border-blue-300"
                    onError={(e) => {
                      e.currentTarget.src = 'https://placehold.co/200x200?text=No+Image';
                    }}
                  />
                  <div className="text-sm">
                    <p className="font-medium">Ảnh này hiển thị trong danh sách sản phẩm</p>
                        <p className="text-xs mt-1 text-muted-foreground break-all">{String((product as any).thumbnailUrl)}</p>
                      {String((product as any).thumbnailUrl).includes('placeholder') && (
                        <p className="text-xs mt-2 text-orange-600 font-medium">
                          ⚠️ Đang dùng ảnh mẫu. Vui lòng cập nhật thumbnail từ trang chỉnh sửa sản phẩm.
                        </p>
                      )}
                  </div>
                </>
                ) : (
                  <span className="text-muted-foreground text-xs">Chưa có ảnh đại diện</span>
                )}
            </div>
          </div>

          {/* Add Image Button */}
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-semibold">Ảnh chi tiết sản phẩm ({images.length})</h3>
            <Button
              onClick={() => setShowAddForm(!showAddForm)}
              variant="default"
              size="sm"
              className="flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Thêm ảnh mới
            </Button>
          </div>

          {/* Add Image Form */}
          {showAddForm && (
            <form onSubmit={handleAddImage} className="mb-6 p-4 border rounded-lg bg-muted/20 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="imageUrl">URL hình ảnh *</Label>
                <Input
                  id="imageUrl"
                  type="url"
                  value={newImageUrl}
                  onChange={(e) => setNewImageUrl(e.target.value)}
                  placeholder="https://example.com/image.jpg"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="altText">Mô tả ảnh (Alt Text)</Label>
                <Input
                  id="altText"
                  value={newImageAlt}
                  onChange={(e) => setNewImageAlt(e.target.value)}
                  placeholder="Ví dụ: Góc nhìn phía trước, góc cạnh..."
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="imageOrder">Thứ tự hiển thị</Label>
                  <Input
                    id="imageOrder"
                    type="number"
                    min="0"
                    value={newImageOrder}
                    onChange={(e) => setNewImageOrder(Number(e.target.value))}
                  />
                </div>

                <div className="flex items-end">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={isPrimary}
                      onChange={(e) => setIsPrimary(e.target.checked)}
                      disabled={images.length > 0}
                      className="w-4 h-4"
                    />
                    <span className="text-sm">
                      Đặt làm ảnh chính
                      {images.length > 0 && (
                        <span className="text-xs text-muted-foreground ml-2">
                          (Đã có ảnh chính)
                        </span>
                      )}
                    </span>
                  </label>
                </div>
              </div>

              <div className="flex gap-2">
                <Button type="submit" disabled={uploading} className="flex-1">
                  {uploading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Đang thêm...
                    </>
                  ) : (
                    <>
                      <Plus className="w-4 h-4 mr-2" />
                      Thêm ảnh
                    </>
                  )}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowAddForm(false)}
                  disabled={uploading}
                >
                  Hủy
                </Button>
              </div>
            </form>
          )}

          {/* Images Grid */}
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
            </div>
          ) : images.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
              <Upload className="w-16 h-16 mb-4 opacity-50" />
              <p className="text-lg font-medium">Chưa có hình ảnh nào</p>
              <p className="text-sm">Sản phẩm này chưa có hình ảnh bổ sung</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {images.map((image) => (
                <div
                  key={image.id}
                  className="relative group border rounded-lg overflow-hidden bg-muted/20 hover:shadow-lg transition-all"
                >
                  {/* Image */}
                  <div className="aspect-square relative">
                    <img
                      src={image.imageUrl}
                      alt={image.altText || `Product image ${image.id}`}
                      className="w-full h-full object-cover"
                    />
                    
                    {/* Primary badge */}
                    {image.isPrimary && (
                      <div className="absolute top-2 left-2 bg-yellow-500 text-white px-2 py-1 rounded text-xs font-medium flex items-center gap-1">
                        <Star className="w-3 h-3" />
                        Chính
                      </div>
                    )}

                    {/* Delete button overlay */}
                    <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <Button
                        onClick={() => handleDeleteImage(image.id!)}
                        disabled={deleting === image.id}
                        variant="destructive"
                        size="sm"
                        className="flex items-center gap-2"
                      >
                        {deleting === image.id ? (
                          <>
                            <Loader2 className="w-4 h-4 animate-spin" />
                            Đang xóa...
                          </>
                        ) : (
                          <>
                            <Trash2 className="w-4 h-4" />
                            Xóa
                          </>
                        )}
                      </Button>
                    </div>
                  </div>

                  {/* Image info */}
                  <div className="p-2 bg-card border-t">
                    <p className="text-xs text-muted-foreground truncate">
                      {image.altText || 'Không có mô tả'}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Thứ tự: {image.imageOrder}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-6 border-t bg-muted/20">
          <p className="text-sm text-muted-foreground">
            Tổng số: <span className="font-medium text-foreground">{images.length}</span> hình ảnh
          </p>
          <Button onClick={onClose} variant="outline">
            Đóng
          </Button>
        </div>
      </div>

      <ConfirmDialog
        open={confirmDialog.open}
        title={confirmDialog.title}
        description={confirmDialog.description}
        onConfirm={confirmDialog.onConfirm}
        onClose={() => setConfirmDialog({ ...confirmDialog, open: false })}
        intent="danger"
        confirmLabel="Xác nhận"
        cancelLabel="Hủy"
      />
    </div>
  );
}
