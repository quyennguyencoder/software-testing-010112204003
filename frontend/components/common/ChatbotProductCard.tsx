'use client';

import React from 'react';
import Image from 'next/image';
import { cn, formatPrice } from '@/lib/utils';
import { Badge } from '@/components/ui/badge';
import { RecommendedProductDTO } from '@/types/chatbot-assistant.d';
import {
  Smartphone,
  Battery,
  HardDrive,
  Cpu,
  Star,
  TrendingUp,
  Sparkles,
  Tag,
  ExternalLink,
} from 'lucide-react';

interface ChatbotProductCardProps {
  product: RecommendedProductDTO;
  index: number;
  className?: string;
}

// Brand colors mapping
const BRAND_COLORS: Record<string, { bg: string; text: string; border: string }> = {
  Apple: { bg: 'bg-gray-900', text: 'text-white', border: 'border-gray-800' },
  Samsung: { bg: 'bg-blue-600', text: 'text-white', border: 'border-blue-500' },
  Xiaomi: { bg: 'bg-orange-500', text: 'text-white', border: 'border-orange-400' },
  OPPO: { bg: 'bg-green-600', text: 'text-white', border: 'border-green-500' },
  Vivo: { bg: 'bg-sky-500', text: 'text-white', border: 'border-sky-400' },
  Realme: { bg: 'bg-yellow-500', text: 'text-black', border: 'border-yellow-400' },
  Huawei: { bg: 'bg-red-600', text: 'text-white', border: 'border-red-500' },
  default: { bg: 'bg-primary', text: 'text-primary-foreground', border: 'border-primary' },
};

// Medal icons for top products
const MEDALS = ['🏆', '🥈', '🥉', '4️⃣', '5️⃣'];

/**
 * Product Card chuyên dụng cho Chatbot
 * Features:
 * - Brand color-coded badges
 * - Category indicators
 * - Technical specs chips
 * - Discount highlights
 * - Match score bar
 */
export const ChatbotProductCard: React.FC<ChatbotProductCardProps> = ({
  product,
  index,
  className,
}) => {
  const brandName = product.brandName || 'Unknown';
  const brandStyle = BRAND_COLORS[brandName] || BRAND_COLORS.default;
  const medal = MEDALS[index] || `${index + 1}`;
  
  // Calculate discount safely
  const hasDiscount = product.originalPrice && product.originalPrice > 0 && product.price && product.originalPrice > product.price;
  const discountPercent = (() => {
    if (hasDiscount && product.originalPrice && product.originalPrice > 0) {
      const calculated = Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100);
      return Math.max(0, Math.min(100, calculated));
    }
    return 0;
  })();
  const savedAmount = hasDiscount && product.originalPrice && product.price ? product.originalPrice - product.price : 0;

  return (
    <a
      href={product.productUrl || `/products/${product.id}`}
      className={cn(
        'group block rounded-xl border-2 transition-all duration-300',
        'hover:shadow-lg hover:scale-[1.01] hover:border-primary',
        'bg-gradient-to-br from-background to-muted/30',
        className
      )}
    >
      {/* Header: Medal + Brand Badge */}
      <div className="flex items-center justify-between px-3 pt-3 pb-2">
        <div className="flex items-center gap-2">
          <span className="text-xl">{medal}</span>
          <Badge 
            className={cn(
              'text-[10px] font-bold px-2 py-0.5',
              brandStyle.bg, 
              brandStyle.text,
              brandStyle.border
            )}
          >
            {brandName}
          </Badge>
          {product.categoryName && (
            <Badge variant="outline" className="text-[10px] gap-1">
              <Smartphone className="w-3 h-3" />
              {product.categoryName}
            </Badge>
          )}
        </div>
        
        {/* Discount Badge */}
        {hasDiscount && discountPercent > 0 && (
          <Badge className="bg-gradient-to-r from-red-500 to-pink-500 text-white text-[10px] font-bold animate-pulse">
            <Tag className="w-3 h-3 mr-1" />
            -{discountPercent}%
          </Badge>
        )}
      </div>

      {/* Main Content */}
      <div className="flex gap-3 px-3 pb-3">
        {/* Product Image */}
        {product.imageUrl && (
          <div className="relative flex-shrink-0 w-20 h-20 rounded-lg bg-white border overflow-hidden group-hover:shadow-md transition-shadow">
            <Image
              src={product.imageUrl}
              alt={product.name}
              fill
              className="object-contain p-1 group-hover:scale-105 transition-transform"
              onError={(e) => {
                const target = e.target as HTMLImageElement;
                target.src = '/placeholder-phone.png';
              }}
            />
            {/* New arrival badge */}
            {product.soldCount !== undefined && product.soldCount < 10 && (
              <div className="absolute top-0 left-0 bg-gradient-to-r from-emerald-500 to-green-500 text-white text-[8px] px-1.5 py-0.5 rounded-br-lg font-bold">
                <Sparkles className="w-2.5 h-2.5 inline mr-0.5" />
                NEW
              </div>
            )}
          </div>
        )}
        
        {/* Product Info */}
        <div className="flex-1 min-w-0 space-y-2">
          {/* Product Name */}
          <h4 className="font-bold text-sm text-foreground line-clamp-2 group-hover:text-primary transition-colors">
            {product.name}
          </h4>
          
          {/* Technical Specs Grid */}
          <div className="flex flex-wrap gap-1">
            {product.ram && (
              <span className="inline-flex items-center gap-1 text-[10px] px-1.5 py-0.5 rounded-full bg-blue-100 dark:bg-blue-950 text-blue-700 dark:text-blue-300 font-medium">
                <Cpu className="w-3 h-3" />
                {product.ram}
              </span>
            )}
            {product.storage && (
              <span className="inline-flex items-center gap-1 text-[10px] px-1.5 py-0.5 rounded-full bg-purple-100 dark:bg-purple-950 text-purple-700 dark:text-purple-300 font-medium">
                <HardDrive className="w-3 h-3" />
                {product.storage}
              </span>
            )}
            {product.batteryCapacity && (
              <span className="inline-flex items-center gap-1 text-[10px] px-1.5 py-0.5 rounded-full bg-green-100 dark:bg-green-950 text-green-700 dark:text-green-300 font-medium">
                <Battery className="w-3 h-3" />
                {product.batteryCapacity}mAh
              </span>
            )}
            {product.operatingSystem && (
              <span className="inline-flex items-center gap-1 text-[10px] px-1.5 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 font-medium">
                📱 {product.operatingSystem}
              </span>
            )}
          </div>
          
          {/* Price Section */}
          <div className="flex items-baseline gap-2 flex-wrap">
            <span className="text-lg font-bold text-primary">
              {formatPrice(product.price)}
            </span>
            {hasDiscount && (
              <>
                <span className="text-xs text-muted-foreground line-through">
                  {formatPrice(product.originalPrice!)}
                </span>
                <Badge variant="secondary" className="text-[10px] bg-red-50 dark:bg-red-950 text-red-600 dark:text-red-400">
                  Tiết kiệm {formatPrice(savedAmount)}
                </Badge>
              </>
            )}
          </div>
          
          {/* Rating + Sold Count */}
          <div className="flex items-center gap-3 text-xs">
            {product.rating !== undefined && product.rating > 0 && (
              <div className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-amber-50 dark:bg-amber-950/50 text-amber-600 dark:text-amber-400">
                <Star className="w-3 h-3 fill-current" />
                <span className="font-semibold">{product.rating.toFixed(1)}</span>
                <span className="text-muted-foreground">({product.reviewCount || 0})</span>
              </div>
            )}
            {product.soldCount !== undefined && product.soldCount > 0 && (
              <div className="flex items-center gap-1 text-muted-foreground">
                <TrendingUp className="w-3 h-3" />
                <span>Đã bán {product.soldCount}</span>
              </div>
            )}
          </div>
          
          {/* Match Score */}
          {product.matchScore !== undefined && product.matchScore > 0 && (
            <div className="space-y-1">
              <div className="flex items-center justify-between text-[10px]">
                <span className="text-muted-foreground">🎯 Độ phù hợp</span>
                <span className="font-bold text-emerald-600 dark:text-emerald-400">
                  {(product.matchScore * 100).toFixed(0)}%
                </span>
              </div>
              <div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full transition-all duration-500"
                  style={{ width: `${product.matchScore * 100}%` }}
                />
              </div>
            </div>
          )}
        </div>
        
        {/* Action Arrow */}
        <div className="flex-shrink-0 flex items-center">
          <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all group-hover:translate-x-1">
            <ExternalLink className="w-4 h-4 text-primary" />
          </div>
        </div>
      </div>
      
      {/* Reason/Description if available */}
      {product.reason && (
        <div className="px-3 pb-3">
          <p className="text-[11px] text-muted-foreground italic bg-muted/50 rounded-lg px-2 py-1.5">
            💡 {product.reason}
          </p>
        </div>
      )}
    </a>
  );
};

/**
 * Component hiển thị danh sách products được group theo brand
 */
interface ChatbotProductListProps {
  products: RecommendedProductDTO[];
  className?: string;
  groupByBrand?: boolean;
}

export const ChatbotProductList: React.FC<ChatbotProductListProps> = ({
  products,
  className,
  groupByBrand = false,
}) => {
  if (!products || products.length === 0) return null;

  // Group by brand nếu cần
  if (groupByBrand) {
    const grouped = products.reduce((acc, product) => {
      const brand = product.brandName || 'Khác';
      if (!acc[brand]) acc[brand] = [];
      acc[brand].push(product);
      return acc;
    }, {} as Record<string, RecommendedProductDTO[]>);

    return (
      <div className={cn('space-y-4', className)}>
        {Object.entries(grouped).map(([brand, brandProducts]) => {
          const brandStyle = BRAND_COLORS[brand] || BRAND_COLORS.default;
          return (
            <div key={brand} className="space-y-2">
              <div className="flex items-center gap-2">
                <Badge className={cn('text-xs', brandStyle.bg, brandStyle.text)}>
                  {brand}
                </Badge>
                <span className="text-xs text-muted-foreground">
                  {brandProducts.length} sản phẩm
                </span>
              </div>
              <div className="space-y-2">
                {brandProducts.map((product, idx) => (
                  <ChatbotProductCard
                    key={product.id || idx}
                    product={product}
                    index={products.indexOf(product)}
                  />
                ))}
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  return (
    <div className={cn('space-y-2', className)}>
      {products.map((product, index) => (
        <ChatbotProductCard
          key={product.id || index}
          product={product}
          index={index}
        />
      ))}
    </div>
  );
};

export default ChatbotProductCard;
