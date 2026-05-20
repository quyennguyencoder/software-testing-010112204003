'use client';

import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Star, ShoppingCart, TrendingUp, Heart, Zap } from 'lucide-react';
import Link from 'next/link';
import Image from 'next/image';
import type { ProductViewResponse } from '@/types/product-view';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { cn } from '@/lib/utils';

interface ProductCardProps {
  product: ProductViewResponse;
  onAddToCart?: (productId: number) => void;
  compareMode?: boolean;
  isSelected?: boolean;
  onSelectForCompare?: (productId: number, selected: boolean) => void;
  onToggleWishlist?: (productId: number) => void;
  isInWishlist?: boolean;
}

/**
 * Product Card Component
 * Displays product information in a card format for listing pages
 * Entire card is clickable for better UX
 */
export function ProductCard({ 
  product, 
  onAddToCart, 
  compareMode = false,
  isSelected = false,
  onSelectForCompare,
  onToggleWishlist,
  isInWishlist = false,
}: ProductCardProps) {
  const primaryImage = product.images?.find(img => img.isPrimary) || product.images?.[0];
  const imageUrl = primaryImage?.imageUrl || product.thumbnailUrl || '/placeholder-product.png';
  
  const minPrice = product.minPrice || 0;
  const maxPrice = product.maxPrice || 0;
  const hasVariants = product.variantsCount && product.variantsCount > 1;
  
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const formatRating = (rating: number) => {
    return rating.toFixed(1);
  };

  return (
    <Link href={`/products/${product.id}`} className="block h-full group">
      <Card className={cn(
        "relative overflow-hidden h-full flex flex-col border border-border/50 bg-card",
        "shadow-sm hover:shadow-xl transition-all duration-300 ease-out",
        "hover:-translate-y-2 hover:border-primary/30 cursor-pointer",
        isSelected && "ring-2 ring-primary ring-offset-2 shadow-xl"
      )}>
        {/* Compare Checkbox */}
        {compareMode && (
          <div 
            className="absolute top-3 left-3 z-20"
            onClick={(e) => e.preventDefault()}
          >
            <div className="bg-white rounded-full p-1.5 shadow-lg border border-gray-200 hover:border-primary transition-colors">
              <Checkbox
                checked={isSelected}
                onCheckedChange={(checked) => onSelectForCompare?.(product.id, !!checked)}
                className="h-5 w-5"
              />
            </div>
          </div>
        )}

        {/* Image Container */}
        <div className="relative aspect-square overflow-hidden bg-gradient-to-br from-gray-50 via-gray-100 to-gray-50">
          <Image
            src={imageUrl}
            alt={product.name}
            fill
            className="object-cover transition-transform duration-500 ease-out group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            priority={false}
          />
          
          {/* Gradient overlay */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/5 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
          
          {/* Badges */}
          <div className={cn(
            "absolute top-3 flex flex-col gap-1.5 z-10",
            compareMode ? "left-12" : "left-3"
          )}>
            {product.discountPercentage && product.discountPercentage > 0 && product.discountPercentage <= 100 && (
              <Badge variant="destructive" className="text-xs font-bold px-2.5 py-1 shadow-md bg-gradient-to-r from-red-500 to-rose-500 border-0">
                <Zap className="w-3 h-3 mr-1" />
                -{Math.round(product.discountPercentage)}%
              </Badge>
            )}
            {product.promotionBadge && (
              <Badge variant="destructive" className="text-xs font-semibold shadow-md">
                {product.promotionBadge}
              </Badge>
            )}
            {product.soldCount && product.soldCount > 100 && (
              <Badge variant="default" className="text-xs flex items-center gap-1 bg-gradient-to-r from-orange-500 to-amber-500 border-0 shadow-md">
                <TrendingUp className="w-3 h-3" />
                Bán chạy
              </Badge>
            )}
          </div>

          {/* Wishlist Button */}
          <button
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onToggleWishlist?.(product.id);
            }}
            className={cn(
              "absolute top-3 right-3 z-10 p-2.5 rounded-full transition-all duration-300",
              "bg-white/95 backdrop-blur-sm shadow-md border border-gray-100",
              "hover:bg-white hover:shadow-lg hover:scale-110 hover:border-red-200",
              "opacity-0 group-hover:opacity-100",
              isInWishlist && "opacity-100 bg-red-50 border-red-200"
            )}
            title={isInWishlist ? "Xóa khỏi yêu thích" : "Thêm vào yêu thích"}
          >
            <Heart className={cn(
              "w-4 h-4 transition-colors",
              isInWishlist ? "fill-red-500 text-red-500" : "text-gray-600 hover:text-red-500"
            )} />
          </button>

          {/* Out of stock overlay */}
          {!product.inStock && (
            <div className="absolute inset-0 bg-black/50 backdrop-blur-[2px] z-10 flex items-center justify-center">
              <Badge variant="secondary" className="px-4 py-2 text-sm font-semibold bg-white/95 shadow-lg">
                Hết hàng
              </Badge>
            </div>
          )}
        </div>

        {/* Content */}
        <CardContent className="p-4 flex flex-col flex-1 bg-white">
          {/* Brand & Category */}
          <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground mb-1.5">
            <span className="font-medium text-primary uppercase tracking-wide">{product.brandName}</span>
            <span className="text-gray-300">•</span>
            <span className="truncate">{product.categoryName}</span>
          </div>

          {/* Product Name */}
          <h3 className="font-semibold text-sm leading-snug line-clamp-2 text-foreground group-hover:text-primary transition-colors duration-200 mb-2 min-h-[2.5rem]">
            {product.name}
          </h3>

          {/* Specs - Compact */}
          {(product.ram || product.storage) && (
            <div className="flex flex-wrap gap-1 mb-2">
              {product.ram && (
                <span className="text-[10px] px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded font-medium">
                  {product.ram}
                </span>
              )}
              {product.storage && (
                <span className="text-[10px] px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded font-medium">
                  {product.storage}
                </span>
              )}
            </div>
          )}

          {/* Rating */}
          {product.averageRating > 0 && (
            <div className="flex items-center gap-1 mb-2">
              <div className="flex">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={cn(
                      "w-3 h-3",
                      i < Math.floor(product.averageRating)
                        ? "fill-amber-400 text-amber-400"
                        : "fill-gray-200 text-gray-200"
                    )}
                  />
                ))}
              </div>
              <span className="text-xs font-medium text-gray-700">{formatRating(product.averageRating)}</span>
              {product.totalReviews > 0 && (
                <span className="text-[10px] text-muted-foreground">({product.totalReviews})</span>
              )}
            </div>
          )}

          {/* Price Section */}
          <div className="mt-auto pt-3 border-t border-gray-100">
            <div className="flex flex-wrap items-baseline gap-x-2 gap-y-1 mb-2">
              {hasVariants && minPrice !== maxPrice ? (
                <span className="text-lg font-bold text-primary">
                  {formatPrice(minPrice)} - {formatPrice(maxPrice)}
                </span>
              ) : (
                <span className="text-lg font-bold text-primary">
                  {formatPrice(minPrice)}
                </span>
              )}
            </div>

            {/* Stock & Add to cart row */}
            <div className="flex items-center justify-between">
              <div className="text-[11px]">
                {product.inStock ? (
                  <span className="flex items-center gap-1 text-green-600">
                    <div className="w-1.5 h-1.5 bg-green-500 rounded-full"></div>
                    <span className="font-medium">Còn hàng</span>
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-red-500">
                    <div className="w-1.5 h-1.5 bg-red-500 rounded-full"></div>
                    <span className="font-medium">Hết hàng</span>
                  </span>
                )}
              </div>
              
              {/* Add to cart button - compact */}
              <Button
                size="sm"
                variant={product.inStock ? "default" : "secondary"}
                className={cn(
                  "h-8 px-3 transition-all duration-200",
                  product.inStock && "hover:scale-105 shadow-sm hover:shadow-md"
                )}
                disabled={!product.inStock}
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  onAddToCart?.(product.id);
                }}
              >
                <ShoppingCart className="w-3.5 h-3.5 mr-1" />
                <span className="text-xs font-medium">
                  {product.inStock ? 'Thêm giỏ' : 'Hết hàng'}
                </span>
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}

/**
 * Product Card Skeleton for loading state
 */
export function ProductCardSkeleton() {
  return (
    <Card className="overflow-hidden h-full flex flex-col border border-border/50 shadow-sm animate-pulse">
      <div className="aspect-square bg-gradient-to-br from-gray-100 via-gray-200 to-gray-100" />
      <CardContent className="p-4 flex flex-col flex-1">
        <div className="h-2.5 bg-gray-200 rounded w-1/3 mb-2" />
        <div className="h-4 bg-gray-200 rounded mb-1" />
        <div className="h-4 bg-gray-200 rounded w-3/4 mb-3" />
        <div className="flex gap-1 mb-2">
          <div className="h-4 bg-gray-200 rounded w-10" />
          <div className="h-4 bg-gray-200 rounded w-12" />
        </div>
        <div className="flex gap-0.5 mb-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="w-3 h-3 bg-gray-200 rounded-full" />
          ))}
        </div>
        <div className="pt-3 border-t border-gray-100 flex justify-between items-center mt-auto">
          <div className="h-5 bg-gray-200 rounded w-24" />
          <div className="h-8 bg-gray-200 rounded w-20" />
        </div>
      </CardContent>
    </Card>
  );
}
