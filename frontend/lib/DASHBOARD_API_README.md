# Dashboard API - Module M10.2

## ✅ Đã hoàn thành

### 1. TypeScript Types (`frontend/types/dashboard.d.ts`)

Đã định nghĩa đầy đủ các interfaces:

- ✅ `DashboardOverview` - 4 chỉ số tổng quan
- ✅ `RevenueChartData` - Dữ liệu biểu đồ doanh thu
- ✅ `OrderStatusChartData` - Dữ liệu biểu đồ trạng thái đơn hàng
- ✅ `UserRegistrationChartData` - Dữ liệu biểu đồ người dùng đăng ký
- ✅ `TopProduct` - Sản phẩm bán chạy
- ✅ `RecentOrder` - Đơn hàng gần đây
- ✅ `LowStockProduct` - Sản phẩm sắp hết hàng
- ✅ `DashboardPeriod` & `RegistrationPeriod` - Enum thời gian

# Dashboard API - Module M10.2

Mục đích: mô tả các endpoint, DTO và cách tích hợp frontend (`frontend/lib/api.ts`) với backend để hiển thị dashboard giỏ hàng (metrics, charts, orders list, exports).

---

## Tổng quan endpoints (tóm tắt)

- GET  `/api/v1/dashboard/overview` — KPIs tổng quan (revenue, orders, aov, abandonedRate)
- GET  `/api/v1/dashboard/metrics` — Các metric dạng time-series (query params: metric, start, end, granularity)
- GET  `/api/v1/dashboard/orders` — Danh sách đơn hàng với pagination + filters
- GET  `/api/v1/dashboard/top-products` — Top sản phẩm theo doanh thu / số lượng
- GET  `/api/v1/dashboard/low-stock` — Sản phẩm sắp hết hàng
- GET  `/api/v1/dashboard/export/orders` — Export CSV/Excel theo filter (triggers background job or sync download)

---

## Contract chi tiết

All responses follow a common wrapper:

```ts
type ApiResponse<T> = {
  success: boolean
  data?: T
  message?: string
}
```

### 1) Overview

GET /api/v1/dashboard/overview?start=YYYY-MM-DD&end=YYYY-MM-DD

Response:

```ts
type DashboardOverview = {
  totalRevenue: number
  totalOrders: number
  averageOrderValue: number
  abandonedRate: number // percent
  ordersByStatus: { [status: string]: number }
  sparklineRevenue?: { date: string; value: number }[]
}
```

### 2) Metrics / Charts

GET /api/v1/dashboard/metrics?metric=revenue|orders|conversion&start=YYYY-MM-DD&end=YYYY-MM-DD&granularity=daily|weekly|monthly

Response:

```ts
type TimeSeriesPoint = { ts: string; value: number }

type MetricSeries = {
  metric: string
  series: TimeSeriesPoint[]
  total?: number
}
```

### 3) Orders list (server-side pagination, filters, sort)

GET /api/v1/dashboard/orders?start=&end=&status=&search=&page=1&size=25&sort=createdAt,desc

Response:

```ts
type OrderItem = { sku: string; name: string; qty: number; price: number }

type Order = {
  id: string
  customer: { id: string; name: string; email?: string }
  items: OrderItem[]
  total: number
  discount?: number
  paymentMethod?: string
  status: string
  createdAt: string
}

type Paginated<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// Example wrapper
ApiResponse<Paginated<Order>>
```

Notes:
- `search` should match order id, customer email or phone.
- Use `status` multi-value: `status=COMPLETED,PENDING`

### 4) Top products

GET /api/v1/dashboard/top-products?limit=10&period=30

Response:

```ts
type TopProduct = { sku: string; name: string; revenue: number; quantity: number }
ApiResponse<TopProduct[]>
```

### 5) Low stock

GET /api/v1/dashboard/low-stock?threshold=5

Response: `ApiResponse<LowStockProduct[]>`

### 6) Export orders

GET /api/v1/dashboard/export/orders?start=&end=&status=&format=csv

- If export is heavy, backend should return 202 + job id; frontend polls `/api/v1/jobs/{id}` for completed download URL.

---

## Authentication & headers

- Use `Authorization: Bearer <token>` for authenticated requests.
- Optionally add `X-Admin-Id` for tracing internal calls.

Example fetch:

```ts
const res = await fetch('/api/v1/dashboard/overview', {
  headers: { Authorization: `Bearer ${token}` }
})
const payload = await res.json()
```

## Frontend integration patterns

- Use `react-query` or `SWR` for caching, background refetch and pagination.
- Cache summary (overview) for short TTL (30s). Cache heavy charts longer (5-10min) and invalidate on data-changing operations.
- Debounce `search` and use server-side filtering.

Example `react-query` hooks (sketch):

```ts
// useDashboard.ts
import { useQuery } from '@tanstack/react-query'

export function useOverview(start, end) {
  return useQuery(['dashboard','overview',start,end], () => fetch(`/api/v1/dashboard/overview?start=${start}&end=${end}`).then(r=>r.json()), { staleTime: 30_000 })
}

export function useOrders(params) {
  return useQuery(['dashboard','orders', params], () => fetch('/api/v1/dashboard/orders?'+new URLSearchParams(params)).then(r=>r.json()), { keepPreviousData: true })
}
```

## Backend implementation notes (recommended)

- Aggregate metrics via optimized SQL (GROUP BY date) or materialized views for large datasets.
- Cache computed charts in Redis with keys per date-range and metric.
- Use background worker (e.g., Spring + @Async or message queue) for heavy exports.
- Add indexes on `orders.created_at`, `orders.status`, `order_items.sku` for fast queries.

## Errors and retry

- Standardize errors: return HTTP 4xx/5xx with JSON `{ success: false, message }`.
- Frontend: show toast on network error and allow retry button for failed fetches.

## Example: calling orders endpoint from frontend (SWR)

```ts
import useSWR from 'swr'

const fetcher = (url: string) => fetch(url, { headers: { Authorization: `Bearer ${token}` } }).then(r => r.json())

export function useDashboardOrders(params) {
  const qs = new URLSearchParams(params).toString()
  const { data, error } = useSWR(`/api/v1/dashboard/orders?${qs}`, fetcher, { revalidateOnFocus: false })
  return { data, error }
}
```

## Pagination & UX notes

- Return `totalElements` so frontend can render accurate pagination.
- For very large datasets consider cursor-based paging.
- Provide `page` and `size` defaults (page=1, size=25).

## Deployment / debug checklist

1. Backend running at `http://localhost:8081` (or configured proxy to Next dev server)
2. CORS: allow `http://localhost:3000` in dev
3. Auth: ensure token generation and refresh flow works
4. Test endpoints using `frontend/lib/test-dashboard-api.ts` helper

---

## Next steps I can do for bạn

- Implement `frontend/lib/dashboardClient.ts` wrappers (fetch / react-query hooks) and wire them into the example dashboard page.
- Add server-side proxy API routes in Next (`/app/api/dashboard/*`) to attach tokens or handle local dev CORS.

Bạn muốn tôi tiếp tục tự động tạo các hooks (`useOverview`, `useOrders`) và cập nhật `frontend/lib/api.ts` để kết nối thật với backend chứ? 
