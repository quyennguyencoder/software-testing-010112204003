'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Images, Loader2 } from 'lucide-react';
import { Product } from '@/types/product';
import { getAllProductsAdmin } from '@/services/product.service';
import { productAPI } from '@/lib/api';
import { ImageManagementModal } from './ImageManagementModal';
import { toast } from 'sonner';

export function ProductImagesTable() {
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [showImageModal, setShowImageModal] = useState(false);

  useEffect(() => {
    loadProducts();
  }, []);

  const handleManageImages = async (product: Product) => {
    try {
      setLoadingDetail(true);
      console.log('🔍 Opening image management for product:', product.id);
      
      // Trực tiếp mở modal với thông tin product hiện có
      setSelectedProduct(product);
      setShowImageModal(true);
    } catch (error) {
      console.error('❌ Error opening image modal:', error);
      toast.error('Lỗi khi mở quản lý ảnh', {
        description: error instanceof Error ? error.message : 'Vui lòng thử lại',
      });
    } finally {
      setLoadingDetail(false);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      console.log('🔍 Loading products for image management...');
      
      const response = await getAllProductsAdmin({
        sortBy: 'createdAt',
        sortDirection: 'desc',
      });

      console.log('📥 API Response:', response);
      
      if (response.success && response.data) {
        // response.data is directly the array of products
        setProducts(response.data);
        console.log('✅ Loaded products:', response.data.length);
        
        // Debug: Check if images field exists
        if (response.data.length > 0) {
          const firstProduct = response.data[0] as any;
          console.log('🔍 First product ID:', firstProduct.id);
          console.log('🔍 First product name:', firstProduct.name);
          console.log('🔍 First product has images field?', 'images' in firstProduct);
          console.log('🔍 First product images:', (firstProduct as any).images);
          console.log('🔍 All fields:', Object.keys(firstProduct));
        }
      } else {
        console.error('❌ Failed to load products:', response);
      }
    } catch (error) {
      console.error('❌ Error loading products:', error);
      toast.error('Lỗi khi tải danh sách sản phẩm', {
        description: 'Vui lòng thử lại sau',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCloseModal = () => {
    setShowImageModal(false);
    setSelectedProduct(null);
    // Reload products to get updated image counts
    loadProducts();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
      <div className="border rounded-lg overflow-hidden bg-card">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-muted/50 border-b">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium">ID</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Tên sản phẩm</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Danh mục</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Thương hiệu</th>
                <th className="px-4 py-3 text-left text-sm font-medium">Số hình ảnh</th>
                <th className="px-4 py-3 text-center text-sm font-medium">Quản lý hình ảnh</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {products.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">
                    Không có sản phẩm nào
                  </td>
                </tr>
              ) : (
                products.map((product) => (
                  <tr key={product.id} className="hover:bg-muted/30 transition-colors">
                    <td className="px-4 py-3 text-sm">{product.id}</td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        {(product as any).thumbnailUrl && (
                          <img
                            src={(product as any).thumbnailUrl}
                            alt={product.name}
                            className="w-10 h-10 object-cover rounded border"
                          />
                        )}
                        <span className="font-medium">{product.name}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-sm">{(product as any).category?.name || '-'}</td>
                    <td className="px-4 py-3 text-sm">{(product as any).brand?.name || '-'}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className="inline-flex items-center gap-1 px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">
                        <Images className="w-3 h-3" />
                        {(product as any).images?.length || 0}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Button
                        onClick={() => handleManageImages(product)}
                        variant="outline"
                        size="sm"
                        className="flex items-center gap-2 mx-auto"
                        disabled={loadingDetail}
                      >
                        {loadingDetail ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          <Images className="w-4 h-4" />
                        )}
                        Quản lý
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Image Management Modal */}
      {showImageModal && selectedProduct && (
        <ImageManagementModal
          product={selectedProduct}
          onClose={handleCloseModal}
        />
      )}
    </>
  );
}
