# QUY CHUẨN CODE (CODING CONVENTIONS)
**DỰ ÁN: UTE PHONE HUB**
**Phiên bản:** 1.0

---

## 1. NGUYÊN TẮC CHUNG (GENERAL PRINCIPLES)
1.  **Ngôn ngữ:**
    *   **Code:** 100% Tiếng Anh (Tên biến, hàm, class, comment trong code).
    *   **Commit Message:** Tiếng Anh (theo chuẩn Conventional Commits).
    *   **Tài liệu/Giải trình:** Tiếng Việt hoặc Tiếng Anh.
2.  **Clean Code:**
    *   **DRY (Don't Repeat Yourself):** Không copy-paste code. Nếu một đoạn logic xuất hiện 2 lần, hãy tách thành hàm hoặc component chung.
    *   **KISS (Keep It Simple, Stupid):** Viết code đơn giản, dễ hiểu. Tránh viết code quá phức tạp (over-engineering).
    *   **Single Responsibility:** Một hàm/class chỉ làm một nhiệm vụ duy nhất.

---

## 2. BACKEND CONVENTIONS (JAVA / SPRING BOOT)

### 2.1. Quy tắc đặt tên (Naming)
*   **Class/Interface:** `PascalCase` (Danh từ). Ví dụ: `UserService`, `ProductController`.
*   **Method/Variable:** `camelCase` (Động từ cho hàm). Ví dụ: `findUserByEmail()`, `totalPrice`.
*   **Constant/Enum:** `UPPER_SNAKE_CASE`. Ví dụ: `MAX_LOGIN_ATTEMPTS`, `ROLE_ADMIN`.
*   **Package:** `lowercase` toàn bộ. Ví dụ: `com.ute.phonehub.service`.
*   **Database Table (Entity):** Ánh xạ tên bảng là `snake_case`.
    *   Class: `OrderDetail` -> Table: `order_details`

### 2.2. Cấu trúc Code
*   **Controller:** Chỉ xử lý request/response, validate input cơ bản. **KHÔNG** viết business logic tại đây.
*   **Service:** Chứa toàn bộ business logic. Phải dùng `@Transactional` cho các hàm làm thay đổi dữ liệu.
*   **Repository:** Chỉ chứa các method truy vấn DB. Trả về `Optional<T>` thay vì `null`.
*   **DTO:**
    *   Luôn sử dụng DTO cho Request và Response. **TUYỆT ĐỐI KHÔNG** trả về Entity trực tiếp ra API.
    *   Tên DTO: `<Action><Entity>Request` / `<Entity>Response`. Ví dụ: `RegisterRequest`, `ProductResponse`.

### 2.3. Sử dụng Lombok & Injection
*   Sử dụng `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` để giảm boilerplate code.
*   **Dependency Injection:** Sử dụng **Constructor Injection** (khuyến nghị dùng `@RequiredArgsConstructor` của Lombok) thay vì `@Autowired` trên field.

**Ví dụ (Good):**
```java
@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các field final
public class UserService {
    private final UserRepository userRepository; // Bắt buộc final
}
```

### 2.4. Xử lý Exception
*   Không dùng `try-catch` bừa bãi trong Controller.
*   Ném ra `CustomException` (ví dụ: `ResourceNotFoundException`) và để `GlobalExceptionHandler` xử lý tập trung.

---

## 3. FRONTEND CONVENTIONS (NEXT.JS / REACT / TYPESCRIPT)

### 3.1. Quy tắc đặt tên
*   **Component/File .tsx:** `PascalCase`. Ví dụ: `ProductCard.tsx`, `LoginForm.tsx`.
*   **Function/Hook/Variable:** `camelCase`. Ví dụ: `handleSubmit`, `useAuth`.
*   **Folder:**
    *   Folder chứa Route (App Router): `kebab-case`. Ví dụ: `app/my-account/page.tsx`.
    *   Folder chứa Component: `PascalCase` hoặc `camelCase` tùy thống nhất (Khuyến nghị `PascalCase` để khớp với tên file chính).
*   **Interface/Type:** `PascalCase`. Ví dụ: `IUser`, `ProductProps`.

### 3.2. TypeScript
*   **NO `any`:** Tuyệt đối không dùng kiểu `any`. Phải định nghĩa type/interface rõ ràng.
*   Sử dụng `type` cho props và `interface` cho data models.

### 3.3. React & Next.js 15
*   **Functional Components:** Sử dụng Arrow Function hoặc Function Declaration. Ưu tiên `export default` cho Page và `export const` cho Component tái sử dụng.
*   **Server vs Client Component:**
    *   Mặc định là Server Component.
    *   Chỉ thêm `'use client'` khi cần tương tác (onClick, useState, useEffect).
*   **Hooks:** Luôn đặt hooks ở đầu function component.

### 3.4. Tailwind CSS v4
*   Sử dụng thư viện `clsx` và `tailwind-merge` (hàm `cn()` trong `lib/utils.ts`) để nối class động.
*   **Thứ tự class:** Sắp xếp class theo logic: `Layout` -> `Box Model` (Margin/Padding) -> `Visual` (Color, Font). (Cài extension *Tailwind CSS IntelliSense* để tự sắp xếp).

**Ví dụ (Good):**
```tsx
// Good
export const Button = ({ className, ...props }: ButtonProps) => {
  return (
    <button 
      className={cn("flex items-center justify-center px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600", className)}
      {...props}
    />
  );
};
```

---

## 4. DATABASE CONVENTIONS (POSTGRESQL)

*   **Table Name:** `snake_case`, danh từ số nhiều. Ví dụ: `users`, `product_images`.
*   **Column Name:** `snake_case`. Ví dụ: `created_at`, `full_name`.
*   **Primary Key:** Luôn đặt tên là `id` (BigInt/UUID).
*   **Foreign Key:** `<table_singular>_id`. Ví dụ: `user_id`, `category_id`.
*   **Constraints:** Luôn đặt ràng buộc `NOT NULL` cho các trường bắt buộc.

---

## 5. API STANDARD (RESTful)

### 5.1. Định dạng URL
*   Sử dụng danh từ số nhiều, chữ thường, gạch nối (`kebab-case`).
*   `GET /api/v1/products` (Lấy danh sách)
*   `GET /api/v1/products/{id}` (Lấy chi tiết)
*   `POST /api/v1/products` (Tạo mới)
*   `PUT /api/v1/products/{id}` (Cập nhật toàn bộ)
*   `DELETE /api/v1/products/{id}` (Xóa)

### 5.2. Định dạng Response (JSON)
Thống nhất một cấu trúc trả về chung cho toàn bộ hệ thống (dùng class `ApiResponse<T>` trong Backend).

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... } // Object hoặc Array
}
```
Hoặc khi lỗi:
```json
{
  "code": 400,
  "message": "Email already exists",
  "data": null
}
```

---

## 6. QUY TRÌNH PHÁT TRIỂN (DEVELOPMENT WORKFLOW)

### 6.1. Chuẩn bị trước khi Dev
Mỗi use case phải có đầy đủ tài liệu:
*   **Use Case Document** (trong `backend/DOCS/usecase/MXX.md`): Mô tả chi tiết actor, trigger, flow, exception.
*   **SRS (Software Requirement Specification)**: Yêu cầu chức năng chi tiết.
*   **Database Schema**: ERD và table definitions trong `backend/DOCS/DATABASE.md`.
*   **Sequence Diagram**: Mô tả luồng tương tác giữa Frontend - Backend - Database.
*   **Class Diagram**: Thiết kế các Entity, Service, Repository.

### 6.2. Quy trình Dev Backend (API First Approach)

**Thứ tự thực hiện (Layer-by-Layer):**

#### **Bước 1: Entity Layer (Database Mapping)**
*   Tạo Entity class trong `entity/` package ánh xạ với database table.
*   Sử dụng Lombok (`@Entity`, `@Table`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
*   Định nghĩa relationships (`@OneToMany`, `@ManyToOne`, etc.).
*   Đặt tên class theo `PascalCase`, table name theo `snake_case`.

**Ví dụ:**
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    // ... other fields
}
```

#### **Bước 2: Repository Layer (Data Access)**
*   Tạo Repository interface trong `repository/` package, kế thừa `JpaRepository<Entity, ID>`.
*   Đặt tên: `<Entity>Repository`.
*   Định nghĩa custom query methods nếu cần.
*   **LUÔN** trả về `Optional<T>` cho find methods, không trả về `null`.

**Ví dụ:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### **Bước 3: DTO Layer (Data Transfer Objects)**
*   Tạo Request DTO trong `dto/request/<module>/` - dùng cho input validation.
*   Tạo Response DTO trong `dto/response/<module>/` - dùng cho output.
*   Đặt tên: `<Action><Entity>Request`, `<Entity>Response`.
*   Sử dụng `@Valid`, `@NotBlank`, `@Size` để validate.
*   Response DTO nên có static method `fromEntity()` hoặc dùng MapStruct.

**Ví dụ:**
```java
// Request DTO
@Data
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @Size(max = 15)
    private String phoneNumber;
}

// Response DTO
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    // ...
    
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
    }
}
```

#### **Bước 4: Service Layer (Business Logic)**
*   Tạo Interface trong `service/` package: `I<Entity>Service`.
*   Tạo Implementation trong `service/impl/`: `<Entity>ServiceImpl`.
*   Sử dụng `@Service`, `@RequiredArgsConstructor` cho dependency injection.
*   Đặt `@Transactional` cho methods thay đổi dữ liệu.
*   Ném custom exception (`ResourceNotFoundException`, `BadRequestException`, etc.) thay vì trả về null.

**Ví dụ:**
```java
// Interface
public interface IUserService {
    UserResponse getUserById(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}

// Implementation
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    
    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        return UserResponse.fromEntity(user);
    }
    
    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }
}
```

#### **Bước 5: Controller Layer (API Endpoints)**
*   Tạo Controller trong `controller/` package.
*   Sử dụng `@RestController`, `@RequestMapping("/api/v1/<resource>")`, `@RequiredArgsConstructor`.
*   Chỉ xử lý request/response, **KHÔNG** chứa business logic.
*   Trả về `ResponseEntity<ApiResponse<T>>` để chuẩn hóa response format.
*   Thêm Swagger annotations (`@Tag`, `@Operation`, `@SecurityRequirement`).
*   Sử dụng `@Valid` để kích hoạt validation cho Request DTO.

**Ví dụ:**
```java
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý người dùng")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final IUserService userService;
    private final SecurityUtils securityUtils;
    
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        Long userId = securityUtils.getCurrentUserId(httpRequest);
        UserResponse user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", user));
    }
}
```

#### **Bước 6: Test API trên Swagger**
1.  Khởi động Backend: `cd backend && docker-compose up -d --build`.
2.  Truy cập Swagger UI: `http://localhost:8081/swagger-ui/index.html`.
3.  Test từng endpoint:
    *   Đăng nhập để lấy JWT token (nếu endpoint cần authentication).
    *   Click "Authorize", nhập `Bearer <token>`.
    *   Test các trường hợp: Success, Validation Error, Not Found, Unauthorized.
4.  Kiểm tra Response format phải đúng chuẩn `ApiResponse<T>`.
5.  Kiểm tra HTTP Status Code: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found).

**✅ Hoàn thành Backend khi:** API test thành công trên Swagger với đầy đủ test cases.

---

### 6.3. Quy trình Dev Frontend (API Integration)

**Thứ tự thực hiện:**

#### **Bước 1: Định nghĩa Types (TypeScript Interfaces)**
*   Tạo file trong `types/` package tương ứng với module (ví dụ: `user.d.ts`, `auth.d.ts`).
*   Copy structure từ Backend DTO Response để đảm bảo đồng bộ.
*   Export từ `types/index.ts`.

**Ví dụ:**
```typescript
// types/user.d.ts
export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  role: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  phoneNumber?: string;
}

// types/api.d.ts
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

#### **Bước 2: Tạo API Service Functions**
*   Thêm functions vào `lib/api.ts` để gọi Backend API.
*   Sử dụng `fetchAPI<T>()` wrapper đã có sẵn.
*   Tự động attach JWT token trong header nếu có.
*   Handle error trong catch block.

**Ví dụ:**
```typescript
// lib/api.ts
export const userAPI = {
  getMe: async (): Promise<ApiResponse<User>> => {
    return fetchAPI<User>('/user/me', { method: 'GET' });
  },
  
  updateProfile: async (data: UpdateProfileRequest): Promise<ApiResponse<User>> => {
    return fetchAPI<User>('/user/profile', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};
```

#### **Bước 3: Tạo Custom Hooks (nếu cần)**
*   Tạo hook trong `hooks/` để tái sử dụng logic (ví dụ: `useAuth`, `useUser`).
*   Sử dụng Zustand store để quản lý state toàn cục (cart, wishlist, user).

**Ví dụ:**
```typescript
// hooks/useAuth.ts
export function useAuth() {
  const { user, setUser } = useAuthStore();
  
  const login = async (credentials: LoginRequest) => {
    const response = await authAPI.login(credentials);
    if (response.code === 200) {
      setAuthTokens(response.data.accessToken, response.data.refreshToken);
      setUser(response.data.user);
    }
  };
  
  return { user, login, logout };
}
```

#### **Bước 4: Tạo UI Components**
*   **Atomic Components (ui/)**: Sử dụng Shadcn/UI components có sẵn (Button, Input, Modal).
*   **Feature Components (features/)**: Tạo component nghiệp vụ (LoginForm, ProductCard).

**Quy tắc:**
*   Mặc định là **Server Component**. Chỉ thêm `'use client'` khi cần:
    *   Sử dụng hooks (`useState`, `useEffect`, `useRouter`).
    *   Xử lý events (`onClick`, `onChange`).
    *   Tương tác với browser API (localStorage, etc.).
*   Component file name: `PascalCase.tsx`.
*   Sử dụng `cn()` utility để merge Tailwind classes.

**Ví dụ:**
```typescript
// components/features/auth/LoginForm.tsx
'use client';

import { useState } from 'react';
import { authAPI, setAuthTokens, setStoredUser } from '@/lib/api';

export function LoginForm() {
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await authAPI.login(formData);
      if (response.code === 200) {
        setAuthTokens(response.data.accessToken, response.data.refreshToken);
        setStoredUser(response.data.user);
        router.push('/');
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
    </form>
  );
}
```

#### **Bước 5: Tạo Pages (App Router)**
*   Tạo folder/file trong `app/` theo route structure.
*   Route Groups `(main)`, `(auth)`, `(admin)` để nhóm pages có cùng layout.
*   `page.tsx`: Main content của route.
*   `layout.tsx`: Shared layout cho các pages con.

**Ví dụ:**
```typescript
// app/(auth)/login/page.tsx
import { LoginForm } from '@/components/features/auth';

export default function LoginPage() {
  return (
    <div className="container mx-auto">
      <h1>Đăng nhập</h1>
      <LoginForm />
    </div>
  );
}
```

#### **Bước 6: Test Frontend**
1.  Khởi động Frontend: `cd frontend && npm run dev`.
2.  Truy cập: `http://localhost:3000`.
3.  Test từng tính năng:
    *   Kiểm tra form validation.
    *   Kiểm tra API calls (mở DevTools Network tab).
    *   Kiểm tra hiển thị data đúng.
    *   Kiểm tra error handling (nhập sai password, etc.).
4.  Test responsive design (Mobile, Tablet, Desktop).

**✅ Hoàn thành Frontend khi:** UI hoạt động đúng, API integration thành công, UX mượt mà.

---

## 7. QUY TRÌNH GIT (GIT WORKFLOW)

### 7.1. Cấu trúc Nhánh (Branch Structure)

```
main (Production Ready)
  ↑ merge sau khi test kỹ
Develop (Integration Branch)
  ↑ Pull Request + Review (Hưng)
feature/ModuleXX (Module Branch - dài hạn)
  ↑ Merge sau khi hoàn thành use case
feature/ModuleXX/use-case-name (Task Branch - ngắn hạn)
```

**Các loại nhánh:**
*   **`main`**: Nhánh production, code ổn định nhất. **CHỈ** Tech Lead được merge vào nhánh này.
*   **`Develop`**: Nhánh tích hợp code từ tất cả các module. Code ở đây phải build được và chạy được.
*   **`feature/ModuleXX`**: Nhánh dài hạn cho từng module (ví dụ: `feature/Module01`, `feature/Module02`). Tồn tại suốt quá trình dev module.
*   **`feature/ModuleXX/use-case-name`**: Nhánh ngắn hạn cho từng use case cụ thể. Sau khi hoàn thành, merge vào `feature/ModuleXX` và **XÓA**.

**Ví dụ:**
*   `feature/Module01` (Quản lý User & Auth)
*   `feature/Module01/register-user` (Use case đăng ký)
*   `feature/Module01/login-user` (Use case đăng nhập)
*   `feature/Module02/product-listing` (Danh sách sản phẩm)

### 7.2. Quy trình Làm việc Chi tiết

#### **Phase 1: Bắt đầu Task (Setup)**

1.  **Pull code mới nhất từ Develop:**
    ```bash
    git checkout Develop
    git pull origin Develop
    ```

2.  **Tạo/Checkout nhánh Module (nếu chưa có):**
    ```bash
    git checkout -b feature/Module01
    # Hoặc nếu đã có:
    git checkout feature/Module01
    git pull origin feature/Module01
    ```

3.  **Tạo nhánh Task từ Module branch:**
    ```bash
    git checkout -b feature/Module01/register-user
    ```

#### **Phase 2: Development (Coding)**

4.  **Code theo quy trình Backend -> Frontend:**
    *   Backend: Entity -> Repository -> DTO -> Service -> Controller -> Test Swagger.
    *   Frontend: Types -> API Service -> Components -> Pages -> Test UI.

5.  **Commit thường xuyên (mỗi khi hoàn thành 1 layer hoặc 1 chức năng nhỏ):**
    ```bash
    git add .
    git commit -m "feat(Module01): implement user registration entity and repository"
    git commit -m "feat(Module01): add register user service logic"
    git commit -m "feat(Module01): create register API endpoint"
    ```

6.  **Push code lên remote thường xuyên:**
    ```bash
    git push origin feature/Module01/register-user
    ```

#### **Phase 3: Hoàn thành Task (Merge to Module Branch)**

7.  **Đảm bảo code chạy ngon:**
    *   Backend: Test API trên Swagger (tất cả test cases).
    *   Frontend: Test UI trên browser (đầy đủ flows).

8.  **Pull code mới nhất từ Module branch để tránh conflict:**
    ```bash
    git checkout feature/Module01
    git pull origin feature/Module01
    git checkout feature/Module01/register-user
    git merge feature/Module01
    # Resolve conflicts nếu có
    ```

9.  **Merge Task branch vào Module branch (Local merge, không cần PR):**
    ```bash
    git checkout feature/Module01
    git merge feature/Module01/register-user --no-ff
    git push origin feature/Module01
    ```

10. **Xóa Task branch (sau khi merge thành công):**
    ```bash
    git branch -d feature/Module01/register-user
    git push origin --delete feature/Module01/register-user
    ```

#### **Phase 4: Hoàn thành Module (Pull Request to Develop)**

11. **Khi hoàn thành TẤT CẢ use cases của Module:**
    *   Pull code mới nhất từ Develop vào Module branch:
    ```bash
    git checkout feature/Module01
    git pull origin Develop
    # Resolve conflicts nếu có
    git push origin feature/Module01
    ```

12. **Tạo Pull Request từ `feature/Module01` -> `Develop`:**
    *   Trên GitHub/GitLab, click "New Pull Request".
    *   Source: `feature/Module01`, Target: `Develop`.
    *   Title: `[Module01] User Management & Authentication`.
    *   Description: Mô tả chi tiết các use cases đã implement, checklist tính năng.
    *   Assign reviewer: **Hưng** (Tech Lead).

13. **Code Review:**
    *   Hưng review code, comment nếu cần sửa.
    *   Developer sửa theo feedback, push thêm commits vào `feature/Module01`.
    *   Sau khi approve, Hưng hoặc Developer merge PR vào Develop.

14. **KHÔNG XÓA** Module branch sau khi merge (giữ lại để tiện maintain).

### 7.3. Commit Message (Conventional Commits)

Cấu trúc: `<type>(<module>): <subject>`

*   **Types:**
    *   `feat`: Tính năng mới
    *   `fix`: Sửa lỗi
    *   `docs`: Thay đổi tài liệu
    *   `style`: Format code (không ảnh hưởng logic)
    *   `refactor`: Refactor code (không thêm feature, không fix bug)
    *   `test`: Thêm test cases
    *   `chore`: Thay đổi config, dependencies

*   **Ví dụ:**
    *   `feat(Module01): implement user registration API`
    *   `fix(Module02): fix product price calculation bug`
    *   `docs(Module01): update API documentation for auth endpoints`
    *   `refactor(Module03): refactor order service to improve performance`
    *   `style(frontend): format code with prettier`

### 7.4. Quy tắc Pull Request (PR)

1.  **Title:** `[ModuleXX] Brief description`.
2.  **Description template:**
    ```markdown
    ## 📋 Mô tả
    Implement các use cases cho Module quản lý User & Auth.
    
    ## ✅ Checklist
    - [x] UC-M01-01: Đăng ký tài khoản
    - [x] UC-M01-02: Đăng nhập bằng email/password
    - [x] UC-M01-03: Đăng nhập bằng Google OAuth2
    - [x] UC-M01-04: Quên mật khẩu (OTP)
    - [x] UC-M01-05: Cập nhật thông tin cá nhân
    
    ## 🧪 Testing
    - [x] Backend: All APIs tested on Swagger
    - [x] Frontend: All pages tested on browser
    - [x] Database: Migrations applied successfully
    
    ## 📸 Screenshots (nếu có)
    [Attach screenshots]
    ```

3.  **Review Requirements:**
    *   **BẮT BUỘC:** Phải có Hưng (Tech Lead) approve.
    *   Code phải pass tất cả checks (build success, no conflicts).
    *   PR description phải đầy đủ, rõ ràng.

4.  **Merge Strategy:**
    *   Sử dụng **"Squash and Merge"** hoặc **"Merge Commit"** (tùy quy định team).
    *   Không dùng "Rebase and Merge" để giữ history rõ ràng.

---

## 8. MÔI TRƯỜNG & TOOLS

### 8.1. IDE & Extensions
*   **Backend IDE:** IntelliJ IDEA (Khuyến nghị) hoặc Eclipse/VS Code.
    *   **Required Plugins:**
        *   Lombok
        *   CheckStyle
        *   Spring Boot Tools
        *   Database Navigator (để xem DB)
*   **Frontend IDE:** VS Code.
    *   **Required Extensions:**
        *   ESLint
        *   Prettier - Code formatter
        *   Tailwind CSS IntelliSense
        *   ES7+ React/Redux/React-Native snippets
        *   Auto Rename Tag
        *   GitLens (để xem git history)

### 8.2. Yêu cầu Hệ thống
*   **Java:** JDK 17 (OpenJDK hoặc Oracle JDK).
*   **Node.js:** Node.js 20+ và npm.
*   **Docker:** Docker Desktop phải được cài đặt và chạy.
*   **Database Client:** DBeaver, pgAdmin hoặc TablePlus để quản lý PostgreSQL.
*   **API Testing:** Swagger UI (built-in) hoặc Postman.

### 8.3. Setup Environment

#### **Backend Setup:**
```bash
# Clone repository
git clone <repo-url>
cd ute-phonehub/backend

# Build và chạy với Docker
docker-compose up -d --build

# Kiểm tra logs
docker-compose logs -f

# Backend API: http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui/index.html
# PostgreSQL: localhost:5432 (user: postgres, pass: postgres)
# Redis: localhost:6379
```

#### **Frontend Setup:**
```bash
cd ute-phonehub/frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Frontend: http://localhost:3000
```

---

## 9. BEST PRACTICES & TIPS

### 9.1. Backend Best Practices
*   **Luôn validate input** ở Controller bằng `@Valid` và Bean Validation annotations.
*   **Không trả về Entity** trực tiếp từ API, luôn dùng DTO.
*   **Sử dụng @Transactional** cho các method thay đổi data (CREATE, UPDATE, DELETE).
*   **Log đầy đủ** bằng `@Slf4j` và `log.info()`, `log.error()`.
*   **Handle Exception** tập trung ở GlobalExceptionHandler, không dùng try-catch ở Controller.
*   **Password phải hash** bằng BCrypt, không lưu plain text.
*   **JWT Token phải verify** ở mọi API endpoint cần authentication.

### 9.2. Frontend Best Practices
*   **Tách logic ra hooks**: Không viết quá nhiều logic trong Component.
*   **Validate form phía client**: Trước khi gọi API, validate input để UX tốt hơn.
*   **Loading state**: Luôn hiển thị loading spinner khi gọi API.
*   **Error handling**: Hiển thị error message rõ ràng cho user.
*   **Type safety**: Không dùng `any`, định nghĩa Interface đầy đủ.
*   **Responsive design**: Test trên nhiều kích thước màn hình (Mobile, Tablet, Desktop).
*   **Accessibility**: Sử dụng semantic HTML, alt text cho images, aria-labels.

### 9.3. Database Best Practices
*   **Đặt index** cho các column thường xuyên query (email, username, foreign keys).
*   **Sử dụng transaction** cho các operations liên quan đến nhiều tables.
*   **Backup database** thường xuyên trước khi thay đổi schema.
*   **Migration scripts**: Ghi lại tất cả thay đổi DB schema trong migration files.

### 9.4. Git Best Practices
*   **Commit thường xuyên**: Commit sau mỗi micro-task (hoàn thành 1 method, 1 component).
*   **Pull trước khi push**: Để tránh conflict.
*   **Không commit file nhạy cảm**: `.env`, `application.properties` với production credentials.
*   **Không commit file build**: `target/`, `node_modules/`, `.next/` (đã có trong .gitignore).
*   **Viết commit message có ý nghĩa**: Người khác đọc commit history phải hiểu bạn làm gì.
*   **Review code kỹ trước khi merge**: Đọc lại diff, kiểm tra có bug hay code smell không.

---

## 10. TROUBLESHOOTING

### 10.1. Backend Issues
**Problem:** Port 8081 already in use.
**Solution:**
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

**Problem:** Database connection failed.
**Solution:**
*   Kiểm tra Docker container đang chạy: `docker ps`.
*   Restart container: `docker-compose restart postgres`.
*   Kiểm tra credentials trong `application.yaml`.

**Problem:** Lombok not working.
**Solution:**
*   Enable Annotation Processing trong IDE settings.
*   Cài plugin Lombok cho IDE.

### 10.2. Frontend Issues
**Problem:** Module not found error.
**Solution:**
```bash
# Xóa node_modules và reinstall
rm -rf node_modules package-lock.json
npm install
```

**Problem:** Tailwind classes not applying.
**Solution:**
*   Kiểm tra `tailwind.config.ts` có đúng content paths không.
*   Restart dev server: `Ctrl+C` và `npm run dev` lại.

**Problem:** API calls returning CORS error.
**Solution:**
*   Kiểm tra `CorsConfig.java` trong Backend có allow origin `http://localhost:3000` chưa.

---

## 11. CHECKLIST TRƯỚC KHI SUBMIT CODE

### Backend Checklist:
- [ ] Code compiles without errors (`mvn clean install`).
- [ ] All APIs tested on Swagger với đầy đủ test cases.
- [ ] No hardcoded secrets (passwords, API keys) trong code.
- [ ] Lombok annotations được dùng đúng cách.
- [ ] Exception handling đầy đủ.
- [ ] Log messages rõ ràng.
- [ ] Database migrations applied successfully.

### Frontend Checklist:
- [ ] No TypeScript errors (`npm run build`).
- [ ] All pages render correctly.
- [ ] Form validation works properly.
- [ ] API integration successful (check Network tab).
- [ ] Responsive design tested (Mobile, Tablet, Desktop).
- [ ] Loading states và error messages hiển thị đúng.
- [ ] No console errors in browser DevTools.

### Git Checklist:
- [ ] Commit message theo Conventional Commits format.
- [ ] Code đã được format (Prettier cho Frontend, CheckStyle cho Backend).
- [ ] No merge conflicts.
- [ ] Branch name đúng format `feature/ModuleXX/use-case-name`.
- [ ] PR description đầy đủ và rõ ràng.

---

**LƯU Ý QUAN TRỌNG:**
1.  **Đọc kỹ Use Case Document** trước khi code để hiểu đầy đủ requirements.
2.  **Hỏi khi chưa rõ**: Tốt hơn là hỏi trước khi code sai hướng.
3.  **Test kỹ trước khi PR**: Đừng để reviewer phát hiện bug cơ bản.
4.  **Respect Conventions**: Conventions không phải để làm khó, mà để code của team nhất quán và dễ maintain.

---

**Yêu cầu:** Toàn bộ thành viên trong nhóm đọc kỹ và setup IDE tuân thủ theo các quy chuẩn trên trước khi viết dòng code đầu tiên.