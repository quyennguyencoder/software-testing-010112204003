# 📊 Revenue Chart - Test Guide

## ✅ Đã hoàn thành

### Files đã tạo:
1. ✅ `/frontend/components/features/dashboard/RevenueChart.tsx` - Component biểu đồ doanh thu
2. ✅ `/frontend/components/features/dashboard/index.ts` - Export RevenueChart
3. ✅ `/frontend/components/features/dashboard/AdminDashboard.tsx` - Đã thêm RevenueChart

### Dependencies đã cài:
- ✅ `chart.js` - Core library cho biểu đồ
- ✅ `react-chartjs-2` - React wrapper cho Chart.js

---

## 🎯 Layout hiển thị

```
AdminDashboard
├── 4 Stats Cards (Grid 4 cột)
│   ├── Tổng Doanh thu
│   ├── Tổng Đơn hàng
│   ├── Tổng Sản phẩm
│   └── Tổng Người dùng
│
├── Revenue Chart (Biểu đồ Doanh thu) ⭐ MỚI
│   ├── Header với selector: 7 ngày / 30 ngày / 3 tháng
│   ├── Line Chart (biểu đồ đường)
│   └── Summary: Tổng doanh thu & TB/ngày
│
└── Recent Orders Table (Đơn hàng gần đây)
```

---

## 🚀 Cách chạy test

### Bước 1: Đảm bảo Backend đang chạy

```bash
cd backend
docker-compose up -d
```

Kiểm tra: http://localhost:8081/swagger-ui/index.html

### Bước 2: Chạy Frontend

```bash
cd frontend
npm run dev
```

### Bước 3: Đăng nhập với tài khoản ADMIN

1. Mở: http://localhost:3000/login
2. Đăng nhập với tài khoản admin
3. Sau khi đăng nhập, bạn sẽ tự động vào trang `/manage`

### Bước 4: Xem Dashboard

- Dashboard sẽ hiển thị:
  - ✅ 4 Stats Cards (đã có sẵn)
  - ⭐ Revenue Chart (biểu đồ mới)
  - ✅ Recent Orders Table (đã có sẵn)

---

## 🔍 Kiểm tra Console

Mở Browser Console (F12) để xem logs:

```javascript
// Nếu API thành công, bạn sẽ thấy:
✅ Revenue Chart data loaded

// Nếu có lỗi:
❌ Error fetching revenue chart: [error details]
```

---

## 🎨 Features của Revenue Chart

### 1. Responsive Design
- Mobile: Chart height 300px
- Desktop: Chart height 350px
- Tự động scale theo màn hình

### 2. Interactive
- Hover vào điểm để xem chi tiết doanh thu
- Tooltip hiển thị format tiền VNĐ
- Smooth animation khi load

### 3. Period Selector
- **7 ngày**: Hiển thị doanh thu 7 ngày gần nhất
- **30 ngày**: Hiển thị doanh thu 30 ngày gần nhất  
- **3 tháng**: Hiển thị doanh thu 3 tháng gần nhất

### 4. Summary Statistics
- **Tổng doanh thu**: Tổng của cả period
- **Trung bình/ngày**: Average per day

---

## 🐛 Troubleshooting

### Lỗi: "Chart is not defined"
**Nguyên nhân**: Chart.js chưa được register đúng
**Giải pháp**: Đã fix trong code bằng cách register tất cả components

### Lỗi: "Cannot read property 'labels' of null"
**Nguyên nhân**: Backend chưa trả về data hoặc API lỗi
**Giải pháp**: 
1. Check backend đang chạy
2. Check endpoint `/api/v1/dashboard/revenue-chart` trong Swagger
3. Check console log để xem response

### Lỗi: "Failed to fetch"
**Nguyên nhân**: CORS hoặc backend không chạy
**Giải pháp**:
1. Đảm bảo backend chạy ở `localhost:8081`
2. Check CORS settings trong backend
3. Check network tab trong browser

### Biểu đồ không hiển thị
**Nguyên nhân**: Data rỗng hoặc format sai
**Giải pháp**:
1. Check console log xem data có đúng không
2. Verify API response format match với `RevenueChartData` type
3. Check backend có dữ liệu để thống kê không

---

## 📊 Expected Data Format

Backend API `/dashboard/revenue-chart?period=MONTH` phải trả về:

```json
{
  "success": true,
  "data": {
    "labels": ["01/12", "02/12", "03/12", ...],
    "values": [5000000, 7500000, 6200000, ...],
    "total": 180000000,
    "averagePerDay": 6000000,
    "period": "MONTH"
  },
  "message": "Success"
}
```

---

## ✨ Next Steps (Nếu test thành công)

Sau khi Revenue Chart chạy thành công, bạn có thể thêm:

1. **Order Status Chart** (Biểu đồ tròn - Doughnut)
2. **User Registration Chart** (Biểu đồ cột - Bar)
3. **Top Products Chart** (Biểu đồ ngang - Horizontal Bar)
4. **Low Stock Products Table**

Mỗi lần thêm 1 component để dễ debug!

---

## 🎉 Success Indicators

Khi chạy thành công, bạn sẽ thấy:

- ✅ 4 stats cards hiển thị đúng số liệu
- ✅ Biểu đồ doanh thu hiển thị smooth với màu xanh dương
- ✅ Hover vào chart point thấy tooltip với format VNĐ
- ✅ Click 3 nút period (7 ngày / 30 ngày / 3 tháng) chart update
- ✅ Summary stats (tổng & TB/ngày) hiển thị đúng
- ✅ Không có lỗi trong console
- ✅ Loading state hiển thị khi fetch data

**Happy Testing! 🚀**
