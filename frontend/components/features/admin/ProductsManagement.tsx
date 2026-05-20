'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { ProductTable } from './ProductTable';
import { ProductImagesTable } from './ProductImagesTable';
import { Plus, Package, Trash2, Images } from 'lucide-react';
import { Button } from '@/components/ui/button';

// Type Definitions
type TabType = 'all' | 'trash' | 'images';

interface ProductFilters {
  deletedStatus?: 'all' | 'active' | 'deleted';
}

export function ProductsManagement() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<TabType>('all');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Quản lý sản phẩm</h1>
          <p className="text-sm text-muted-foreground">
            Quản lý kho sản phẩm của bạn
          </p>
        </div>
      </div>

      {/* Tabs - All in one row */}
      <div className="flex items-center gap-2 border-b border-border">
        <button
          onClick={() => setActiveTab('all')}
          className={`flex items-center gap-2 px-4 py-2 border-b-2 transition-colors ${
            activeTab === 'all'
              ? 'border-primary text-primary font-medium'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Package className="w-4 h-4" />
          All Products
        </button>

        <Button
          onClick={() => router.push('/manage/products/new')}
          className="flex items-center gap-2 h-9"
          variant="default"
        >
          <Plus className="w-4 h-4" />
          Add New
        </Button>

        <button
          onClick={() => setActiveTab('images')}
          className={`flex items-center gap-2 px-4 py-2 border-b-2 transition-colors ${
            activeTab === 'images'
              ? 'border-blue-500 text-blue-600 font-medium'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Images className="w-4 h-4" />
          Quản lý hình ảnh
        </button>

        <button
          onClick={() => setActiveTab('trash')}
          className={`flex items-center gap-2 px-4 py-2 border-b-2 transition-colors ${
            activeTab === 'trash'
              ? 'border-destructive text-destructive font-medium'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Trash2 className="w-4 h-4" />
          Trash
        </button>
      </div>

      {/* Content */}
      {activeTab === 'images' ? (
        <ProductImagesTable />
      ) : (
        <ProductTable
          filters={{
            deletedStatus: activeTab === 'trash' ? 'deleted' : 'active',
          }}
        />
      )}
    </div>
  );
}
