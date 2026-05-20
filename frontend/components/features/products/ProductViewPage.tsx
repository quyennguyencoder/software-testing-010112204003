'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { ProductCard, ProductCardSkeleton } from '@/components/features/products/NewProductCard';
import { ComparisonBar } from '@/components/features/products/ComparisonBar';
import { QuickLinksBar } from '@/components/features/products/QuickLinksBar';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Grid3x3, LayoutGrid, ArrowLeft, GitCompare, Package, ArrowRight, Smartphone } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';
import { useWishlistStore } from '@/store';
import { useCartActions } from '@/hooks/useCartActions';
import type { ProductCardResponse, PageResponse } from '@/services/new-product.service';

interface SortOption {
  value: string;
  label: string;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

interface ProductViewPageProps {
  title: string;
  subtitle: string;
  icon: React.ReactNode;
  iconBgClass: string;
  headerGradient: string;
  emptyIcon: React.ReactNode;
  emptyTitle: string;
  emptyDescription: string;
  basePath: string;
  defaultSort: string;
  sortOptions: SortOption[];
  showViewAllButton?: boolean;
  customHeaderContent?: React.ReactNode;
  fetchProducts: (params: {
    page: number;
    size: number;
    sortBy: string;
    sortDirection: 'asc' | 'desc';
  }) => Promise<PageResponse<ProductCardResponse>>;
}

const PAGE_SIZE_OPTIONS = [12, 24, 48];

export function ProductViewPage({
  title,
  subtitle,
  icon,
  iconBgClass,
  headerGradient,
  emptyIcon,
  emptyTitle,
  emptyDescription,
  basePath,
  defaultSort,
  sortOptions,
  showViewAllButton = true,
  customHeaderContent,
  fetchProducts,
}: ProductViewPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  // Cart & Wishlist stores
  const { addToCart } = useCartActions();
  const { toggleItem: toggleWishlist, isInWishlist } = useWishlistStore();

  // State
  const [products, setProducts] = useState<ProductCardResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [viewMode, setViewMode] = useState<'grid' | 'compact'>('grid');

  // Comparison state
  const [compareMode, setCompareMode] = useState(false);
  const [selectedForCompare, setSelectedForCompare] = useState<Map<number, ProductCardResponse>>(new Map());

  // Pagination & Sort state from URL
  const currentPage = parseInt(searchParams.get('page') || '0');
  const pageSize = parseInt(searchParams.get('size') || '12');
  const sortValue = searchParams.get('sort') || defaultSort;

  // Parse sort value
  const [sortBy, sortDirection] = sortValue.split(':') as [string, 'asc' | 'desc'];

  // Fetch products
  const loadProducts = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await fetchProducts({
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      });

      setProducts(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Error fetching products:', error);
      toast.error('Không thể tải sản phẩm');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, sortBy, sortDirection, fetchProducts]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  // Update URL params
  const updateParams = (params: { page?: number; size?: number; sort?: string }) => {
    const newParams = new URLSearchParams(searchParams.toString());

    if (params.page !== undefined) {
      newParams.set('page', params.page.toString());
    }
    if (params.size !== undefined) {
      newParams.set('size', params.size.toString());
      newParams.set('page', '0');
    }
    if (params.sort !== undefined) {
      newParams.set('sort', params.sort);
      newParams.set('page', '0');
    }

    router.push(`${basePath}?${newParams.toString()}`);
  };

  // Comparison handlers
  const handleToggleCompare = (product: ProductCardResponse) => {
    setSelectedForCompare(prev => {
      const newMap = new Map(prev);
      if (newMap.has(product.id)) {
        newMap.delete(product.id);
      } else {
        if (newMap.size >= 4) {
          toast.warning('Chỉ có thể so sánh tối đa 4 sản phẩm');
          return prev;
        }
        newMap.set(product.id, product);
      }
      return newMap;
    });
  };

  const handleRemoveFromCompare = (productId: number) => {
    setSelectedForCompare(prev => {
      const newMap = new Map(prev);
      newMap.delete(productId);
      return newMap;
    });
  };

  const handleClearCompare = () => {
    setSelectedForCompare(new Map());
    setCompareMode(false);
  };

  // Cart & Wishlist handlers
  const handleAddToCart = (productId: number) => {
    const product = products.find(p => p.id === productId);
    if (product) {
      addToCart(product);
    }
  };

  const handleToggleWishlist = (productId: number) => {
    const product = products.find(p => p.id === productId);
    if (product) {
      const wasInWishlist = isInWishlist(productId);
      toggleWishlist({
        productId: product.id,
        productName: product.name,
        price: product.discountedPrice || product.originalPrice,
        productImage: product.thumbnailUrl || '',
        inStock: product.inStock,
      });
      toast.success(wasInWishlist ? 'Đã xóa khỏi yêu thích' : 'Đã thêm vào yêu thích');
    }
  };

  // Pagination helpers
  const getVisiblePages = () => {
    const pages: (number | 'ellipsis')[] = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    pages.push(0);

    if (currentPage > 2) {
      pages.push('ellipsis');
    }

    const start = Math.max(1, currentPage - 1);
    const end = Math.min(totalPages - 2, currentPage + 1);

    for (let i = start; i <= end; i++) {
      if (!pages.includes(i)) pages.push(i);
    }

    if (currentPage < totalPages - 3) {
      pages.push('ellipsis');
    }

    if (!pages.includes(totalPages - 1)) {
      pages.push(totalPages - 1);
    }

    return pages;
  };

  // Transform selected products for ComparisonBar
  const selectedProductsList = Array.from(selectedForCompare.values()).map(p => ({
    id: p.id,
    name: p.name,
    image: p.thumbnailUrl || '/placeholder-product.png',
    price: p.discountedPrice || p.minPrice,
    categoryName: p.categoryName,
  }));

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
      {/* Header */}
      <div className={cn("border-b backdrop-blur-sm", headerGradient)}>
        <div className="max-w-7xl mx-auto px-4 py-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors mb-4 group"
          >
            <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
            Quay lại trang chủ
          </Link>

          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className={cn("p-2.5 rounded-xl shadow-lg", iconBgClass)}>
                  {icon}
                </div>
                <h1 className="text-2xl md:text-3xl font-bold text-foreground">
                  {title}
                </h1>
                {totalElements > 0 && (
                  <Badge variant="secondary" className="hidden sm:flex">
                    {totalElements} sản phẩm
                  </Badge>
                )}
              </div>
              <p className="text-muted-foreground max-w-xl">{subtitle}</p>
            </div>

            {/* Custom header content (e.g., countdown timer) */}
            {customHeaderContent}
          </div>
        </div>
      </div>

      {/* Quick Links - Sticky */}
      <div className="sticky top-0 z-40 bg-background/95 backdrop-blur-md border-b shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <QuickLinksBar />
        </div>
      </div>

      {/* Controls */}
      <div className="max-w-7xl mx-auto px-4 py-4">
        <Card className="shadow-sm">
          <CardContent className="p-4">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-3 flex-wrap">
                {/* Sort */}
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground hidden sm:inline">Sắp xếp:</span>
                  <Select
                    value={sortValue}
                    onValueChange={(value) => updateParams({ sort: value })}
                  >
                    <SelectTrigger className="w-[180px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {sortOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* Page Size */}
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground hidden sm:inline">Hiển thị:</span>
                  <Select
                    value={pageSize.toString()}
                    onValueChange={(value) => updateParams({ size: parseInt(value) })}
                  >
                    <SelectTrigger className="w-[80px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {PAGE_SIZE_OPTIONS.map((size) => (
                        <SelectItem key={size} value={size.toString()}>
                          {size}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* Compare Mode Toggle */}
                <Button
                  variant={compareMode ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setCompareMode(!compareMode)}
                  className={cn(
                    "gap-2 transition-all",
                    compareMode && "bg-primary text-primary-foreground shadow-md"
                  )}
                >
                  <GitCompare className="w-4 h-4" />
                  <span className="hidden sm:inline">So sánh</span>
                  {selectedForCompare.size > 0 && (
                    <Badge variant={compareMode ? "outline" : "secondary"} className="ml-1 text-xs">
                      {selectedForCompare.size}/4
                    </Badge>
                  )}
                </Button>
              </div>

              {/* View Mode */}
              <div className="flex items-center gap-1 border rounded-lg p-1 bg-muted/50">
                <Button
                  variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setViewMode('grid')}
                  title="Lưới lớn"
                >
                  <LayoutGrid className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === 'compact' ? 'secondary' : 'ghost'}
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setViewMode('compact')}
                  title="Lưới nhỏ"
                >
                  <Grid3x3 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Products Grid */}
      <div className="max-w-7xl mx-auto px-4 pb-8">
        {isLoading ? (
          <div className={cn(
            "grid gap-4 animate-pulse",
            viewMode === 'grid'
              ? 'grid-cols-2 md:grid-cols-3 lg:grid-cols-4'
              : 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6'
          )}>
            {Array.from({ length: pageSize }).map((_, i) => (
              <ProductCardSkeleton key={i} />
            ))}
          </div>
        ) : products.length > 0 ? (
          <div className={cn(
            "grid gap-4",
            viewMode === 'grid'
              ? 'grid-cols-2 md:grid-cols-3 lg:grid-cols-4'
              : 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6'
          )}>
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                compareMode={compareMode}
                isSelected={selectedForCompare.has(product.id)}
                onSelectForCompare={(productId, selected) => {
                  if (selected) {
                    handleToggleCompare(product);
                  } else {
                    handleRemoveFromCompare(productId);
                  }
                }}
                onAddToCart={handleAddToCart}
                onToggleWishlist={handleToggleWishlist}
                isInWishlist={isInWishlist(product.id)}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            {emptyIcon}
            <h3 className="text-lg font-semibold mb-2">{emptyTitle}</h3>
            <p className="text-muted-foreground mb-4">{emptyDescription}</p>
            <Link href="/products">
              <Button>Xem tất cả sản phẩm</Button>
            </Link>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-8 flex justify-center">
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      if (currentPage > 0) updateParams({ page: currentPage - 1 });
                    }}
                    className={cn(
                      "transition-opacity",
                      currentPage === 0 && 'pointer-events-none opacity-50'
                    )}
                  />
                </PaginationItem>

                {getVisiblePages().map((page, index) => (
                  <PaginationItem key={index}>
                    {page === 'ellipsis' ? (
                      <PaginationEllipsis />
                    ) : (
                      <PaginationLink
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          updateParams({ page: page as number });
                        }}
                        isActive={currentPage === page}
                        className={cn(
                          currentPage === page && "bg-primary text-primary-foreground"
                        )}
                      >
                        {(page as number) + 1}
                      </PaginationLink>
                    )}
                  </PaginationItem>
                ))}

                <PaginationItem>
                  <PaginationNext
                    href="#"
                    onClick={(e) => {
                      e.preventDefault();
                      if (currentPage < totalPages - 1) updateParams({ page: currentPage + 1 });
                    }}
                    className={cn(
                      "transition-opacity",
                      currentPage >= totalPages - 1 && 'pointer-events-none opacity-50'
                    )}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}

        {/* Page info */}
        {totalElements > 0 && (
          <div className="mt-4 text-center text-sm text-muted-foreground">
            Hiển thị {currentPage * pageSize + 1} - {Math.min((currentPage + 1) * pageSize, totalElements)} trong tổng số {totalElements} sản phẩm
          </div>
        )}

        {/* View All Products Section */}
        {showViewAllButton && basePath !== '/products' && (
          <div className="mt-12 mb-8">
            <Card className="bg-gradient-to-r from-primary/5 via-primary/10 to-primary/5 border-primary/20">
              <CardContent className="py-8">
                <div className="text-center space-y-4">
                  <div className="inline-flex items-center justify-center p-3 rounded-full bg-primary/10">
                    <Smartphone className="w-8 h-8 text-primary" />
                  </div>
                  <div>
                    <h3 className="text-xl font-semibold mb-2">Khám phá thêm sản phẩm</h3>
                    <p className="text-muted-foreground max-w-md mx-auto">
                      Xem toàn bộ các sản phẩm điện thoại, tablet và phụ kiện với nhiều lựa chọn đa dạng
                    </p>
                  </div>
                  <Link href="/products">
                    <Button size="lg" className="gap-2 group">
                      <Package className="w-5 h-5" />
                      Xem tất cả sản phẩm
                      <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>

      {/* Comparison Bar */}
      {selectedForCompare.size > 0 && (
        <ComparisonBar
          selectedProducts={selectedProductsList}
          onRemove={handleRemoveFromCompare}
          onClear={handleClearCompare}
        />
      )}
    </div>
  );
}
