/**
 * Category Metadata Configuration
 * Định nghĩa các trường metadata cho từng loại sản phẩm
 */

export interface MetadataField {
  name: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'textarea' | 'boolean';
  options?: string[];
  placeholder?: string;
  required?: boolean;
  group?: string;
  min?: number;
  max?: number;
}

export const CATEGORY_METADATA: Record<string, MetadataField[]> = {
  // Điện thoại (categoryId phụ thuộc vào database)
  'Điện thoại': [
    // Nhóm Màn hình - Backend fields
    { name: 'screenSize', label: 'Kích thước màn hình (inch)', type: 'number', group: 'Màn hình', placeholder: '6.7 (1-50)', min: 1, max: 50 },
    { name: 'screenTechnology', label: 'Công nghệ màn hình', type: 'text', group: 'Màn hình', placeholder: 'Dynamic AMOLED 2X' },
    { name: 'screenResolution', label: 'Độ phân giải', type: 'text', group: 'Màn hình', placeholder: '3120 x 1440 pixels' },
    { name: 'refreshRate', label: 'Tần số quét (Hz)', type: 'number', group: 'Màn hình', placeholder: '120 (30-500)', min: 30, max: 500 },
    
    // Nhóm Camera - Backend fields
    { name: 'cameraMegapixels', label: 'Camera chính (MP)', type: 'number', group: 'Camera', placeholder: '50 (0.1-200)', min: 0.1, max: 200 },
    { name: 'cameraDetails', label: 'Chi tiết camera sau', type: 'text', group: 'Camera', placeholder: '50 MP + 10 MP + 12 MP' },
    { name: 'frontCameraMegapixels', label: 'Camera trước (MP)', type: 'number', group: 'Camera', placeholder: '12 (0.1-200)', min: 0.1, max: 200 },
    
    // Nhóm Cấu hình - Backend fields
    { name: 'cpuChipset', label: 'Chipset', type: 'text', group: 'Cấu hình', placeholder: 'Snapdragon 8 Gen 3', required: true },
    { name: 'gpu', label: 'GPU', type: 'text', group: 'Cấu hình', placeholder: 'Adreno 750' },
    { name: 'operatingSystem', label: 'Hệ điều hành', type: 'text', group: 'Cấu hình', placeholder: 'Android 14' },
    
    // Nhóm Pin - Backend fields
    { name: 'batteryCapacity', label: 'Dung lượng pin (mAh)', type: 'number', group: 'Pin & Sạc', placeholder: '5000 (100-100000)', required: true, min: 100, max: 100000 },
    { name: 'chargingPower', label: 'Công suất sạc (W)', type: 'number', group: 'Pin & Sạc', placeholder: '45 (≤200)', max: 200 },
    { name: 'chargingType', label: 'Loại sạc', type: 'text', group: 'Pin & Sạc', placeholder: 'USB-C PD' },
    
    // Nhóm Thiết kế - Backend fields
    { name: 'dimensions', label: 'Kích thước', type: 'text', group: 'Thiết kế', placeholder: '158.5 x 75.9 x 7.75mm' },
    { name: 'weight', label: 'Trọng lượng (g)', type: 'number', group: 'Thiết kế', placeholder: '196', min: 1, max: 5000 },
    { name: 'material', label: 'Chất liệu', type: 'text', group: 'Thiết kế', placeholder: 'Titanium' },
    
    // Nhóm Kết nối - Backend fields
    { name: 'simType', label: 'Loại SIM', type: 'text', group: 'Kết nối', placeholder: 'Dual SIM (nano-SIM + eSIM)' },
    { name: 'wirelessConnectivity', label: 'Kết nối không dây', type: 'text', group: 'Kết nối', placeholder: '5G, Wi-Fi 6E, Bluetooth 5.3' },
    { name: 'waterResistance', label: 'Chống nước', type: 'text', group: 'Kết nối', placeholder: 'IP68' },
    { name: 'securityFeatures', label: 'Bảo mật', type: 'text', group: 'Kết nối', placeholder: 'Face ID, Fingerprint' },
  ],

  'Tablet': [
    // Màn hình - Backend fields
    { name: 'screenSize', label: 'Kích thước màn hình (inch)', type: 'number', group: 'Màn hình', placeholder: '11 (1-50)', min: 1, max: 50 },
    { name: 'screenTechnology', label: 'Công nghệ màn hình', type: 'text', group: 'Màn hình', placeholder: 'IPS LCD' },
    { name: 'screenResolution', label: 'Độ phân giải', type: 'text', group: 'Màn hình', placeholder: '2560 x 1600' },
    { name: 'refreshRate', label: 'Tần số quét (Hz)', type: 'number', group: 'Màn hình', placeholder: '120 (30-500)', min: 30, max: 500 },
    
    // Camera - Backend fields
    { name: 'cameraMegapixels', label: 'Camera sau (MP)', type: 'number', group: 'Camera', placeholder: '13 (0.1-200)', min: 0.1, max: 200 },
    { name: 'frontCameraMegapixels', label: 'Camera trước (MP)', type: 'number', group: 'Camera', placeholder: '12 (0.1-200)', min: 0.1, max: 200 },
    
    // Cấu hình - Backend fields
    { name: 'cpuChipset', label: 'Chipset', type: 'text', group: 'Cấu hình', placeholder: 'Apple A16 Bionic', required: true },
    { name: 'gpu', label: 'GPU', type: 'text', group: 'Cấu hình', placeholder: '4 core GPU' },
    { name: 'operatingSystem', label: 'Hệ điều hành', type: 'text', group: 'Cấu hình', placeholder: 'iPadOS 18' },
    
    // Pin - Backend fields
    { name: 'batteryCapacity', label: 'Dung lượng pin (mAh)', type: 'number', group: 'Pin & Sạc', placeholder: '8400 (100-100000)', min: 100, max: 100000 },
    { name: 'chargingPower', label: 'Công suất sạc (W)', type: 'number', group: 'Pin & Sạc', placeholder: '45 (≤200)', max: 200 },
    { name: 'chargingType', label: 'Cổng sạc', type: 'text', group: 'Pin & Sạc', placeholder: 'USB Type-C' },
    
    // Thiết kế - Backend fields
    { name: 'dimensions', label: 'Kích thước', type: 'text', group: 'Thiết kế', placeholder: '248.6 x 179.5 x 7 mm' },
    { name: 'weight', label: 'Trọng lượng (g)', type: 'number', group: 'Thiết kế', placeholder: '477', min: 1, max: 5000 },
    { name: 'material', label: 'Chất liệu', type: 'text', group: 'Thiết kế', placeholder: 'Nhôm' },
    
  ],

  'Laptop': [
    // Màn hình - Backend fields
    { name: 'screenSize', label: 'Kích thước màn hình (inch)', type: 'number', group: 'Màn hình', placeholder: '14 (1-50)', min: 1, max: 50 },
    { name: 'screenTechnology', label: 'Công nghệ màn hình', type: 'text', group: 'Màn hình', placeholder: 'IPS' },
    { name: 'screenResolution', label: 'Độ phân giải', type: 'text', group: 'Màn hình', placeholder: '1920 x 1080' },
    { name: 'refreshRate', label: 'Tần số quét (Hz)', type: 'number', group: 'Màn hình', placeholder: '60 (30-500)', min: 30, max: 500 },
    
    // Cấu hình - Backend fields
    { name: 'cpuChipset', label: 'CPU', type: 'text', group: 'Cấu hình', placeholder: 'Intel Core i5-13420H', required: true },
    { name: 'gpu', label: 'GPU', type: 'text', group: 'Cấu hình', placeholder: 'Intel UHD Graphics' },
    { name: 'operatingSystem', label: 'Hệ điều hành', type: 'text', group: 'Cấu hình', placeholder: 'Windows 11 Home' },
    
    // Pin - Backend fields
    { name: 'batteryCapacity', label: 'Pin (mAh hoặc Wh)', type: 'number', group: 'Pin & Sạc', placeholder: '60 (100-100000)', min: 100, max: 100000 },
    { name: 'chargingPower', label: 'Công suất sạc (W)', type: 'number', group: 'Pin & Sạc', placeholder: '65 (≤200)', max: 200 },
    
    // Thiết kế - Backend fields
    { name: 'dimensions', label: 'Kích thước', type: 'text', group: 'Thiết kế', placeholder: '314 x 222 x 17mm' },
    { name: 'weight', label: 'Trọng lượng (g hoặc kg)', type: 'number', group: 'Thiết kế', placeholder: '1430', min: 1, max: 5000 },
    { name: 'material', label: 'Chất liệu', type: 'text', group: 'Thiết kế', placeholder: 'Nhôm' },
    { name: 'keyboardType', label: 'Loại bàn phím', type: 'text', group: 'Thiết kế', placeholder: 'Backlit Chiclet' },
    
    // Kết nối - Backend fields
    { name: 'ports', label: 'Cổng kết nối', type: 'text', group: 'Kết nối', placeholder: '2x USB-A, 1x USB-C, 1x HDMI' },
    { name: 'wirelessConnectivity', label: 'Kết nối không dây', type: 'text', group: 'Kết nối', placeholder: 'Wi-Fi 6, Bluetooth 5.2' },
    { name: 'audioFeatures', label: 'Âm thanh', type: 'text', group: 'Kết nối', placeholder: 'Dolby Audio, Stereo 2W x2' },
  ],

  'Đồng hồ thông minh': [
    // Màn hình - Backend fields
    { name: 'screenSize', label: 'Kích thước màn hình (inch)', type: 'number', group: 'Màn hình', placeholder: '1.34 (1-50)', min: 1, max: 50 },
    { name: 'screenTechnology', label: 'Công nghệ màn hình', type: 'text', group: 'Màn hình', placeholder: 'AMOLED' },
    { name: 'screenResolution', label: 'Độ phân giải', type: 'text', group: 'Màn hình', placeholder: '466 x 466 pixels' },
    
    // Thiết kế - Backend fields
    { name: 'caseSize', label: 'Kích thước mặt', type: 'text', group: 'Thiết kế', placeholder: '42mm' },
    { name: 'weight', label: 'Trọng lượng (g)', type: 'number', group: 'Thiết kế', placeholder: '30', min: 1, max: 5000 },
    { name: 'material', label: 'Chất liệu', type: 'text', group: 'Thiết kế', placeholder: 'Nhôm' },
    { name: 'waterResistance', label: 'Chống nước', type: 'text', group: 'Thiết kế', placeholder: '5 ATM' },
    
    // Tính năng sức khỏe - Backend fields
    { name: 'healthFeatures', label: 'Tính năng sức khỏe', type: 'textarea', group: 'Tính năng', placeholder: 'Đo nhịp tim, SpO2, ECG, Theo dõi giấc ngủ...' },
    
    // Pin - Backend fields
    { name: 'batteryCapacity', label: 'Dung lượng pin (mAh)', type: 'number', group: 'Pin', placeholder: '325 (100-100000)', min: 100, max: 100000 },
    { name: 'batteryLifeDays', label: 'Thời lượng pin (ngày)', type: 'number', group: 'Pin', placeholder: '1' },
    
    // Cấu hình - Backend fields
    { name: 'operatingSystem', label: 'Hệ điều hành', type: 'text', group: 'Cấu hình', placeholder: 'WatchOS' },
    { name: 'wirelessConnectivity', label: 'Kết nối', type: 'text', group: 'Kết nối', placeholder: 'Bluetooth 5.3, Wi-Fi, GPS' },
    { name: 'additionalSpecs', label: 'Tính năng khác', type: 'textarea', group: 'Khác', placeholder: 'Phát hiện té ngã, SOS khẩn cấp, NFC...' },
  ],
};

// Helper function để lấy metadata fields theo category name
export function getMetadataFields(categoryName: string): MetadataField[] {
  return CATEGORY_METADATA[categoryName] || [];
}

// Helper function để group fields theo nhóm
export function groupMetadataFields(fields: MetadataField[]): Record<string, MetadataField[]> {
  const grouped: Record<string, MetadataField[]> = {};
  
  fields.forEach(field => {
    const group = field.group || 'Khác';
    if (!grouped[group]) {
      grouped[group] = [];
    }
    grouped[group].push(field);
  });
  
  return grouped;
}
