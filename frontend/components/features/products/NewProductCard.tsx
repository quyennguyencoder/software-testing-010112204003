'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Star, ShoppingCart, Heart, Zap } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { ProductCardResponse } from '@/services/new-product.service';

interface ProductCardProps {
  product: ProductCardResponse;
  onAddToCart?: (productId: number) => void;
  compareMode?: boolean;
  isSelected?: boolean;
  onSelectForCompare?: (productId: number, selected: boolean) => void;
  onToggleWishlist?: (productId: number) => void;
  isInWishlist?: boolean;
}

export function ProductCard({ 
  product, 
  onAddToCart, 
  compareMode = false,
  isSelected = false,
  onSelectForCompare,
  onToggleWishlist,
  isInWishlist = false,
}: ProductCardProps) {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const formatRating = (rating: number) => {
    return rating.toFixed(1);
  };

  // Determine if product has discount
  const isDiscounted = product.hasDiscount || 
    (product.discountedPrice && product.originalPrice && product.discountedPrice < product.originalPrice) || 
    (product.discountPercentage && product.discountPercentage > 0);

  const displayPrice = product.discountedPrice || product.originalPrice || 0;
  
  // Calculate discount percentage safely
  const discountPercent = (() => {
    // If discountPercentage is provided and valid, use it
    if (product.discountPercentage && product.discountPercentage > 0 && product.discountPercentage <= 100) {
      return Math.round(product.discountPercentage);
    }
    
    // Otherwise calculate from prices
    if (product.originalPrice && product.originalPrice > 0 && displayPrice < product.originalPrice) {
      const calculated = Math.round((1 - displayPrice / product.originalPrice) * 100);
      // Ensure it's a valid percentage (0-100)
      return Math.max(0, Math.min(100, calculated));
    }
    
    return 0;
  })();

  return (
    <Link href={`/products/${product.id}`} className="block h-full group">
      <Card className={cn(
        "relative overflow-hidden h-full flex flex-col border border-border/50 bg-card",
        "shadow-sm hover:shadow-xl transition-all duration-300 ease-out",
        "hover:-translate-y-2 hover:border-primary/30",
        "cursor-pointer",
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
            src={product.thumbnailUrl || '/placeholder-product.png'}
            alt={product.name}
            fill
            className="object-cover transition-transform duration-500 ease-out group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            priority={false}
          />
          
          {/* Gradient overlay for better text readability */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/5 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
          
          {/* Badges Container */}
          <div className={cn(
            "absolute top-3 flex flex-col gap-1.5 z-10",
            compareMode ? "left-12" : "left-3"
          )}>
            {/* Discount Badge */}
            {isDiscounted && discountPercent > 0 && (
              <Badge 
                variant="destructive" 
                className="px-2.5 py-1 text-xs font-bold shadow-md bg-gradient-to-r from-red-500 to-rose-500 border-0"
              >
                <Zap className="w-3 h-3 mr-1" />
                -{discountPercent}%
              </Badge>
            )}
          </div>

          {/* Wishlist button */}
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
            aria-label={isInWishlist ? "Xóa khỏi yêu thích" : "Thêm vào yêu thích"}
          >
            <Heart 
              className={cn(
                "w-4 h-4 transition-all duration-300",
                isInWishlist ? "fill-red-500 text-red-500" : "text-gray-600 hover:text-red-500"
              )}
            />
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

        {/* Product Info */}
        <CardContent className="p-4 flex-1 flex flex-col bg-white">
          {/* Brand */}
          <div className="text-[11px] text-muted-foreground mb-1.5 font-medium uppercase tracking-wider">
            {product.brandName}
          </div>

          {/* Product Name */}
          <h3 className="font-semibold text-sm leading-snug line-clamp-2 mb-2 text-foreground group-hover:text-primary transition-colors duration-200 min-h-[2.5rem]">
            {product.name}
          </h3>

          {/* Key Specs - Compact */}
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
              {isDiscounted ? (
                <>
                  <span className="text-lg font-bold text-primary">
                    {formatPrice(displayPrice)}
                  </span>
                  <span className="text-xs text-gray-400 line-through">
                    {formatPrice(product.originalPrice)}
                  </span>
                </>
              ) : (
                <span className="text-lg font-bold text-gray-900">
                  {formatPrice(product.originalPrice)}
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

// Skeleton component for loading state
export function ProductCardSkeleton() {
  return (
    <Card className="overflow-hidden h-full border border-border/50 shadow-sm animate-pulse">
      <div className="aspect-square bg-gradient-to-br from-gray-100 via-gray-200 to-gray-100" />
      <CardContent className="p-4 space-y-3">
        <div className="h-2.5 bg-gray-200 rounded w-1/4" />
        <div className="h-4 bg-gray-200 rounded w-3/4" />
        <div className="h-4 bg-gray-200 rounded w-full" />
        <div className="flex gap-1">
          <div className="h-4 bg-gray-200 rounded w-10" />
          <div className="h-4 bg-gray-200 rounded w-12" />
        </div>
        <div className="flex gap-0.5">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="w-3 h-3 bg-gray-200 rounded-full" />
          ))}
        </div>
        <div className="pt-3 border-t border-gray-100 flex justify-between items-center">
          <div className="h-5 bg-gray-200 rounded w-24" />
          <div className="h-8 bg-gray-200 rounded w-20" />
        </div>
      </CardContent>
    </Card>
  );
}