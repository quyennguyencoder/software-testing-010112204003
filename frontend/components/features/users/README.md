# User Management Frontend - Hướng dẫn sử dụng

## 📍 Truy cập tính năng

### URL: `/manage/users`

**Hoặc từ Dashboard Admin:**
1. Đăng nhập với tài khoản Admin
2. Vào Dashboard (`/manage`)
3. Truy cập: `http://localhost:3000/manage/users`

---

## ✅ Các tính năng đã implement

### 1. **Xem danh sách người dùng**
- Hiển thị bảng user với các cột:
  - ID, Họ tên, Email, Số điện thoại
  - Loại tài khoản (CUSTOMER / ADMIN)
  - Trạng thái (ACTIVE / LOCKED)
  - Ngày tạo
  - Thao tác (Khóa/Mở khóa)
- Pagination (10 users/page)
- Loading state và Empty state

### 2. **Tìm kiếm & Lọc**
- **Tìm kiếm theo keyword:** Tên hoặc Email (debounce 500ms)
- **Lọc theo Loại tài khoản:** Tất cả / Khách hàng / Quản trị viên
- **Lọc theo Trạng thái:** Tất cả / Đang hoạt động / Đã khóa

### 3. **Khóa/Mở khóa tài khoản**
- **Khóa:** Icon khóa đỏ (chỉ với CUSTOMER)
- **Mở khóa:** Icon mở khóa xanh
- **Confirm Dialog:** Xác nhận trước khi thao tác
- **Không thể khóa ADMIN:** Nút khóa không hiển thị với role ADMIN

### 4. **Tạo tài khoản mới**
- Nút "Tạo tài khoản" (góc trên phải)
- Form validation:
  - Email (regex validation)
  - Password (min 6 ký tự)
  - Họ tên (required)
  - Số điện thoại (optional)
  - Loại tài khoản (CUSTOMER / ADMIN)
- Toast notification sau khi tạo thành công

---

## 🎨 UI Components sử dụng

### Atomic Components
- `ConfirmDialog` - Alert dialog xác nhận
- `CreateUserDialog` - Form tạo user
- `UserFiltersComponent` - Bộ lọc tìm kiếm
- `UserTable` - Bảng danh sách user

### Shadcn UI Components
- `alert-dialog` - Confirm actions
- `dialog` - Modal forms
- `input` - Text inputs
- `label` - Form labels
- `select` - Dropdown selects
- `sonner` - Toast notifications
- `button` - Action buttons

---

## 🔌 API Endpoints sử dụng

```typescript
// Get users với filters & pagination
GET /api/v1/admin/users?page=0&size=10&role=CUSTOMER&status=ACTIVE&keyword=search

// Lock user
PUT /api/v1/admin/users/{userId}/lock

// Unlock user
PUT /api/v1/admin/users/{userId}/unlock

// Create new user
POST /api/v1/admin/users
Body: {
  email: string,
  password: string,
  fullName: string,
  phoneNumber?: string,
  role: 'CUSTOMER' | 'ADMIN'
}
```

---

## 🚀 Chạy ứng dụng

### Backend (Terminal 1)
```bash
cd backend
docker-compose up -d --build
```

### Frontend (Terminal 2)
```bash
cd frontend
npm run dev
```

### Truy cập
```
Frontend: http://localhost:3000
Swagger API: http://localhost:8081/swagger-ui/index.html
Manage Users: http://localhost:3000/manage/users
```

---

## 🧪 Test các tính năng

### 1. Test tìm kiếm
- [ ] Nhập keyword vào ô search
- [ ] Kiểm tra debounce 500ms
- [ ] Verify kết quả phù hợp

### 2. Test lọc
- [ ] Chọn "Khách hàng" → Chỉ hiện CUSTOMER
- [ ] Chọn "Quản trị viên" → Chỉ hiện ADMIN
- [ ] Chọn "Đã khóa" → Chỉ hiện LOCKED status

### 3. Test khóa/mở khóa
- [ ] Click icon khóa đỏ trên user CUSTOMER
- [ ] Verify confirm dialog hiện ra
- [ ] Click "Xác nhận"
- [ ] Kiểm tra toast success
- [ ] Verify trạng thái đổi thành "Đã khóa"
- [ ] Click icon mở khóa xanh
- [ ] Verify mở khóa thành công

### 4. Test tạo tài khoản
- [ ] Click "Tạo tài khoản"
- [ ] Nhập email không hợp lệ → Hiện lỗi
- [ ] Nhập password < 6 ký tự → Hiện lỗi
- [ ] Điền form hợp lệ → Tạo thành công
- [ ] Verify user mới xuất hiện trong danh sách

### 5. Test pagination
- [ ] Verify tổng số trang hiển thị đúng
- [ ] Click "Sau" → Chuyển trang
- [ ] Click "Trước" → Quay lại
- [ ] Verify disable nút ở trang đầu/cuối

---

## 📁 File Structure

```
frontend/
├── app/
│   └── (admin)/
│       └── manage/
│           └── users/
│               └── page.tsx          # Main Manage Users Page ✅
├── components/
│   ├── features/
│   │   └── users/
│   │       ├── ConfirmDialog.tsx     # Alert confirm ✅
│   │       ├── CreateUserDialog.tsx  # Form tạo user ✅
│   │       ├── UserFilters.tsx       # Search & Filters ✅
│   │       ├── UserTable.tsx         # Table component ✅
│   │       └── index.ts              # Exports ✅
│   └── ui/                           # Shadcn UI components ✅
├── lib/
│   └── api.ts                        # adminUserAPI ✅
└── types/
    ├── user.d.ts                     # User types ✅
    └── index.ts                      # Type exports ✅
```

---

## 🎯 Use Case Coverage (M10.1)

| Use Case | Status | Notes |
|----------|--------|-------|
| 3.A. Tìm kiếm tài khoản | ✅ Done | Debounced search với keyword |
| 3.B. Khóa tài khoản | ✅ Done | Với confirm dialog, không khóa ADMIN |
| 3.C. Mở khóa tài khoản | ✅ Done | Với confirm dialog |
| 3.D. Tạo tài khoản mới | ✅ Done | Form validation đầy đủ |
| A1. Lọc theo loại tài khoản | ✅ Done | Dropdown CUSTOMER/ADMIN/ALL |
| A1. Lọc theo trạng thái | ✅ Done | Dropdown ACTIVE/LOCKED/ALL |
| E1. Không tìm thấy kết quả | ✅ Done | Empty state message |
| E2. Lỗi khi khóa ADMIN | ✅ Done | Không hiện nút khóa với ADMIN |
| E3. Email đã tồn tại | ✅ Done | Backend error handling + toast |

---

## 🐛 Troubleshooting

### Lỗi: "Không có quyền truy cập"
**Nguyên nhân:** Chưa đăng nhập hoặc không phải Admin  
**Giải pháp:** Đăng nhập với tài khoản Admin (xem `backend/ADMIN-CREDENTIALS.md`)

### Lỗi: "Không thể tải danh sách người dùng"
**Nguyên nhân:** Backend chưa chạy hoặc API endpoint sai  
**Giải pháp:**
1. Kiểm tra backend: `docker ps` (phải có container chạy)
2. Kiểm tra API URL: `NEXT_PUBLIC_API_URL` trong `.env.local`
3. Test endpoint: `curl http://localhost:8081/api/v1/admin/users`

### Toast không hiện
**Nguyên nhân:** Chưa thêm `<Toaster />` vào layout  
**Giải pháp:** Đã fix trong `app/layout.tsx` ✅

---

## 📝 Conventions tuân thủ

### Backend conventions ✅
- API endpoint: `/admin/users` (REST chuẩn)
- DTO: `CreateUserRequest`, `UsersPageResponse`
- Pagination: `page`, `size`, `totalPages`, `totalElements`

### Frontend conventions ✅
- **Server Component default:** `page.tsx` dùng `'use client'` vì cần state
- **Shadcn/UI:** Tất cả UI components từ `components/ui/`
- **TypeScript strict:** Không dùng `any`, interface rõ ràng
- **Naming:** PascalCase components, camelCase functions
- **Styling:** Tailwind CSS với `cn()` utility
- **State:** useState cho local, useAuth cho global
- **Error handling:** Try-catch + toast notifications

---

## 🎉 Completed!

Bạn đã có đầy đủ tính năng **Manage User (M10.1)** theo Use Case!

**Next steps:**
1. Test toàn bộ tính năng theo checklist trên
2. Thêm link "Quản lý người dùng" vào Sidebar (optional)
3. Add loading skeleton cho better UX (optional)
4. Add unit tests (optional)
