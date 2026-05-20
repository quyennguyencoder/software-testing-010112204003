/**
 * ProductsTable component - Display products in table format for admin
 */

'use client';

import { Edit, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { formatPrice } from '@/lib/utils';
import { cn } from '@/lib/utils';
import type { Product } from '@/lib/mockData';

interface ProductsTableProps {
  products: Product[];
}

export function ProductsTable({ products }: ProductsTableProps) {
  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h3 className="text-lg font-semibold text-foreground">Danh sách sản phẩm</h3>
        <Button className="gap-2">
          <span className="text-xl">+</span>
          Thêm sản phẩm
        </Button>
      </div>

      <div className="bg-card rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-secondary/50">
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Sản phẩm</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                  Danh mục
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Giá</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">Kho</th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden lg:table-cell">
                  Đã bán
                </th>
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-b border-border hover:bg-secondary/50">
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <span className="text-2xl">{product.image}</span>
                      <span className="font-medium line-clamp-1">{product.name}</span>
                    </div>
                  </td>
                  <td className="py-3 px-4 text-muted-foreground hidden md:table-cell">{product.category}</td>
                  <td className="py-3 px-4 font-semibold text-primary">{formatPrice(product.salePrice)}</td>
                  <td className="py-3 px-4 hidden sm:table-cell">
                    <span
                      className={cn(
                        "px-2 py-1 rounded-full text-xs font-semibold",
                        (product.stock ?? 0) > 30
                          ? "bg-green-100 text-green-700"
                          : (product.stock ?? 0) > 15
                          ? "bg-yellow-100 text-yellow-700"
                          : "bg-red-100 text-red-700"
                      )}
                    >
                      {product.stock}
                    </span>
                  </td>
                  <td className="py-3 px-4 hidden lg:table-cell">{product.sales}</td>
                  <td className="py-3 px-4">
                    <div className="flex gap-1">
                      <button className="p-2 hover:bg-secondary rounded-lg text-blue-600">
                        <Edit className="w-4 h-4" />
                      </button>
                      <button className="p-2 hover:bg-secondary rounded-lg text-red-600">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
