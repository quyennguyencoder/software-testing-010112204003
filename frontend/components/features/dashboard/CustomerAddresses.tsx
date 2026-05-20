/**
 * CustomerAddresses component - Address management for customers
 */

'use client';

import { useState, useEffect } from 'react';
import { Edit, Trash2, Plus, Loader2, MapPin } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { addressAPI } from '@/lib/api';
import type { AddressResponse } from '@/types/address';
import { AddressDialog } from './AddressDialog';
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
import { toast } from 'sonner';

export function CustomerAddresses() {
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState<AddressResponse | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingAddressId, setDeletingAddressId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  // Load danh sách địa chỉ
  const loadAddresses = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await addressAPI.getAll();
      if (response.success && response.data) {
        setAddresses(response.data);
      } else {
        setError(response.message || 'Không thể tải danh sách địa chỉ');
      }
    } catch (err) {
      console.error('Error loading addresses:', err);
      setError('Đã xảy ra lỗi khi tải danh sách địa chỉ');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAddresses();
  }, []);

  // Xử lý thêm địa chỉ mới
  const handleAddAddress = () => {
    setEditingAddress(null);
    setDialogOpen(true);
  };

  // Xử lý sửa địa chỉ
  const handleEditAddress = (address: AddressResponse) => {
    setEditingAddress(address);
    setDialogOpen(true);
  };

  // Xử lý xóa địa chỉ
  const handleDeleteClick = (addressId: number) => {
    setDeletingAddressId(addressId);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingAddressId) return;

    setDeleting(true);
    try {
      const response = await addressAPI.delete(deletingAddressId);
      if (response.success) {
        toast.success('Xóa địa chỉ thành công');
        await loadAddresses();
        setDeleteDialogOpen(false);
        setDeletingAddressId(null);
      } else {
        toast.error(response.message || 'Không thể xóa địa chỉ');
      }
    } catch (err) {
      console.error('Error deleting address:', err);
      toast.error('Đã xảy ra lỗi khi xóa địa chỉ');
    } finally {
      setDeleting(false);
    }
  };

  // Xử lý đặt địa chỉ mặc định
  const handleSetDefault = async (addressId: number) => {
    try {
      const response = await addressAPI.setDefault(addressId);
      if (response.success) {
        toast.success('Đặt địa chỉ mặc định thành công');
        await loadAddresses();
      } else {
        toast.error(response.message || 'Không thể đặt địa chỉ mặc định');
      }
    } catch (err) {
      console.error('Error setting default address:', err);
      toast.error('Đã xảy ra lỗi khi đặt địa chỉ mặc định');
    }
  };

  // Xử lý sau khi thêm/sửa thành công
  const handleDialogSuccess = () => {
    setDialogOpen(false);
    setEditingAddress(null);
    loadAddresses();
  };

  // Format địa chỉ đầy đủ
  const formatFullAddress = (address: AddressResponse) => {
    const parts = [
      address.streetAddress,
      address.ward,
      address.province,
    ].filter(Boolean);
    return parts.join(', ');
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-foreground">Địa chỉ của bạn</h3>
        <Button onClick={handleAddAddress} className="gap-2">
          <Plus className="w-4 h-4" />
          Thêm địa chỉ
        </Button>
      </div>

      {/* Loading state */}
      {loading && (
        <div className="flex items-center justify-center py-8">
          <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
          <span className="ml-2 text-sm text-muted-foreground">Đang tải...</span>
        </div>
      )}

      {/* Error state */}
      {error && !loading && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4">
          <p className="text-sm text-red-700">{error}</p>
          <Button
            variant="outline"
            size="sm"
            className="mt-2"
            onClick={loadAddresses}
          >
            Thử lại
          </Button>
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && addresses.length === 0 && (
        <div className="rounded-xl border border-border bg-card p-8 text-center">
          <MapPin className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground mb-4">Bạn chưa có địa chỉ nào</p>
          <Button onClick={handleAddAddress} className="gap-2">
            <Plus className="w-4 h-4" />
            Thêm địa chỉ đầu tiên
          </Button>
        </div>
      )}

      {/* Address list */}
      {!loading && !error && addresses.length > 0 && (
        <div className="grid gap-4">
          {addresses.map((address) => (
            <div
              key={address.id}
              className="rounded-xl border border-border bg-card p-4 shadow-sm"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="font-medium text-foreground">
                      {address.recipientName}
                    </span>
                    {address.isDefault && (
                      <span className="rounded-full bg-primary/15 px-2 py-0.5 text-xs font-semibold text-primary">
                        Mặc định
                      </span>
                    )}
                  </div>
                  <p className="text-muted-foreground text-sm">{address.phoneNumber}</p>
                  <p className="text-muted-foreground text-sm mt-1">
                    {formatFullAddress(address)}
                  </p>
                </div>
                <div className="flex gap-2">
                  {!address.isDefault && (
                    <button
                      onClick={() => handleSetDefault(address.id)}
                      className="rounded-lg p-2 text-blue-600 hover:bg-secondary transition-colors"
                      aria-label="Đặt làm mặc định"
                      title="Đặt làm mặc định"
                    >
                      <MapPin className="w-4 h-4" />
                    </button>
                  )}
                  <button
                    onClick={() => handleEditAddress(address)}
                    className="rounded-lg p-2 text-blue-600 hover:bg-secondary transition-colors"
                    aria-label="Chỉnh sửa địa chỉ"
                    title="Chỉnh sửa địa chỉ"
                  >
                    <Edit className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => handleDeleteClick(address.id)}
                    className="rounded-lg p-2 text-red-600 hover:bg-secondary transition-colors"
                    aria-label="Xóa địa chỉ"
                    title="Xóa địa chỉ"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Address Dialog */}
      <AddressDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        address={editingAddress}
        onSuccess={handleDialogSuccess}
      />

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xác nhận xóa địa chỉ</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc chắn muốn xóa địa chỉ này? Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleting}>Hủy</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              disabled={deleting}
              className="bg-red-600 hover:bg-red-700"
            >
              {deleting ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Đang xóa...
                </>
              ) : (
                'Xóa'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
