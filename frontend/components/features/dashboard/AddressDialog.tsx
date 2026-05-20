/**
 * AddressDialog component - Dialog để thêm/sửa địa chỉ
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AddressForm } from '@/components/common/AddressForm';
import { addressAPI } from '@/lib/api';
import type { AddressResponse, AddressRequest } from '@/types/address';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';
import { useAddress } from '@/hooks/useAddress';

interface AddressDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  address: AddressResponse | null; // null = thêm mới, có giá trị = sửa
  onSuccess: () => void;
}

export function AddressDialog({
  open,
  onOpenChange,
  address,
  onSuccess,
}: AddressDialogProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<{
    recipientName: string;
    phoneNumber: string;
    streetAddress: string;
    province: string;
    provinceCode: string;
    ward: string;
    wardCode: string;
    isDefault: boolean;
  }>({
    recipientName: '',
    phoneNumber: '',
    streetAddress: '',
    province: '',
    provinceCode: '',
    ward: '',
    wardCode: '',
    isDefault: false,
  });

  const [addressFormData, setAddressFormData] = useState<{
    provinceCode: number;
    provinceName: string;
    wardCode: number;
    wardName: string;
    streetAddress?: string;
  } | null>(null);

  const { fetchProvinceByCode, fetchWardByCode } = useAddress();

  // Load dữ liệu khi mở dialog để sửa
  useEffect(() => {
    if (open && address) {
      // Đang sửa địa chỉ
      setFormData({
        recipientName: address.recipientName || '',
        phoneNumber: address.phoneNumber || '',
        streetAddress: address.streetAddress || '',
        province: address.province || '',
        provinceCode: address.provinceCode || '',
        ward: address.ward || '',
        wardCode: address.wardCode || '',
        isDefault: address.isDefault || false,
      });

      // Load province và ward nếu có code
      if (address.provinceCode) {
        fetchProvinceByCode(Number(address.provinceCode));
      }
      if (address.wardCode && address.provinceCode) {
        fetchWardByCode(Number(address.provinceCode), Number(address.wardCode));
      }
    } else if (open && !address) {
      // Thêm mới - reset form
      setFormData({
        recipientName: '',
        phoneNumber: '',
        streetAddress: '',
        province: '',
        provinceCode: '',
        ward: '',
        wardCode: '',
        isDefault: false,
      });
      setAddressFormData(null);
    }
  }, [open, address, fetchProvinceByCode, fetchWardByCode]);

  // Xử lý khi AddressForm thay đổi
  const handleAddressChange = useCallback(
    (data: {
      provinceCode: number;
      provinceName: string;
      wardCode: number;
      wardName: string;
      streetAddress?: string;
    }) => {
      setAddressFormData(data);
      setFormData((prev) => ({
        ...prev,
        province: data.provinceName,
        provinceCode: data.provinceCode.toString(),
        ward: data.wardName,
        wardCode: data.wardCode.toString(),
        streetAddress: data.streetAddress || prev.streetAddress,
      }));
    },
    []
  );

  // Xử lý thay đổi input
  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  // Validation
  const validate = (): boolean => {
    if (!formData.recipientName.trim()) {
      toast.error('Vui lòng nhập tên người nhận');
      return false;
    }
    if (!formData.phoneNumber.trim()) {
      toast.error('Vui lòng nhập số điện thoại');
      return false;
    }
    if (!formData.streetAddress.trim()) {
      toast.error('Vui lòng nhập địa chỉ cụ thể');
      return false;
    }
    if (!formData.province.trim()) {
      toast.error('Vui lòng chọn hoặc nhập tỉnh/thành phố');
      return false;
    }
    if (!formData.ward.trim()) {
      toast.error('Vui lòng chọn hoặc nhập phường/xã');
      return false;
    }
    return true;
  };

  // Xử lý submit
  const handleSubmit = async () => {
    if (!validate()) return;

    setLoading(true);
    try {
      const requestData: AddressRequest = {
        recipientName: formData.recipientName.trim(),
        phoneNumber: formData.phoneNumber.trim(),
        streetAddress: formData.streetAddress.trim(),
        province: formData.province.trim(),
        provinceCode: formData.provinceCode || undefined,
        ward: formData.ward.trim(),
        wardCode: formData.wardCode || undefined,
        isDefault: formData.isDefault,
      };

      let response;
      if (address) {
        // Cập nhật
        response = await addressAPI.update(address.id, requestData);
      } else {
        // Thêm mới
        response = await addressAPI.create(requestData);
      }

      if (response.success) {
        toast.success(
          address ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ thành công'
        );
        onSuccess();
      } else {
        toast.error(response.message || 'Đã xảy ra lỗi');
      }
    } catch (err) {
      console.error('Error saving address:', err);
      toast.error('Đã xảy ra lỗi khi lưu địa chỉ');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {address ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ mới'}
          </DialogTitle>
          <DialogDescription>
            {address
              ? 'Cập nhật thông tin địa chỉ giao hàng của bạn'
              : 'Thêm địa chỉ giao hàng mới để sử dụng khi đặt hàng'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Tên người nhận */}
          <div className="space-y-2">
            <Label htmlFor="recipientName">
              Tên người nhận <span className="text-red-500">*</span>
            </Label>
            <Input
              id="recipientName"
              value={formData.recipientName}
              onChange={(e) => handleInputChange('recipientName', e.target.value)}
              placeholder="Nhập tên người nhận"
              disabled={loading}
            />
          </div>

          {/* Số điện thoại */}
          <div className="space-y-2">
            <Label htmlFor="phoneNumber">
              Số điện thoại <span className="text-red-500">*</span>
            </Label>
            <Input
              id="phoneNumber"
              value={formData.phoneNumber}
              onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
              placeholder="Nhập số điện thoại"
              disabled={loading}
            />
          </div>

          {/* AddressForm - Tỉnh/Phường */}
          <AddressForm
            onAddressChange={handleAddressChange}
            showStreetAddress={false}
            defaultValue={
              address && address.provinceCode && address.wardCode
                ? {
                    provinceCode: Number(address.provinceCode),
                    wardCode: Number(address.wardCode),
                    streetAddress: address.streetAddress,
                  }
                : undefined
            }
          />

          {/* Số nhà, tên đường */}
          <div className="space-y-2">
            <Label htmlFor="streetAddress">
              Số nhà, tên đường <span className="text-red-500">*</span>
            </Label>
            <Input
              id="streetAddress"
              value={formData.streetAddress}
              onChange={(e) => handleInputChange('streetAddress', e.target.value)}
              placeholder="Ví dụ: 123 Đường ABC, Tòa nhà XYZ"
              disabled={loading}
            />
          </div>

          {/* Đặt làm mặc định */}
          <div className="flex items-center space-x-2">
            <input
              type="checkbox"
              id="isDefault"
              checked={formData.isDefault}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, isDefault: e.target.checked }))
              }
              disabled={loading}
              className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
            />
            <Label htmlFor="isDefault" className="text-sm font-normal cursor-pointer">
              Đặt làm địa chỉ mặc định
            </Label>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={loading}
          >
            Hủy
          </Button>
          <Button onClick={handleSubmit} disabled={loading}>
            {loading ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Đang lưu...
              </>
            ) : address ? (
              'Cập nhật'
            ) : (
              'Thêm địa chỉ'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

