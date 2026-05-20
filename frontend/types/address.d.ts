/**
 * Tỉnh/Thành phố - Response từ Backend API
 */
export interface Province {
  id: number;
  provinceCode: string;
  name: string;
  placeType: string;
  country: string;
  // Alias cho tương thích với code cũ
  code: number; // Mapped từ provinceCode (parseInt)
}

/**
 * Phường/Xã - Response từ Backend API
 */
export interface Ward {
  id: number;
  wardCode: string;
  name: string;
  provinceCode: string;
  // Alias cho tương thích với code cũ
  code: number; // Mapped từ wardCode (parseInt)
}

export interface AddressOpenAPIResponse<T> {
  code?: number;
  name?: string;
  codename?: string;
  division_type?: string;
  district_code?: number;
}

export interface ApiVersion {
  version: string;
  date: string;
}

/**
 * Dữ liệu form địa chỉ được chọn
 */
export interface AddressFormData {
  provinceCode: number;
  provinceName: string;
  wardCode: number;
  wardName: string;
  streetAddress?: string;
}

/**
 * Address Response từ Backend API
 */
export interface AddressResponse {
  id: number;
  recipientName: string;
  phoneNumber: string;
  streetAddress: string;
  ward: string;
  wardCode: string;
  province: string;
  provinceCode: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Address Request để gửi lên Backend API
 */
export interface AddressRequest {
  recipientName: string;
  phoneNumber: string;
  streetAddress: string;
  ward: string;
  wardCode?: string;
  province: string;
  provinceCode?: string;
  isDefault?: boolean;
}