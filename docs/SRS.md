**UTE Phone Hub**

---

# MỤC LỤC

**DANH SÁCH SINH VIÊN THỰC HIỆN** .....................................................................
**LỜI CẢM ƠN** ............................................................................
**LỜI MỞ ĐẦU** .........................................................
**MỤC LỤC** .........................................................

**CHƯƠNG 1. GIỚI THIỆU** .......................................................
1.1. Mục tiêu ...................................................................
1.2. Phạm vi ......................................................................
1.3. Các định nghĩa, từ viết tắt, chữ viết tắt ......................................................... 
1.4. Tổng quan ..............................................................................

**CHƯƠNG 2. MÔ TẢ TỔNG QUAN** ....................................................................................
2.1. Bối cảnh sản phẩm ..............................................................................................
2.2. Đặc điểm người dùng ........................................................................................
2.3. Chức năng sản phẩm ...................................................................................
2.4. Các ràng buộc .............................................................................................
2.5. Kiến trúc hệ thống (Microservices-frontend / BFF) ...........................................................
2.6. Giả định và phụ thuộc ..........................................................................................
2.7. Phân bổ yêu cầu ...................................................................................................

**CHƯƠNG 3. YÊU CẦU CỤ THỂ** ......................................................................................
3.1. Yêu cầu chức năng (Functional Requirements) ..................................................................
   3.1.1. Phân hệ Khách hàng (Client) .......................................................................
   3.1.2. Phân hệ Quản trị (Admin) .............................................................................
3.2. Yêu cầu phi chức năng (Non-functional Requirements) .....................................................
   3.2.1. Hiệu năng (Performance) ..........................................................................
   3.2.2. Bảo mật (Security) ...........................................................................
   3.2.3. Khả năng bảo trì và mở rộng .....................................................................

**CHƯƠNG 4. YÊU CẦU MÔ HÌNH HÓA** ........................................................................
4.1. Use Case Diagram (Biểu đồ Ca sử dụng tổng quát) ...........................................................
4.2. Đặc tả Use Case (Use Case Specifications) ........................................................................
   **A. Nhóm chức năng Xác thực & Tài khoản (Module 1)**
   4.2.1. Register - Đăng ký tài khoản ....................................................................
   4.2.2. Verify Email - Xác thực email ......................................................................
   4.2.3. Log in - Đăng nhập (Email/Password & Google OAuth2) ..........................................
   4.2.4. Forgot Password - Quên mật khẩu ..............................................................................
   4.2.5. Manage Profile - Quản lý hồ sơ & Địa chỉ ..................................................................
   
   **B. Nhóm chức năng Mua sắm & Tương tác (Module 4, 5, 8)**
   4.2.6. View & Search Product - Xem & Tìm kiếm sản phẩm ................................................
   4.2.7. Compare Products - So sánh sản phẩm .......................................................................
   4.2.8. AI Chatbot Interaction - Tương tác với Chatbot hỗ trợ ...............................................
   4.2.9. Manage Cart - Quản lý giỏ hàng ................................................................
   4.2.10. Review Product - Đánh giá & Bình luận sản phẩm ...................................................

   **C. Nhóm chức năng Đặt hàng & Thanh toán (Module 6, 7, 9)**
   4.2.11. Checkout & Apply Voucher - Đặt hàng & Áp dụng Voucher ...................................
   4.2.12. Online Payment - Thanh toán trực tuyến (VNPay/Momo) .......................................
   4.2.13. Track Order - Tra cứu & Theo dõi đơn hàng (Kèm QR Code) ................................
   4.2.14. Cancel Order - Hủy đơn hàng ..........................................................................

   **D. Nhóm chức năng Quản trị (Module 2, 3, 10)**
   4.2.15. Manage Product - Quản lý sản phẩm ....................................................................
   4.2.16. Manage Category & Brand - Quản lý Danh mục & Thương hiệu ...........................
   4.2.17. Manage Order (Admin) - Quản lý & Cập nhật trạng thái đơn hàng ......................
   4.2.18. Manage User - Quản lý người dùng ......................................................................
   4.2.19. Manage Voucher - Quản lý khuyến mãi ..............................................................
   4.2.20. View Dashboard - Xem báo cáo thống kê ............................................................

**PHỤ LỤC MÔ TẢ USE CASE** .....................................................................................
4.3. Class Diagram (Biểu đồ Lớp) ................................................................. 
4.4. Sequence Diagrams (Biểu đồ Tuần tự) .......................................................................
   4.4.1. Sequence Diagram: Đăng nhập bằng Google .........................................................
   4.4.2. Sequence Diagram: Đặt hàng & Thanh toán Online ...............................................
   4.4.3. Sequence Diagram: Quản lý sản phẩm ....................................................................
4.5. Activity Diagrams (Biểu đồ Hoạt động) ......................................................................

**CHƯƠNG 5. ĐÍNH KÈM** .............................................................................................
5.1. API Documentation (Link tham chiếu) .......................................................................
5.2. Database Schema (Link tham chiếu) ...............................................................

**CHƯƠNG 6. TÀI LIỆU THAM KHẢO** ...........................................................


# CHƯƠNG 1. GIỚI THIỆU

## 1.1. Mục tiêu
Mục đích của tài liệu này là mô tả chi tiết các yêu cầu phần mềm (Software Requirements Specification - SRS) cho dự án **UTE Phone Hub**. Tài liệu này đóng vai trò là hồ sơ đặc tả chính thức, bao gồm toàn bộ các yêu cầu chức năng và phi chức năng mà hệ thống cần đáp ứng.

Tài liệu được biên soạn nhằm phục vụ các đối tượng sau:
*   **Đội ngũ phát triển (Developers):** Hiểu rõ các chức năng cần xây dựng, quy tắc nghiệp vụ và các ràng buộc kỹ thuật để triển khai mã nguồn (Frontend & Backend).
*   **Đội ngũ kiểm thử (Testers):** Dùng làm cơ sở để xây dựng kế hoạch kiểm thử (Test Plan) và các kịch bản kiểm thử (Test Cases) nhằm đảm bảo chất lượng sản phẩm.
*   **Quản trị dự án (Project Manager):** Dùng để theo dõi tiến độ, quản lý phạm vi dự án và kiểm soát sự thay đổi.
*   **Giảng viên hướng dẫn & Hội đồng phản biện:** Dùng để đánh giá mức độ hoàn thiện và tính phức tạp của đồ án môn học.

## 1.2. Phạm vi
**UTE Phone Hub** là một hệ thống thương mại điện tử B2C (Business to Consumer) chuyên kinh doanh các sản phẩm điện thoại di động và phụ kiện công nghệ. Hệ thống được phát triển mới hoàn toàn để thay thế các giải pháp cũ, áp dụng kiến trúc hiện đại **Backend For Frontend (BFF)** với sự tách biệt giữa **Spring Boot** (Backend) và **Next.js** (Frontend).

**Phạm vi hệ thống bao gồm các phân hệ chính sau:**

1.  **Phân hệ Khách hàng (Client Side Application):**
    *   Cung cấp giao diện mua sắm trực quan, hỗ trợ Responsive trên mọi thiết bị.
    *   Các chức năng cốt lõi: Tìm kiếm, Lọc sản phẩm, Xem chi tiết, **So sánh sản phẩm**, Quản lý giỏ hàng.
    *   Chức năng đặt hàng và thanh toán: Hỗ trợ Guest Checkout, **Thanh toán trực tuyến (VNPay/Momo)**, Sử dụng mã giảm giá.
    *   Chức năng tương tác: Đánh giá sản phẩm, **Tương tác với AI Chatbot** để được hỗ trợ tự động.
    *   Chức năng theo dõi: Tra cứu đơn hàng thông qua mã đơn hoặc **quét mã QR**.

2.  **Phân hệ Quản trị (Admin Side Application):**
    *   Quản lý toàn bộ vòng đời sản phẩm: Nhập mới, cập nhật thông tin, hình ảnh, thông số kỹ thuật và xóa mềm (Soft Delete).
    *   Quản lý kho hàng và đơn hàng: Cập nhật trạng thái đơn hàng theo quy trình nghiệp vụ.
    *   Quản lý người dùng và phân quyền hệ thống.
    *   Quản lý các chương trình khuyến mãi (Voucher).
    *   **Dashboard:** Xem báo cáo thống kê doanh thu, hiệu suất bán hàng trực quan.

3.  **Hệ thống Backend & API:**
    *   Cung cấp bộ RESTful API chuẩn để phục vụ dữ liệu cho cả Client và Admin.
    *   Xử lý các nghiệp vụ phức tạp, xác thực bảo mật (JWT, OAuth2) và tích hợp với các dịch vụ bên thứ 3 (Google, Payment Gateway, Chatbot).

**Các thành phần nằm ngoài phạm vi (Out of Scope):**
*   Hệ thống quản lý vận chuyển chi tiết (Logistics Management System) - Hệ thống chỉ ghi nhận trạng thái giao hàng, không điều phối shipper.
*   Xây dựng cổng thanh toán riêng - Hệ thống tích hợp API của các đơn vị trung gian.

## 1.3. Các định nghĩa, từ viết tắt, chữ viết tắt
Bảng dưới đây liệt kê các thuật ngữ chuyên môn và từ viết tắt được sử dụng trong tài liệu để đảm bảo sự hiểu biết thống nhất giữa các bên liên quan.

| Thuật ngữ / Viết tắt | Định nghĩa / Giải thích |
| :--- | :--- |
| **API** | Application Programming Interface - Giao diện lập trình ứng dụng. |
| **BFF** | Backend For Frontend - Kiến trúc tách biệt Backend phục vụ riêng cho Frontend. |
| **CRUD** | Create, Read, Update, Delete - Bốn thao tác cơ bản của cơ sở dữ liệu. |
| **DTO** | Data Transfer Object - Đối tượng dùng để truyền dữ liệu giữa các lớp trong hệ thống. |
| **JWT** | JSON Web Token - Chuẩn mở (RFC 7519) dùng để truyền tải thông tin an toàn, sử dụng cho xác thực. |
| **OAuth2** | Giao thức ủy quyền mở, được sử dụng trong tính năng "Đăng nhập bằng Google". |
| **OTP** | One Time Password - Mật khẩu dùng một lần (dùng cho Reset Password). |
| **QR Code** | Quick Response Code - Mã phản hồi nhanh, dùng để tra cứu đơn hàng. |
| **SKU** | Stock Keeping Unit - Mã định danh sản phẩm trong kho. |
| **Soft Delete** | Xóa mềm - Kỹ thuật đánh dấu dữ liệu là "đã xóa" (active = false) thay vì xóa vĩnh viễn khỏi CSDL. |
| **SRS** | Software Requirements Specification - Tài liệu đặc tả yêu cầu phần mềm. |
| **SSR** | Server-Side Rendering - Kỹ thuật render trang web từ phía server (Next.js) để tối ưu SEO. |
| **User** | Người dùng hệ thống nói chung (bao gồm cả Khách vãng lai và Thành viên). |
| **Admin** | Quản trị viên hệ thống, có quyền truy cập vào trang Dashboard. |

## 1.4. Tổng quan
Phần còn lại của tài liệu SRS được tổ chức như sau:

*   **Chương 2: Mô tả tổng quan** – Cung cấp cái nhìn tổng thể về bối cảnh sản phẩm, đặc điểm người dùng, môi trường vận hành, các giả định và ràng buộc của dự án.
*   **Chương 3: Các yêu cầu cụ thể** – Mô tả chi tiết từng yêu cầu chức năng (theo từng phân hệ Client và Admin) và các yêu cầu phi chức năng (hiệu năng, bảo mật, v.v.).
*   **Chương 4: Yêu cầu mô hình hóa** – Trình bày các biểu đồ phân tích hệ thống bao gồm Use Case Diagram (tổng quát và chi tiết), Class Diagram và các Sequence Diagram cho các luồng nghiệp vụ chính.
*   **Chương 5: Đính kèm** – Các tài liệu bổ trợ như link tới API Documentation hoặc Database Schema.
*   **Chương 6: Tài liệu tham khảo** – Danh sách các nguồn tài liệu tham chiếu.

Chào bạn Hưng,

Tiếp nối mạch nội dung, đây là **Chương 2: Mô tả tổng quan** của tài liệu SRS. Nội dung chương này tập trung vào bức tranh toàn cảnh của hệ thống **UTE Phone Hub**, làm rõ mối quan hệ với các hệ thống bên ngoài, đặc điểm người dùng và kiến trúc kỹ thuật tổng thể.

---

# CHƯƠNG 2. MÔ TẢ TỔNG QUAN

## 2.1. Bối cảnh sản phẩm
**UTE Phone Hub** là một ứng dụng web thương mại điện tử độc lập, được xây dựng mới hoàn toàn để thay thế các hệ thống cũ vốn hạn chế về công nghệ và khả năng mở rộng. Sản phẩm hoạt động như một nền tảng khép kín nhưng có khả năng giao tiếp với các dịch vụ bên ngoài thông qua API. Sản phẩm này được tham khảo với app: thegioididong.com

Mối quan hệ của hệ thống với các thực thể xung quanh:
*   **Hệ thống kế thừa:** Không có (Đây là sản phẩm phát triển mới từ đầu - Greenfield Project).
*   **Các thành phần tương tác bên ngoài:**
    *   **Google Identity Platform:** Cung cấp dịch vụ xác thực ủy quyền (OAuth2) giúp người dùng đăng nhập nhanh.
    *   **Payment Gateway (VNPay):** Xử lý các giao dịch thanh toán trực tuyến an toàn.
    *   **Chatbot Service (Tawk.to/Dialogflow):** Cung cấp nền tảng trí tuệ nhân tạo để hỗ trợ trả lời khách hàng tự động.
    *   **Delivery Service (Giao hàng tiết kiệm):** Cung cấp nền tảng API giao hàng.

## 2.2. Đặc điểm người dùng
Dựa trên phân tích nghiệp vụ, hệ thống phục vụ các nhóm đối tượng người dùng chính với các đặc điểm kỹ thuật và nhu cầu như sau:

### 2.2.1. Khách hàng (End User)
Đây là nhóm người dùng chính, bao gồm cả khách vãng lai (Guest) và thành viên (Member).
*   **Đặc điểm:** Đa dạng về độ tuổi và trình độ công nghệ. Mong muốn trải nghiệm mua sắm nhanh chóng, giao diện trực quan, dễ sử dụng trên điện thoại di động.
*   **Tần suất sử dụng:** Không thường xuyên (mua hàng) đến thường xuyên (theo dõi đơn hàng, xem sản phẩm mới).
*   **Yêu cầu chính:** Tìm kiếm sản phẩm dễ dàng, so sánh thông số kỹ thuật, quy trình thanh toán đơn giản, bảo mật thông tin cá nhân.

### 2.2.2. Quản trị viên (Admin)
*   **Đặc điểm:** Là nhân viên vận hành hoặc chủ cửa hàng. Có hiểu biết cơ bản về quy trình quản lý bán hàng và sử dụng máy tính thành thạo.
*   **Tần suất sử dụng:** Hàng ngày, liên tục trong giờ làm việc.
*   **Yêu cầu chính:** Giao diện quản trị (Dashboard) phải hiển thị số liệu chính xác, các thao tác nhập liệu/xử lý đơn hàng phải nhanh chóng và hạn chế sai sót.

### 2.2.3. Hệ thống/Dịch vụ (System Actors)
*   **Payment Gateway:** Yêu cầu hệ thống gửi đúng định dạng dữ liệu thanh toán (Secure Hash, Amount, Order Info) và phản hồi trạng thái giao dịch (IPN/Callback) kịp thời.

## 2.3. Chức năng sản phẩm
Tóm tắt các nhóm chức năng chính của hệ thống UTE Phone Hub:

**1. Nhóm chức năng Khám phá & Mua sắm (Client Side):**
*   Hiển thị danh sách sản phẩm với bộ lọc nâng cao (Giá, Hãng, Cấu hình).
*   **So sánh sản phẩm:** Cho phép so sánh thông số kỹ thuật giữa 2-3 sản phẩm.
*   **Gợi ý thông minh:** Hiển thị sản phẩm liên quan hoặc phụ kiện đi kèm.
*   Tương tác tự động qua **AI Chatbot**.

**2. Nhóm chức năng Đặt hàng & Thanh toán (Client Side):**
*   Quản lý giỏ hàng (Lưu trữ tạm thời bằng Redis).
*   Quy trình Checkout đa bước, hỗ trợ Guest Checkout.
*   Tích hợp thanh toán Online (VNPay/Momo) và COD.
*   Áp dụng mã giảm giá (Voucher).

**3. Nhóm chức năng Quản lý Cá nhân & Đơn hàng (Client Side):**
*   Đăng ký/Đăng nhập (Email & Google).
*   Quản lý sổ địa chỉ giao hàng.
*   Theo dõi trạng thái đơn hàng chi tiết.
*   **Tra cứu đơn hàng qua mã QR.**
*   Đánh giá và bình luận sản phẩm đã mua.

**4. Nhóm chức năng Quản trị (Admin Side):**
*   **Dashboard:** Báo cáo doanh thu, đơn hàng, người dùng theo thời gian thực.
*   Quản lý Sản phẩm: CRUD, quản lý kho, hình ảnh, thông số kỹ thuật (JSON).
*   Quản lý Đơn hàng: Duyệt đơn, cập nhật trạng thái vận chuyển.
*   Quản lý Người dùng: Phân quyền, khóa tài khoản.
*   Quản lý Khuyến mãi: Tạo và cấu hình Voucher.

## 2.4. Các ràng buộc
Dự án phải tuân thủ các ràng buộc sau trong quá trình phát triển và vận hành:

*   **Ràng buộc về Thời gian:** Dự án phải hoàn thành các chức năng cốt lõi và tài liệu trong vòng **6 tuần**.
*   **Ràng buộc về Công nghệ:**
    *   **Backend:** Bắt buộc sử dụng **Java Spring Boot 3.5.8**.
    *   **Frontend:** Bắt buộc sử dụng **Next.js 15.5.6 (Turbopack) và React 19**.
    *   **Styling:** Tailwind CSS 4.x.
    *   **Database:** PostgreSQL cho dữ liệu quan hệ, Redis cho Caching/Session.
    *   **API Documetation:**  SpringDoc OpenAPI 2.3.0
*   **Ràng buộc về Giao diện:** Phải đảm bảo **Responsive Design**, hiển thị tốt trên Mobile, Tablet và Desktop.
*   **Ràng buộc về Pháp lý:** Tuân thủ các quy định cơ bản về bảo mật thông tin người dùng (không lưu mật khẩu dạng plain-text).

## 2.5. Kiến trúc hệ thống
Hệ thống được thiết kế theo kiến trúc **Backend For Frontend (BFF)** kết hợp với mô hình Client-Server hiện đại:

*   **Frontend (Client):** Được xây dựng bằng **Next.js**, đóng vai trò là tầng trình diễn (Presentation Layer). Frontend giao tiếp với Backend thông qua RESTful API.
*   **Backend (Server):** Được xây dựng bằng **Spring Boot**, đóng vai trò là tầng xử lý nghiệp vụ (Business Logic Layer). Backend cung cấp các API, xử lý xác thực (JWT), và tương tác với Cơ sở dữ liệu.
*   **Data Layer:**
    *   **PostgreSQL:** Lưu trữ dữ liệu bền vững (Sản phẩm, Đơn hàng, User).
    *   **Redis:** Lưu trữ dữ liệu tạm thời (Session, Cart, Token Blacklist) để tăng tốc độ truy xuất.
*   **Infrastructure:** Toàn bộ hệ thống được đóng gói (Containerization) và triển khai bằng **Docker Compose**.

## 2.6. Giả định và phụ thuộc
*   **Giả định:**
    *   Các dịch vụ bên thứ 3 (VNPay, Google, Chatbot, GHTK) hoạt động ổn định và API không thay đổi cấu trúc trong quá trình phát triển dự án.
    *   Người dùng sử dụng các trình duyệt web hiện đại (Chrome, Edge, Safari) phiên bản mới nhất.
*   **Phụ thuộc:**
    *   Hệ thống phụ thuộc hoàn toàn vào kết nối Internet để hoạt động.
    *   Chức năng gửi email (Xác thực, Quên mật khẩu) phụ thuộc vào dịch vụ SMTP Server (ví dụ: Gmail SMTP).

## 2.7. Phân bổ yêu cầu
Do giới hạn về thời gian (6 tuần), các yêu cầu được phân bổ như sau:

*   **Giai đoạn 1 (Tuần 1-4 - Release 1.0):** Tập trung hoàn thiện các chức năng cốt lõi: Quản lý sản phẩm, Giỏ hàng, Đặt hàng, Thanh toán VNPay, Xác thực người dùng, Dashboard cơ bản.
*   **Giai đoạn 2 (Tuần 5-6 - Release 1.1):** Hoàn thiện các chức năng nâng cao: Chatbot, So sánh sản phẩm, Mã QR, Tra cứu đơn hàng công khai, Tối ưu UI/UX.
*   **Các chức năng hoãn lại (Future Scope):** Mobile App Native (Android/iOS), Hệ thống gợi ý sản phẩm chuyên sâu (Deep Learning).


# CHƯƠNG 3. CÁC YÊU CẦU CỤ THỂ

## 3.1. Yêu cầu chức năng (Functional Requirements)

Các yêu cầu chức năng được tổ chức theo hai phân hệ chính: **Khách hàng (Client Side)** và **Quản trị viên (Admin Side)**. Mỗi yêu cầu được gán một mã định danh duy nhất (ID) để thuận tiện cho việc tra cứu và kiểm thử sau này.

### 3.1.1. Phân hệ Khách hàng (User Side)

#### 3.1.1.1. Nhóm chức năng Xác thực & Tài khoản (Authentication & Account)
*Module phụ trách: M01*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-CLIENT-01** | **Đăng ký tài khoản** | Người dùng (Guest) có thể tạo tài khoản mới bằng Email. Tài khoản được kích hoạt ngay sau khi đăng ký thành công. |
| **FR-CLIENT-02** | **Đăng nhập (Email/Password)** | Người dùng đăng nhập bằng Email và Mật khẩu đã đăng ký. Hệ thống xác thực và trả về JWT (Access & Refresh Token). |
| **FR-CLIENT-03** | **Đăng nhập Google (OAuth2)** | Người dùng có thể đăng nhập nhanh bằng tài khoản Google. Nếu email chưa tồn tại, hệ thống tự động tạo tài khoản mới. |
| **FR-CLIENT-04** | **Quên mật khẩu** | Người dùng yêu cầu đặt lại mật khẩu qua Email. Hệ thống gửi mã OTP/Link để xác thực quyền sở hữu trước khi cho phép nhập mật khẩu mới. |
| **FR-CLIENT-05** | **Quản lý Hồ sơ cá nhân** | Người dùng (Member) có thể xem và cập nhật thông tin cá nhân: Họ tên, Số điện thoại. |
| **FR-CLIENT-06** | **Quản lý Sổ địa chỉ** | Người dùng có thể thêm, sửa, xóa các địa chỉ giao hàng và thiết lập một địa chỉ làm mặc định. |
| **FR-CLIENT-07** | **Đổi mật khẩu** | Người dùng có thể thay đổi mật khẩu đăng nhập (yêu cầu nhập mật khẩu cũ để xác thực). |

#### 3.1.1.2. Nhóm chức năng Khám phá & Tương tác sản phẩm
*Module phụ trách: M04, M08*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-CLIENT-08** | **Xem danh sách sản phẩm** | Hiển thị danh sách sản phẩm có phân trang (Pagination). Hỗ trợ hiển thị dạng Lưới (Grid) hoặc Danh sách (List). |
| **FR-CLIENT-09** | **Lọc & Sắp xếp** | Cho phép lọc sản phẩm theo: Khoảng giá, Thương hiệu, Danh mục, Thông số (RAM, ROM). Sắp xếp theo: Giá tăng/giảm, Mới nhất, Bán chạy. |
| **FR-CLIENT-10** | **Tìm kiếm sản phẩm** | Người dùng nhập từ khóa để tìm kiếm sản phẩm theo Tên hoặc Thương hiệu. Hệ thống gợi ý kết quả khi gõ (Autocomplete). |
| **FR-CLIENT-11** | **Xem chi tiết sản phẩm** | Hiển thị đầy đủ thông tin: Hình ảnh (Slide), Giá, Thông số kỹ thuật, Mô tả chi tiết, Tình trạng kho hàng. |
| **FR-CLIENT-12** | **So sánh sản phẩm** | Người dùng chọn 2-3 sản phẩm để so sánh trực quan các thông số kỹ thuật trên cùng một bảng. |
| **FR-CLIENT-13** | **Gợi ý sản phẩm** | Hệ thống hiển thị danh sách "Sản phẩm tương tự" hoặc "Phụ kiện thường mua kèm" tại trang chi tiết. |
| **FR-CLIENT-14** | **Tương tác Chatbot AI** | Người dùng có thể chat với Bot để hỏi thông tin sản phẩm, chính sách bảo hành hoặc tra cứu đơn hàng cơ bản. |
| **FR-CLIENT-15** | **Đánh giá & Bình luận** | Người dùng đã mua hàng có thể đánh giá (1-5 sao) và viết bình luận. Có thể tải lên hình ảnh thực tế. |

#### 3.1.1.3. Nhóm chức năng Giỏ hàng & Đặt hàng
*Module phụ trách: M05, M06, M09*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-CLIENT-16** | **Thêm vào giỏ hàng** | Cho phép thêm sản phẩm vào giỏ. Hỗ trợ lưu giỏ hàng tạm (Redis) cho khách vãng lai và đồng bộ khi đăng nhập. |
| **FR-CLIENT-17** | **Quản lý giỏ hàng** | Xem danh sách sản phẩm trong giỏ, cập nhật số lượng, xóa sản phẩm. Tự động tính tạm tính (Subtotal). |
| **FR-CLIENT-18** | **Áp dụng Mã giảm giá** | Người dùng nhập mã Voucher tại bước thanh toán. Hệ thống kiểm tra điều kiện và trừ tiền trực tiếp. |
| **FR-CLIENT-19** | **Đặt hàng (Checkout)** | Quy trình nhập thông tin giao hàng, chọn phương thức vận chuyển và xác nhận đơn hàng. |
| **FR-CLIENT-20** | **Thanh toán trực tuyến** | Tích hợp cổng thanh toán VNPay/Momo. Chuyển hướng người dùng sang trang thanh toán và xử lý kết quả trả về. |
| **FR-CLIENT-21** | **Thanh toán COD** | Cho phép chọn phương thức "Thanh toán khi nhận hàng". |

#### 3.1.1.4. Nhóm chức năng Quản lý Đơn hàng
*Module phụ trách: M07*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-CLIENT-22** | **Xem lịch sử đơn hàng** | Người dùng (Member) xem danh sách các đơn hàng đã đặt và trạng thái hiện tại của chúng. |
| **FR-CLIENT-23** | **Chi tiết đơn hàng** | Xem thông tin chi tiết của một đơn hàng: Sản phẩm, Tổng tiền, Địa chỉ, Lịch sử trạng thái (Timeline). |
| **FR-CLIENT-24** | **Tra cứu đơn hàng (Guest)** | Khách vãng lai tra cứu đơn hàng bằng cách nhập "Mã đơn hàng" và "Email" mà không cần đăng nhập. |
| **FR-CLIENT-25** | **Mã QR Đơn hàng** | Hệ thống tạo mã QR cho mỗi đơn hàng. Người dùng dùng mã này để xác nhận nhanh khi nhận hàng tại quầy. |
| **FR-CLIENT-26** | **Hủy đơn hàng** | Người dùng có thể hủy đơn hàng nếu đơn hàng đang ở trạng thái "Chờ xác nhận". |

---

### 3.1.2. Phân hệ Quản trị (Admin Side)

#### 3.1.2.1. Nhóm chức năng Quản lý Sản phẩm & Kho
*Module phụ trách: M02, M03*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-ADMIN-01** | **Quản lý Danh mục & Thương hiệu** | Thêm, Sửa, Xóa (có kiểm tra ràng buộc) các Danh mục sản phẩm và Thương hiệu. |
| **FR-ADMIN-02** | **Thêm mới sản phẩm** | Nhập thông tin sản phẩm, giá, tồn kho, upload nhiều hình ảnh, cấu hình thông số kỹ thuật (JSON). |
| **FR-ADMIN-03** | **Cập nhật sản phẩm** | Chỉnh sửa thông tin sản phẩm. Hỗ trợ cập nhật trạng thái "Ẩn/Hiện" hoặc "Hết hàng". |
| **FR-ADMIN-04** | **Xóa mềm sản phẩm (Soft Delete)** | Đánh dấu sản phẩm là "Đã xóa" (không hiển thị trên Client) nhưng vẫn giữ dữ liệu trong Database để tham chiếu đơn hàng cũ. |

#### 3.1.2.2. Nhóm chức năng Quản lý Đơn hàng & Thanh toán
*Module phụ trách: M07*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-ADMIN-05** | **Danh sách đơn hàng** | Xem toàn bộ đơn hàng trong hệ thống. Hỗ trợ lọc theo Trạng thái, Ngày đặt, Phương thức thanh toán. |
| **FR-ADMIN-06** | **Cập nhật trạng thái đơn** | Admin chuyển đổi trạng thái đơn hàng theo quy trình: Chờ xác nhận -> Đang đóng gói -> Đang giao -> Hoàn thành/Hủy. |
| **FR-ADMIN-07** | **Quét QR xác nhận đơn** | Admin sử dụng thiết bị để quét mã QR đơn hàng của khách, hệ thống tự động tìm đơn và cho phép cập nhật trạng thái nhanh. |

#### 3.1.2.3. Nhóm chức năng Quản trị Hệ thống & Marketing
*Module phụ trách: M09, M10*

| ID | Tên chức năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **FR-ADMIN-08** | **Dashboard Thống kê** | Hiển thị biểu đồ doanh thu, số lượng đơn hàng mới, top sản phẩm bán chạy trong ngày/tuần/tháng. |
| **FR-ADMIN-09** | **Quản lý Người dùng** | Xem danh sách người dùng. Khóa (Ban) hoặc Mở khóa tài khoản vi phạm. Cấp quyền Admin. |
| **FR-ADMIN-10** | **Quản lý Voucher** | Tạo mã giảm giá mới (theo % hoặc số tiền cố định), thiết lập ngày hết hạn và số lượng sử dụng tối đa. |
| **FR-ADMIN-11** | **Kiểm duyệt Đánh giá** | Xem danh sách đánh giá của khách hàng. Có quyền Ẩn các đánh giá có nội dung không phù hợp. |

---

## 3.2. Yêu cầu phi chức năng (Non-functional Requirements)

### 3.2.1. Hiệu năng (Performance)
*   **NFR-01:** Thời gian tải trang chủ (First Contentful Paint) phải dưới **1.5 giây** nhờ công nghệ Server-Side Rendering (Next.js).
*   **NFR-02:** Thời gian phản hồi của API cho các tác vụ đọc (Read) phải dưới **300ms**.
*   **NFR-03:** Hệ thống hỗ trợ tối thiểu **100 người dùng đồng thời** (Concurrent users) mà không gây lỗi timeout.
*   **NFR-04:** Dữ liệu giỏ hàng và phiên đăng nhập phải được lưu trong Redis để đảm bảo tốc độ truy xuất tức thì (< 50ms).

### 3.2.2. Bảo mật (Security)
*   **NFR-05:** Mật khẩu người dùng phải được băm (hashing) bằng thuật toán **BCrypt** trước khi lưu vào cơ sở dữ liệu.
*   **NFR-06:** Giao tiếp giữa Client và Server phải được mã hóa qua giao thức **HTTPS**.
*   **NFR-07:** Sử dụng **JWT** (Access Token ngắn hạn, Refresh Token dài hạn) cho cơ chế xác thực. Refresh Token phải được lưu trong Cookie với cờ `HttpOnly` và `Secure`.
*   **NFR-08:** API phải có cơ chế **Rate Limiting** để chống tấn công DDoS hoặc Spam (ví dụ: giới hạn 100 request/phút cho mỗi IP).
*   **NFR-09:** Phân quyền chặt chẽ (Role-Based Access Control): API quản trị (`/api/v1/admin/**`) chỉ cho phép tài khoản có role `ADMIN` truy cập.

### 3.2.3. Khả năng bảo trì & Mở rộng (Maintainability & Scalability)
*   **NFR-10:** Mã nguồn phải tuân thủ quy chuẩn **Coding Conventions** đã thống nhất (ESLint cho Frontend, Checkstyle cho Backend).
*   **NFR-11:** Hệ thống Backend phải được thiết kế theo kiến trúc phân tầng (Controller - Service - Repository) để dễ dàng thay đổi logic nghiệp vụ.
*   **NFR-12:** Hệ thống phải được đóng gói bằng **Docker** để đảm bảo tính nhất quán khi triển khai và dễ dàng mở rộng (Scale-out) khi cần thiết.
*   **NFR-13:** API phải được tài liệu hóa tự động bằng **OpenAPI (Swagger)** để đảm bảo tính cập nhật của tài liệu.

### 3.2.4. Giao diện người dùng (Usability)
*   **NFR-14:** Giao diện phải tuân thủ chuẩn **Responsive Web Design**, hiển thị tốt trên các độ phân giải màn hình thông dụng: Mobile (375px+), Tablet (768px+), Desktop (1024px+).
*   **NFR-15:** Hệ thống phải cung cấp phản hồi (Feedback) rõ ràng cho mọi hành động của người dùng (ví dụ: thông báo thành công, thông báo lỗi, trạng thái loading).


# CHƯƠNG 4. YÊU CẦU MÔ HÌNH HÓA

Chương này trình bày các mô hình phân tích hệ thống nhằm làm rõ các chức năng, cấu trúc dữ liệu và luồng tương tác của **UTE Phone Hub**. Các mô hình được xây dựng dựa trên ngôn ngữ mô hình hóa thống nhất (UML).

## 4.1. Use Case Diagram (Biểu đồ Ca sử dụng)

Biểu đồ Use Case cung cấp cái nhìn tổng quan về các tác nhân (Actors) và các chức năng (Use Cases) của hệ thống, cùng với mối quan hệ giữa chúng.

**[SECTION: DÁN HÌNH ẢNH USE CASE DIAGRAM TỔNG QUÁT TẠI ĐÂY]**

### 4.1.1. Mô tả các tác nhân (Actors)
Dựa trên biểu đồ, hệ thống bao gồm các tác nhân chính sau:

1.  **User (Người dùng nói chung):** Đại diện cho bất kỳ ai truy cập vào hệ thống. Có thể thực hiện các chức năng cơ bản như Tìm kiếm, Xem sản phẩm.
2.  **User chưa đăng ký (Guest):** Là người dùng chưa có tài khoản hoặc chưa đăng nhập. Họ có thể thực hiện Đăng ký.
3.  **User đã đăng ký (Registered User):** Là người dùng đã đăng nhập thành công. Họ được kế thừa quyền của User và có thêm quyền thực hiện các chức năng: Mua hàng, Quản lý giỏ hàng, Đánh giá, Quản lý hồ sơ cá nhân.
4.  **Admin (Quản trị viên):** Người chịu trách nhiệm vận hành hệ thống. Có quyền truy cập vào các chức năng quản trị như: Quản lý sản phẩm, Đơn hàng, Người dùng, Thống kê.
5.  **Payment Gateway (Cổng thanh toán):** Tác nhân hệ thống bên ngoài (VNPay/Momo), tham gia vào quá trình xử lý thanh toán trực tuyến.

### 4.1.2. Mô tả các gói chức năng (Packages)
Hệ thống được chia thành các nhóm chức năng chính:
*   **Nhóm Xác thực & Tài khoản:** Bao gồm Đăng ký, Đăng nhập, Quên mật khẩu, Quản lý profile.
*   **Nhóm Mua sắm:** Bao gồm Tìm kiếm, Xem chi tiết, Giỏ hàng, Đặt hàng, Thanh toán.
*   **Nhóm Quản trị:** Bao gồm các chức năng CRUD cho Sản phẩm, Danh mục, Voucher và xử lý Đơn hàng.

---

## 4.2. Đặc tả Use Case (Use Case Specifications)

Phần này mô tả chi tiết luồng đi của các Use Case quan trọng. Các bước được mô tả ngắn gọn, súc tích, tập trung vào logic nghiệp vụ.

### 4.2.1. Nhóm Use Case Xác thực & Tài khoản (Module 1)

#### **UC-01: Đăng ký tài khoản (Register)**

| Mục | Nội dung |
| :--- | :--- |
| **Actor** | User chưa đăng ký (Guest) |
| **Trigger** | Người dùng chọn chức năng "Đăng ký" trên giao diện. |
| **Description** | Cho phép người dùng tạo tài khoản mới bằng email để truy cập các chức năng dành cho thành viên. |
| **Pre-conditions** | Người dùng chưa đăng nhập. |
| **Post-conditions** | Tài khoản được tạo trong hệ thống với trạng thái "Hoạt động" (Active). Email chào mừng được gửi đi. |
| **Main Flow** | 1. Người dùng nhập thông tin: Họ tên, Email, Mật khẩu, Nhập lại mật khẩu. 2. Người dùng nhấn nút "Đăng ký". 3. Hệ thống kiểm tra định dạng email và độ mạnh mật khẩu. 4. Hệ thống kiểm tra email đã tồn tại trong cơ sở dữ liệu chưa. 5. Hệ thống mã hóa mật khẩu và lưu thông tin tài khoản mới. 6. Hệ thống gửi email chào mừng. 7. Hệ thống hiển thị thông báo thành công và chuyển hướng về trang đăng nhập. |
| **Alternate Flow** | **4a. Email đã tồn tại:** 1. Hệ thống báo lỗi "Email đã được sử dụng". 2. Gợi ý người dùng chuyển sang trang Đăng nhập. |
| **Exception Flow** | **3a. Dữ liệu không hợp lệ:** Hệ thống hiển thị thông báo lỗi ngay tại trường nhập liệu tương ứng (Client-side validation). **6a. Lỗi gửi mail:** Hệ thống log lỗi nhưng vẫn cho phép đăng ký thành công. |

#### **UC-02: Đăng nhập (Login)**

| Mục | Nội dung |
| :--- | :--- |
| **Actor** | User (Guest) |
| **Trigger** | Người dùng chọn chức năng "Đăng nhập". |
| **Description** | Xác thực danh tính người dùng để cấp quyền truy cập vào hệ thống. Hỗ trợ đăng nhập bằng Email/Mật khẩu và Google OAuth2. |
| **Pre-conditions** | Tài khoản đã tồn tại và không bị khóa. |
| **Post-conditions** | Người dùng nhận được JWT (Access Token & Refresh Token) và được chuyển hướng về trang chủ. |
| **Main Flow** | 1. Người dùng nhập Email và Mật khẩu. 2. Người dùng nhấn "Đăng nhập". 3. Hệ thống xác thực thông tin đăng nhập (so khớp hash mật khẩu). 4. Hệ thống kiểm tra trạng thái tài khoản (Active/Locked). 5. Hệ thống tạo chuỗi JWT và trả về cho trình duyệt. 6. Hệ thống chuyển hướng người dùng về trang chủ. |
| **Alternate Flow** | **(Đăng nhập Google OAuth2)** 1. Người dùng chọn "Đăng nhập bằng Google". 2. Hệ thống chuyển hướng sang trang xác thực của Google. 3. Google trả về thông tin người dùng sau khi xác thực thành công. 4. Hệ thống kiểm tra email từ Google: - Nếu chưa có: Tự động tạo tài khoản mới. - Nếu đã có: Thực hiện đăng nhập. 5. Hệ thống cấp JWT và chuyển hướng về trang chủ. |
| **Exception Flow** | **3a. Sai thông tin:** Hệ thống báo "Email hoặc mật khẩu không chính xác". **4a. Tài khoản bị khóa:** Hệ thống báo "Tài khoản đã bị khóa, vui lòng liên hệ Admin". |

#### **UC-03: Quên mật khẩu (Forgot Password)**

| Mục | Nội dung |
| :--- | :--- |
| **Actor** | User (Guest) |
| **Trigger** | Người dùng chọn "Quên mật khẩu" tại trang Đăng nhập. |
| **Description** | Cho phép người dùng thiết lập lại mật khẩu mới thông qua xác thực email. |
| **Pre-conditions** | Email đã được đăng ký trong hệ thống. |
| **Post-conditions** | Mật khẩu của người dùng được cập nhật mới. |
| **Main Flow** | 1. Người dùng nhập Email và nhấn "Gửi yêu cầu". 2. Hệ thống kiểm tra email tồn tại và gửi mã OTP qua email. 3. Người dùng nhập mã OTP và Mật khẩu mới. 4. Hệ thống xác thực OTP (kiểm tra đúng/sai và thời hạn). 5. Hệ thống cập nhật mật khẩu mới (đã mã hóa) vào cơ sở dữ liệu. 6. Hệ thống thông báo thành công và yêu cầu đăng nhập lại. |
| **Exception Flow** | **2a. Email không tồn tại:** Hệ thống vẫn báo "Nếu email tồn tại, mã OTP sẽ được gửi" để bảo mật thông tin. **4a. OTP sai/hết hạn:** Hệ thống báo lỗi và yêu cầu lấy mã mới. |

#### **UC-04: Quản lý thông tin cá nhân (Manage Profile)**

| Mục | Nội dung |
| :--- | :--- |
| **Actor** | User đã đăng ký |
| **Trigger** | Người dùng truy cập trang "Tài khoản của tôi". |
| **Description** | Người dùng xem và cập nhật thông tin cá nhân, đổi mật khẩu và quản lý sổ địa chỉ. |
| **Pre-conditions** | Người dùng đã đăng nhập. |
| **Post-conditions** | Thông tin người dùng trong cơ sở dữ liệu được cập nhật. |
| **Main Flow** | 1. Hệ thống hiển thị thông tin hiện tại (Họ tên, SĐT, Email). 2. Người dùng chỉnh sửa các trường thông tin cho phép. 3. Người dùng nhấn "Lưu thay đổi". 4. Hệ thống kiểm tra tính hợp lệ dữ liệu. 5. Hệ thống cập nhật vào cơ sở dữ liệu và thông báo thành công. |
| **Alternate Flow** | **(Quản lý địa chỉ)** 1. Người dùng chọn tab "Sổ địa chỉ". 2. Người dùng chọn "Thêm địa chỉ mới" hoặc "Sửa/Xóa" địa chỉ cũ. 3. Hệ thống lưu địa chỉ và thiết lập địa chỉ mặc định (nếu được chọn). |
| **Exception Flow** | **4a. Lỗi dữ liệu:** Hệ thống báo lỗi nếu SĐT không đúng định dạng hoặc để trống các trường bắt buộc. |

---

### 4.2.2. Nhóm Use Case Mua sắm & Tương tác (Module 4, 5, 8)
*(Liệt kê các Use Case thuộc các module khác)*
*   UC-05: Tìm kiếm & Xem chi tiết sản phẩm
*   UC-06: So sánh sản phẩm
*   UC-07: Quản lý giỏ hàng
*   UC-08: Đánh giá sản phẩm & Tương tác (Like/Unlike)
*   UC-09: Chat với AI Bot

### 4.2.3. Nhóm Use Case Đặt hàng & Thanh toán (Module 6, 7)
*   UC-10: Đặt hàng (Checkout)
*   UC-11: Thanh toán Online (VNPay/Momo)
*   UC-12: Xem lịch sử đơn hàng
*   UC-13: Tra cứu đơn hàng (Guest/QR Code)
*   UC-14: Hủy đơn hàng

### 4.2.4. Nhóm Use Case Quản trị (Admin - Module 2, 3, 9, 10)
*   UC-15: Quản lý Sản phẩm (CRUD)
*   UC-16: Quản lý Danh mục & Thương hiệu
*   UC-17: Quản lý Đơn hàng & Cập nhật trạng thái
*   UC-18: Quản lý Người dùng
*   UC-19: Quản lý Voucher
*   UC-20: Xem Dashboard thống kê


## 4.3. Biểu đồ Lớp (Class Diagram)

Biểu đồ lớp mô tả cấu trúc tĩnh của hệ thống, bao gồm các lớp thực thể (Entities), thuộc tính của chúng và các mối quan hệ (kết hợp, tập hợp, kế thừa) giữa các lớp. Đây là cơ sở để thiết kế Cơ sở dữ liệu (Database Schema).

**[SECTION: DÁN HÌNH ẢNH CLASS DIAGRAM TỔNG QUÁT TẠI ĐÂY]**

### 4.3.1. Mô tả các thực thể chính
Hệ thống bao gồm các nhóm thực thể chính sau:

1.  **Nhóm User (Người dùng):**
    *   `User`: Lớp cha chứa thông tin chung (id, email, password, full_name, role, status).
    *   `Address`: Lưu trữ danh sách địa chỉ giao hàng của người dùng (Quan hệ 1-N với User).
    *   `Role`: Xác định quyền hạn (ADMIN, USER).

2.  **Nhóm Product (Sản phẩm):**
    *   `Product`: Thông tin chính của sản phẩm (name, price, stock, description).
    *   `Category`: Danh mục sản phẩm (Quan hệ 1-N với Product).
    *   `Brand`: Thương hiệu sản phẩm.
    *   `ProductImage`: Lưu URL hình ảnh sản phẩm (Quan hệ 1-N).
    *   `ProductAttribute`: Lưu thông số kỹ thuật dạng JSON hoặc EAV (RAM, ROM, Color).

3.  **Nhóm Order (Đơn hàng):**
    *   `Order`: Thông tin đơn hàng tổng quát (total_amount, status, payment_method, shipping_address).
    *   `OrderItem`: Chi tiết từng sản phẩm trong đơn hàng (product_id, quantity, price_at_purchase).
    *   `Voucher`: Mã giảm giá áp dụng cho đơn hàng.

4.  **Nhóm Interaction (Tương tác):**
    *   `Cart` & `CartItem`: Giỏ hàng tạm thời.
    *   `Review`: Đánh giá và bình luận của người dùng về sản phẩm.

---

## 4.4. Biểu đồ Tuần tự (Sequence Diagrams)

Biểu đồ tuần tự mô tả sự tương tác giữa các đối tượng theo trình tự thời gian để thực hiện một chức năng cụ thể.

### 4.4.1. Mô tả luồng tổng thể (General Flow)
Hầu hết các chức năng trong hệ thống **UTE Phone Hub** tuân theo luồng tương tác chuẩn của kiến trúc Layered (Phân tầng):
1.  **Client (Next.js):** Người dùng tương tác trên giao diện, gửi HTTP Request (GET/POST/PUT/DELETE) kèm theo dữ liệu (JSON) tới Backend.
2.  **Controller (Spring Boot):** Tiếp nhận Request, thực hiện Validate dữ liệu đầu vào.
3.  **Service:** Thực thi các logic nghiệp vụ (Business Logic), tính toán, kiểm tra điều kiện.
4.  **Repository:** Truy xuất hoặc cập nhật dữ liệu vào Database (PostgreSQL) hoặc Cache (Redis).
5.  **Response:** Kết quả được trả về ngược lại theo chuỗi: Repository -> Service -> Controller -> Client (JSON Response).

### 4.4.2. Mô tả chi tiết cho Module 1 (Xác thực & Tài khoản)

#### **SQ-01: Đăng ký tài khoản (Register Flow)**
**[SECTION: DÁN HÌNH ẢNH SEQUENCE DIAGRAM ĐĂNG KÝ TẠI ĐÂY]**

**Mô tả luồng:**
1.  **User** nhập thông tin đăng ký trên giao diện và nhấn Submit.
2.  **Next.js Client** gọi API `POST /api/v1/auth/register`.
3.  **AuthController** nhận yêu cầu, gọi `AuthService`.
4.  **AuthService** kiểm tra email tồn tại trong `UserRepository`.
5.  Nếu hợp lệ, **AuthService** mã hóa mật khẩu, lưu User mới với trạng thái `UNVERIFIED` vào DB.
6.  **AuthService** tạo mã xác thực (Token), lưu vào Redis (có TTL).
7.  **EmailService** gửi email chứa link kích hoạt cho User.
8.  Hệ thống trả về thông báo thành công cho Client.

#### **SQ-02: Đăng nhập (Login Flow)**
**[SECTION: DÁN HÌNH ẢNH SEQUENCE DIAGRAM ĐĂNG NHẬP TẠI ĐÂY]**

**Mô tả luồng:**
1.  **User** nhập Email/Password.
2.  **Client** gọi API `POST /api/v1/auth/login`.
3.  **AuthenticationManager** (Spring Security) xác thực thông tin.
4.  Nếu đúng, **JwtTokenProvider** tạo `Access Token` và `Refresh Token`.
5.  **AuthService** lưu `Refresh Token` vào Redis để quản lý phiên.
6.  API trả về cặp Token cho **Client**.
7.  **Client** lưu Token (Cookie/LocalStorage) và chuyển hướng vào trang chủ.

#### **SQ-03: Đăng nhập Google (OAuth2 Flow)**
**[SECTION: DÁN HÌNH ẢNH SEQUENCE DIAGRAM OAUTH2 TẠI ĐÂY]**

**Mô tả luồng:**
1.  **User** nhấn "Login with Google".
2.  **Client** chuyển hướng User sang trang xác thực của Google.
3.  **Google** xác thực và trả về `Authorization Code`.
4.  **Client** gửi Code này tới API Backend.
5.  **Backend** trao đổi Code lấy `IdToken` từ Google, trích xuất email/name.
6.  **AuthService** kiểm tra Email trong DB:
    *   Nếu chưa có: Tạo mới User.
    *   Nếu có: Cập nhật thông tin (nếu cần).
7.  **Backend** tạo JWT riêng của hệ thống và trả về cho Client.

---

## 4.5. Biểu đồ Cộng tác (Collaboration Diagrams)

Biểu đồ cộng tác là một phiên bản khác của biểu đồ tuần tự, nhấn mạnh vào các mối quan hệ tổ chức giữa các đối tượng thay vì trình tự thời gian.

### 4.5.1. Mô tả luồng tổng thể
Các đối tượng tham gia chính bao gồm:
*   `:UserInterface` (Giao diện React Component)
*   `:APIController` (REST Endpoint)
*   `:BusinessService` (Logic xử lý)
*   `:DataRepository` (Truy xuất dữ liệu)
*   `:ExternalService` (Email, Google, VNPay)

### 4.5.2. Mô tả chi tiết cho Module 1

#### **CO-01: Quên mật khẩu (Forgot Password)**
**[SECTION: DÁN HÌNH ẢNH COLLABORATION DIAGRAM QUÊN MẬT KHẨU TẠI ĐÂY]**

**Mô tả tương tác:**
1.  `:User` gửi yêu cầu reset pass qua `:UserInterface`.
2.  `:APIController` chuyển tiếp yêu cầu đến `:AuthService`.
3.  `:AuthService` yêu cầu `:UserRepository` tìm kiếm email.
4.  Nếu tìm thấy, `:AuthService` tạo OTP và yêu cầu `:RedisTemplate` lưu trữ.
5.  `:AuthService` kích hoạt `:MailSender` để gửi OTP đến `:User`.

#### **CO-02: Quản lý hồ sơ (Update Profile)**
**[SECTION: DÁN HÌNH ẢNH COLLABORATION DIAGRAM UPDATE PROFILE TẠI ĐÂY]**

**Mô tả tương tác:**
1.  `:User` gửi thông tin mới (SĐT, Tên) qua `:ProfileComponent`.
2.  `:UserController` nhận request (kèm JWT Header).
3.  `:SecurityContext` xác thực người dùng hiện tại.
4.  `:UserService` gọi `:UserRepository` để cập nhật thực thể User.
5.  `:UserRepository` lưu thay đổi vào Database và trả về đối tượng User mới.


## 4.6. Kiến trúc Hệ thống (System Architecture)

Phần này mô tả kiến trúc triển khai mức cao (High-Level Architecture) của dự án, thể hiện các thành phần phần mềm, container và cách chúng giao tiếp với nhau.

**[SECTION: DÁN HÌNH ẢNH SYSTEM ARCHITECTURE DIAGRAM TẠI ĐÂY]**

### 4.6.1. Các thành phần kiến trúc
Hệ thống được thiết kế theo mô hình **Microservices-ready (BFF Pattern)**, triển khai trên nền tảng **Docker**.

1.  **Client Layer (Tầng Khách hàng):**
    *   **Web Browser / Mobile Device:** Thiết bị của người dùng truy cập vào ứng dụng.

2.  **Presentation Layer (Tầng Trình diễn - Frontend):**
    *   **Next.js Container (Node.js):** Chạy ứng dụng Frontend.
    *   Thực hiện Server-Side Rendering (SSR) để tối ưu SEO và tải trang ban đầu.
    *   Gọi API tới Backend Container.

3.  **Business Layer (Tầng Nghiệp vụ - Backend):**
    *   **Spring Boot Container (Tomcat):** Chạy ứng dụng Backend (Java 17).
    *   Cung cấp RESTful API.
    *   Xử lý Authentication (Spring Security), Business Logic.
    *   Giao tiếp với Database và Cache.

4.  **Data Layer (Tầng Dữ liệu):**
    *   **PostgreSQL Container:** Cơ sở dữ liệu quan hệ chính, lưu trữ bền vững.
    *   **Redis Container:** Cơ sở dữ liệu In-memory, dùng để lưu Session, Cache giỏ hàng, OTP.

5.  **External Services (Dịch vụ bên ngoài):**
    *   **Google Cloud:** Dịch vụ OAuth2 Login.
    *   **Payment Gateway:** VNPay/Momo Sandbox.
    *   **SMTP Server:** Dịch vụ gửi email (Gmail SMTP).
    *   **Chatbot Provider:** Dịch vụ Chatbot AI (Tawk.to).


# CHƯƠNG 5. PHỤ LỤC (APPENDICES)

Chương này cung cấp các thông tin bổ trợ chi tiết, bao gồm các quy tắc nghiệp vụ (Business Rules) và đường dẫn đến các tài liệu kỹ thuật liên quan.

## 5.1. Các quy tắc nghiệp vụ (Business Rules)
Bảng dưới đây mô tả các quy tắc ràng buộc logic mà hệ thống phải tuân thủ để đảm bảo tính toàn vẹn dữ liệu và quy trình vận hành.

| Mã quy tắc | Tên quy tắc | Mô tả chi tiết |
| :--- | :--- | :--- |
| **BR-AUTH-01** | **Độ mạnh mật khẩu** | Mật khẩu người dùng bắt buộc phải có độ dài tối thiểu 8 ký tự, bao gồm ít nhất: 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt. |

| **BR-ORDER-01** | **Hủy đơn hàng** | Khách hàng chỉ được phép tự hủy đơn hàng khi trạng thái đơn hàng là "Chờ xác nhận" (Pending). Khi đơn hàng đã chuyển sang "Đang xử lý" hoặc "Đang giao", chỉ Admin mới có quyền hủy. |
| **BR-ORDER-02** | **Quy trình trạng thái** | Trạng thái đơn hàng chỉ được chuyển đổi một chiều theo quy trình: `Pending` -> `Confirmed` -> `Shipping` -> `Delivered`. Không được phép chuyển ngược trạng thái (trừ trường hợp `Cancelled` hoặc `Returned`). |
| **BR-PROD-01** | **Mã SKU** | Mã SKU (Stock Keeping Unit) của sản phẩm phải là duy nhất trên toàn hệ thống. |
| **BR-PROD-02** | **Xóa danh mục** | Không được phép xóa một Danh mục (Category) hoặc Thương hiệu (Brand) nếu vẫn còn sản phẩm đang liên kết với nó. Phải gỡ bỏ hoặc chuyển sản phẩm sang danh mục khác trước khi xóa. |
| **BR-REV-01** | **Điều kiện đánh giá** | Người dùng chỉ được phép đánh giá và bình luận về sản phẩm khi và chỉ khi họ đã mua sản phẩm đó và đơn hàng ở trạng thái "Hoàn thành" (Delivered). |
| **BR-VOUCHER-01**| **Áp dụng Voucher** | Mỗi đơn hàng chỉ được áp dụng tối đa 01 mã giảm giá. Giá trị giảm giá không được vượt quá tổng giá trị đơn hàng (Tổng tiền thanh toán >= 0). |

## 5.2. Tài liệu kỹ thuật đính kèm
Do tính chất của dự án phần mềm hiện đại, các tài liệu kỹ thuật chi tiết được quản lý dưới dạng "Live Documentation" (Tài liệu sống) và được lưu trữ cùng với mã nguồn hoặc trên các công cụ chuyên dụng.

### 5.2.1. Tài liệu API (API Documentation)
Hệ thống sử dụng **SpringDoc OpenAPI 2.3.0** để tự động sinh tài liệu API từ mã nguồn Backend.
*   **Link truy cập (Localhost):** `http://localhost:8081/swagger-ui.html`
*   **Định dạng:** JSON/YAML (Tuân thủ chuẩn OpenAPI 3.0).
*   **Nội dung:** Danh sách Endpoint, phương thức (GET/POST/PUT/DELETE), cấu trúc Request/Response Body và mã lỗi (HTTP Status Code).

### 5.2.2. Thiết kế Cơ sở dữ liệu (Database Schema)
*   **File thiết kế:** `docs/database/ERD_UTEPhoneHub.png` (Ảnh sơ đồ ERD).
*   **Script khởi tạo:** `backend/src/main/resources/db/migration/V1__init_schema.sql` (Nếu dùng Flyway) hoặc `docker/postgres/init.sql`.

### 5.2.3. Giao diện mẫu (UI/UX Mockups)
*   **Công cụ thiết kế:** Figma.
*   **Link Project:** `[Dán Link Figma của nhóm tại đây]`
*   **Mô tả:** Chứa các bản vẽ Wireframe và High-fidelity design cho các trang chính (Trang chủ, Chi tiết sản phẩm, Checkout, Admin Dashboard).

---

# CHƯƠNG 6. TÀI LIỆU THAM KHẢO

## 6.1. Tài liệu chuẩn mực
1.  **IEEE Std 830-1998**: IEEE Recommended Practice for Software Requirements Specifications.
2.  **SWEBOK V3.0**: Guide to the Software Engineering Body of Knowledge.

## 6.2. Tài liệu công nghệ
1.  **Spring Boot 3.5.8 Documentation**: [https://docs.spring.io/spring-boot/index.html](https://docs.spring.io/spring-boot/index.html)
2.  **Next.js 15 Documentation**: [https://nextjs.org/docs](https://nextjs.org/docs)
3.  **React 19 Reference**: [https://react.dev/reference/react](https://react.dev/reference/react)
4.  **Tailwind CSS v4 Documentation**: [https://tailwindcss.com/docs](https://tailwindcss.com/docs)
5.  **PostgreSQL 15 Documentation**: [https://www.postgresql.org/docs/15/index.html](https://www.postgresql.org/docs/15/index.html)
6.  **VNPay Sandbox API Documentation**: Tài liệu tích hợp cổng thanh toán dành cho môi trường thử nghiệm.

## 6.3. Tài liệu nội bộ dự án
1.  **UTE Phone Hub - Project Definition**: Tài liệu định nghĩa bài toán (Version 2.0).
2.  **UTE Phone Hub - Coding Conventions**: Quy chuẩn lập trình cho Backend và Frontend.
3.  **UTE Phone Hub - Git Workflow**: Quy trình quản lý mã nguồn.

---

**DANH SÁCH HÌNH ẢNH**
*(Liệt kê danh sách các hình ảnh/biểu đồ đã sử dụng trong tài liệu để tạo mục lục hình ảnh tự động nếu cần)*
*   Hình 1: Use Case Diagram tổng quát
*   Hình 2: Class Diagram
*   Hình 3: Sequence Diagram - Đăng ký tài khoản
*   Hình 4: Sequence Diagram - Đăng nhập
*   Hình 5: Sequence Diagram - Google OAuth2
*   Hình 6: Collaboration Diagram - Quên mật khẩu
*   Hình 7: System Architecture Diagram
