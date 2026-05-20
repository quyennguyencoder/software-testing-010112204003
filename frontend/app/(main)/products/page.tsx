'use client';

import React, { useState, useEffect, useCallback, useRef, Suspense } from 'react';
import { useProductDiscovery } from '@/hooks/useNewProductDiscovery';
import { useBrands } from '@/hooks/useBrands';
import { useCategories } from '@/hooks/useCategories';
import { ProductCard, ProductCardSkeleton } from '@/components/features/products/NewProductCard';
import { NewProductFilterSidebar } from '@/components/features/products/NewProductFilterSidebar';
import { QuickLinksBar } from '@/components/features';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
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
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import {
  Search,
  Grid3x3,
  LayoutGrid,
  GitCompare,
  SlidersHorizontal,
  X,
} from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCartStore } from '@/store/cartStore';
import { useWishlistStore } from '@/store/wishlistStore';
import { toast } from 'sonner';
import type {
  ProductCardResponse,
  ProductSearchRequest,
  ProductFilterRequest
} from '@/services/new-product.service';

const SORT_OPTIONS = [
  { value: 'created_date:desc', label: 'Mới nhất', sortBy: 'created_date', sortDirection: 'desc' as const },
  { value: 'created_date:asc', label: 'Cũ nhất', sortBy: 'created_date', sortDirection: 'asc' as const },
  { value: 'price:asc', label: 'Giá: Thấp đến cao', sortBy: 'price', sortDirection: 'asc' as const },
  { value: 'price:desc', label: 'Giá: Cao đến thấp', sortBy: 'price', sortDirection: 'desc' as const },
  { value: 'rating:desc', label: 'Đánh giá cao nhất', sortBy: 'rating', sortDirection: 'desc' as const },
  { value: 'name:asc', label: 'Tên: A-Z', sortBy: 'name', sortDirection: 'asc' as const },
  { value: 'name:desc', label: 'Tên: Z-A', sortBy: 'name', sortDirection: 'desc' as const },
];

function ProductsPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  // Get initial values from URL
  const initialKeyword = searchParams.get('keyword') || '';
  const initialCategoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined;
  const initialTab = searchParams.get('tab') || 'all';

  // State
  const [activeTab, setActiveTab] = useState(initialTab);
  const [searchKeyword, setSearchKeyword] = useState(initialKeyword);
  const [gridCols, setGridCols] = useState<3 | 4>(4);
  const [currentSort, setCurrentSort] = useState('created_date:desc');
  const [isInitialized, setIsInitialized] = useState(false);

  // Filter state
  const [currentFilters, setCurrentFilters] = useState<ProductFilterRequest>({
    categoryIds: initialCategoryId ? [initialCategoryId] : undefined,
    sortBy: 'created_date',
    sortDirection: 'desc',
    page: 0,
    size: 20,
  });

  // Track if filter change is from user interaction (not initial load)
  const isUserInteraction = useRef(false);

  // Comparison state
  const [compareMode, setCompareMode] = useState(false);
  const [selectedForCompare, setSelectedForCompare] = useState<Set<number>>(new Set());
  const [quickViewProductId, setQuickViewProductId] = useState<number | null>(null);

  // Cart and Wishlist stores
  const { addItem: addToCart } = useCartStore();
  const { toggleItem: toggleWishlistItem, isInWishlist } = useWishlistStore();

  // Fetch brands and categories for filters
  const { brands, loading: brandsLoading } = useBrands();
  const { categories, loading: categoriesLoading } = useCategories({ parentId: null });

  // Use product discovery hook
  const {
    featuredProducts,
    bestSellingProducts,
    newArrivals,
    onSaleProducts,
    discoveryLoading,
    searchResults,
    searchLoading,
    searchError,
    filterResults,
    filterLoading,
    filterError,
    performSearch,
    performFilter,
    clearResults,
  } = useProductDiscovery();

  // Track URL keyword changes for search from header
  const urlKeyword = searchParams.get('keyword') || '';

  // Handle URL keyword changes (when searching from header while on products page)
  useEffect(() => {
    if (isInitialized && urlKeyword) {
      // URL keyword changed, perform new search
      setSearchKeyword(urlKeyword);
      const [sortBy, sortDirection] = currentSort.split(':') as [string, 'asc' | 'desc'];
      performSearch({
        keyword: urlKeyword,
        sortBy,
        sortDirection,
        page: 0,
        size: 20,
      });
    } else if (isInitialized && !urlKeyword && searchKeyword) {
      // URL keyword removed, clear search and go back to filter
      setSearchKeyword('');
      clearResults();
      performFilter(currentFilters);
    }
  }, [urlKeyword]);

  // Find "Điện thoại" category and set as default on initial load
  // OR perform search if keyword is provided in URL
  useEffect(() => {
    if (!isInitialized && categories.length > 0) {
      // If keyword is provided, perform search instead of filter
      if (initialKeyword) {
        const [sortBy, sortDirection] = currentSort.split(':') as [string, 'asc' | 'desc'];
        performSearch({
          keyword: initialKeyword,
          sortBy,
          sortDirection,
          page: 0,
          size: 20,
        });
        setIsInitialized(true);
        return;
      }

      if (!initialCategoryId) {
        // Find the phone category (Điện thoại)
        const phoneCategory = categories.find(cat =>
          cat.name.toLowerCase().includes('điện thoại') ||
          cat.name.toLowerCase().includes('phone') ||
          cat.name.toLowerCase().includes('smartphone')
        );

        if (phoneCategory) {
          // Set phone category as default
          setActiveTab(String(phoneCategory.id));
          const newFilters: ProductFilterRequest = {
            ...currentFilters,
            categoryIds: [phoneCategory.id],
            page: 0,
          };
          setCurrentFilters(newFilters);
          performFilter(newFilters);
        } else {
          // If no phone category found, load all products
          performFilter(currentFilters);
        }
      } else {
        // Use the category from URL
        performFilter(currentFilters);
      }
      setIsInitialized(true);
    }
  }, [categories, isInitialized, initialCategoryId, initialKeyword]);

  // Auto-filter when filters change (from user interaction)
  useEffect(() => {
    if (isUserInteraction.current && isInitialized) {
      performFilter(currentFilters);
      isUserInteraction.current = false;
    }
  }, [currentFilters, isInitialized]);

  // Get current products to display
  const getCurrentProducts = (): ProductCardResponse[] => {
    // Use filter results if available
    if (filterResults && filterResults.content.length > 0) {
      return filterResults.content;
    }
    // Use search results if available
    if (searchResults && searchResults.content.length > 0) {
      return searchResults.content;
    }
    return [];
  };

  const currentProducts = getCurrentProducts();
  const currentLoading = filterLoading || searchLoading;
  const currentError = searchError || filterError;
  const totalPages = filterResults?.totalPages || searchResults?.totalPages || 1;
  const totalElements = filterResults?.totalElements || searchResults?.totalElements || currentProducts.length;

  // Handle search - uses GET /products/search
  const handleSearch = async () => {
    if (!searchKeyword.trim()) return;

    const [sortBy, sortDirection] = currentSort.split(':') as [string, 'asc' | 'desc'];
    const searchRequest: ProductSearchRequest = {
      keyword: searchKeyword,
      sortBy,
      sortDirection,
      page: 0,
      size: 20,
    };

    await performSearch(searchRequest);
  };

  // Handle category tab change
  const handleCategoryTabChange = (categoryId: string) => {
    setActiveTab(categoryId);
    clearResults();

    // Update URL
    const params = new URLSearchParams(searchParams);
    params.set('tab', categoryId);
    if (categoryId !== 'all') {
      params.set('categoryId', categoryId);
    } else {
      params.delete('categoryId');
    }
    router.push(`/products?${params.toString()}`, { scroll: false });

    // Update filters with new category
    const newFilters: ProductFilterRequest = {
      ...currentFilters,
      categoryIds: categoryId !== 'all' ? [Number(categoryId)] : undefined,
      page: 0,
    };
    setCurrentFilters(newFilters);
    performFilter(newFilters);
  };

  // Handle sort changes
  const handleSortChange = (value: string) => {
    setCurrentSort(value);
    const [sortBy, sortDirection] = value.split(':') as [string, 'asc' | 'desc'];

    const newFilters = {
      ...currentFilters,
      sortBy,
      sortDirection,
      page: 0, // Reset to first page
    };
    isUserInteraction.current = true;
    setCurrentFilters(newFilters);
  };

  // Handle filter changes - auto-filter on tick
  const handleFiltersChange = (filters: ProductFilterRequest) => {
    isUserInteraction.current = true;
    setCurrentFilters(filters);
  };

  const handleClearFilters = () => {
    const clearedFilters: ProductFilterRequest = {
      categoryIds: activeTab !== 'all' ? [Number(activeTab)] : undefined,
      sortBy: 'created_date',
      sortDirection: 'desc',
      page: 0,
      size: 20,
    };
    isUserInteraction.current = true;
    setCurrentFilters(clearedFilters);
    setSearchKeyword('');
  };

  // Handle pagination
  const handlePageChange = (page: number) => {
    const newFilters = { ...currentFilters, page };
    isUserInteraction.current = true;
    setCurrentFilters(newFilters);
  };

  // Handle comparison
  const handleSelectForCompare = (productId: number, selected: boolean) => {
    if (selected) {
      if (selectedForCompare.size >= 4) {
        alert('Chỉ có thể so sánh tối đa 4 sản phẩm!');
        return;
      }
    }

    setSelectedForCompare(prev => {
      const newSet = new Set(prev);
      if (selected) {
        newSet.add(productId);
      } else {
        newSet.delete(productId);
      }
      return newSet;
    });
  };

  const handleClearCompare = () => {
    setSelectedForCompare(new Set());
    setCompareMode(false);
  };

  // Other handlers
  const handleAddToCart = (productId: number) => {
    const product = currentProducts.find(p => p.id === productId);
    if (!product) return;

    addToCart({
      productId: product.id,
      productName: product.name,
      productImage: product.thumbnailUrl || '/placeholder-product.png',
      price: product.discountedPrice || product.minPrice,
      quantity: 1,
      color: product.color,
      storage: product.storage,
    });
    toast.success('Đã thêm vào giỏ hàng', {
      description: product.name,
    });
  };

  const handleToggleWishlist = (productId: number) => {
    const product = currentProducts.find(p => p.id === productId);
    if (!product) return;

    const wasInWishlist = isInWishlist(productId);
    toggleWishlistItem({
      productId: product.id,
      productName: product.name,
      productImage: product.thumbnailUrl || '/placeholder-product.png',
      price: product.discountedPrice || product.minPrice,
      inStock: product.inStock,
    });

    if (wasInWishlist) {
      toast.info('Đã xóa khỏi danh sách yêu thích');
    } else {
      toast.success('Đã thêm vào danh sách yêu thích', {
        description: product.name,
      });
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // Prepare filter options - include children for nested display
  const mapCategoryToFilterOption = (cat: typeof categories[0]): { id: number; label: string; count?: number; children?: any[] } => ({
    id: cat.id,
    label: cat.name,
    count: cat.productCount,
    children: cat.children?.map(mapCategoryToFilterOption),
  });
  const categoryOptions = categories.map(mapCategoryToFilterOption);

  const brandOptions = brands.map(brand => ({
    id: brand.id,
    label: brand.name,
    count: brand.productCount,
  }));

  // Generate pagination range
  const getPaginationRange = () => {
    const range: (number | 'ellipsis')[] = [];
    const maxVisible = 5;
    const currentPage = currentFilters.page || 0;

    if (totalPages <= maxVisible) {
      for (let i = 0; i < totalPages; i++) {
        range.push(i);
      }
    } else {
      if (currentPage <= 2) {
        for (let i = 0; i < 3; i++) range.push(i);
        range.push('ellipsis');
        range.push(totalPages - 1);
      } else if (currentPage >= totalPages - 3) {
        range.push(0);
        range.push('ellipsis');
        for (let i = totalPages - 3; i < totalPages; i++) range.push(i);
      } else {
        range.push(0);
        range.push('ellipsis');
        range.push(currentPage - 1);
        range.push(currentPage);
        range.push(currentPage + 1);
        range.push('ellipsis');
        range.push(totalPages - 1);
      }
    }

    return range;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-white">
      <div className="container mx-auto px-4 py-6">
        {/* Quick Links Bar */}
        <div className="mb-6">
          <QuickLinksBar />
        </div>

        {/* Search and Controls Bar */}
        <div className="flex flex-col md:flex-row gap-4 mb-6">
          <div className="flex-1 flex gap-2">
            <Input
              placeholder="Tìm kiếm sản phẩm..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={handleKeyPress}
              className="flex-1"
            />
            <Button onClick={handleSearch} disabled={searchLoading}>
              <Search className="w-4 h-4 mr-2" />
              {searchLoading ? 'Đang tìm...' : 'Tìm kiếm'}
            </Button>
          </div>
          <div className="flex gap-2">
            <Select value={currentSort} onValueChange={handleSortChange}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="Sắp xếp" />
              </SelectTrigger>
              <SelectContent>
                {SORT_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Button
              variant={compareMode ? 'default' : 'outline'}
              onClick={() => setCompareMode(!compareMode)}
            >
              <GitCompare className="w-4 h-4 mr-2" />
              So sánh
              {selectedForCompare.size > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {selectedForCompare.size}
                </Badge>
              )}
            </Button>

            <div className="hidden md:flex gap-1 border rounded-md p-1">
              <Button
                variant={gridCols === 3 ? 'default' : 'ghost'}
                size="icon"
                onClick={() => setGridCols(3)}
                className="h-8 w-8"
              >
                <Grid3x3 className="w-4 h-4" />
              </Button>
              <Button
                variant={gridCols === 4 ? 'default' : 'ghost'}
                size="icon"
                onClick={() => setGridCols(4)}
                className="h-8 w-8"
              >
                <LayoutGrid className="w-4 h-4" />
              </Button>
            </div>

            {/* Mobile Filter Button */}
            <Sheet>
              <SheetTrigger asChild>
                <Button variant="outline" className="md:hidden">
                  <SlidersHorizontal className="w-4 h-4 mr-2" />
                  Bộ lọc
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-[300px] overflow-y-auto">
                <NewProductFilterSidebar
                  categories={categoryOptions}
                  brands={brandOptions}
                  currentFilters={currentFilters}
                  onFiltersChange={handleFiltersChange}
                  onClearFilters={handleClearFilters}
                  isLoading={filterLoading}
                  autoApply={true}
                />
              </SheetContent>
            </Sheet>
          </div>
        </div>

        {/* Main Content */}
        <div className="flex gap-6">
          {/* Desktop Filter Sidebar */}
          <div className="hidden lg:block w-80 flex-shrink-0">
            <NewProductFilterSidebar
              categories={categoryOptions}
              brands={brandOptions}
              currentFilters={currentFilters}
              onFiltersChange={handleFiltersChange}
              onClearFilters={handleClearFilters}
              isLoading={filterLoading}
              autoApply={true}
            />
          </div>

          {/* Products Grid */}
          <div className="flex-1">
            {/* Results info */}
            {currentProducts.length > 0 && (
              <div className="mb-4 flex items-center justify-between">
                <p className="text-muted-foreground">
                  Tìm thấy <strong>{totalElements.toLocaleString('vi-VN')}</strong> sản phẩm
                </p>
                {activeTab !== 'all' && (
                  <Badge variant="outline" className="flex items-center gap-1">
                    {categories.find(c => String(c.id) === activeTab)?.name || 'Danh mục'}
                    <button
                      onClick={() => handleCategoryTabChange('all')}
                      className="ml-1 hover:bg-muted rounded-full p-0.5"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </Badge>
                )}
              </div>
            )}

            {/* Error display */}
            {currentError && (
              <div className="bg-destructive/10 text-destructive px-4 py-3 rounded-lg mb-4">
                {currentError}
              </div>
            )}

            {/* Products grid */}
            {currentLoading ? (
              <div className={`grid grid-cols-1 sm:grid-cols-2 ${gridCols === 3 ? 'lg:grid-cols-3' : 'lg:grid-cols-4'} gap-6`}>
                {Array.from({ length: 12 }).map((_, i) => (
                  <ProductCardSkeleton key={i} />
                ))}
              </div>
            ) : currentProducts.length === 0 ? (
              <div className="text-center py-16">
                <div className="max-w-md mx-auto">
                  <div className="mb-6">
                    <Search className="w-16 h-16 mx-auto text-muted-foreground/50" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">Không tìm thấy sản phẩm</h3>
                  <p className="text-muted-foreground mb-6">
                    {searchKeyword || filterResults
                      ? 'Thử điều chỉnh bộ lọc hoặc tìm kiếm với từ khóa khác'
                      : 'Đang tải sản phẩm...'}
                  </p>
                  <div className="flex gap-2 justify-center">
                    {searchKeyword && (
                      <Button onClick={() => setSearchKeyword('')} variant="outline">
                        Xóa từ khóa
                      </Button>
                    )}
                    <Button onClick={handleClearFilters} variant="outline">
                      Xóa bộ lọc
                    </Button>
                  </div>
                </div>
              </div>
            ) : (
              <>
                <div className={`grid grid-cols-1 sm:grid-cols-2 ${gridCols === 3 ? 'lg:grid-cols-3' : 'lg:grid-cols-4'} gap-6`}>
                  {currentProducts.map((product) => (
                    <ProductCard
                      key={product.id}
                      product={product}
                      onAddToCart={handleAddToCart}
                      compareMode={compareMode}
                      isSelected={selectedForCompare.has(product.id)}
                      onSelectForCompare={handleSelectForCompare}
                      onToggleWishlist={handleToggleWishlist}
                      isInWishlist={isInWishlist(product.id)}
                    />
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="mt-8">
                    <Pagination>
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationPrevious
                            onClick={() => (currentFilters.page || 0) > 0 && handlePageChange((currentFilters.page || 0) - 1)}
                            className={(currentFilters.page || 0) === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>

                        {getPaginationRange().map((page, index) => (
                          <PaginationItem key={index}>
                            {page === 'ellipsis' ? (
                              <PaginationEllipsis />
                            ) : (
                              <PaginationLink
                                onClick={() => handlePageChange(page)}
                                isActive={(currentFilters.page || 0) === page}
                                className="cursor-pointer"
                              >
                                {page + 1}
                              </PaginationLink>
                            )}
                          </PaginationItem>
                        ))}

                        <PaginationItem>
                          <PaginationNext
                            onClick={() => (currentFilters.page || 0) < totalPages - 1 && handlePageChange((currentFilters.page || 0) + 1)}
                            className={(currentFilters.page || 0) === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>

                    <p className="text-center text-sm text-muted-foreground mt-4">
                      Trang {(currentFilters.page || 0) + 1} / {totalPages}
                    </p>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* Comparison Bar - Show when products are selected for comparison */}
        {selectedForCompare.size > 0 && (
          <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-50">
            <Card className="shadow-lg">
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2">
                    <GitCompare className="w-5 h-5" />
                    <span className="font-medium">
                      Đã chọn {selectedForCompare.size} sản phẩm để so sánh
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      disabled={selectedForCompare.size < 2}
                      onClick={() => {
                        const ids = Array.from(selectedForCompare).join(',');
                        router.push(`/products/compare?ids=${ids}`);
                      }}
                    >
                      So sánh ngay
                    </Button>
                    <Button size="sm" variant="outline" onClick={handleClearCompare}>
                      <X className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  );
}

export default function ProductsPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-white">
        <div className="container mx-auto px-4 py-8">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {Array.from({ length: 12 }).map((_, i) => (
              <ProductCardSkeleton key={i} />
            ))}
          </div>
        </div>
      </div>
    }>
      <ProductsPageContent />
    </Suspense>
  );
}