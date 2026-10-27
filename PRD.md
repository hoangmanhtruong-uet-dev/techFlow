PRODUCT REQUIREMENTS DOCUMENT — TASKFLOW

Version: 1.0
Product: TaskFlow
Product Type: Personal Productivity / Task Management Application
Target Platform: Web responsive, có thể mở rộng Mobile/PWA sau này.

1. PRODUCT OVERVIEW

TaskFlow là ứng dụng quản lý công việc cá nhân giúp người dùng tổ chức, ưu tiên, theo dõi và hoàn thành các nhiệm vụ hằng ngày.

Sản phẩm không chỉ dừng lại ở một To-do List CRUD cơ bản mà hướng tới một hệ thống quản lý công việc cá nhân có:

Task.
Deadline.
Priority.
Project.
Tag.
Subtask.
Reminder.
Recurring Task.
Search & Filter.
Calendar View.
Basic Productivity Statistics.

Mục tiêu là tạo ra một ứng dụng đủ đơn giản để người dùng có thể tạo task nhanh, nhưng đủ mạnh để quản lý nhiều công việc và deadline trong thời gian dài.

2. PROBLEM STATEMENT

+ Sinh viên, freelancer và người đi làm thường phải quản lý nhiều nhiệm vụ cùng lúc.

+ Các công việc có thể bị phân tán ở:

    + Ghi chú điện thoại.
    + Tin nhắn.
    + Lịch.
    + Giấy ghi chú.
    + Trí nhớ cá nhân.
  
Điều này dẫn đến:

+ Quên công việc.
+ Bỏ lỡ deadline.
+ Không biết công việc nào cần ưu tiên.
+ Khó theo dõi tiến độ.
+ Phải tạo thủ công những công việc lặp lại.
+ Khó nhìn tổng quan công việc theo ngày/tuần/tháng.

-> TaskFlow giải quyết vấn đề bằng cách cung cấp một hệ thống quản lý công việc tập trung.

3. PRODUCT GOALS

G-01:
Cho phép người dùng ghi lại công việc nhanh chóng.

G-02:
Giúp người dùng biết: 
Hôm nay cần làm gì?

G-03
:
Giúp người dùng ưu tiên công việc dựa trên:

Deadline.
Priority.
Project.
G-04

Giảm khả năng bỏ lỡ deadline bằng reminder.

G-05

Giúp người dùng quản lý cả công việc đơn lẻ và công việc thuộc project.

G-06

Cho phép theo dõi mức độ hoàn thành công việc.

4. NON-GOALS — MVP

Phiên bản MVP chưa tập trung vào:

Chat.
Realtime collaboration.
Quản lý team.
Phân công task cho nhiều thành viên.
AI tự động lập kế hoạch.
Google Calendar Sync.
Outlook Calendar Sync.
Gamification.
Pomodoro.
File storage nâng cao.
Enterprise permission.
Billing / Subscription.

Những tính năng này có thể được phát triển trong V2 hoặc V3.

5. TARGET USERS
   5.1 Primary User
   Sinh viên

Độ tuổi:

18–25.

Nhu cầu:

Quản lý bài tập.
Deadline.
Thi cử.
Project nhóm.
Công việc cá nhân.
Lịch học.
Các nhiệm vụ lặp lại.
5.2 Secondary User
Freelancer

Nhu cầu:

Theo dõi deadline khách hàng.
Chia công việc theo project.
Theo dõi tiến độ.
Nhân viên văn phòng

Nhu cầu:

Quản lý công việc cá nhân.
Theo dõi task theo ngày.
Ưu tiên các việc quan trọng.
6. USER PAIN POINTS
   PP-01 — Quên công việc

Người dùng có nhiều công việc nhưng không có nơi quản lý tập trung.

PP-02 — Quên deadline

Người dùng nhớ công việc nhưng không nhớ chính xác thời hạn.

PP-03 — Không biết việc nào quan trọng

Danh sách task dài nhưng không có priority rõ ràng.

PP-04 — Khó quản lý nhiều nhóm công việc

Task học tập, cá nhân và công việc bị trộn lẫn.

PP-05 — Task lặp lại gây thao tác thủ công

Ví dụ:

Học tiếng Anh mỗi ngày.
Báo cáo mỗi thứ Hai.
Đóng tiền đầu tháng.
PP-06 — Khó nhìn tổng quan

Người dùng khó biết:

Hôm nay có bao nhiêu việc?
Việc nào quá hạn?
Tuần này có deadline nào?
Đã hoàn thành bao nhiêu task?
7. CORE PRODUCT CONCEPT

TaskFlow tổ chức dữ liệu theo mô hình:

User
→ Projects
→ Tasks
→ Subtasks

Task có thể gắn:

Deadline.
Priority.
Tags.
Reminder.
Recurrence.
8. FUNCTIONAL REQUIREMENTS
   Authentication
   FR-001 — Register

Người dùng có thể tạo tài khoản bằng email và password.

FR-002 — Login

Người dùng có thể đăng nhập.

FR-003 — Logout

Người dùng có thể đăng xuất.

Task Management
FR-004 — Create Task

Người dùng có thể tạo task.

Task hỗ trợ:

Title.
Description.
Deadline.
Priority.
Project.
Tag.
Reminder.
Recurrence.
FR-005 — View Task

Người dùng có thể xem chi tiết task.

FR-006 — Update Task

Người dùng có thể chỉnh sửa task thuộc sở hữu của mình.

FR-007 — Delete Task

Người dùng có thể xóa task thuộc sở hữu của mình.

FR-008 — Task Status

Task hỗ trợ trạng thái:

TODO.
IN_PROGRESS.
COMPLETED.
CANCELLED.
FR-009 — Complete Task

Người dùng có thể đánh dấu một task hoàn thành.

Organization
FR-010 — Project

Người dùng có thể:

Tạo project.
Sửa project.
Xóa project.
Gán task vào project.
FR-011 — Tags

Người dùng có thể tạo và gắn tag vào task.

Một task có thể có nhiều tag.

FR-012 — Subtask

Người dùng có thể tạo các subtask bên trong một task.

Productivity
FR-013 — Priority

Task hỗ trợ:

LOW.
MEDIUM.
HIGH.
URGENT.
FR-014 — Deadline

Người dùng có thể đặt deadline cho task.

FR-015 — Reminder

Người dùng có thể đặt thời điểm nhắc việc trước deadline.

FR-016 — Recurring Task

Task có thể lặp theo:

DAILY.
WEEKLY.
MONTHLY.

Custom recurrence chưa bắt buộc trong MVP.

Search & Navigation
FR-017 — Search

Người dùng có thể tìm task theo title.

FR-018 — Filter

Người dùng có thể lọc task theo:

Status.
Priority.
Project.
Tag.
Deadline.
FR-019 — Sort

Người dùng có thể sắp xếp task theo:

Deadline.
Priority.
Created Date.
Views
FR-020 — Today View

Hiển thị task cần xử lý trong ngày.

FR-021 — Upcoming View

Hiển thị các task sắp tới.

FR-022 — Calendar View

Hiển thị task có deadline trên lịch.

FR-023 — Overdue View

Hiển thị task đã quá deadline nhưng chưa hoàn thành.

Statistics
FR-024 — Basic Statistics

Người dùng có thể xem:

Tổng số task.
Task đã hoàn thành.
Task đang chờ.
Task quá hạn.
Completion rate.
9. NON-FUNCTIONAL REQUIREMENTS
   NFR-001 — Security

Password không được lưu dưới dạng plain text.

NFR-002 — Authorization

Người dùng không được truy cập dữ liệu thuộc người dùng khác.

NFR-003 — Performance

Các API CRUD thông thường nên phản hồi trong khoảng:

P95 < 500 ms

trong điều kiện tải MVP thông thường.

NFR-004 — Responsive

Ứng dụng phải sử dụng được trên:

Desktop.
Tablet.
Mobile browser.
NFR-005 — Validation

Dữ liệu đầu vào phải được kiểm tra ở server.

Không chỉ validate phía client.

NFR-006 — Error Handling

API phải trả về error code và message rõ ràng.

NFR-007 — Data Integrity

Không được tồn tại Task tham chiếu tới Project của user khác.

NFR-008 — Accessibility

Các thao tác chính phải có label và trạng thái focus cơ bản.

10. CORE SCREENS

MVP gồm:

Register.
Login.
Dashboard.
Today.
Upcoming.
All Tasks.
Create Task.
Task Detail.
Edit Task.
Projects.
Project Detail.
Calendar.
Statistics.
Settings.
11. MVP SUCCESS METRICS
    SM-01

Người dùng có thể tạo task trong tối đa 3 thao tác chính từ Dashboard.

SM-02

100% Task API phải kiểm tra ownership.

SM-03

Không có trường hợp User A đọc/sửa/xóa Task của User B.

SM-04

≥ 95% request CRUD hợp lệ hoạt động thành công trong test suite.

SM-05

Các luồng chính phải hoạt động:

Create → Update → Complete → Delete.

SM-06

Task quá hạn phải được xác định chính xác.

12. FUTURE ROADMAP
    V2
    Custom recurrence.
    Pomodoro.
    Drag & Drop.
    File attachment.
    Google Calendar Sync.
    Notification push nâng cao.
    V3
    AI Planner.
    AI Priority Recommendation.
    Natural Language Task Creation.
    Team Workspace.
    Shared Project.
    Task Assignment.
    Productivity Insights.