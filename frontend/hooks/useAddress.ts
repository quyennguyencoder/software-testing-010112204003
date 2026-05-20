import { useEffect, useState, useCallback } from 'react';
import { Province, Ward } from '@/types/address';
import {
  listProvinces,
  getProvinceByCode,
  listWards,
  getWardByCode,
  getWardsByProvinceCode,
} from '@/services/address.service';

interface UseAddressReturn {
  provinces: Province[];
  wards: Ward[];
  loadingProvinces: boolean;
  loadingWards: boolean;
  selectedProvince: Province | null;
  selectedWard: Ward | null;
  selectProvince: (code: number) => Promise<void>;
  selectWard: (code: number) => Promise<void>;
  fetchProvinceByCode: (code: number) => Promise<void>;
  fetchWardByCode: (provinceCode: number, wardCode: number) => Promise<void>;
  reset: () => void;
}

/**
 * Custom hook để quản lý lựa chọn địa chỉ Việt Nam
 * Tải danh sách tỉnh/xã khi component mount
 */
export const useAddress = (): UseAddressReturn => {
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [wards, setWards] = useState<Ward[]>([]);

  const [loadingProvinces, setLoadingProvinces] = useState(false);
  const [loadingWards, setLoadingWards] = useState(false);

  const [selectedProvince, setSelectedProvince] = useState<Province | null>(null);
  const [selectedWard, setSelectedWard] = useState<Ward | null>(null);

  // Tải danh sách tỉnh khi component mount
  useEffect(() => {
    const loadProvinces = async () => {
      setLoadingProvinces(true);
      try {
        const data = await listProvinces();
        setProvinces(data);
      } finally {
        setLoadingProvinces(false);
      }
    };
    loadProvinces();
  }, []);

  // Không load tất cả wards khi mount - chỉ load khi chọn tỉnh
  // useEffect để load wards đã được xóa - sẽ load khi selectProvince được gọi

  // Chọn tỉnh
  const selectProvince = useCallback(async (code: number) => {
    setLoadingWards(true);
    try {
      const province = await getProvinceByCode(code);
      setSelectedProvince(province);
      
      // Load wards theo tỉnh đã chọn - dùng provinceCode (string) thay vì code (number)
      if (province) {
        const provinceCode = province.provinceCode || code.toString().padStart(2, '0');
        const wardsData = await getWardsByProvinceCode(provinceCode);
        setWards(wardsData);
      } else {
        setWards([]);
      }
    } catch (error) {
      console.error('Error selecting province:', error);
      setWards([]);
    } finally {
      setLoadingWards(false);
    }
  }, []);

  // Chọn phường/xã
  const selectWard = useCallback(async (code: number) => {
    const ward = await getWardByCode(code);
    setSelectedWard(ward);
  }, []);

  // Reset tất cả lựa chọn
  const reset = useCallback(() => {
    setSelectedProvince(null);
    setSelectedWard(null);
  }, []);

  // Fetch province by code (dùng cho default value)
  const fetchProvinceByCode = useCallback(async (code: number) => {
    try {
      const province = await getProvinceByCode(code);
      setSelectedProvince(province);
    } catch (error) {
      console.error('❌ Lỗi fetch province:', error);
    }
  }, []);

  // Fetch ward by code (dùng cho default value)
  const fetchWardByCode = useCallback(async (provinceCode: number, wardCode: number) => {
    try {
      // Đầu tiên load wards của province đó
      setLoadingWards(true);
      // Convert number thành string với padding (01, 79)
      const provinceCodeStr = provinceCode.toString().padStart(2, '0');
      const data = await getWardsByProvinceCode(provinceCodeStr);
      setWards(data);
      
      // Sau đó select ward
      const ward = await getWardByCode(wardCode);
      setSelectedWard(ward);
    } catch (error) {
      console.error('❌ Lỗi fetch ward:', error);
    } finally {
      setLoadingWards(false);
    }
  }, []);

  return {
    provinces,
    wards,
    loadingProvinces,
    loadingWards,
    selectedProvince,
    selectedWard,
    selectProvince,
    selectWard,
    fetchProvinceByCode,
    fetchWardByCode,
    reset,
  };
};
