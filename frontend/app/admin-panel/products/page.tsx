'use client';

import { useState } from 'react';
import { ProductTable, ProductEditForm } from '@/components/features/admin';
import type { Product } from '@/types';

export default function ProductsPage() {
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
  };

  const handleEditSuccess = () => {
    setEditingProduct(null);
  };

  const handleEditCancel = () => {
    setEditingProduct(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Products</h1>
          <p className="text-sm text-muted-foreground">
            Manage your product inventory
          </p>
        </div>
      </div>

      <ProductTable onEdit={handleEdit} />

      {editingProduct && (
        <ProductEditForm
          product={editingProduct}
          onSuccess={handleEditSuccess}
          onCancel={handleEditCancel}
        />
      )}
    </div>
  );
}
