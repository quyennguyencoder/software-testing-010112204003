'use client'

import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Slider } from '@/components/ui/slider'
import { Badge } from '@/components/ui/badge'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { Separator } from '@/components/ui/separator'
import { X, Star, Smartphone, Zap, Monitor, HardDrive, Cpu } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { Brand } from '@/types/brand'
import type { Category } from '@/types/category'

interface FilterState {
  keyword?: string
  categoryIds: number[]
  brandIds: number[]
  minPrice?: number
  maxPrice?: number
  ramOptions: string[]
  storageOptions: string[]
  minBattery?: number
  maxBattery?: number
  screenSizeOptions: string[]
  osOptions: string[]
  minRating?: number
  inStockOnly?: boolean
  hasDiscountOnly?: boolean
  sortBy: string
  sortDirection: string
  page: number
  size: number
}

interface ProductFiltersProps {
  isOpen: boolean
  filters: FilterState
  onFilterChange: (filters: Partial<FilterState>) => void
  categories: Category[]
  brands: Brand[]
  className?: string
}

// Filter options constants
const RAM_OPTIONS = ['4GB', '6GB', '8GB', '12GB', '16GB', '32GB']
const STORAGE_OPTIONS = ['64GB', '128GB', '256GB', '512GB', '1TB', '2TB']
const SCREEN_SIZE_OPTIONS = ['5.4', '6.1', '6.4', '6.7', '6.8', '7.6', '10.9', '11', '12.9', '13', '14', '15.6', '16']
const OS_OPTIONS = ['iOS', 'Android', 'macOS', 'Windows 11', 'watchOS', 'iPadOS', 'Wear OS', 'HarmonyOS']

const PRICE_RANGES = [
  { min: 0, max: 5000000, label: 'Dưới 5 triệu' },
  { min: 5000000, max: 10000000, label: '5 - 10 triệu' },
  { min: 10000000, max: 20000000, label: '10 - 20 triệu' },
  { min: 20000000, max: 30000000, label: '20 - 30 triệu' },
  { min: 30000000, max: 50000000, label: '30 - 50 triệu' },
  { min: 50000000, max: undefined, label: 'Trên 50 triệu' }
]

const formatSliderPrice = (price: number): string => {
  if (price >= 1000000) {
    return `${(price / 1000000).toFixed(0)} triệu`
  }
  return `${(price / 1000).toFixed(0)}K`
}

export function ProductFilters({ 
  isOpen, 
  filters, 
  onFilterChange, 
  categories = [], 
  brands = [], 
  className 
}: ProductFiltersProps) {
  const [priceRange, setPriceRange] = useState<[number, number]>([
    filters.minPrice || 0,
    filters.maxPrice || 100000000
  ])

  const [batteryRange, setBatteryRange] = useState<[number, number]>([
    filters.minBattery || 3000,
    filters.maxBattery || 10000
  ])

  // Handle checkbox filters
  const handleCheckboxFilter = (
    filterKey: keyof FilterState,
    value: string | number,
    checked: boolean
  ) => {
    const currentValues = (filters[filterKey] as any[]) || []
    let newValues: any[]

    if (checked) {
      newValues = [...currentValues, value]
    } else {
      newValues = currentValues.filter(v => v !== value)
    }

    onFilterChange({ [filterKey]: newValues })
  }

  // Handle price range
  const handlePriceRangeChange = (values: number[]) => {
    setPriceRange([values[0], values[1]])
  }

  const handlePriceRangeCommit = (values: number[]) => {
    onFilterChange({
      minPrice: values[0] > 0 ? values[0] : undefined,
      maxPrice: values[1] < 100000000 ? values[1] : undefined
    })
  }

  // Handle battery range
  const handleBatteryRangeChange = (values: number[]) => {
    setBatteryRange([values[0], values[1]])
  }

  const handleBatteryRangeCommit = (values: number[]) => {
    onFilterChange({
      minBattery: values[0] > 3000 ? values[0] : undefined,
      maxBattery: values[1] < 10000 ? values[1] : undefined
    })
  }

  // Quick price range selection
  const handleQuickPriceRange = (min: number | undefined, max: number | undefined) => {
    onFilterChange({ minPrice: min, maxPrice: max })
    setPriceRange([min || 0, max || 100000000])
  }

  // Clear filter section
  const clearFilterSection = (section: string) => {
    switch (section) {
      case 'categories':
        onFilterChange({ categoryIds: [] })
        break
      case 'brands':
        onFilterChange({ brandIds: [] })
        break
      case 'price':
        onFilterChange({ minPrice: undefined, maxPrice: undefined })
        setPriceRange([0, 100000000])
        break
      case 'specs':
        onFilterChange({ 
          ramOptions: [], 
          storageOptions: [], 
          screenSizeOptions: [], 
          osOptions: [] 
        })
        break
      case 'battery':
        onFilterChange({ minBattery: undefined, maxBattery: undefined })
        setBatteryRange([3000, 10000])
        break
      case 'status':
        onFilterChange({ 
          inStockOnly: false, 
          hasDiscountOnly: false, 
          minRating: undefined 
        })
        break
    }
  }

  // Count active filters
  const getActiveFilterCount = (section: string): number => {
    switch (section) {
      case 'categories':
        return filters.categoryIds.length
      case 'brands':
        return filters.brandIds.length
      case 'price':
        return (filters.minPrice || filters.maxPrice) ? 1 : 0
      case 'specs':
        return filters.ramOptions.length + filters.storageOptions.length + 
               filters.screenSizeOptions.length + filters.osOptions.length
      case 'battery':
        return (filters.minBattery || filters.maxBattery) ? 1 : 0
      case 'status':
        return (filters.inStockOnly ? 1 : 0) + (filters.hasDiscountOnly ? 1 : 0) + 
               (filters.minRating ? 1 : 0)
      default:
        return 0
    }
  }

  if (!isOpen) return null

  return (
    <Card className={cn("h-fit sticky top-24", className)}>
      <CardHeader className="pb-4">
        <CardTitle className="text-lg flex items-center gap-2">
          <Smartphone className="h-5 w-5" />
          Bộ lọc sản phẩm
        </CardTitle>
      </CardHeader>

      <CardContent className="p-0">
        <div className="h-[calc(100vh-200px)] overflow-y-auto">
          <div className="px-6 pb-6 space-y-4">
            
            {/* Categories Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Danh mục</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('categories') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('categories')}
                      </Badge>
                    )}
                    {filters.categoryIds.length > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('categories')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-2">
                  {categories.map((category) => (
                    <div key={category.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`category-${category.id}`}
                        checked={filters.categoryIds.includes(category.id)}
                        onCheckedChange={(checked) =>
                          handleCheckboxFilter('categoryIds', category.id, !!checked)
                        }
                      />
                      <label
                        htmlFor={`category-${category.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        {category.name}
                      </label>
                    </div>
                  ))}
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Brands Filter */}
            <Collapsible>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Thương hiệu</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('brands') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('brands')}
                      </Badge>
                    )}
                    {filters.brandIds.length > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('brands')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-2">
                  {brands.map((brand) => (
                    <div key={brand.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`brand-${brand.id}`}
                        checked={filters.brandIds.includes(brand.id)}
                        onCheckedChange={(checked) =>
                          handleCheckboxFilter('brandIds', brand.id, !!checked)
                        }
                      />
                      <label
                        htmlFor={`brand-${brand.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        {brand.name}
                      </label>
                    </div>
                  ))}
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Price Range Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Khoảng giá</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('price') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {filters.minPrice || filters.maxPrice ? 
                          `${filters.minPrice ? formatSliderPrice(filters.minPrice) : '0'} - ${filters.maxPrice ? formatSliderPrice(filters.maxPrice) : '∞'}`
                          : '1'
                        }
                      </Badge>
                    )}
                    {(filters.minPrice || filters.maxPrice) && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('price')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-4">
                  {/* Quick selection buttons */}
                  <div className="grid grid-cols-2 gap-2">
                    {PRICE_RANGES.map((range, index) => (
                      <Button
                        key={index}
                        variant={
                          filters.minPrice === range.min && filters.maxPrice === range.max
                            ? "default"
                            : "outline"
                        }
                        size="sm"
                        onClick={() => handleQuickPriceRange(range.min, range.max)}
                        className="text-xs h-8"
                      >
                        {range.label}
                      </Button>
                    ))}
                  </div>
                  
                  <Separator />
                  
                  {/* Custom range slider */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm text-gray-600">
                      <span>{formatSliderPrice(priceRange[0])}</span>
                      <span>{formatSliderPrice(priceRange[1])}</span>
                    </div>
                    <Slider
                      value={priceRange}
                      onValueChange={handlePriceRangeChange}
                      onValueCommit={handlePriceRangeCommit}
                      min={0}
                      max={100000000}
                      step={1000000}
                      className="w-full"
                    />
                  </div>
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Technical Specs Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Thông số kỹ thuật</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('specs') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('specs')}
                      </Badge>
                    )}
                    {getActiveFilterCount('specs') > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('specs')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                  <div className="space-y-4">
                    {/* RAM Options */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Cpu className="h-4 w-4" />
                        <span className="text-sm font-medium">RAM</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {RAM_OPTIONS.map((ram) => (
                          <Button
                            key={ram}
                            variant={filters.ramOptions.includes(ram) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('ramOptions', ram, !filters.ramOptions.includes(ram))
                            }
                            className="h-8 text-xs"
                          >
                            {ram}
                          </Button>
                        ))}
                      </div>
                    </div>

                    {/* Storage Options */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <HardDrive className="h-4 w-4" />
                        <span className="text-sm font-medium">Bộ nhớ</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {STORAGE_OPTIONS.map((storage) => (
                          <Button
                            key={storage}
                            variant={filters.storageOptions.includes(storage) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('storageOptions', storage, !filters.storageOptions.includes(storage))
                            }
                            className="h-8 text-xs"
                          >
                            {storage}
                          </Button>
                        ))}
                      </div>
                    </div>

                    {/* Screen Size Options */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Monitor className="h-4 w-4" />
                        <span className="text-sm font-medium">Màn hình (inch)</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {SCREEN_SIZE_OPTIONS.map((size) => (
                          <Button
                            key={size}
                            variant={filters.screenSizeOptions.includes(size) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('screenSizeOptions', size, !filters.screenSizeOptions.includes(size))
                            }
                            className="h-8 text-xs"
                          >
                            {size}"
                          </Button>
                        ))}
                      </div>
                    </div>

                    {/* OS Options */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Smartphone className="h-4 w-4" />
                        <span className="text-sm font-medium">Hệ điều hành</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {OS_OPTIONS.map((os) => (
                          <Button
                            key={os}
                            variant={filters.osOptions.includes(os) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('osOptions', os, !filters.osOptions.includes(os))
                            }
                            className="h-8 text-xs"
                          >
                            {os}
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>
                </CollapsibleContent>
              </Collapsible>

              {/* Battery Filter */}
              <Collapsible>
                <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                  <div className="flex items-center justify-between w-full mr-2">
                    <span className="font-medium">Dung lượng pin</span>
                    <div className="flex items-center gap-2">
                      {getActiveFilterCount('battery') > 0 && (
                        <Badge variant="secondary" className="text-xs">
                          {filters.minBattery || filters.maxBattery ? 
                            `${filters.minBattery || 3000} - ${filters.maxBattery || 10000} mAh`
                            : '1'
                          }
                        </Badge>
                      )}
                      {(filters.minBattery || filters.maxBattery) && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation()
                            clearFilterSection('battery')
                          }}
                          className="h-4 w-4 p-0 hover:bg-red-100"
                        >
                          <X className="h-3 w-3" />
                        </Button>
                      )}
                    </div>
                  </div>
                </CollapsibleTrigger>
                <CollapsibleContent className="p-2">
                  <div className="space-y-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Zap className="h-4 w-4" />
                      <span className="text-sm text-gray-600">
                        {batteryRange[0]} - {batteryRange[1]} mAh
                      </span>
                    </div>
                    <Slider
                      value={batteryRange}
                      onValueChange={handleBatteryRangeChange}
                      onValueCommit={handleBatteryRangeCommit}
                      min={3000}
                      max={10000}
                      step={100}
                      className="w-full"
                    />
                  </div>
                </CollapsibleContent>
              </Collapsible>

              {/* Status & Rating Filter */}
              <Collapsible>
                <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                  <div className="flex items-center justify-between w-full mr-2">
                    <span className="font-medium">Trạng thái & Đánh giá</span>
                    <div className="flex items-center gap-2">
                      {getActiveFilterCount('status') > 0 && (
                        <Badge variant="secondary" className="text-xs">
                          {getActiveFilterCount('status')}
                        </Badge>
                      )}
                      {getActiveFilterCount('status') > 0 && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation()
                            clearFilterSection('status')
                          }}
                          className="h-4 w-4 p-0 hover:bg-red-100"
                        >
                          <X className="h-3 w-3" />
                        </Button>
                      )}
                    </div>
                  </div>
                </CollapsibleTrigger>
                <CollapsibleContent className="p-2">
                  <div className="space-y-4">
                    {/* Stock Status */}
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="inStockOnly"
                        checked={!!filters.inStockOnly}
                        onCheckedChange={(checked) =>
                          onFilterChange({ inStockOnly: !!checked })
                        }
                      />
                      <label
                        htmlFor="inStockOnly"
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        Chỉ hiện sản phẩm còn hàng
                      </label>
                    </div>

                    {/* Discount Status */}
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="hasDiscountOnly"
                        checked={!!filters.hasDiscountOnly}
                        onCheckedChange={(checked) =>
                          onFilterChange({ hasDiscountOnly: !!checked })
                        }
                      />
                      <label
                        htmlFor="hasDiscountOnly"
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        Chỉ hiện sản phẩm giảm giá
                      </label>
                    </div>

                    {/* Rating Filter */}
                    <div>
                      <span className="text-sm font-medium mb-2 block">
                        Đánh giá tối thiểu
                      </span>
                      <div className="flex flex-wrap gap-2">
                        {[3, 3.5, 4, 4.5, 5].map((rating) => (
                          <Button
                            key={rating}
                            variant={filters.minRating === rating ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              onFilterChange({ 
                                minRating: filters.minRating === rating ? undefined : rating 
                              })
                            }
                            className="h-8 text-xs flex items-center gap-1"
                          >
                            <Star className="h-3 w-3" />
                            {rating}+
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>
                </CollapsibleContent>
              </Collapsible>

          </div>
        </div>
      </CardContent>
    </Card>
  )
}