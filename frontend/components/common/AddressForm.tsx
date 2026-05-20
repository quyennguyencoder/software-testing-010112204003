'use client';

import { useState, useEffect, useCallback } from 'react';
import { useAddress } from '@/hooks/useAddress';
import { Combobox, type ComboboxOption } from '@/components/ui/combobox';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { AlertCircle, CheckCircle2, MapPin, X } from 'lucide-react';

interface AddressFormProps {
  onAddressChange?: (address: {
    provinceCode: number;
    provinceName: string;
    wardCode: number;
    wardName: string;
    streetAddress?: string;
  }) => void;
  showStreetAddress?: boolean;
  className?: string;
  disabled?: boolean;
  required?: boolean;
  error?: string;
  defaultValue?: {
    provinceCode?: number;
    wardCode?: number;
    streetAddress?: string;
  };
}

/**
 * Component form địa chỉ
 * Sử dụng Combobox cho tỉnh/thành phố và phường/xã
 * Cho phép người dùng tự nhập HOẶC chọn từ dropdown
 */
export const AddressForm: React.FC<AddressFormProps> = ({
  onAddressChange,
  showStreetAddress = true,
  className,
  disabled = false,
  required = true,
  error,
  defaultValue,
}) => {
  const {
    provinces,
    wards,
    loadingProvinces,
    loadingWards,
    selectedProvince,
    selectedWard,
    selectProvince,
    selectWard,
    reset,
    fetchProvinceByCode,
    fetchWardByCode,
  } = useAddress();

  const [streetAddress, setStreetAddress] = useState(defaultValue?.streetAddress || '');
  const [provinceInput, setProvinceInput] = useState('');
  const [wardInput, setWardInput] = useState('');
  const [isInitialized, setIsInitialized] = useState(false);

  // Khởi tạo giá trị mặc định
  useEffect(() => {
    const initializeDefaultValues = async () => {
      if (defaultValue && !isInitialized) {
        if (defaultValue.provinceCode) {
          await fetchProvinceByCode(defaultValue.provinceCode);
        }
        if (defaultValue.wardCode && defaultValue.provinceCode) {
          await fetchWardByCode(defaultValue.provinceCode, defaultValue.wardCode);
        }
        setIsInitialized(true);
      }
    };

    initializeDefaultValues();
  }, [defaultValue, isInitialized, fetchProvinceByCode, fetchWardByCode]);

  // Sync province input với selectedProvince
  useEffect(() => {
    if (selectedProvince) {
      setProvinceInput(selectedProvince.name);
    }
  }, [selectedProvince]);

  // Sync ward input với selectedWard
  useEffect(() => {
    if (selectedWard) {
      setWardInput(selectedWard.name);
    }
  }, [selectedWard]);

  // Thông báo component cha khi địa chỉ thay đổi
  useEffect(() => {
    if (onAddressChange) {
      // Nếu có selectedProvince và selectedWard (chọn từ dropdown)
      if (selectedProvince && selectedWard) {
        onAddressChange({
          provinceCode: selectedProvince.code,
          provinceName: selectedProvince.name,
          wardCode: selectedWard.code,
          wardName: selectedWard.name,
          streetAddress: streetAddress.trim() || undefined,
        });
      }
      // Nếu người dùng tự nhập (không có selectedProvince/selectedWard nhưng có input)
      else if (provinceInput.trim() && wardInput.trim()) {
        // Tìm province và ward từ input
        const province = provinces.find(
          (p) => p.name.toLowerCase() === provinceInput.toLowerCase()
        );
        const ward = wards.find(
          (w) => w.name.toLowerCase() === wardInput.toLowerCase()
        );

        onAddressChange({
          provinceCode: province?.code || 0,
          provinceName: provinceInput.trim(),
          wardCode: ward?.code || 0,
          wardName: wardInput.trim(),
          streetAddress: streetAddress.trim() || undefined,
        });
      }
    }
  }, [selectedProvince, selectedWard, provinceInput, wardInput, streetAddress, onAddressChange, provinces, wards]);

  // Xử lý chọn tỉnh từ dropdown
  const handleProvinceSelect = useCallback(
    async (option: ComboboxOption) => {
      if (option.code) {
        await selectProvince(Number(option.code));
      }
    },
    [selectProvince]
  );

  // Xử lý thay đổi input tỉnh (tự nhập)
  const handleProvinceChange = useCallback(
    async (value: string) => {
      setProvinceInput(value);
      // Reset ward khi thay đổi tỉnh
      setWardInput('');
      reset();

      // Nếu người dùng nhập đúng tên tỉnh, tự động load wards
      const matchedProvince = provinces.find(
        (p) => p.name.toLowerCase() === value.toLowerCase()
      );
      if (matchedProvince) {
        await selectProvince(matchedProvince.code);
      }
    },
    [reset, provinces, selectProvince]
  );

  // Xử lý chọn phường từ dropdown
  const handleWardSelect = useCallback(
    async (option: ComboboxOption) => {
      if (option.code && selectedProvince) {
        await selectWard(Number(option.code));
      }
    },
    [selectWard, selectedProvince]
  );

  // Xử lý thay đổi input phường (tự nhập)
  const handleWardChange = useCallback((value: string) => {
    setWardInput(value);
  }, []);

  const handleReset = useCallback(() => {
    reset();
    setStreetAddress('');
    setProvinceInput('');
    setWardInput('');
  }, [reset]);

  const handleStreetAddressChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setStreetAddress(e.target.value);
  }, []);

  // Convert provinces to ComboboxOptions
  const provinceOptions: ComboboxOption[] = provinces.map((p) => ({
    value: p.code.toString(),
    label: p.name,
    code: p.code,
  }));

  // Convert wards to ComboboxOptions
  const wardOptions: ComboboxOption[] = wards.map((w) => ({
    value: w.code.toString(),
    label: w.name,
    code: w.code,
  }));

  // Tính trạng thái validation
  const isValid = (selectedProvince && selectedWard) || (provinceInput.trim() && wardInput.trim());
  const isProvinceSelected = !!selectedProvince || !!provinceInput.trim();
  const isWardDisabled = disabled || (!selectedProvince && !provinceInput.trim()) || loadingWards;

  // Hiển thị skeleton khi loading
  if (loadingProvinces && provinces.length === 0) {
    return (
      <div className={cn('space-y-4 w-full', className)}>
        <div className="space-y-2">
          <div className="h-5 w-32 animate-pulse rounded-md bg-slate-200" />
          <div className="h-10 w-full animate-pulse rounded-md bg-slate-200" />
        </div>
        <div className="space-y-2">
          <div className="h-5 w-32 animate-pulse rounded-md bg-slate-200" />
          <div className="h-10 w-full animate-pulse rounded-md bg-slate-200" />
        </div>
        {showStreetAddress && (
          <div className="space-y-2">
            <div className="h-5 w-32 animate-pulse rounded-md bg-slate-200" />
            <div className="h-10 w-full animate-pulse rounded-md bg-slate-200" />
          </div>
        )}
      </div>
    );
  }

  return (
    <div className={cn('space-y-4 w-full', className)}>
      {/* Tỉnh/Thành phố */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="province-combobox" className="text-sm font-medium flex items-center gap-1">
            <MapPin className="h-3.5 w-3.5" />
            Tỉnh/Thành phố
            {required && <span className="text-red-500">*</span>}
          </Label>
          {isProvinceSelected && (
            <div className="flex items-center gap-1 text-xs text-green-600">
              <CheckCircle2 className="h-3 w-3" />
              Đã chọn
            </div>
          )}
        </div>
        <Combobox
          options={provinceOptions}
          value={provinceInput}
          onChange={handleProvinceChange}
          onSelect={handleProvinceSelect}
          placeholder={
            loadingProvinces
              ? 'Đang tải dữ liệu...'
              : 'Nhập hoặc chọn tỉnh/thành phố'
          }
          loading={loadingProvinces}
          disabled={disabled}
          error={error && !provinceInput.trim() ? error : undefined}
          allowCustomInput={true}
        />
      </div>

      {/* Phường/Xã */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="ward-combobox" className="text-sm font-medium">
            Phường/Xã
            {required && <span className="text-red-500">*</span>}
          </Label>
          {(selectedWard || wardInput.trim()) && (
            <div className="flex items-center gap-1 text-xs text-green-600">
              <CheckCircle2 className="h-3 w-3" />
              Đã chọn
            </div>
          )}
        </div>
        <Combobox
          options={wardOptions}
          value={wardInput}
          onChange={handleWardChange}
          onSelect={handleWardSelect}
          placeholder={
            !selectedProvince && !provinceInput.trim()
              ? 'Vui lòng nhập tỉnh/thành phố trước'
              : loadingWards
                ? 'Đang tải dữ liệu...'
                : wards.length === 0
                  ? 'Không có phường/xã'
                  : 'Nhập hoặc chọn phường/xã'
          }
          loading={loadingWards}
          disabled={isWardDisabled}
          error={error && !wardInput.trim() ? error : undefined}
          allowCustomInput={true}
        />
      </div>

      {/* Số nhà, tên đường (tùy chọn) */}
      {showStreetAddress && (
        <div className="space-y-2">
          <Label htmlFor="street-address" className="text-sm font-medium">
            Số nhà, tên đường
            <span className="text-gray-500 text-xs ml-1">(Tùy chọn)</span>
          </Label>
          <Input
            id="street-address"
            placeholder="Ví dụ: 123 Đường ABC, Tòa nhà XYZ"
            value={streetAddress}
            onChange={handleStreetAddressChange}
            disabled={disabled}
            className={cn(
              'transition-all',
              disabled && 'opacity-50 cursor-not-allowed',
              streetAddress && 'border-blue-500'
            )}
          />
          {streetAddress && (
            <p className="text-xs text-gray-500">
              {streetAddress.length}/200 ký tự
            </p>
          )}
        </div>
      )}

      {/* Hiển thị lỗi */}
      {error && (
        <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-md text-sm">
          <AlertCircle className="h-4 w-4 text-red-600 flex-shrink-0" />
          <p className="text-red-700">{error}</p>
        </div>
      )}

      {/* Hiển thị địa chỉ đã chọn */}
      {isValid && (
        <div className="p-3 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-md space-y-2 animate-in fade-in duration-300">
          <div className="flex items-center justify-between">
            <p className="text-blue-900 font-medium text-sm flex items-center gap-2">
              <MapPin className="h-4 w-4" />
              Địa chỉ đã chọn
            </p>
            {isValid && (
              <div className="flex items-center gap-1 text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full">
                <CheckCircle2 className="h-3 w-3" />
                Hợp lệ
              </div>
            )}
          </div>
          <div className="text-blue-800 text-sm bg-white p-2 rounded border border-blue-100">
            <p className="font-medium">
              {streetAddress && (
                <>
                  <span className="text-gray-900">{streetAddress}</span>
                  <span className="text-gray-500 mx-2">•</span>
                </>
              )}
              <span className="text-blue-700">
                {selectedWard?.name || wardInput.trim()}
              </span>
              <span className="text-gray-500 mx-2">•</span>
              <span className="text-blue-900 font-semibold">
                {selectedProvince?.name || provinceInput.trim()}
              </span>
            </p>
            {(selectedProvince || selectedWard) && (
              <div className="flex items-center gap-2 mt-2 text-xs text-gray-600">
                {selectedProvince && (
                  <div className="flex items-center gap-1">
                    <span className="font-medium">Mã tỉnh:</span>
                    <code className="bg-gray-100 px-1.5 py-0.5 rounded">
                      {selectedProvince.code}
                    </code>
                  </div>
                )}
                {selectedWard && (
                  <div className="flex items-center gap-1">
                    <span className="font-medium">Mã phường:</span>
                    <code className="bg-gray-100 px-1.5 py-0.5 rounded">
                      {selectedWard.code}
                    </code>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Nút xóa lựa chọn */}
      {(selectedProvince || provinceInput || streetAddress) && (
        <div className="flex gap-2">
          <Button
            type="button"
            onClick={handleReset}
            disabled={disabled}
            variant="outline"
            size="sm"
            className={cn(
              'flex-1 transition-all',
              disabled && 'opacity-50 cursor-not-allowed'
            )}
          >
            <X className="h-4 w-4 mr-2" />
            Xóa tất cả
          </Button>
          {streetAddress && (
            <Button
              type="button"
              onClick={() => setStreetAddress('')}
              disabled={disabled}
              variant="ghost"
              size="sm"
              className="transition-all"
            >
              Xóa địa chỉ đường
            </Button>
          )}
        </div>
      )}
    </div>
  );
};
