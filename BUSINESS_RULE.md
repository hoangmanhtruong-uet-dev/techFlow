# 05_BUSINESS_RULE.md

# BUSINESS RULES — TASKFLOW

---

# TASK RULES

## BR-001 — Task Title Required

Mọi Task phải có title.

Title sau khi trim phải:

`length >= 1`

---

## BR-002 — Task Title Maximum Length

Task title tối đa:

`150 characters`

---

## BR-003 — Task Ownership

Mỗi Task thuộc duy nhất một User.

`Task.userId` bắt buộc tồn tại.

---

## BR-004 — Task Status

Status chỉ được nhận:

`TODO`

`IN_PROGRESS`

`COMPLETED`

`CANCELLED`

---

## BR-005 — Default Status

Task mới mặc định:

`status = TODO`

---

## BR-006 — Completion Timestamp

Khi Task chuyển sang:

`COMPLETED`

System phải set:

`completedAt = now()`

---

## BR-007 — Reopen Task

Khi Task chuyển từ COMPLETED về TODO hoặc IN_PROGRESS:

`completedAt = null`

---

# PRIORITY RULES

## BR-008 — Allowed Priority

Priority gồm:

`LOW`

`MEDIUM`

`HIGH`

`URGENT`

---

## BR-009 — Default Priority

Nếu user không chọn:

`priority = MEDIUM`

---

# DEADLINE RULES

## BR-010 — Deadline Optional

Task có thể không có deadline.

---

## BR-011 — Deadline Format

Nếu deadline tồn tại, nó phải là datetime hợp lệ.

---

## BR-012 — Overdue

Không lưu `OVERDUE` như một Task status.

Một task được xem là overdue khi:

`deadline < currentTime`

AND

`status != COMPLETED`

AND

`status != CANCELLED`

---

# PROJECT RULES

## BR-013 — Project Ownership

Task chỉ được gán vào Project thuộc cùng User.

---

## BR-014 — Project Name

Project name:

* Required.
* Trim trước khi lưu.
* Maximum 100 characters.

---

## BR-015 — Delete Project

Khi Project bị xóa:

Task không bị xóa.

Thay vào đó:

`task.projectId = null`

---

# TAG RULES

## BR-016 — Tag Ownership

Tag phải thuộc cùng User với Task.

---

## BR-017 — Multiple Tags

Một Task có thể có nhiều Tag.

Một Tag có thể thuộc nhiều Task.

Relationship:

Many-to-Many.

---

## BR-018 — Duplicate Tag

Một User không được có hai Tag có cùng tên sau khi normalize.

Ví dụ:

`Study`

và

`study`

được xem là trùng.

---

# SUBTASK RULES

## BR-019 — Subtask Parent

Subtask bắt buộc có Task cha.

---

## BR-020 — Subtask Ownership

Subtask kế thừa quyền sở hữu từ Task cha.

---

## BR-021 — Parent Completion

Hoàn thành tất cả Subtask **không tự động** complete Task cha trong MVP.

User phải tự complete Task cha.

---

## BR-022 — Delete Parent Task

Khi Task cha bị xóa:

Toàn bộ Subtask thuộc Task đó cũng bị xóa.

---

# REMINDER RULES

## BR-023 — Reminder Requires Time

Reminder phải có datetime hợp lệ.

---

## BR-024 — Reminder Ownership

Reminder chỉ thuộc một Task và kế thừa ownership từ Task.

---

## BR-025 — Completed Task Reminder

Khi Task đã COMPLETED:

Reminder chưa gửi của Task phải bị vô hiệu hóa.

---

## BR-026 — Cancelled Task Reminder

Task CANCELLED không tiếp tục gửi reminder.

---

# RECURRING TASK RULES

## BR-027 — Supported Recurrence

MVP hỗ trợ:

`DAILY`

`WEEKLY`

`MONTHLY`

---

## BR-028 — Generate Next Occurrence

Trong MVP, occurrence tiếp theo được tạo khi occurrence hiện tại được hoàn thành.

---

## BR-029 — Recurrence Copy

Task mới kế thừa:

* Title.
* Description.
* Priority.
* Project.
* Tags.
* Recurrence rule.

Deadline được tính lại dựa theo recurrence.

---

## BR-030 — Cancel Recurrence

User có thể tắt recurrence.

Việc tắt recurrence không xóa các occurrence đã tồn tại.

---

# SEARCH RULES

## BR-031 — Search Scope

Search chỉ tìm trong Task thuộc authenticated User.

---

## BR-032 — Search Field

MVP tìm kiếm ít nhất theo:

`title`

Có thể mở rộng description ở V2.

---

# FILTER RULES

## BR-033 — Multiple Filters

Các filter khác nhóm được kết hợp bằng AND.

Ví dụ:

`status = TODO`

AND

`priority = HIGH`

---

## BR-034 — Multiple Values Same Filter

Nhiều giá trị trong cùng một nhóm có thể dùng OR.

Ví dụ:

`priority = HIGH OR URGENT`

---

# CALENDAR RULES

## BR-035 — Calendar Eligibility

Chỉ Task có deadline mới xuất hiện trên Calendar.

---

# STATISTICS RULES

## BR-036 — Completion Rate

Completion Rate:

`completedTasks / totalNonCancelledTasks * 100`

Nếu không có Task:

`completionRate = 0`

---

# AUTHENTICATION RULES

## BR-037 — Email Uniqueness

Email phải unique.

---

## BR-038 — Email Normalize

Email được normalize trước khi so sánh.

Ví dụ:

* Trim.
* Lowercase phần phù hợp với policy hệ thống.

---

## BR-039 — Password Storage

Password không được lưu plain text.

Chỉ lưu password hash.

---

# AUTHORIZATION RULES

## BR-040 — Ownership Validation

Mọi thao tác trên private resource phải kiểm tra ownership ở server.

---

## BR-041 — Client User ID

Backend không được tin `userId` gửi từ client để xác định ownership.

User ID phải lấy từ authenticated session/token.

---

# DELETE RULES

## BR-042 — Task Delete Confirmation

UI phải yêu cầu xác nhận trước khi xóa Task.

---

## BR-043 — Delete Task

Trong MVP, Delete Task là permanent delete.

Trash/Restore nằm ngoài MVP.

---

# 06_MVP_SCORE.md

# MVP FEATURE PRIORITIZATION — TASKFLOW

---

# 1. PURPOSE

MVP Score được dùng để tránh việc đưa quá nhiều tính năng vào phiên bản đầu tiên.

Mỗi tính năng được đánh giá theo:

* User Value.
* Usage Frequency.
* Product Importance.
* Implementation Cost.
* Technical Risk.

---

# 2. SCORING

Mỗi yếu tố chấm từ:

`1 → 5`

Trong đó:

## User Value

1 = ít giá trị
5 = cực kỳ quan trọng với người dùng

## Usage Frequency

1 = hiếm sử dụng
5 = sử dụng gần như hàng ngày

## Product Importance

1 = không ảnh hưởng core product
5 = không có thì sản phẩm mất ý nghĩa

## Implementation Cost

1 = rất dễ
5 = rất tốn công

## Technical Risk

1 = rủi ro thấp
5 = rủi ro cao

---

# 3. FORMULA

MVP Score:

`(User Value + Usage Frequency + Product Importance) / (Implementation Cost + Technical Risk)`

Score càng cao → càng nên ưu tiên.

Score chỉ là công cụ hỗ trợ quyết định, không thay thế hoàn toàn Product Judgment.

---

# 4. FEATURE SCORE

| Feature              | Value | Frequency | Importance | Cost | Risk | Score |
| -------------------- | ----: | --------: | ---------: | ---: | ---: | ----: |
| Register/Login       |     5 |         5 |          5 |    2 |    2 |  3.75 |
| Create Task          |     5 |         5 |          5 |    1 |    1 |  7.50 |
| View Task            |     5 |         5 |          5 |    1 |    1 |  7.50 |
| Edit Task            |     5 |         4 |          5 |    1 |    1 |  7.00 |
| Delete Task          |     4 |         3 |          4 |    1 |    1 |  5.50 |
| Complete Task        |     5 |         5 |          5 |    1 |    1 |  7.50 |
| Deadline             |     5 |         5 |          5 |    2 |    1 |  5.00 |
| Priority             |     4 |         4 |          4 |    1 |    1 |  6.00 |
| Project              |     4 |         4 |          4 |    2 |    1 |  4.00 |
| Tag                  |     3 |         3 |          3 |    2 |    1 |  3.00 |
| Subtask              |     4 |         3 |          4 |    2 |    2 |  2.75 |
| Search               |     4 |         4 |          4 |    2 |    1 |  4.00 |
| Filter               |     5 |         4 |          4 |    2 |    1 |  4.33 |
| Sort                 |     3 |         3 |          3 |    1 |    1 |  4.50 |
| Today View           |     5 |         5 |          5 |    2 |    1 |  5.00 |
| Upcoming View        |     5 |         4 |          4 |    2 |    1 |  4.33 |
| Overdue View         |     5 |         4 |          5 |    2 |    1 |  4.67 |
| Reminder             |     5 |         4 |          4 |    3 |    3 |  2.17 |
| Recurring Task       |     4 |         3 |          3 |    3 |    3 |  1.67 |
| Calendar View        |     4 |         3 |          3 |    3 |    2 |  2.00 |
| Statistics           |     3 |         2 |          2 |    2 |    1 |  2.33 |
| Dark Mode            |     2 |         3 |          1 |    1 |    1 |  3.00 |
| Google Calendar Sync |     4 |         2 |          2 |    5 |    4 |  0.89 |
| Team Collaboration   |     4 |         2 |          2 |    5 |    5 |  0.80 |
| AI Planner           |     4 |         2 |          2 |    5 |    5 |  0.80 |
| Pomodoro             |     3 |         2 |          1 |    3 |    2 |  1.20 |

---

# 5. MOSCOW PRIORITIZATION

## MUST HAVE — P0

Bắt buộc cho MVP:

* Register.
* Login.
* Logout.
* Create Task.
* View Task.
* Edit Task.
* Delete Task.
* Complete Task.
* Task Status.
* Deadline.
* Priority.
* Today View.
* Overdue View.
* Search.
* Filter.
* Basic Project.
* Ownership & Permission.
* Server-side Validation.

Không có nhóm này thì sản phẩm chưa đủ chức năng cốt lõi.

---

# SHOULD HAVE — P1

Nên có để MVP vượt khỏi To-do List CRUD cơ bản:

* Tags.
* Subtasks.
* Upcoming View.
* Sort.
* Reminder.
* Recurring Tasks.
* Calendar View.

Đây là nhóm giúp TaskFlow trở thành productivity application thực sự.

---

# COULD HAVE — P2

Có thì tốt nhưng không chặn release:

* Basic Statistics.
* Dark Mode.
* Advanced Filters.
* Drag & Drop.
* Keyboard Shortcuts.

---

# WON'T HAVE — MVP

Không đưa vào phiên bản đầu:

* AI Planner.
* AI chatbot.
* Google Calendar Sync.
* Outlook Sync.
* Team Workspace.
* Task Assignment.
* Realtime Collaboration.
* Chat.
* File Storage nâng cao.
* Gamification.
* Pomodoro.
* Billing.
* Subscription.

---

# 6. FINAL MVP SCOPE

Phiên bản MVP chính thức gồm:

## Authentication

* Register.
* Login.
* Logout.

## Task

* CRUD.
* Status.
* Complete.
* Deadline.
* Priority.

## Organization

* Project.
* Tag.
* Subtask.

## Productivity

* Reminder.
* Recurring Task.

## Discovery

* Search.
* Filter.
* Sort.

## Views

* Today.
* Upcoming.
* Overdue.
* Calendar.

## Analytics

* Basic Statistics.

## Security

* Authentication.
* Authorization.
* Ownership.
* Input Validation.

---

# 7. MVP RELEASE GATE

MVP chỉ được xem là hoàn thành khi:

### RG-01

User có thể:

Create → Edit → Complete → Delete Task.

### RG-02

Deadline và Overdue hoạt động chính xác.

### RG-03

User A không thể truy cập Task của User B.

### RG-04

Project, Tag và Subtask hoạt động.

### RG-05

Search và Filter hoạt động.

### RG-06

Recurring Task tạo đúng occurrence tiếp theo.

### RG-07

Reminder không gửi cho Task đã completed hoặc cancelled.

### RG-08

Calendar hiển thị đúng Task có deadline.

### RG-09

Không còn lỗi Critical/High Severity chưa xử lý.

### RG-10

Core User Flow có automated hoặc manual test đầy đủ.

---

# 8. DEVELOPMENT PRIORITY

Thứ tự nên code:

### Phase 1

Authentication
→ User Model
→ Session

### Phase 2

Task Model
→ Task CRUD
→ Ownership

### Phase 3

Status
→ Deadline
→ Priority
→ Today/Overdue

### Phase 4

Project
→ Tag
→ Subtask

### Phase 5

Search
→ Filter
→ Sort

### Phase 6

Reminder
→ Recurring Task

### Phase 7

Calendar
→ Statistics

### Phase 8

Testing
→ Security Review
→ Performance Review
→ MVP Release
