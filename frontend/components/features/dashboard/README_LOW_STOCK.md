# LowStockProductsTable Component

## 📋 Tổng quan

Component `LowStockProductsTable` hiển thị danh sách sản phẩm có số lượng tồn kho thấp (stock < threshold) với khả năng điều chỉnh ngưỡng cảnh báo.

## 🔧 Backend API

### Endpoint
```
GET /api/v1/admin/dashboard/low-stock-products?threshold={threshold}
```

### Request Parameters
- `threshold` (optional): Ngưỡng cảnh báo (default: 10)
  - Nếu `threshold=10`: Trả về products có `stockQuantity < 10`
  - Nếu `threshold=100`: Trả về products có `stockQuantity < 100`
  - Backend KHÔNG có hard limit, threshold có thể thay đổi tùy ý

### Response Structure
```json
{
  "success": true,
  "data": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro Max",
      "imageUrl": "https://...",
      "stockQuantity": 5,
      "categoryName": "Smartphone",
      "brandName": "Apple",
      "status": true
    }
  ],
  "message": "Success"
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| `productId` | `Long` | ID sản phẩm |
| `productName` | `String` | Tên sản phẩm |
| `imageUrl` | `String` | URL hình ảnh sản phẩm |
| `stockQuantity` | `Integer` | Số lượng tồn kho hiện tại |
| `categoryName` | `String` | Tên danh mục |
| `brandName` | `String` | Tên thương hiệu |
| `status` | `Boolean` | Trạng thái (true=đang bán, false=ngừng bán) |

## 🎨 UI Features

### 1. Header Section
```
┌─────────────────────────────────────────────────────────┐
│ ⚠️ Sản phẩm sắp hết hàng        Ngưỡng cảnh báo: [10] [Áp dụng] │
│ 12 sản phẩm có số lượng < 10                              │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- ✅ Icon cảnh báo (AlertTriangle - orange)
- ✅ Số lượng sản phẩm hiển thị động
- ✅ Input để thay đổi threshold
- ✅ Button "Áp dụng" để reload data với threshold mới

### 2. Empty State
```
┌─────────────────────────────────────────────────────────┐
│                    📦                                    │
│         Tuyệt vời! Tất cả sản phẩm đều đủ hàng         │
│      Không có sản phẩm nào có số lượng dưới 10         │
└─────────────────────────────────────────────────────────┘
```

**Trigger:** Khi `data.length === 0`

### 3. Table Structure

| Sản phẩm | Danh mục | Thương hiệu | Số lượng còn | Trạng thái | Thao tác |
|----------|----------|-------------|--------------|------------|----------|
| 🖼️ **iPhone 15**<br>ID: 1 | Smartphone | Apple | **5** (🟠) | ✅ Đang bán | 📦 Nhập hàng |
| 🖼️ **Galaxy S24**<br>ID: 2 | Smartphone | Samsung | **0** (🔴)<br>Hết hàng | ✅ Đang bán | 📦 Nhập hàng |

**Column Details:**

#### Sản phẩm (Product Info)
- Product image (48x48px, rounded)
- Product name (truncated with line-clamp-1)
- Product ID

#### Số lượng còn (Stock Quantity)
- **Color coding:**
  - 🔴 Red (`text-red-600`): `stockQuantity === 0`
  - 🟠 Orange (`text-orange-600`): `stockQuantity <= 30% of threshold`
  - 🟡 Yellow (`text-yellow-600`): `stockQuantity > 30% of threshold`
- Label "Hết hàng" khi quantity = 0

#### Trạng thái (Status)
- ✅ **Đang bán** (green badge): `status === true`
- ⚫ **Ngừng bán** (gray badge): `status === false`

#### Thao tác (Actions)
- 📦 **Nhập hàng** button (orange)
- Click → Navigate to `/manage?tab=products&productId={id}`

### 4. Footer
```
┌─────────────────────────────────────────────────────────┐
│ Hiển thị 12 sản phẩm cần nhập hàng     Quản lý sản phẩm → │
└─────────────────────────────────────────────────────────┘
```

## 🎯 Interactive Features

### 1. Threshold Filter
```typescript
// User changes threshold from 10 to 50
onThresholdChange(50)
  → fetchLowStockProducts(50)
  → GET /low-stock-products?threshold=50
  → Update table with new data
```

**Flow:**
1. User types new threshold in input
2. User clicks "Áp dụng"
3. Component calls `onThresholdChange(newThreshold)`
4. Parent fetches data with new threshold
5. Table re-renders with updated products

### 2. Row Click Navigation
```typescript
handleRowClick(productId)
  → router.push(`/manage?tab=products&productId=${productId}`)
```

**Behavior:**
- Click anywhere on row → Navigate to product detail
- Except "Nhập hàng" button (stopPropagation)

### 3. Restock Button
```typescript
handleRestock(productId)
  → router.push(`/manage?tab=products&productId=${productId}`)
```

**Behavior:**
- Click button → Navigate to product management
- `stopPropagation()` to prevent row click

## 📊 Use Cases

### Case 1: Normal Stock Alert (threshold=10)
```
Backend: 12 products with stock < 10
Display: Table with 12 products
Colors: Red (0), Orange (1-3), Yellow (4-9)
```

### Case 2: High Threshold (threshold=100)
```
Backend: 45 products with stock < 100
Display: Table with 45 products
Message: "45 sản phẩm có số lượng < 100"
```

### Case 3: No Low Stock Products
```
Backend: [] (empty array)
Display: Green success message with Package icon
Message: "Tuyệt vời! Tất cả sản phẩm đều đủ hàng"
```

### Case 4: Out of Stock Products
```
Backend: Products with stockQuantity === 0
Display: Red text + "Hết hàng" label
Priority: Highest urgency
```

## 🔄 Data Flow

```
AdminDashboard
  │
  ├─ State: lowStockProducts, lowStockProductsLoading, lowStockThreshold
  │
  ├─ fetchLowStockProducts(threshold)
  │   └─ GET /api/v1/admin/dashboard/low-stock-products?threshold={threshold}
  │       └─ setLowStockProducts(response.data)
  │
  ├─ handleThresholdChange(newThreshold)
  │   └─ fetchLowStockProducts(newThreshold)
  │
  └─ <LowStockProductsTable
        data={lowStockProducts}
        loading={lowStockProductsLoading}
        threshold={lowStockThreshold}
        onThresholdChange={handleThresholdChange}
      />
```

## 🎨 Color System

### Stock Quantity Colors
```typescript
const stockPercentage = (stockQuantity / threshold) * 100;

if (stockQuantity === 0) {
  color = 'text-red-600';      // 🔴 Critical: Out of stock
} else if (stockPercentage <= 30) {
  color = 'text-orange-600';   // 🟠 Warning: Very low
} else {
  color = 'text-yellow-600';   // 🟡 Alert: Low
}
```

### Status Badge Colors
```typescript
status === true
  ? 'bg-green-100 text-green-800 border-green-300'  // ✅ Active
  : 'bg-gray-100 text-gray-800 border-gray-300'     // ⚫ Inactive
```

### Button Colors
```
Restock Button: bg-orange-500 hover:bg-orange-600
Apply Button: bg-secondary hover:bg-secondary/80
Link: text-primary hover:underline
```

## 🚀 Testing Checklist

### Functional Tests
- [ ] Load dashboard → Shows products with stock < 10
- [ ] Change threshold to 50 → Updates product list
- [ ] Click product row → Navigate to product detail
- [ ] Click "Nhập hàng" button → Navigate to product management
- [ ] Empty state → Shows when no low stock products
- [ ] Loading state → Shows skeleton while fetching

### Visual Tests
- [ ] Product images render correctly
- [ ] Stock colors match severity (red/orange/yellow)
- [ ] Status badges show correct state
- [ ] Table is responsive on mobile
- [ ] Threshold input accepts valid numbers

### Edge Cases
- [ ] Threshold = 0 → Should show empty state
- [ ] Threshold = 1000 → Shows all products < 1000
- [ ] No image URL → Shows Package icon placeholder
- [ ] Long product name → Truncates with ellipsis
- [ ] stockQuantity = 0 → Shows "Hết hàng" label

## 📝 Files

- **Component:** `frontend/components/features/dashboard/LowStockProductsTable.tsx`
- **Container:** `frontend/components/features/dashboard/AdminDashboard.tsx`
- **API Service:** `frontend/lib/api.ts` → `dashboardAPI.getLowStockProducts()`
- **Types:** `frontend/types/dashboard.d.ts` → `LowStockProduct`
- **Backend DTO:** `backend/.../LowStockProductResponse.java`

## 🎯 Future Enhancements

- [ ] Add sorting (by stock quantity, product name)
- [ ] Add filtering (by category, brand)
- [ ] Add bulk restock action
- [ ] Show stock history chart
- [ ] Email alert when stock reaches threshold
- [ ] Export low stock report (CSV/PDF)

---

**Last Updated:** 17/12/2024
**Author:** AI Coding Agent
**Version:** 1.0.0
