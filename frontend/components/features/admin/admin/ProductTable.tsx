'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import {
  Search,
  MoreVertical,
  Edit,
  Trash2,
  Eye,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  RotateCcw,
} from 'lucide-react';

interface Product {
  id: number;
  name: string;
  price?: number;
  stockQuantity?: number;
  thumbnailUrl?: string;
  status?: boolean;
  categoryName?: string;
  brandName?: string;
}

interface Category {
  id: number;
  name: string;
  parentId?: number;
  parentName?: string;
  hasChildren?: boolean;
}

interface Brand {
  id: number;
  name: string;
  description?: string;
  logoUrl?: string;
}

interface ProductTableProps {
  products: Product[];
  categories: Category[];
  brands: Brand[];
  totalPages: number;
  currentPage: number;
  totalItems: number;
  onPageChange: (page: number) => void;
  onSearch: (keyword: string) => void;
  onSort: (sortBy: string, direction: 'asc' | 'desc') => void;
  onFilter: (filters: { categoryId?: number; brandId?: number; status?: boolean }) => void;
  onDelete: (id: number) => void;
  onEdit?: (product: Product) => void;
  onRestore?: (id: number) => void;
  isTrashView?: boolean;
}

export function ProductTable({
  products,
  categories,
  brands,
  totalPages,
  currentPage,
  totalItems,
  onPageChange,
  onSearch,
  onSort,
  onFilter,
  onDelete,
  onEdit,
  onRestore,
  isTrashView = false,
}: ProductTableProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
  const [brokenImageIds, setBrokenImageIds] = useState<Set<number>>(() => new Set());

  // Clear broken images cache when products change to prevent memory leak
  useEffect(() => {
    setBrokenImageIds((prev) => {
      const currentIds = new Set(products.map((p) => p.id));
      const filtered = new Set<number>();
      prev.forEach((id) => {
        if (currentIds.has(id)) filtered.add(id);
      });
      return filtered;
    });
  }, [products]);

  // Debounce search
  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    const timeoutId = setTimeout(() => {
      onSearch(value);
    }, 500);
    return () => clearTimeout(timeoutId);
  };

  const handleSort = (column: string) => {
    const newDirection = sortBy === column && sortDirection === 'asc' ? 'desc' : 'asc';
    setSortBy(column);
    setSortDirection(newDirection);
    onSort(column, newDirection);
  };

  const handleDeleteClick = (id: number) => {
    setSelectedProductId(id);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = () => {
    if (selectedProductId) {
      onDelete(selectedProductId);
      setDeleteDialogOpen(false);
      setSelectedProductId(null);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex items-center justify-between gap-4">
        {/* Search */}
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search products by name, SKU..."
            className="pl-9"
            value={searchQuery}
            onChange={(e) => handleSearchChange(e.target.value)}
          />
        </div>

        {/* Filters */}
        <div className="flex items-center gap-2">
          <Select onValueChange={(value: string) => onFilter({ categoryId: value === 'all' ? undefined : Number(value) })}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Categories</SelectItem>
              {categories.map((category) => (
                <SelectItem key={category.id} value={category.id.toString()}>
                  {category.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select onValueChange={(value: string) => onFilter({ brandId: value === 'all' ? undefined : Number(value) })}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Brand" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Brands</SelectItem>
              {brands.map((brand) => (
                <SelectItem key={brand.id} value={brand.id.toString()}>
                  {brand.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select onValueChange={(value: string) => onFilter({ status: value === 'true' ? true : value === 'false' ? false : undefined })}>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All</SelectItem>
              <SelectItem value="true">Active</SelectItem>
              <SelectItem value="false">Inactive</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Add New Button */}
        {!isTrashView && (
          <Button asChild>
            <Link href="/admin/products/new">Add Product</Link>
          </Button>
        )}
      </div>

      {/* Table */}
      <div className="rounded-lg border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-20">Image</TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort('name')}
                >
                  Name
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Brand</TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort('price')}
                >
                  Price
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead>
                <Button
                  variant="ghost"
                  size="sm"
                  className="-ml-3 h-8"
                  onClick={() => handleSort('stock')}
                >
                  Stock
                  <ArrowUpDown className="ml-2 h-4 w-4" />
                </Button>
              </TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="h-24 text-center text-muted-foreground">
                  No products found.
                </TableCell>
              </TableRow>
            ) : (
              products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>
                    <div className="relative h-12 w-12 overflow-hidden rounded-md border">
                      {product.thumbnailUrl && !brokenImageIds.has(product.id) ? (
                        <Image
                          src={product.thumbnailUrl}
                          alt={product.name}
                          fill
                          className="object-cover"
                          sizes="48px"
                          unoptimized={/^https?:\/\//i.test(product.thumbnailUrl)}
                          onError={() => {
                            setBrokenImageIds((prev) => {
                              const next = new Set(prev);
                              next.add(product.id);
                              return next;
                            });
                          }}
                        />
                      ) : (
                        <div className="flex h-full w-full items-center justify-center bg-muted text-xs text-muted-foreground">
                          No img
                        </div>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="font-medium">{product.name}</TableCell>
                  <TableCell>{product.categoryName || '-'}</TableCell>
                  <TableCell>{product.brandName || '-'}</TableCell>
                  <TableCell className="font-semibold text-primary">
                    {product.price !== undefined ? formatPrice(product.price) : '-'}
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={product.stockQuantity !== undefined && product.stockQuantity > 10 ? 'default' : product.stockQuantity !== undefined && product.stockQuantity > 0 ? 'secondary' : 'destructive'}
                    >
                      {product.stockQuantity ?? '-'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant={product.status ? 'default' : 'secondary'}>
                      {product.status ? 'Active' : 'Inactive'}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem asChild>
                          <Link href={`/admin/products/${product.id}`} className="flex items-center">
                            <Eye className="mr-2 h-4 w-4" />
                            View
                          </Link>
                        </DropdownMenuItem>
                        {!isTrashView && (
                          <>
                            <DropdownMenuItem 
                              onClick={() => onEdit && onEdit(product)}
                              className="flex items-center cursor-pointer"
                            >
                              <Edit className="mr-2 h-4 w-4" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              className="text-destructive"
                              onClick={() => handleDeleteClick(product.id)}
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </>
                        )}
                        {isTrashView && onRestore && (
                          <DropdownMenuItem onClick={() => onRestore(product.id)}>
                            <RotateCcw className="mr-2 h-4 w-4" />
                            Restore
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-muted-foreground">
          Showing {currentPage * 20 + 1} - {Math.min((currentPage + 1) * 20, totalItems)} of {totalItems} products
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
          >
            <ChevronLeft className="h-4 w-4" />
            Previous
          </Button>
          <div className="flex items-center gap-1">
            {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
              const pageNum = currentPage < 3 ? i : currentPage - 2 + i;
              if (pageNum >= totalPages) return null;
              return (
                <Button
                  key={pageNum}
                  variant={currentPage === pageNum ? 'default' : 'outline'}
                  size="sm"
                  className="h-8 w-8 p-0"
                  onClick={() => onPageChange(pageNum)}
                >
                  {pageNum + 1}
                </Button>
              );
            })}
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
          >
            Next
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will soft delete the product. You can restore it later from the trash.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
