'use client';

/**
 * ProductForm - Form to create new products with dynamic metadata fields
 */

import { useState, useEffect, useMemo } from 'react';
import { productAPI, adminAPI } from '@/lib/api';
import type { CreateProductRequest, ProductMetadata } from '@/types';
import type { CategoryResponse } from '@/types/category';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { getMetadataFields, groupMetadataFields, type MetadataField } from '@/lib/constants/categoryMetadata';

interface ProductFormProps {
  onSuccess?: () => void;
}

export function ProductForm({ onSuccess }: ProductFormProps) {
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<Array<{ id: number; name: string }>>([]);
  const [brands, setBrands] = useState<Array<{ id: number; name: string }>>([]);
  const [metadata, setMetadata] = useState<ProductMetadata>({});
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState<CreateProductRequest>({
    name: '',
    description: '',
    thumbnailUrl: '',
    categoryId: 1,
    brandId: 1,
    status: true,
    templates: [
      {
        sku: '',
        color: '',
        storage: '',
        ram: '',
        price: 0,
        stockQuantity: 0,
        status: true,
      },
    ],
    metadata: {},
    images: [],
  });

  // Get selected category name
  const selectedCategory = useMemo(() => {
    return categories.find(cat => cat.id === formData.categoryId);
  }, [categories, formData.categoryId]);

  // Get metadata fields for selected category
  const metadataFields = useMemo(() => {
    if (!selectedCategory) return [];
    return getMetadataFields(selectedCategory.name);
  }, [selectedCategory]);

  // Group metadata fields
  const groupedFields = useMemo(() => {
    return groupMetadataFields(metadataFields);
  }, [metadataFields]);

  // Constants
  const PRICE_ROUNDING_FACTOR = 1000;
  const EXCLUDED_CATEGORY = 'Phụ kiện';

  // Fill random data for testing
  const fillRandomData = () => {
    const randomNum = (min: number, max: number, decimals = 0) => {
      const value = Math.random() * (max - min) + min;
      return decimals > 0 ? parseFloat(value.toFixed(decimals)) : Math.floor(value);
    };

    const randomChoice = <T,>(arr: T[]): T => arr[randomNum(0, arr.length)];

    const colors = ['Đen', 'Trắng', 'Xanh', 'Vàng', 'Tím', 'Hồng', 'Bạc', 'Xám'];
    const storages = ['128GB', '256GB', '512GB', '1TB'];
    const rams = ['8GB', '12GB', '16GB', '32GB'];
    
    // Generate product name and price based on category
    let productName = '';
    let price = 0;
    let description = '';

    if (!selectedCategory) return;

    if (selectedCategory.name === 'Điện thoại') {
      const phones = ['iPhone 15', 'Samsung Galaxy S24', 'Xiaomi 14', 'OPPO Find X7', 'Vivo X100'];
      productName = `${randomChoice(phones)} Pro Max ${randomNum(2024, 2026)}`;
      price = Math.floor(randomNum(10000000, 35000000, 0) / PRICE_ROUNDING_FACTOR) * PRICE_ROUNDING_FACTOR; // Round to nearest 1000
      description = 'Sản phẩm flagship cao cấp với hiệu năng mạnh mẽ và camera xuất sắc.';
    } else if (selectedCategory.name === 'Tablet') {
      const tablets = ['iPad Pro', 'iPad Air', 'Galaxy Tab S9', 'MatePad Pro', 'Xiaomi Pad'];
      productName = `${randomChoice(tablets)} ${randomNum(11, 13)}" (${randomNum(2024, 2025)})`;
      price = Math.floor(randomNum(15000000, 45000000, 0) / PRICE_ROUNDING_FACTOR) * PRICE_ROUNDING_FACTOR; // Round to nearest 1000
      description = 'Máy tính bảng cao cấp với màn hình lớn, hiệu năng mạnh mẽ cho công việc và giải trí.';
    } else if (selectedCategory.name === 'Laptop') {
      const laptops = ['MacBook Pro', 'Dell XPS', 'ThinkPad X1', 'ASUS Zenbook', 'HP Spectre'];
      productName = `${randomChoice(laptops)} ${randomNum(13, 16)}" ${randomNum(2024, 2025)}`;
      price = Math.floor(randomNum(20000000, 60000000, 0) / PRICE_ROUNDING_FACTOR) * PRICE_ROUNDING_FACTOR; // Round to nearest 1000
      description = 'Laptop cao cấp với hiệu năng mạnh mẽ, thiết kế sang trọng, phù hợp cho công việc chuyên nghiệp.';
    } else if (selectedCategory.name === 'Đồng hồ thông minh') {
      const watches = ['Apple Watch Series', 'Galaxy Watch', 'Huawei Watch GT', 'Xiaomi Watch', 'Amazfit GTR'];
      productName = `${randomChoice(watches)} ${randomNum(6, 9)} ${randomChoice(['', 'Pro', 'Ultra'])}`;
      price = Math.floor(randomNum(3000000, 15000000, 0) / PRICE_ROUNDING_FACTOR) * PRICE_ROUNDING_FACTOR; // Round to nearest 1000
      description = 'Đồng hồ thông minh hiện đại với nhiều tính năng sức khỏe và thể thao.';
    }

    // Fill basic info
    setFormData({
      ...formData,
      name: productName,
      description: description,
      thumbnailUrl: 'https://via.placeholder.com/400x400.png?text=Product',
      templates: [{
        sku: `SKU${Date.now()}`,
        color: randomChoice(colors),
        storage: randomChoice(storages),
        ram: randomChoice(rams),
        price: price,
        stockQuantity: randomNum(10, 100),
        status: true,
      }],
    });

    // Fill metadata based on category
    const newMetadata: any = {};

    if (selectedCategory.name === 'Điện thoại') {
      newMetadata.screenSize = randomNum(6, 8, 1); // 6.0-7.9 inch (decimal)
      newMetadata.screenTechnology = randomChoice(['AMOLED', 'Super AMOLED', 'OLED', 'IPS LCD']);
      newMetadata.screenResolution = randomChoice(['1080 x 2400', '1440 x 3200', '1284 x 2778']);
      newMetadata.refreshRate = randomChoice([60, 90, 120, 144]); // 30-500Hz
      newMetadata.cameraMegapixels = randomNum(48, 108, 0); // 0.1-200MP
      newMetadata.cameraDetails = `${randomNum(48, 108)}MP + ${randomNum(8, 50)}MP + ${randomNum(2, 12)}MP`;
      newMetadata.frontCameraMegapixels = randomNum(10, 32, 0);
      newMetadata.cpuChipset = randomChoice(['Snapdragon 8 Gen 3', 'A17 Pro', 'Dimensity 9300', 'Exynos 2400']);
      newMetadata.gpu = randomChoice(['Adreno 750', 'Apple GPU', 'Mali-G720']);
      newMetadata.operatingSystem = randomChoice(['Android 14', 'iOS 17', 'HyperOS']);
      newMetadata.batteryCapacity = randomNum(4000, 6000, 0); // 100-100000mAh
      newMetadata.chargingPower = randomChoice([33, 45, 67, 100, 120]); // ≤200W
      newMetadata.chargingType = 'USB-C PD';
      newMetadata.dimensions = `${randomNum(150, 165, 1)} x ${randomNum(70, 80, 1)} x ${randomNum(7, 9, 1)}mm`;
      newMetadata.weight = randomNum(180, 220, 0); // 1-5000g
      newMetadata.material = randomChoice(['Titanium', 'Aluminum', 'Glass', 'Ceramic']);
      newMetadata.simType = 'Dual SIM (nano-SIM + eSIM)';
      newMetadata.wirelessConnectivity = '5G, Wi-Fi 6E, Bluetooth 5.3, NFC';
      newMetadata.waterResistance = randomChoice(['IP68', 'IP67', 'IPX8']);
      newMetadata.securityFeatures = randomChoice(['Face ID', 'Fingerprint', 'Face + Fingerprint']);
    } 
    else if (selectedCategory.name === 'Tablet') {
      newMetadata.screenSize = randomNum(10, 14, 1); // 10.0-13.9 inch (decimal)
      newMetadata.screenTechnology = randomChoice(['IPS LCD', 'AMOLED', 'Liquid Retina']);
      newMetadata.screenResolution = randomChoice(['2560 x 1600', '2732 x 2048', '2388 x 1668']);
      newMetadata.refreshRate = randomChoice([60, 90, 120]); // 30-500Hz
      newMetadata.cameraMegapixels = randomNum(8, 13, 0); // 0.1-200MP
      newMetadata.frontCameraMegapixels = randomNum(7, 12, 0);
      newMetadata.cpuChipset = randomChoice(['Apple A16 Bionic', 'Apple M2', 'Snapdragon 8 Gen 2', 'Dimensity 9000']);
      newMetadata.gpu = randomChoice(['Apple GPU 5 core', 'Adreno 740', 'Mali-G710']);
      newMetadata.operatingSystem = randomChoice(['iPadOS 18', 'Android 14', 'HarmonyOS']);
      newMetadata.batteryCapacity = randomNum(7000, 11000, 0); // 100-100000mAh
      newMetadata.chargingPower = randomChoice([20, 33, 45, 65]); // ≤200W
      newMetadata.chargingType = randomChoice(['USB Type-C', 'USB-C PD']);
      newMetadata.dimensions = `${randomNum(240, 280, 1)} x ${randomNum(170, 215, 1)} x ${randomNum(5, 7, 1)}mm`;
      newMetadata.weight = randomNum(450, 680, 0); // 1-5000g
      newMetadata.material = randomChoice(['Nhôm', 'Kính cường lực', 'Titan']);
      newMetadata.additionalSpecs = 'Hỗ trợ bút stylus, Magic Keyboard, Smart Connector';
    }
    else if (selectedCategory.name === 'Laptop') {
      newMetadata.screenSize = randomNum(13, 17, 1); // 13.0-16.9 inch (decimal)
      newMetadata.screenTechnology = randomChoice(['IPS', 'OLED', 'Mini-LED', 'Retina']);
      newMetadata.screenResolution = randomChoice(['1920 x 1080', '2560 x 1600', '3840 x 2160']);
      newMetadata.refreshRate = randomChoice([60, 90, 120, 144]); // 30-500Hz
      newMetadata.cpuChipset = randomChoice(['Intel Core i5-13420H', 'Intel Core i7-13700H', 'AMD Ryzen 7 7840HS', 'Apple M2 Pro']);
      newMetadata.gpu = randomChoice(['Intel UHD Graphics', 'NVIDIA RTX 4050', 'AMD Radeon', 'Apple M2 GPU']);
      newMetadata.operatingSystem = randomChoice(['Windows 11 Home', 'Windows 11 Pro', 'macOS Sonoma', 'Ubuntu']);
      newMetadata.batteryCapacity = randomNum(50, 100, 0); // 50-100 Wh
      newMetadata.chargingPower = randomChoice([45, 65, 90, 120, 140]); // ≤200W
      newMetadata.dimensions = `${randomNum(300, 360, 1)} x ${randomNum(210, 250, 1)} x ${randomNum(14, 20, 1)}mm`;
      newMetadata.weight = randomNum(1200, 2200, 0); // 1.2-2.2kg
      newMetadata.material = randomChoice(['Nhôm', 'Nhựa ABS', 'Magie', 'Carbon fiber']);
      newMetadata.keyboardType = randomChoice(['Backlit Chiclet', 'Mechanical RGB', 'Butterfly', 'Magic Keyboard']);
      newMetadata.ports = '2x USB-A 3.2, 1x USB-C Thunderbolt 4, 1x HDMI 2.1, 1x Audio Jack';
      newMetadata.wirelessConnectivity = randomChoice(['Wi-Fi 6, Bluetooth 5.2', 'Wi-Fi 6E, Bluetooth 5.3']);
      newMetadata.audioFeatures = randomChoice(['Dolby Atmos, Stereo 2W x2', 'Bang & Olufsen, Quad speakers']);
    }
    else if (selectedCategory.name === 'Đồng hồ thông minh') {
      newMetadata.screenSize = randomNum(1.2, 2.0, 1); // 1.2-1.9 inch (decimal)
      newMetadata.screenTechnology = randomChoice(['AMOLED', 'Super AMOLED', 'LTPO OLED', 'Retina']);
      newMetadata.screenResolution = randomChoice(['466 x 466', '484 x 396', '368 x 448']);
      newMetadata.caseSize = randomChoice(['40mm', '42mm', '44mm', '45mm', '46mm', '49mm']);
      newMetadata.weight = randomNum(25, 55, 0); // 25-55g
      newMetadata.material = randomChoice(['Nhôm', 'Thép không gỉ', 'Titan', 'Ceramic']);
      newMetadata.waterResistance = randomChoice(['5 ATM', '10 ATM', 'IP68', '50m']);
      newMetadata.healthFeatures = 'Đo nhịp tim, SpO2, ECG, Theo dõi giấc ngủ, Đo stress, VO2 Max';
      newMetadata.batteryCapacity = randomNum(250, 500, 0); // 250-500mAh
      newMetadata.batteryLifeDays = randomChoice([1, 2, 3, 5, 7, 14]); // Battery life in days
      newMetadata.operatingSystem = randomChoice(['WatchOS 10', 'Wear OS 4', 'HarmonyOS', 'Tizen']);
      newMetadata.wirelessConnectivity = randomChoice(['Bluetooth 5.3, Wi-Fi, GPS', 'Bluetooth 5.3, LTE, GPS', 'Bluetooth 5.2, NFC']);
      newMetadata.additionalSpecs = 'Phát hiện té ngã, SOS khẩn cấp, NFC thanh toán, Chống nước bơi lội';
    }

    setMetadata(newMetadata);
    toast.success('Đã điền dữ liệu random thành công!', {
      description: 'Kiểm tra lại thông tin trước khi lưu',
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (!formData.name || formData.name.length < 5) {
      toast.error('Tên sản phẩm phải có ít nhất 5 ký tự');
      return;
    }

    if (!formData.templates[0].sku || !formData.templates[0].price || formData.templates[0].stockQuantity === undefined) {
      toast.error('Vui lòng điền đầy đủ SKU, Giá và Tồn kho cho biến thể sản phẩm');
      return;
    }

    try {
      setLoading(true);
      
      // Filter out empty/invalid metadata fields
      const cleanedMetadata: any = {};
      Object.entries(metadata).forEach(([key, value]) => {
        if (value !== '' && value !== null && value !== undefined) {
          cleanedMetadata[key] = value;
        }
      });
      
      // Merge metadata into formData
      const submitData = {
        ...formData,
        metadata: Object.keys(cleanedMetadata).length > 0 ? cleanedMetadata : undefined,
      };
      
      const response = await productAPI.create(submitData);
      
      // Show success message
      toast.success('Tạo sản phẩm thành công!', {
        description: `Sản phẩm "${formData.name}" đã được thêm vào danh sách.`,
        duration: 5000,
      });
      
      // Reset form
      setFormData({
        name: '',
        description: '',
        thumbnailUrl: '',
        categoryId: categories[0]?.id || 1,
        brandId: brands[0]?.id || 1,
        status: true,
        templates: [
          {
            sku: '',
            color: '',
            storage: '',
            ram: '',
            price: 0,
            stockQuantity: 0,
            status: true,
          },
        ],
        metadata: {},
        images: [],
      });
      setMetadata({});
      setValidationErrors({});
      
      onSuccess?.();
    } catch (err) {
      // Try to extract validation errors from response
      let errorMessage = 'Không thể tạo sản phẩm';
      let errorDescription = '';
      const validationErrs: Record<string, string> = {};
      
      if (err instanceof Error) {
        errorMessage = err.message;
        
        // Try to parse validation errors from error object
        // Backend sends errors in data.data object
        try {
          const errObj = err as any;
          if (errObj.data && typeof errObj.data === 'object') {
            Object.entries(errObj.data).forEach(([key, msg]) => {
              validationErrs[key] = msg as string;
            });
            setValidationErrors(validationErrs);
            
            // Build error list for description
            const errorList = Object.entries(validationErrs)
              .map(([field, msg]) => `• ${field}: ${msg}`)
              .join('\n');
            
            if (errorList) {
              errorMessage = 'Lỗi validation';
              errorDescription = errorList;
            }
          }
        } catch (parseErr) {
          console.error('Failed to parse validation errors:', parseErr);
        }
        
        // If it's a generic validation error, add helpful hints
        if (errorMessage.includes('Validation failed') && Object.keys(validationErrs).length === 0) {
          errorDescription = 'Vui lòng kiểm tra:\n• Tên sản phẩm (5-200 ký tự)\n• SKU, giá, tồn kho cho biến thể\n• Các trường metadata (screenSize 1-50, batteryCapacity 100-100000, chargingPower ≤200, refreshRate ≥30...)';
        }
        
        // Network or server errors
        if (errorMessage.includes('Network') || errorMessage.includes('fetch')) {
          errorMessage = 'Lỗi kết nối';
          errorDescription = 'Không thể kết nối tới server. Vui lòng kiểm tra kết nối mạng.';
        }
      }
      
      // Show error toast
      toast.error(errorMessage, {
        description: errorDescription,
        duration: 7000,
      });
    } finally {
      setLoading(false);
    }
  };

  // Load categories and brands on mount
  useEffect(() => {
    const loadData = async () => {
      try {
        const [catsRes, brandsRes] = await Promise.all([
          adminAPI.getAllCategories(),
          adminAPI.getAllBrands()
        ]);
        if (catsRes.success && catsRes.data) {
          // Filter out excluded category
          const filteredCategories = catsRes.data.filter((c: CategoryResponse) => c.name !== EXCLUDED_CATEGORY);
          setCategories(filteredCategories);
        }
        if (brandsRes.success && brandsRes.data) setBrands(brandsRes.data);
      } catch (err) {
        console.error('Failed to load categories/brands:', err);
      }
    };
    loadData();
  }, []);

  const updateTemplate = (index: number, field: string, value: any) => {
    const newTemplates = [...formData.templates];
    newTemplates[index] = { ...newTemplates[index], [field]: value };
    setFormData({ ...formData, templates: newTemplates });
  };

  const updateMetadata = (fieldName: string, value: any) => {
    setMetadata(prev => ({ ...prev, [fieldName]: value }));
  };

  const renderMetadataField = (field: MetadataField) => {
    const value = metadata[field.name] ?? '';

    switch (field.type) {
      case 'number':
        return (
          <Input
            type="number"
            value={value}
            onChange={(e) => updateMetadata(field.name, Number(e.target.value))}
            placeholder={field.placeholder}
            required={field.required}
            min={field.min}
            max={field.max}
            step={field.name.includes('Megapixels') || field.name === 'screenSize' ? '0.1' : '1'}
          />
        );
      
      case 'select':
        return (
          <select
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            required={field.required}
          >
            <option value="">-- Chọn --</option>
            {field.options?.map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
        );
      
      case 'textarea':
        return (
          <textarea
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 border border-input rounded-md min-h-[80px]"
            required={field.required}
          />
        );
      
      case 'boolean':
        return (
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={!!value}
              onChange={(e) => updateMetadata(field.name, e.target.checked)}
              className="w-4 h-4"
            />
            <span className="text-sm text-muted-foreground">Có</span>
          </div>
        );
      
      default: // text
        return (
          <Input
            type="text"
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        );
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-card rounded-lg border shadow-sm p-6 space-y-6">
      {/* Quick Fill Button */}
      <div className="flex justify-between items-center pb-4 border-b">
        <h2 className="text-xl font-bold">Thêm sản phẩm mới</h2>
        <button
          type="button"
          onClick={fillRandomData}
          className="px-4 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-md hover:from-purple-600 hover:to-pink-600 transition-all shadow-md flex items-center gap-2"
        >
          <span>🎲</span>
          <span>Điền Random (Test)</span>
        </button>
      </div>

      {/* Thông tin cơ bản */}
      <div className="space-y-4">
        <h3 className="font-semibold text-lg">Thông tin cơ bản</h3>
        
        <div className="space-y-2">
          <Label htmlFor="name">Tên sản phẩm *</Label>
          <Input
            id="name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="VD: Samsung Galaxy S24 Ultra"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="description">Mô tả</Label>
          <textarea
            id="description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Mô tả sản phẩm..."
            className="w-full px-3 py-2 border rounded-md min-h-[100px]"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="thumbnailUrl">URL ảnh đại diện</Label>
          <Input
            id="thumbnailUrl"
            value={formData.thumbnailUrl}
            onChange={(e) => setFormData({ ...formData, thumbnailUrl: e.target.value })}
            placeholder="https://example.com/image.jpg"
          />
          <p className="text-xs text-muted-foreground">
            Để trống nếu chưa có. Có thể cập nhật sau.
          </p>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="categoryId">Danh mục *</Label>
            <select
              id="categoryId"
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
              className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              required
            >
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="brandId">Thương hiệu *</Label>
            <select
              id="brandId"
              value={formData.brandId}
              onChange={(e) => setFormData({ ...formData, brandId: Number(e.target.value) })}
              className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              required
            >
              {brands.map(brand => (
                <option key={brand.id} value={brand.id}>{brand.name}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Thông số kỹ thuật (Metadata) - Dynamic based on category */}
      {metadataFields.length > 0 && (
        <div className="space-y-4">
          <h3 className="font-semibold text-lg">Thông số kỹ thuật ({selectedCategory?.name})</h3>
          <p className="text-sm text-muted-foreground">
            Các thông số này sẽ giúp khách hàng hiểu rõ hơn về sản phẩm
          </p>

          {Object.entries(groupedFields).map(([groupName, fields]) => (
            <div key={groupName} className="space-y-3 p-4 border rounded-lg bg-muted/20">
              <h4 className="font-medium text-sm text-primary">{groupName}</h4>
              <div className="grid grid-cols-2 gap-4">
                {fields.map(field => {
                  const errorKey = `metadata.${field.name}`;
                  const hasError = !!validationErrors[errorKey];
                  
                  return (
                    <div key={field.name} className="space-y-2">
                      <Label htmlFor={field.name} className={hasError ? 'text-destructive' : ''}>
                        {field.label} {field.required && <span className="text-destructive">*</span>}
                      </Label>
                      {renderMetadataField(field)}
                      {hasError && (
                        <p className="text-sm text-destructive font-medium">
                          {validationErrors[errorKey]}
                        </p>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Template (Variant) */}
      <div className="space-y-4">
        <h3 className="font-semibold text-lg">Biến thể sản phẩm (Template) *</h3>
        <p className="text-sm text-muted-foreground">Phải có ít nhất 1 biến thể</p>

        <div className="grid grid-cols-2 gap-4 p-4 border rounded-lg">
          <div className="space-y-2">
            <Label>SKU * (VD: SGS24U-256-BLACK)</Label>
            <Input
              value={formData.templates[0].sku}
              onChange={(e) => updateTemplate(0, 'sku', e.target.value)}
              placeholder="Mã SKU unique"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Màu sắc</Label>
            <Input
              value={formData.templates[0].color}
              onChange={(e) => updateTemplate(0, 'color', e.target.value)}
              placeholder="VD: Titan Black"
            />
          </div>
          <div className="space-y-2">
            <Label>Dung lượng</Label>
            <Input
              value={formData.templates[0].storage}
              onChange={(e) => updateTemplate(0, 'storage', e.target.value)}
              placeholder="VD: 256GB"
            />
          </div>
          <div className="space-y-2">
            <Label>RAM</Label>
            <Input
              value={formData.templates[0].ram}
              onChange={(e) => updateTemplate(0, 'ram', e.target.value)}
              placeholder="VD: 12GB"
            />
          </div>
          <div className="space-y-2">
            <Label>Giá *</Label>
            <Input
              type="number"
              value={formData.templates[0].price}
              onChange={(e) => updateTemplate(0, 'price', Number(e.target.value))}
              placeholder="27990000"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Tồn kho *</Label>
            <Input
              type="number"
              value={formData.templates[0].stockQuantity}
              onChange={(e) => updateTemplate(0, 'stockQuantity', Number(e.target.value))}
              placeholder="50"
              required
            />
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-3 pt-4">
        <Button type="submit" disabled={loading}>
          {loading ? 'Đang tạo...' : 'Tạo sản phẩm'}
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={() => {
            setFormData({
              name: '',
              description: '',
              thumbnailUrl: '',
              categoryId: categories[0]?.id || 1,
              brandId: brands[0]?.id || 1,
              status: true,
              templates: [
                {
                  sku: '',
                  color: '',
                  storage: '',
                  ram: '',
                  price: 0,
                  stockQuantity: 0,
                  status: true,
                },
              ],
              metadata: {},
              images: [],
            });
            setMetadata({});
          }}
        >
          Reset
        </Button>
      </div>
    </form>
  );
}
