'use client';

import { useState, useEffect, useCallback } from 'react';
import { addressAPI } from '@/lib/api';
import { AddressForm } from '@/components/common/AddressForm';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Loader2, MapPin, Plus, Check } from 'lucide-react';
import type { AddressResponse, AddressFormData } from '@/types/address';
import { toast } from 'sonner';

interface AddressSelectorProps {
  onAddressChange: (address: {
    recipientName: string;
    phoneNumber: string;
    streetAddress: string;
    province: string;
    provinceCode: string;
    ward: string;
    wardCode: string;
  }) => void;
  defaultValue?: {
    recipientName?: string;
    phoneNumber?: string;
  };
  error?: string;
}

type AddressMode = 'existing' | 'new';

/**
 * Component quản lý địa chỉ trong checkout
 * - Cho phép chọn địa chỉ có sẵn của user
 * - Cho phép nhập địa chỉ mới với dropdown tỉnh/phường
 * - Tự động chọn địa chỉ mặc định
 */
export function AddressSelector({
  onAddressChange,
  defaultValue,
  error,
}: AddressSelectorProps) {
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState(true);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [mode, setMode] = useState<AddressMode>('existing');
  
  // Form states cho địa chỉ mới
  const [recipientName, setRecipientName] = useState(defaultValue?.recipientName || '');
  const [phoneNumber, setPhoneNumber] = useState(defaultValue?.phoneNumber || '');
  const [addressFormData, setAddressFormData] = useState<AddressFormData | null>(null);

  // Load danh sách địa chỉ của user
  useEffect(() => {
    const loadAddresses = async () => {
      try {
        setLoadingAddresses(true);
        const response = await addressAPI.getAll();
        if (response.success && response.data) {
          const userAddresses = response.data;
          setAddresses(userAddresses);
          
          // Tự động chọn địa chỉ mặc định
          const defaultAddress = userAddresses.find(addr => addr.isDefault);
          if (defaultAddress) {
            setSelectedAddressId(defaultAddress.id);
            handleSelectAddress(defaultAddress);
          } else if (userAddresses.length > 0) {
            // Nếu không có địa chỉ mặc định, chọn địa chỉ đầu tiên
            setSelectedAddressId(userAddresses[0].id);
            handleSelectAddress(userAddresses[0]);
          } else {
            // Không có địa chỉ nào, chuyển sang mode nhập mới
            setMode('new');
          }
        }
      } catch (err: any) {
        // Nếu không đăng nhập hoặc không có địa chỉ, chuyển sang mode nhập mới
        if (err.message?.includes('401') || err.message?.includes('404')) {
          setMode('new');
        } else {
          console.error('Error loading addresses:', err);
          toast.error('Không thể tải danh sách địa chỉ');
        }
      } finally {
        setLoadingAddresses(false);
      }
    };

    loadAddresses();
  }, []);

  // Xử lý chọn địa chỉ có sẵn
  const handleSelectAddress = useCallback((address: AddressResponse) => {
    onAddressChange({
      recipientName: address.recipientName,
      phoneNumber: address.phoneNumber,
      streetAddress: address.streetAddress,
      province: address.province,
      provinceCode: address.provinceCode,
      ward: address.ward,
      wardCode: address.wardCode,
    });
  }, [onAddressChange]);

  // Xử lý thay đổi địa chỉ được chọn
  const handleAddressIdChange = useCallback((addressId: string) => {
    const id = Number(addressId);
    setSelectedAddressId(id);
    const address = addresses.find(addr => addr.id === id);
    if (address) {
      handleSelectAddress(address);
    }
  }, [addresses, handleSelectAddress]);

  // Xử lý thay đổi mode
  const handleModeChange = useCallback((newMode: AddressMode) => {
    setMode(newMode);
    if (newMode === 'existing' && addresses.length > 0 && selectedAddressId) {
      const address = addresses.find(addr => addr.id === selectedAddressId);
      if (address) {
        handleSelectAddress(address);
      }
    }
  }, [mode, addresses, selectedAddressId, handleSelectAddress]);

  // Xử lý thay đổi form địa chỉ mới
  const handleAddressFormChange = useCallback((data: AddressFormData) => {
    setAddressFormData(data);
    
    // Chỉ update khi có đủ thông tin
    if (data.provinceName && data.wardName && recipientName && phoneNumber) {
      onAddressChange({
        recipientName,
        phoneNumber,
        streetAddress: data.streetAddress || '',
        province: data.provinceName,
        provinceCode: data.provinceCode.toString().padStart(2, '0'),
        ward: data.wardName,
        wardCode: data.wardCode.toString().padStart(5, '0'),
      });
    }
  }, [recipientName, phoneNumber, onAddressChange]);

  // Xử lý thay đổi tên người nhận
  const handleRecipientNameChange = useCallback((value: string) => {
    setRecipientName(value);
    if (addressFormData && value && phoneNumber) {
      handleAddressFormChange({
        ...addressFormData,
        provinceName: addressFormData.provinceName,
        wardName: addressFormData.wardName,
      });
    }
  }, [addressFormData, phoneNumber, handleAddressFormChange]);

  // Xử lý thay đổi số điện thoại
  const handlePhoneNumberChange = useCallback((value: string) => {
    setPhoneNumber(value);
    if (addressFormData && recipientName && value) {
      handleAddressFormChange({
        ...addressFormData,
        provinceName: addressFormData.provinceName,
        wardName: addressFormData.wardName,
      });
    }
  }, [addressFormData, recipientName, handleAddressFormChange]);

  // Loading state
  if (loadingAddresses) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
        <span className="ml-2 text-sm text-muted-foreground">Đang tải địa chỉ...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Mode selector */}
      <div className="flex gap-4 border-b border-border pb-4">
        <button
          type="button"
          onClick={() => handleModeChange('existing')}
          className={cn(
            'flex items-center gap-2 px-4 py-2 rounded-md transition-colors',
            mode === 'existing'
              ? 'bg-primary text-primary-foreground'
              : 'bg-muted text-muted-foreground hover:bg-muted/80'
          )}
        >
          <MapPin className="h-4 w-4" />
          <span>Chọn địa chỉ có sẵn</span>
          {addresses.length > 0 && (
            <span className="text-xs bg-white/20 px-2 py-0.5 rounded">
              {addresses.length}
            </span>
          )}
        </button>
        <button
          type="button"
          onClick={() => handleModeChange('new')}
          className={cn(
            'flex items-center gap-2 px-4 py-2 rounded-md transition-colors',
            mode === 'new'
              ? 'bg-primary text-primary-foreground'
              : 'bg-muted text-muted-foreground hover:bg-muted/80'
          )}
        >
          <Plus className="h-4 w-4" />
          <span>Nhập địa chỉ mới</span>
        </button>
      </div>

      {/* Existing addresses */}
      {mode === 'existing' && addresses.length > 0 && (
        <div className="space-y-3">
          <Label className="text-base font-semibold">Chọn địa chỉ giao hàng</Label>
          <div className="space-y-3">
            {addresses.map((address) => (
              <div
                key={address.id}
                onClick={() => handleAddressIdChange(address.id.toString())}
                className={cn(
                  'border rounded-lg p-4 cursor-pointer transition-all',
                  selectedAddressId === address.id
                    ? 'border-primary bg-primary/5'
                    : 'border-border hover:border-primary/50'
                )}
              >
                <div className="flex items-start gap-3">
                  <div className={cn(
                    'mt-1 w-4 h-4 rounded-full border-2 flex items-center justify-center flex-shrink-0',
                    selectedAddressId === address.id
                      ? 'border-primary bg-primary'
                      : 'border-muted-foreground'
                  )}>
                    {selectedAddressId === address.id && (
                      <div className="w-2 h-2 rounded-full bg-white" />
                    )}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-semibold">{address.recipientName}</span>
                          {address.isDefault && (
                            <span className="text-xs bg-primary text-primary-foreground px-2 py-0.5 rounded">
                              Mặc định
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {address.phoneNumber}
                        </p>
                        <p className="text-sm mt-1">
                          {address.streetAddress}
                          {address.streetAddress && (address.ward || address.province) && ', '}
                          {address.ward && `${address.ward}, `}
                          {address.province}
                        </p>
                      </div>
                      {selectedAddressId === address.id && (
                        <Check className="h-5 w-5 text-primary flex-shrink-0" />
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* No existing addresses */}
      {mode === 'existing' && addresses.length === 0 && (
        <div className="text-center py-8 border border-dashed border-border rounded-lg">
          <MapPin className="h-12 w-12 text-muted-foreground mx-auto mb-3" />
          <p className="text-sm text-muted-foreground mb-4">
            Bạn chưa có địa chỉ nào. Vui lòng thêm địa chỉ mới.
          </p>
          <Button
            type="button"
            variant="outline"
            onClick={() => setMode('new')}
          >
            <Plus className="h-4 w-4 mr-2" />
            Thêm địa chỉ mới
          </Button>
        </div>
      )}

      {/* New address form */}
      {mode === 'new' && (
        <div className="space-y-4">
          <Label className="text-base font-semibold">Thông tin người nhận</Label>
          
          <div>
            <Label htmlFor="recipient-name" className="text-sm font-medium mb-2">
              Họ và tên người nhận <span className="text-red-500">*</span>
            </Label>
            <Input
              id="recipient-name"
              placeholder="Nguyễn Văn A"
              value={recipientName}
              onChange={(e) => handleRecipientNameChange(e.target.value)}
              required
            />
          </div>

          <div>
            <Label htmlFor="phone-number" className="text-sm font-medium mb-2">
              Số điện thoại <span className="text-red-500">*</span>
            </Label>
            <Input
              id="phone-number"
              type="tel"
              placeholder="0901234567"
              value={phoneNumber}
              onChange={(e) => handlePhoneNumberChange(e.target.value)}
              required
            />
          </div>

          <div>
            <Label className="text-sm font-medium mb-2">
              Địa chỉ <span className="text-red-500">*</span>
            </Label>
            <AddressForm
              onAddressChange={handleAddressFormChange}
              showStreetAddress={true}
              required={true}
              error={error}
            />
          </div>
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded text-sm text-red-600">
          {error}
        </div>
      )}
    </div>
  );
}

function cn(...classes: (string | undefined | null | false)[]): string {
  return classes.filter(Boolean).join(' ');
}

