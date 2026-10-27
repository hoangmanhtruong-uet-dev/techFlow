# 03_USER_FLOW.md

# USER FLOWS — TASKFLOW

---

# UF-001 — REGISTER

## Actor

Guest.

## Precondition

Người dùng chưa đăng nhập.

## Main Flow

1. Guest mở Register Page.
2. Hệ thống hiển thị form đăng ký.
3. Guest nhập email.
4. Guest nhập password.
5. Guest chọn Register.
6. Hệ thống validate dữ liệu.
7. Hệ thống kiểm tra email.
8. Hệ thống tạo account.
9. Hệ thống chuyển người dùng tới Login hoặc tạo session.
10. User vào Dashboard.

## Alternative Flow

### 6A — Invalid email

Hệ thống hiển thị:

`Invalid email address.`

### 6B — Weak password

Hệ thống hiển thị validation error.

### 7A — Email exists

Hệ thống hiển thị:

`Email is already registered.`

---

# UF-002 — LOGIN

## Actor

Registered User.

## Precondition

Người dùng có tài khoản.

## Main Flow

1. User mở Login.
2. Nhập email.
3. Nhập password.
4. Chọn Login.
5. System validate.
6. System kiểm tra credentials.
7. System tạo authenticated session.
8. User được chuyển tới Dashboard.

## Alternative Flow

Sai email/password:

→ Login bị từ chối.
→ Hiển thị error message.

---

# UF-003 — CREATE TASK

## Actor

Authenticated User.

## Precondition

User đã login.

## Trigger

User chọn `Add Task`.

## Main Flow

1. Dashboard.
2. User chọn Add Task.
3. System mở Create Task Form.
4. User nhập Title.
5. User có thể nhập:

    * Description.
    * Deadline.
    * Priority.
    * Project.
    * Tags.
    * Reminder.
    * Recurrence.
6. User chọn Create.
7. System validate dữ liệu.
8. System kiểm tra ownership của project/tag liên quan.
9. System tạo task.
10. System hiển thị success state.
11. Task xuất hiện trong Task List.

## Alternative Flow

### 7A — Empty title

Không tạo task.

Hiển thị:

`Task title is required.`

### 8A — Invalid Project

Nếu project không thuộc user:

→ Reject request.

### 8B — Invalid Reminder

Nếu reminder không hợp lệ:

→ Reject request.

## Postcondition

Task mới tồn tại và thuộc authenticated user.

---

# UF-004 — UPDATE TASK

1. User mở Task Detail.
2. User chọn Edit.
3. System hiển thị dữ liệu hiện tại.
4. User chỉnh sửa.
5. User chọn Save.
6. System validate.
7. System kiểm tra ownership.
8. System cập nhật task.
9. System trả task mới.
10. UI cập nhật.

Nếu task không tồn tại:

→ TASK_NOT_FOUND.

Nếu task thuộc user khác:

→ FORBIDDEN.

---

# UF-005 — COMPLETE TASK

1. User xem Task.
2. User chọn Complete.
3. System kiểm tra ownership.
4. System chuyển:

`status = COMPLETED`

5. System set:

`completedAt = current timestamp`

6. Nếu task recurring:

    * System tính lần tiếp theo.
    * System tạo occurrence tiếp theo.
7. UI hiển thị task completed.

---

# UF-006 — DELETE TASK

1. User mở Task.
2. Chọn Delete.
3. System hiển thị confirm dialog.
4. User xác nhận.
5. System kiểm tra ownership.
6. System xóa task.
7. User được đưa về Task List.

Cancel:

→ Không thay đổi dữ liệu.

---

# UF-007 — CREATE PROJECT

1. User mở Projects.
2. Chọn New Project.
3. Nhập project name.
4. Chọn Create.
5. System validate.
6. Project được tạo.
7. Project xuất hiện trong Sidebar/Project List.

---

# UF-008 — DELETE PROJECT

1. User mở Project.
2. Chọn Delete.
3. System yêu cầu xác nhận.
4. User xác nhận.
5. System xóa project.
6. Các task thuộc project được chuyển:

`projectId = null`

7. Task không bị xóa.

---

# UF-009 — SEARCH TASK

1. User nhập từ khóa.
2. System debounce input.
3. System tìm task thuộc user hiện tại.
4. System trả kết quả.
5. UI hiển thị danh sách phù hợp.

Nếu không có kết quả:

`No tasks found.`

---

# UF-010 — FILTER TASK

1. User mở Task List.
2. Chọn Filter.
3. Chọn một hoặc nhiều điều kiện.
4. System áp dụng filter.
5. UI hiển thị kết quả.

Ví dụ:

`status = TODO`

*

`priority = HIGH`

*

`project = University`

---

# UF-011 — SET REMINDER

1. User tạo hoặc chỉnh sửa Task.
2. User chọn reminder.
3. User chọn thời gian.
4. System kiểm tra deadline.
5. System lưu reminder.
6. Đến thời điểm reminder:

    * System tạo notification.
7. User nhận thông báo.

---

# UF-012 — RECURRING TASK

1. User tạo Task.
2. Bật Recurring.
3. Chọn:

    * Daily.
    * Weekly.
    * Monthly.
4. System lưu recurrence rule.
5. User hoàn thành occurrence hiện tại.
6. System tính next occurrence.
7. System tạo task tiếp theo.

---

# UF-013 — TODAY VIEW

1. User login.
2. Dashboard tải.
3. System lấy:

    * Task có deadline hôm nay.
    * Task overdue chưa completed.
4. System sắp xếp.
5. UI hiển thị Today.

Thứ tự ưu tiên đề xuất:

1. Overdue.
2. Urgent.
3. High.
4. Deadline gần nhất.

---

# UF-014 — CALENDAR VIEW

1. User mở Calendar.
2. System lấy các task có deadline.
3. Task được group theo ngày.
4. UI hiển thị Calendar.
5. User chọn một task.
6. Task Detail mở ra.

---

# 04_ROLE_PERMISSION.md

# ROLE & PERMISSION — TASKFLOW

---

# 1. ROLES

MVP có ba trạng thái/role:

## GUEST

Người chưa đăng nhập.

## USER

Người dùng thông thường.

## ADMIN

Quản trị hệ thống.

---

# 2. PERMISSION MATRIX

| Permission                       | Guest | User | Admin |
| -------------------------------- | ----: | ---: | ----: |
| Register                         |     ✅ |    ❌ |     ✅ |
| Login                            |     ✅ |    ❌ |     ✅ |
| Logout                           |     ❌ |    ✅ |     ✅ |
| Create own task                  |     ❌ |    ✅ |     ✅ |
| View own task                    |     ❌ |    ✅ |     ✅ |
| Update own task                  |     ❌ |    ✅ |     ✅ |
| Delete own task                  |     ❌ |    ✅ |     ✅ |
| View another user's private task |     ❌ |    ❌ |     ❌ |
| Update another user's task       |     ❌ |    ❌ |     ❌ |
| Create own project               |     ❌ |    ✅ |     ✅ |
| Manage own project               |     ❌ |    ✅ |     ✅ |
| Manage own tags                  |     ❌ |    ✅ |     ✅ |
| View own statistics              |     ❌ |    ✅ |     ✅ |
| Manage user account status       |     ❌ |    ❌ |     ✅ |
| View system-level metrics        |     ❌ |    ❌ |     ✅ |

---

# 3. OWNERSHIP RULE

Role `USER` không đồng nghĩa với việc được truy cập mọi Task.

Mọi request liên quan tới resource cá nhân phải kiểm tra:

`resource.userId === authenticatedUser.id`

Áp dụng cho:

* Task.
* Project.
* Tag.
* Reminder.
* Subtask.

---

# 4. TASK PERMISSIONS

## Create

USER được tạo Task.

Task tự động nhận:

`userId = authenticatedUser.id`

Client không được tự truyền arbitrary `userId`.

---

## Read

USER chỉ được đọc Task có:

`task.userId === currentUser.id`

---

## Update

USER chỉ được sửa Task mình sở hữu.

---

## Delete

USER chỉ được xóa Task mình sở hữu.

---

# 5. PROJECT PERMISSIONS

User có thể:

* Create own project.
* Read own project.
* Update own project.
* Delete own project.

User không được gán Task vào Project thuộc người khác.

---

# 6. TAG PERMISSIONS

Tag thuộc phạm vi User.

User A không được dùng Tag của User B.

---

# 7. SUBTASK PERMISSIONS

Subtask kế thừa ownership từ Task cha.

Không cần client gửi `userId` riêng cho Subtask.

---

# 8. ADMIN PRIVACY RULE

Admin quản lý hệ thống nhưng mặc định **không được đọc nội dung task cá nhân của người dùng**.

Admin chỉ nên truy cập dữ liệu vận hành như:

* User ID.
* Account status.
* Created date.
* Last login.
* Aggregate metrics.

Điều này giúp bảo vệ privacy của người dùng.

---

# 9. AUTHORIZATION RESULT

## Không đăng nhập

Return:

`401 Unauthorized`

## Đã login nhưng không có quyền

Return:

`403 Forbidden`

## Resource không tồn tại

Return:

`404 Not Found`

Có thể áp dụng chính sách trả `404` thay cho `403` với private resource để tránh tiết lộ resource có tồn tại hay không.

---

# 10. SECURITY PRINCIPLE

TaskFlow áp dụng:

**Deny by default.**

Nếu hệ thống không xác định chắc chắn user có quyền:

→ Từ chối request.

Không dựa vào việc frontend ẩn nút để bảo vệ dữ liệu.

Permission bắt buộc phải kiểm tra tại backend.
