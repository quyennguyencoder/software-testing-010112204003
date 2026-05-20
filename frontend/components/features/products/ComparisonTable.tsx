'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Star, ShoppingCart, X } from 'lucide-react';
import Image from 'next/image';
import type { ProductComparisonResponse } from '@/types/product-view';

interface ComparisonTableProps {
  products: ProductComparisonResponse['products'];
  onRemoveProduct: (productId: number) => void;
  onBuyNow?: (product: ProductComparisonResponse['products'][0]) => void;
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

const renderStars = (rating: number) => {
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`w-3 h-3 ${star <= rating
              ? 'fill-yellow-400 text-yellow-400'
              : 'fill-gray-200 text-gray-200'
            }`}
        />
      ))}
      <span className="ml-1 text-sm font-medium">{rating.toFixed(1)}</span>
    </div>
  );
};

// Product Image component with error handling
function ProductImage({ src, alt, className }: { src: string; alt: string; className?: string }) {
  const [imageSrc, setImageSrc] = useState(src);

  return (
    <Image
      src={imageSrc}
      alt={alt}
      fill
      className={className}
      unoptimized={true}
      onError={() => setImageSrc('/placeholder-product.svg')}
    />
  );
}

export function ComparisonTable({ products, onRemoveProduct, onBuyNow }: ComparisonTableProps) {
  const specRows = [
    { key: 'screen', label: 'Màn hình' },
    { key: 'os', label: 'Hệ điều hành' },
    { key: 'cpu', label: 'Chip xử lý' },
    { key: 'ram', label: 'RAM' },
    { key: 'internalMemory', label: 'Bộ nhớ trong' },
    { key: 'rearCamera', label: 'Camera sau' },
    { key: 'frontCamera', label: 'Camera trước' },
    { key: 'battery', label: 'Pin' },
    { key: 'charging', label: 'Sạc' },
    { key: 'weight', label: 'Trọng lượng' },
    { key: 'dimensions', label: 'Kích thước' },
    { key: 'connectivity', label: 'Kết nối' },
    { key: 'sim', label: 'SIM' },
    { key: 'materials', label: 'Chất liệu' },
  ];

  return (
    <div className="overflow-x-auto">
      <div className="min-w-[800px]">
        {/* Product Headers */}
        <div className="grid gap-4 mb-6" style={{ gridTemplateColumns: `250px repeat(${products.length}, 1fr)` }}>
          {/* Empty cell for spec labels */}
          <div></div>

          {/* Product cards */}
          {products.map((product) => {
            // Calculate discount safely
            const discount = (() => {
              if (product.hasDiscount && product.originalPrice && product.originalPrice > 0 && product.discountedPrice && product.discountedPrice < product.originalPrice) {
                const calculated = Math.round((1 - product.discountedPrice / product.originalPrice) * 100);
                return Math.max(0, Math.min(100, calculated));
              }
              return 0;
            })();
            const displayPrice = product.discountedPrice || product.originalPrice || 0;

            return (
              <Card key={product.id} className="relative">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onRemoveProduct(product.id)}
                  className="absolute top-2 right-2 z-10 h-8 w-8 rounded-full bg-white/80 hover:bg-white shadow-sm"
                >
                  <X className="w-4 h-4" />
                </Button>

                <CardHeader className="text-center pb-4">
                  <div className="relative aspect-square w-32 mx-auto rounded-lg overflow-hidden bg-gray-50">
                    <ProductImage
                      src={product.thumbnailUrl || '/placeholder-product.svg'}
                      alt={product.name}
                      className="object-cover"
                    />
                    {discount > 0 && (
                      <Badge className="absolute top-2 left-2 bg-red-500 text-white text-xs">
                        -{discount}%
                      </Badge>
                    )}
                  </div>

                  <div className="space-y-2 mt-4">
                    <h3 className="font-semibold text-sm leading-tight line-clamp-2">
                      {product.name}
                    </h3>

                    <p className="text-xs text-muted-foreground">
                      {product.brandName}
                    </p>

                    {product.averageRating > 0 && (
                      <div className="flex justify-center">
                        {renderStars(product.averageRating)}
                      </div>
                    )}

                    <div className="space-y-1">
                      <div className="text-center">
                        <div className="text-lg font-bold text-primary">
                          {formatPrice(displayPrice)}
                        </div>
                        {product.hasDiscount && product.originalPrice && (
                          <div className="text-xs text-muted-foreground line-through">
                            {formatPrice(product.originalPrice)}
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center justify-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${product.inStock ? 'bg-green-500' : 'bg-red-500'}`} />
                      <span className={`text-xs ${product.inStock ? 'text-green-700' : 'text-red-700'}`}>
                        {product.inStock ? 'Còn hàng' : 'Hết hàng'}
                      </span>
                    </div>

                    <Button
                      size="sm"
                      className="w-full text-xs"
                      disabled={!product.inStock}
                      onClick={() => onBuyNow?.(product)}
                    >
                      <ShoppingCart className="w-3 h-3 mr-1" />
                      Mua ngay
                    </Button>
                  </div>
                </CardHeader>
              </Card>
            );
          })}
        </div>

        {/* Comparison Table */}
        <Card>
          <CardContent className="p-0">
            {specRows.map((spec, index) => {
              // Check if any product has this spec
              const hasSpec = products.some(product =>
                product.specs[spec.key as keyof typeof product.specs]
              );

              if (!hasSpec) return null;

              return (
                <div
                  key={spec.key}
                  className={`grid gap-4 p-4 ${index > 0 ? 'border-t' : ''}`}
                  style={{ gridTemplateColumns: `250px repeat(${products.length}, 1fr)` }}
                >
                  {/* Spec label */}
                  <div className="font-medium text-sm text-muted-foreground self-center">
                    {spec.label}
                  </div>

                  {/* Spec values */}
                  {products.map((product) => (
                    <div key={`${product.id}-${spec.key}`} className="text-sm self-center">
                      {product.specs[spec.key as keyof typeof product.specs] || (
                        <span className="text-muted-foreground italic">Không có thông tin</span>
                      )}
                    </div>
                  ))}
                </div>
              );
            })}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}