# RecentOrdersTable - Client-side Pagination

## 📋 Tổng quan

Component `RecentOrdersTable` sử dụng **client-side pagination** vì backend chỉ hỗ trợ `limit` parameter (không có `page` parameter).

## 🔧 Cách hoạt động

### 1. Backend API
```
GET /api/v1/admin/dashboard/recent-orders?limit={limit}
```

**Response:**
```json
{
  "success": true,
  "data": [
    { "orderId": 1, "customerName": "...", ... },
    { "orderId": 2, "customerName": "...", ... },
    // ... có thể có 0-100 orders
  ],
  "message": "Success"
}
```

**Đặc điểm:**
- ✅ Backend trả về TẤT CẢ orders có trong DB (tối đa = limit)
- ✅ Nếu DB có 25 orders, gọi `limit=100` vẫn chỉ trả về 25 orders
- ❌ Không có pagination metadata (totalPages, hasNext, etc.)
- ❌ Không có `page` parameter

### 2. Frontend Implementation

#### Constants
```typescript
const ORDERS_PER_PAGE = 10;        // 10 orders mỗi trang
const TOTAL_ORDERS_TO_FETCH = 100; // Fetch tối đa 100 orders
```

#### State Management
```typescript
// Store ALL orders fetched from API
const [allRecentOrders, setAllRecentOrders] = useState<RecentOrder[]>([]);

// Current page (0-indexed)
const [recentOrdersPage, setRecentOrdersPage] = useState(0);

// Calculate paginated data on-the-fly (không cần state)
const paginatedOrders = allRecentOrders.slice(
  recentOrdersPage * ORDERS_PER_PAGE,
  (recentOrdersPage + 1) * ORDERS_PER_PAGE
);

// Calculate hasNext on-the-fly
const recentOrdersHasNext = (recentOrdersPage + 1) * ORDERS_PER_PAGE < allRecentOrders.length;
```

#### Fetch Logic
```typescript
const fetchRecentOrders = useCallback(async () => {
  const response = await dashboardAPI.getRecentOrders(TOTAL_ORDERS_TO_FETCH);
  
  if (response.success && response.data) {
    // Store ALL orders
    setAllRecentOrders(response.data);
    
    // Reset to first page
    setRecentOrdersPage(0);
  }
}, []);
```

**Quan trọng:** 
- ✅ Chỉ gọi API **MỘT LẦN** khi component mount
- ✅ Không cần gọi lại API khi đổi trang
- ✅ Data được cache trong `allRecentOrders` state

#### Pagination Logic
```typescript
const handleRecentOrdersPageChange = useCallback((page: number) => {
  // Chỉ update state, KHÔNG gọi API
  setRecentOrdersPage(page);
  
  // Optional: Scroll to table top
  document.getElementById('recent-orders-table')?.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  });
}, []);
```

## 📊 Ví dụ

### Case 1: Backend có 25 orders

**Initial fetch:**
```
GET /recent-orders?limit=100
→ Returns: 25 orders
→ allRecentOrders = [order1, order2, ..., order25]
```

**Pagination:**
- **Page 0:** orders[0-9] (10 orders) → `hasNext = true`
- **Page 1:** orders[10-19] (10 orders) → `hasNext = true`
- **Page 2:** orders[20-24] (5 orders) → `hasNext = false`

**UI:**
```
Page 0:  [Xem tất cả →]                [Trang 1] [Sau →]
Page 1:  [Xem tất cả →]    [← Trước] [Trang 2] [Sau →]
Page 2:  [Xem tất cả →]    [← Trước] [Trang 3]
```

### Case 2: Backend có 8 orders

**Initial fetch:**
```
GET /recent-orders?limit=100
→ Returns: 8 orders
→ allRecentOrders = [order1, order2, ..., order8]
```

**Pagination:**
- **Page 0:** orders[0-7] (8 orders) → `hasNext = false`

**UI:**
```
Page 0:  [Xem tất cả →]                [Trang 1]
         (Không có nút pagination vì chỉ 1 trang)
```

### Case 3: Backend không có orders

**Initial fetch:**
```
GET /recent-orders?limit=100
→ Returns: []
→ allRecentOrders = []
```

**UI:**
```
┌─────────────────────────────────┐
│ Đơn hàng gần đây                │
├─────────────────────────────────┤
│                                 │
│    Chưa có đơn hàng nào        │
│                                 │
└─────────────────────────────────┘
```

## 🎯 Ưu điểm

✅ **Performance tốt:** Chỉ gọi API 1 lần
✅ **UX tốt:** Đổi trang tức thì, không có loading
✅ **Simple:** Không cần quản lý cache phức tạp
✅ **Flexible:** Dễ thêm sorting/filtering trên client

## ⚠️ Giới hạn

❌ **Memory:** Load 100 orders vào memory (acceptable cho dashboard)
❌ **Real-time:** Data không tự động refresh (cần F5 hoặc manual refresh)
❌ **Scaling:** Không phù hợp nếu cần pagination với hàng nghìn records

## 🔄 Refresh Data

Để refresh data (sau khi tạo order mới):
```typescript
// Option 1: Gọi lại fetchRecentOrders
fetchRecentOrders();

// Option 2: Dùng button refresh
<button onClick={fetchRecentOrders}>
  🔄 Refresh
</button>
```

## 📝 Code Files

- **Component:** `frontend/components/features/dashboard/RecentOrdersTable.tsx`
- **Container:** `frontend/components/features/dashboard/AdminDashboard.tsx`
- **API Service:** `frontend/lib/api.ts` → `dashboardAPI.getRecentOrders()`
- **Types:** `frontend/types/dashboard.d.ts` → `RecentOrder`

## 🚀 Testing

```bash
# Start frontend
cd frontend && npm run dev

# Test cases:
1. Load dashboard → Should show first 10 orders
2. Click "Sau" → Should show next 10 orders (no API call)
3. Click "Trước" → Should go back to previous page
4. Last page → Should NOT show "Sau" button
5. < 10 orders → Should NOT show pagination
6. 0 orders → Should show empty state
```

## 🎨 UI Components

```
┌─────────────────────────────────────────────────────────────┐
│ Đơn hàng gần đây                                            │
│ Hiển thị 10 đơn hàng (Trang 1)                              │
├─────────────────────────────────────────────────────────────┤
│ Mã đơn | Khách hàng | Tổng tiền | Trạng thái | Ngày tạo   │
│ #0001  | Nguyễn A   | 5.000.000đ | Đã giao   | 15/12/2024 │
│ ...    | ...        | ...       | ...       | ...        │
├─────────────────────────────────────────────────────────────┤
│ [Xem tất cả →]              [← Trước] [Trang 1] [Sau →]   │
└─────────────────────────────────────────────────────────────┘
```

---

**Last Updated:** 17/12/2024
**Author:** AI Coding Agent
**Version:** 1.0.0
