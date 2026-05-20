'use client';

import React from 'react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { X, ArrowRight } from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface ComparisonBarProps {
  selectedProducts: Array<{
    id: number;
    name: string;
    image: string;
    price: number;
    categoryName?: string;
  }>;
  onRemove: (productId: number) => void;
  onClear: () => void;
}

export function ComparisonBar({ selectedProducts, onRemove, onClear }: ComparisonBarProps) {
  const router = useRouter();
  
  if (selectedProducts.length === 0) return null;

  const handleCompare = () => {
    const ids = selectedProducts.map(p => p.id).join(',');
    router.push(`/products/compare?ids=${ids}`);
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 border-t bg-background shadow-lg">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <h3 className="font-semibold text-sm">
              So sánh sản phẩm ({selectedProducts.length}/4)
            </h3>
            {selectedProducts.length > 0 && selectedProducts[0].categoryName && (
              <span className="text-xs text-muted-foreground">
                • {selectedProducts[0].categoryName}
              </span>
            )}
            <Button
              variant="ghost"
              size="sm"
              onClick={onClear}
              className="text-xs"
            >
              Xóa tất cả
            </Button>
          </div>

          <div className="flex items-center gap-3 flex-1 overflow-x-auto">
            {selectedProducts.map(product => (
              <Card key={product.id} className="relative flex-shrink-0 p-2 w-32">
                <Button
                  size="icon"
                  variant="ghost"
                  className="absolute -top-2 -right-2 h-5 w-5 rounded-full bg-background border"
                  onClick={() => onRemove(product.id)}
                >
                  <X className="h-3 w-3" />
                </Button>
                <div className="relative aspect-square mb-1 overflow-hidden rounded">
                  <Image
                    src={product.image}
                    alt={product.name}
                    fill
                    className="object-cover"
                  />
                </div>
                <p className="text-xs line-clamp-2 mb-1">{product.name}</p>
                <p className="text-xs font-semibold text-primary">
                  {formatPrice(product.price)}
                </p>
              </Card>
            ))}
            
            {selectedProducts.length < 4 && (
              <div className="flex-shrink-0 w-32 h-full flex items-center justify-center border-2 border-dashed rounded-lg p-4">
                <p className="text-xs text-muted-foreground text-center">
                  Chọn thêm sản phẩm để so sánh
                </p>
              </div>
            )}
          </div>

          <Button
            onClick={handleCompare}
            disabled={selectedProducts.length < 2}
            size="lg"
          >
            So sánh ngay
            <ArrowRight className="w-4 h-4 ml-2" />
          </Button>
        </div>
      </div>
    </div>
  );
}
