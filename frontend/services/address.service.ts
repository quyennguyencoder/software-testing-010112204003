import { Province, Ward, ApiVersion } from '@/types/address';
import fetchAPI from '@/lib/api';

const LOCATION_BASE_URL = '/locations';

/**
 * Transform ProvinceResponse từ backend sang Province type
 * Backend trả về: { id, provinceCode: "01", name, placeType, country }
 * Frontend expect: { id, provinceCode, name, placeType, country, code: number }
 */
const transformProvince = (data: any): Province => {
  if (!data) return data;
  
  // Parse provinceCode (string như "01", "79") thành number
  const provinceCodeStr = data.provinceCode || data.code?.toString() || '0';
  const code = parseInt(provinceCodeStr, 10);
  
  return {
    id: data.id,
    provinceCode: data.provinceCode || provinceCodeStr,
    name: data.name,
    placeType: data.placeType,
    country: data.country,
    code, // Alias cho tương thích với code cũ
  };
};

/**
 * Transform WardResponse từ backend sang Ward type
 * Backend trả về: { id, wardCode: "00070", name, provinceCode }
 * Frontend expect: { id, wardCode, name, provinceCode, code: number }
 */
const transformWard = (data: any): Ward => {
  if (!data) return data;
  
  // Parse wardCode (string như "00070") thành number
  const wardCodeStr = data.wardCode || data.code?.toString() || '0';
  const code = parseInt(wardCodeStr, 10);
  
  return {
    id: data.id,
    wardCode: data.wardCode || wardCodeStr,
    name: data.name,
    provinceCode: data.provinceCode,
    code, // Alias cho tương thích với code cũ
  };
};

/**
 * Lấy danh sách tất cả tỉnh/thành phố
 */
export const listProvinces = async (): Promise<Province[]> => {
  try {
    const response = await fetchAPI<any[]>(`${LOCATION_BASE_URL}/provinces`);
    const data = response.data || [];
    return data.map(transformProvince);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tỉnh:', error);
    return [];
  }
};

/**
 * Lấy chi tiết tỉnh/thành phố theo mã
 * @param code - Mã tỉnh (number hoặc string). Nếu là number, sẽ convert thành string với padding (01, 79)
 */
export const getProvinceByCode = async (code: number | string): Promise<Province | null> => {
  try {
    // Convert number thành string với padding (01, 79) để match với backend
    const provinceCode = typeof code === 'number' 
      ? code.toString().padStart(2, '0') 
      : code;
    const response = await fetchAPI<any>(`${LOCATION_BASE_URL}/provinces/${provinceCode}`);
    if (response.data) {
      return transformProvince(response.data);
    }
    return null;
  } catch (error) {
    console.error('Lỗi khi lấy tỉnh:', error);
    return null;
  }
};

/**
 * Lấy danh sách tất cả phường/xã
 */
export const listWards = async (): Promise<Ward[]> => {
  try {
    const response = await fetchAPI<any[]>(`${LOCATION_BASE_URL}/wards`);
    const data = response.data || [];
    return data.map(transformWard);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách phường/xã:', error);
    return [];
  }
};

/**
 * Lấy chi tiết phường/xã theo mã
 * @param code - Mã phường/xã (number hoặc string). Nếu là number, sẽ convert thành string với padding (00070)
 */
export const getWardByCode = async (code: number | string): Promise<Ward | null> => {
  try {
    // Convert number thành string với padding (00070) để match với backend
    const wardCode = typeof code === 'number' 
      ? code.toString().padStart(5, '0') 
      : code;
    const response = await fetchAPI<any>(`${LOCATION_BASE_URL}/wards/${wardCode}`);
    if (response.data) {
      return transformWard(response.data);
    }
    return null;
  } catch (error) {
    console.error('Lỗi khi lấy phường/xã:', error);
    return null;
  }
};

/**
 * Lấy danh sách phường/xã theo tỉnh/thành phố
 */
export const getWardsByProvinceCode = async (provinceCode: string | number): Promise<Ward[]> => {
  try {
    const response = await fetchAPI<any[]>(`${LOCATION_BASE_URL}/provinces/${provinceCode}/wards`);
    const data = response.data || [];
    return data.map(transformWard);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách phường/xã theo tỉnh:', error);
    return [];
  }
};

/**
 * Lấy danh sách toàn bộ cấp hành chính
 */
export const listAllDivisions = async (depth?: number): Promise<Province[]> => {
  try {
    const url = depth 
      ? `${LOCATION_BASE_URL}/divisions?depth=${depth}`
      : `${LOCATION_BASE_URL}/divisions`;
    const response = await fetchAPI<Province[]>(url);
    return response.data || [];
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cấp hành chính:', error);
    return [];
  }
};

