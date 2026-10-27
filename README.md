# TechFlow — Task Management API

Backend API quản lý công việc cá nhân (To-do List nâng cao) hỗ trợ task, project, tag, subtask, reminder, recurring task, thống kê năng suất và xem theo lịch.

## 🚀 Tính năng chính

- **Xác thực & phân quyền:** Đăng ký, đăng nhập, refresh token (JWT stateless), mã hóa mật khẩu BCrypt, kiểm tra ownership trên mọi resource.
- **Quản lý task:** Tạo, xem, cập nhật, xóa task; trạng thái (TODO/IN_PROGRESS/COMPLETED/CANCELLED), ưu tiên (LOW/MEDIUM/HIGH/URGENT), hạn hoàn thành, tác vụ lặp lại (DAILY/WEEKLY/MONTHLY).
- **Project:** Nhóm task theo dự án, thống kê số task / task hoàn thành trong project.
- **Tag:** Phân loại task qua thẻ (nhiều thẻ/task, tên thẻ chuẩn hóa unique theo user).
- **Subtask:** Chia task thành các công việc nhỏ, tự động cập nhật status task cha khi tất cả subtask hoàn thành.
- **Reminder:** Đặt nhắc tác vụ, scheduler nền xử lý gửi nhắc.
- **Tìm kiếm & lọc:** Tìm theo tiêu đề, lọc theo trạng thái/ưu tiên/project/thẻ/khoảng thời gian, sắp xếp, phân trang.
- **Xem theo thời gian:** Today (hôm nay + quá hạn), Upcoming (sắp tới), Overdue (quá hạn), Calendar (theo ngày), Statistics (tổng quan completion rate).

## 🛠 Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.4.3 |
| Cơ sở dữ liệu | PostgreSQL (prod/dev), H2 (test) |
| Migration | Flyway |
| Xác thực | JWT (io.jsonwebtoken 0.12.6), BCrypt |
| Xây dựng | Maven |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Giám sát | Spring Boot Actuator (health, info, metrics) |
| Test | JUnit 5, MockMvc, Testcontainers |

## 📂 Cấu trúc thư mục

```
src/main/java/com/techflow/
├── auth/             # Đăng ký, đăng nhập, JWT
├── common/           # BaseEntity, response, exception, config
├── project/          # Quản lý dự án
├── reminder/         # Nhắc việc, scheduler
├── security/         # Spring Security, JWT filter, entrypoint
├── task/             # Task, tag, subtask
└── user/             # Model người dùng
src/main/resources/
├── application.yml               # Cấu hình chung
├── application-dev.yml           # Môi trường phát triển
├── application-prod.yml          # Môi trường sản xuất
├── application-test.yml          # Môi trường kiểm thử
└── db/migration/V1__init_schema.sql  # Flyway migration
```

## 📦 Cài đặt & chạy cục bộ

### Yêu cầu

- JDK 17+
- Maven 3.9+
- PostgreSQL 16+ (hoặc dùng Docker Compose)

### 1. Clone & cấu hình env

```bash
cp .env.example .env
# Chỉnh sửa các giá trị trong .env (đặc biệt JWT_SECRET, DB_PASSWORD)
```

### 2. Chạy với Docker Compose (khuyên dùng)

```bash
docker-compose up --build
```

API sẽ chạy tại: `http://localhost:8080`  
PostgreSQL tại: `localhost:5432`

### 3. Chạy trực tiếp với Maven (dev)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API chạy tại: `http://localhost:8080`

## 🧪 Chạy kiểm thử

```bash
mvn test
```

Kiểm thử sử dụng H2 in-memory (`application-test.yml`), không cần cơ sở dữ liệu ngoài.

## 🐳 Triển khai sản xuất

### Biến môi trường bắt buộc (xem `.env.example`)

| Biến | Mô tả | Ví dụ |
|---|---|---|
| `DB_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://db:5432/techflow_db` |
| `DB_USERNAME` | Tên user DB | `techflow_user` |
| `DB_PASSWORD` | Mật khẩu DB | `strong_password` |
| `JWT_SECRET` | Khá ký JWT (tối thiểu 256-bit) | `random-hex-string` |
| `SPRING_PROFILES_ACTIVE` | Chân Profile | `prod` |

### Build image Docker

```bash
docker build -t techflow-api:latest .
```

### Health check

```bash
curl -f http://localhost:8080/actuator/health
```

## 🔌 API Endpoints (tóm tắt)

| Phương thức | Đường dẫn | Mô tả | Auth |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Đăng ký | ❌ |
| `POST` | `/api/v1/auth/login` | Đăng nhập | ❌ |
| `POST` | `/api/v1/auth/refresh` | Làm mới token | ❌ |
| `GET` | `/api/v1/tasks` | Danh sách task (lọc, tìm, sắp xếp, phân trang) | ✅ |
| `POST` | `/api/v1/tasks` | Tạo task mới | ✅ |
| `GET` | `/api/v1/tasks/{id}` | Chi tiết task | ✅ |
| `PUT` | `/api/v1/tasks/{id}` | Cập nhật task | ✅ |
| `DELETE` | `/api/v1/tasks/{id}` | Xóa task | ✅ |
| `POST` | `/api/v1/tasks/{id}/complete` | Đánh dấu hoàn thành | ✅ |
| `GET` | `/api/v1/tasks/views/today` | Task hôm nay + quá hạn | ✅ |
| `GET` | `/api/v1/tasks/views/upcoming` | Task sắp tới | ✅ |
| `GET` | `/api/v1/tasks/views/overdue` | Task quá hạn | ✅ |
| `GET` | `/api/v1/calendar` | Lịch các task có hạn | ✅ |
| `GET` | `/api/v1/statistics/summary` | Thống kê năng suất | ✅ |
| `GET` | `/api/v1/projects` | Danh sách dự án | ✅ |
| `POST` | `/api/v1/projects` | Tạo dự án | ✅ |
| `GET` | `/api/v1/tags` | Danh sách thẻ | ✅ |
| `POST` | `/api/v1/tags` | Tạo thẻ | ✅ |
| `GET` | `/api/v1/tasks/{taskId}/subtasks` | Danh sách subtask | ✅ |
| `POST` | `/api/v1/tasks/{taskId}/subtasks` | Tạo subtask | ✅ |
| `GET` | `/api/v1/tasks/{taskId}/reminders` | Danh sách nhắc việc | ✅ |
| `POST` | `/api/v1/tasks/{taskId}/reminders` | Tạo nhắc việc | ✅ |

## 🔐 Bảo mật

- Mật khẩu được lưu dưới dạng BCrypt hash (không lưu plain text).
- Tất cả endpoint private yêu cầu JWT Bearer token.
- Ownership được kiểm tra trên backend (user không thể truy cập task/project/tag của người khác).
- Session stateless (không lưu session ở server).
- CORS chỉ cho phép các origin được cấu hình.

## 📊 Giám sát

- `/actuator/health` — trạng thái hoạt động (liveness/readiness)
- `/actuator/info` — thông tin ứng dụng
- `/actuator/metrics` — chỉ số đo lường
- Log có chứa `correlationId` để truy vết request.

## 🤝 Đóng góp

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/ten-tinh-nang`)
3. Commit thay đổi (`git commit -m "feat: mô tả"`)
4. Push (`git push origin feature/ten-tinh-nang`)
5. Mở Pull Request

## 📄 Giấy phép

Trong quá trình phát triển.

---
💡 **Lưu ý:** Nếu đây là lần đầu triển khai, hãy tạo repository trên GitHub/GitLab, đổi branch thành `main`, thêm remote và push mã nguồn trước khi kích hoạt CI/CD.