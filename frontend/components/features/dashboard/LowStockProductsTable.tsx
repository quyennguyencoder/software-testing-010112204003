/**
 * LowStockProductsTable component - Display products with low stock
 * Module M10.2 - View Dashboard
 */

'use client';

import { LowStockProduct } from '@/types';
import { AlertTriangle, Package } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

interface LowStockProductsTableProps {
  data: LowStockProduct[];
  loading?: boolean;
  threshold?: number;
  onThresholdChange?: (threshold: number) => void;
}

export function LowStockProductsTable({ 
  data, 
  loading = false,
  threshold = 10,
  onThresholdChange,
}: LowStockProductsTableProps) {
  const router = useRouter();
  const [localThreshold, setLocalThreshold] = useState(threshold);

  // Handle threshold change
  const handleThresholdSubmit = () => {
    if (onThresholdChange && localThreshold > 0) {
      onThresholdChange(localThreshold);
    }
  };

  // Handle row click
  const handleRowClick = (productId: number) => {
    router.push(`/manage?tab=products&productId=${productId}`);
  };

  if (loading) {
    return (
      <div className="bg-card rounded-xl border border-border p-4 md:p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-secondary rounded w-48 mb-4"></div>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-16 bg-secondary rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header with Threshold Filter */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
        <div>
          <h3 className="text-lg font-semibold text-foreground flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-orange-500" />
            Sản phẩm sắp hết hàng
          </h3>
          <p className="text-sm text-muted-foreground mt-1">
            {data.length > 0 
              ? `${data.length} sản phẩm có số lượng < ${threshold}`
              : `Không có sản phẩm nào có số lượng < ${threshold}`
            }
          </p>
        </div>

        {/* Threshold Filter */}
        <div className="flex items-center gap-2">
          <label className="text-sm text-muted-foreground whitespace-nowrap">
            Ngưỡng cảnh báo:
          </label>
          <input
            type="number"
            min="1"
            value={localThreshold}
            onChange={(e) => setLocalThreshold(Number(e.target.value))}
            className="w-20 px-3 py-1.5 text-sm border border-border rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
          <button
            onClick={handleThresholdSubmit}
            className="px-3 py-1.5 text-sm font-medium text-foreground bg-secondary hover:bg-secondary/80 rounded-lg transition-colors"
          >
            Áp dụng
          </button>
        </div>
      </div>

      {/* Empty State */}
      {data.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <Package className="w-12 h-12 text-green-500 mb-3" />
          <p className="text-foreground font-medium">Tuyệt vời! Tất cả sản phẩm đều đủ hàng</p>
          <p className="text-sm text-muted-foreground mt-1">
            Không có sản phẩm nào có số lượng dưới {threshold}
          </p>
        </div>
      ) : (
        <>
          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                    Sản phẩm
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                    Danh mục
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                    Thương hiệu
                  </th>
                  <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                    Số lượng còn
                  </th>
                  <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                    Trạng thái
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.map((product) => {
                  // Calculate stock warning level
                  const stockPercentage = (product.stockQuantity / threshold) * 100;
                  const stockColor = 
                    product.stockQuantity === 0 ? 'text-red-600' :
                    stockPercentage <= 30 ? 'text-orange-600' :
                    'text-yellow-600';

                  return (
                    <tr
                      key={product.productId}
                      className="border-b border-border hover:bg-secondary/50 transition-colors cursor-pointer"
                      onClick={() => handleRowClick(product.productId)}
                      title="Click để xem chi tiết sản phẩm"
                    >
                      {/* Product Info */}
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-3">
                          {/* Product Image */}
                          <div className="w-12 h-12 rounded-lg bg-secondary flex-shrink-0 overflow-hidden">
                            {product.imageUrl ? (
                              <img
                                src={product.imageUrl}
                                alt={product.productName}
                                className="w-full h-full object-cover"
                              />
                            ) : (
                              <div className="w-full h-full flex items-center justify-center">
                                <Package className="w-6 h-6 text-muted-foreground" />
                              </div>
                            )}
                          </div>
                          {/* Product Name */}
                          <div className="flex flex-col">
                            <p className="text-sm font-medium text-foreground line-clamp-1">
                              {product.productName}
                            </p>
                            <p className="text-xs text-muted-foreground">
                              ID: {product.productId}
                            </p>
                          </div>
                        </div>
                      </td>

                      {/* Category */}
                      <td className="py-3 px-4">
                        <p className="text-sm text-foreground">
                          {product.categoryName || '-'}
                        </p>
                      </td>

                      {/* Brand */}
                      <td className="py-3 px-4">
                        <p className="text-sm text-foreground">
                          {product.brandName || '-'}
                        </p>
                      </td>

                      {/* Stock Quantity */}
                      <td className="py-3 px-4">
                        <div className="flex flex-col items-center">
                          <p className={`text-sm font-semibold ${stockColor}`}>
                            {product.stockQuantity}
                          </p>
                          {product.stockQuantity === 0 && (
                            <span className="text-xs text-red-600 font-medium">
                              Hết hàng
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Status */}
                      <td className="py-3 px-4">
                        <div className="flex justify-center">
                          <span
                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${
                              product.status
                                ? 'bg-green-100 text-green-800 border-green-300'
                                : 'bg-gray-100 text-gray-800 border-gray-300'
                            }`}
                          >
                            {product.status ? 'Đang bán' : 'Ngừng bán'}
                          </span>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Footer Summary */}
          <div className="mt-4 pt-4 border-t border-border flex justify-between items-center">
            <p className="text-sm text-muted-foreground">
              Hiển thị {data.length} sản phẩm cần nhập hàng
            </p>
            <button
              onClick={() => router.push('/manage?tab=products')}
              className="text-sm text-primary hover:underline font-medium"
            >
              Quản lý sản phẩm →
            </button>
          </div>
        </>
      )}
    </div>
  );
}
