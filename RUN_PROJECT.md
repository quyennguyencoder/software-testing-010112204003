# 🚀 Hướng Dẫn Chạy Dự Án PhoneHub

## Yêu Cầu Hệ Thống
- Docker Desktop (v2.40.3+)
- Docker Compose
- PowerShell hoặc Terminal

---

## 📋 Các Bước Chạy Dự Án

### 1️⃣ Clone/Mở Dự Án
```bash
cd d:\Hky2-2025-2026\KiemChungPhanMen\phonehub
```

### 2️⃣ Khởi Động Toàn Bộ Backend, Database, Redis
```bash
cd backend
docker-compose up --build
```
**Thời gian chạy lần đầu:** ~5-10 phút (tải dependencies Maven)

**Đợi cho đến khi thấy:**
- ✅ PostgreSQL: "database system is ready to accept connections"
- ✅ Redis: Started (hoặc "Ready to accept connections")
- ✅ Backend: Hibernate queries đang chạy (Hibernate: select...)

### 3️⃣ Khởi Động Frontend (Terminal mới)
```bash
cd frontend
npm run dev
```

**Thời gian chạy:** ~1-2 phút lần đầu

**Đợi cho đến khi thấy:**
```
▲ Next.js 16.1.6
- Local:         http://localhost:3000
- Network:       http://192.168.120.1:3000
✓ Ready in 952ms
```

### 4️⃣ Truy Cập Ứng Dụng
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8081/api/v1
- **Database:** localhost:5434 (user: utephonehub / pwd: utephonehub)

---

## 🔑 Tài Khoản Test

| Username | Password | Role |
|----------|----------|------|
| leader_user | password123 | Leader |
| member_user | password123 | Member |
| seller_user | password123 | Seller |

---

## 📊 Dịch Vụ Chạy

| Dịch Vụ | Port | Trạng Thái |
|---------|------|-----------|
| Frontend (Next.js) | 3000 | ✅ Running |
| Backend (Spring Boot) | 8081 | ✅ Running |
| PostgreSQL | 5434 (external) / 5432 (internal) | ✅ Ready |
| Redis | 6379 | ✅ Ready |

---

## 🛑 Dừng Toàn Bộ Dự Án

**Trong terminal Backend:**
```bash
Ctrl + C
docker-compose down
```

**Trong terminal Frontend:**
```bash
Ctrl + C
```

---

## 🐛 Khắc Phục Sự Cố

### Backend không kết nối Database
```bash
# Kiểm tra status container
docker ps

# Xem log backend
docker logs phonehub-backend

# Reset Docker
docker system prune -af --volumes
docker-compose up --build
```

### Frontend không tải API
- Đảm bảo Backend đang chạy trên port 8081
- Kiểm tra Console (F12) để xem lỗi

### Database bị lỗi
```bash
# Xóa volume database cũ
docker-compose down -v
docker-compose up --build
```

---

## 📁 Cấu Trúc Thư Mục

```
phonehub/
├── backend/              # Spring Boot API
│   ├── src/             # Source code
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── .env             # Environment variables
├── frontend/            # Next.js Frontend
│   ├── app/            # Pages
│   ├── components/     # React components
│   └── lib/            # Utilities & API
├── postman_tests/      # Postman collections
└── docs/               # Documentation
```

---

## 📝 Ghi Chú

- **Database:** PostgreSQL 17 + Redis 8
- **ORM:** Hibernate JPA
- **Framework Frontend:** Next.js 16 + TypeScript
- **Framework Backend:** Spring Boot 3.5.8 + Java 17
- **Containerization:** Docker + Docker Compose

✅ **Dự án sẵn sàng chạy!** 🎉
