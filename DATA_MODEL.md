# 07_DATA_MODEL.md

# DATA MODEL — TASKFLOW

**Version:** 1.0
**Product:** TaskFlow
**Database Type:** Relational Database
**Recommended:** PostgreSQL

---

# 1. PURPOSE

Tài liệu này mô tả cấu trúc dữ liệu của TaskFlow.

Data Model phải đáp ứng các requirement chính:

* User Authentication.
* Task Management.
* Project.
* Tags.
* Subtasks.
* Reminder.
* Recurring Task.
* Statistics.
* Ownership & Permission.

---

# 2. CORE ENTITIES

TaskFlow MVP gồm các entity chính:

```text
User
Project
Task
Tag
TaskTag
Subtask
Reminder
```

Quan hệ tổng quát:

```text
User
 ├── Projects
 ├── Tasks
 └── Tags

Project
 └── Tasks

Task
 ├── Subtasks
 ├── Tags
 └── Reminder
```

---

# 3. ENTITY RELATIONSHIP OVERVIEW

```text
┌────────────┐
│    User    │
└─────┬──────┘
      │
      │ 1:N
      ▼
┌────────────┐
│  Project   │
└─────┬──────┘
      │
      │ 1:N
      ▼
┌────────────┐
│    Task    │
└─────┬──────┘
      │
      ├───────────────┐
      │               │
      │ 1:N           │ 1:N
      ▼               ▼
┌────────────┐   ┌────────────┐
│  Subtask   │   │  Reminder  │
└────────────┘   └────────────┘

Task
  │
  │ N:N
  ▼
Tag
```

---

# 4. USER

## Table

`users`

## Purpose

Lưu tài khoản người dùng.

## Fields

| Field         | Type         | Required | Description        |
| ------------- | ------------ | -------: | ------------------ |
| id            | UUID         |        ✅ | Primary key        |
| email         | VARCHAR(255) |        ✅ | Email đăng nhập    |
| password_hash | TEXT         |        ✅ | Password đã hash   |
| name          | VARCHAR(100) |        ❌ | Tên người dùng     |
| role          | ENUM         |        ✅ | USER / ADMIN       |
| status        | ENUM         |        ✅ | ACTIVE / DISABLED  |
| created_at    | TIMESTAMP    |        ✅ | Ngày tạo           |
| updated_at    | TIMESTAMP    |        ✅ | Ngày cập nhật      |
| last_login_at | TIMESTAMP    |        ❌ | Lần login gần nhất |

---

## Constraints

```text
email UNIQUE
```

Email phải được normalize trước khi lưu.

Ví dụ:

```text
User@Example.com
```

normalize thành:

```text
user@example.com
```

---

## Role Enum

```text
USER
ADMIN
```

---

## Status Enum

```text
ACTIVE
DISABLED
```

---

# 5. PROJECT

## Table

`projects`

## Purpose

Nhóm các Task theo mục đích.

Ví dụ:

```text
University
Personal
Work
IELTS
```

---

## Fields

| Field       | Type         | Required | Description   |
| ----------- | ------------ | -------: | ------------- |
| id          | UUID         |        ✅ | Primary key   |
| user_id     | UUID         |        ✅ | Owner         |
| name        | VARCHAR(100) |        ✅ | Tên project   |
| description | TEXT         |        ❌ | Mô tả         |
| created_at  | TIMESTAMP    |        ✅ | Ngày tạo      |
| updated_at  | TIMESTAMP    |        ✅ | Ngày cập nhật |

---

## Relationships

```text
User 1 ─── N Project
```

Foreign key:

```text
projects.user_id → users.id
```

---

## Constraints

Project phải thuộc một User.

Project name:

```text
1–100 characters
```

Có thể cho phép user có nhiều project trùng tên trong MVP.

---

# 6. TASK

## Table

`tasks`

## Purpose

Entity trung tâm của TaskFlow.

---

## Fields

| Field                | Type         | Required | Description          |
| -------------------- | ------------ | -------: | -------------------- |
| id                   | UUID         |        ✅ | Primary key          |
| user_id              | UUID         |        ✅ | Owner                |
| project_id           | UUID         |        ❌ | Project              |
| title                | VARCHAR(150) |        ✅ | Task title           |
| description          | TEXT         |        ❌ | Nội dung             |
| status               | ENUM         |        ✅ | Trạng thái           |
| priority             | ENUM         |        ✅ | Mức ưu tiên          |
| deadline             | TIMESTAMP    |        ❌ | Deadline             |
| completed_at         | TIMESTAMP    |        ❌ | Thời gian hoàn thành |
| recurrence_type      | ENUM         |        ❌ | Chu kỳ               |
| recurrence_parent_id | UUID         |        ❌ | Task recurrence gốc  |
| created_at           | TIMESTAMP    |        ✅ | Ngày tạo             |
| updated_at           | TIMESTAMP    |        ✅ | Ngày sửa             |

---

# 7. TASK STATUS

Enum:

```text
TODO
IN_PROGRESS
COMPLETED
CANCELLED
```

Default:

```text
TODO
```

---

# 8. TASK PRIORITY

Enum:

```text
LOW
MEDIUM
HIGH
URGENT
```

Default:

```text
MEDIUM
```

---

# 9. TASK DEADLINE

Deadline:

```text
nullable
```

Task không bắt buộc có deadline.

---

# 10. OVERDUE

Không lưu:

```text
status = OVERDUE
```

Overdue là trạng thái tính toán.

Điều kiện:

```text
deadline < now
AND
status NOT IN (COMPLETED, CANCELLED)
```

Ví dụ SQL logic:

```sql
deadline < CURRENT_TIMESTAMP
AND status NOT IN ('COMPLETED', 'CANCELLED')
```

---

# 11. COMPLETED_AT

Khi:

```text
status = COMPLETED
```

thì:

```text
completed_at = now()
```

Nếu user reopen:

```text
status = TODO
```

hoặc:

```text
IN_PROGRESS
```

thì:

```text
completed_at = null
```

---

# 12. PROJECT RELATIONSHIP

```text
Project 1 ─── N Task
```

`project_id` có thể null.

Task không thuộc project được coi là:

```text
No Project
```

---

# 13. DELETE PROJECT BEHAVIOR

Khi Project bị xóa:

Không xóa Task.

Thay vào đó:

```text
tasks.project_id = null
```

Foreign Key recommendation:

```text
ON DELETE SET NULL
```

---

# 14. RECURRENCE

MVP hỗ trợ:

```text
DAILY
WEEKLY
MONTHLY
```

Enum:

```text
NONE
DAILY
WEEKLY
MONTHLY
```

---

## Example

Task:

```text
Đọc sách
DAILY
```

Khi task hôm nay hoàn thành:

```text
Task #101
status = COMPLETED
```

System tạo:

```text
Task #102
deadline = next day
status = TODO
```

---

# 15. RECURRENCE_PARENT_ID

Cho phép liên kết các occurrence.

Ví dụ:

```text
Task 100
  ↓
Task 101
  ↓
Task 102
```

Có thể lưu:

```text
recurrence_parent_id = Task 100
```

cho toàn bộ occurrence.

Mục đích:

* Tracking.
* Statistics.
* Cancel recurrence.
* Debug.

---

# 16. SUBTASK

## Table

`subtasks`

## Fields

| Field        | Type         | Required |
| ------------ | ------------ | -------: |
| id           | UUID         |        ✅ |
| task_id      | UUID         |        ✅ |
| title        | VARCHAR(150) |        ✅ |
| is_completed | BOOLEAN      |        ✅ |
| completed_at | TIMESTAMP    |        ❌ |
| position     | INTEGER      |        ✅ |
| created_at   | TIMESTAMP    |        ✅ |
| updated_at   | TIMESTAMP    |        ✅ |

---

## Relationship

```text
Task 1 ─── N Subtask
```

---

## Default

```text
is_completed = false
```

---

## Position

Dùng để sắp xếp subtask.

Ví dụ:

```text
1. Tìm tài liệu
2. Lập dàn ý
3. Viết bài
4. Kiểm tra
```

---

# 17. SUBTASK DELETE

Task bị xóa:

Subtask phải bị xóa theo.

Recommendation:

```text
ON DELETE CASCADE
```

---

# 18. TAG

## Table

`tags`

## Fields

| Field           | Type        | Required |
| --------------- | ----------- | -------: |
| id              | UUID        |        ✅ |
| user_id         | UUID        |        ✅ |
| name            | VARCHAR(50) |        ✅ |
| normalized_name | VARCHAR(50) |        ✅ |
| created_at      | TIMESTAMP   |        ✅ |

---

## Examples

```text
Important
Study
Exam
Personal
Deep Work
```

---

# 19. TAG OWNERSHIP

```text
User 1 ─── N Tag
```

User không được sử dụng Tag của người khác.

---

# 20. NORMALIZED TAG NAME

Ví dụ:

```text
" Study "
```

normalize:

```text
"study"
```

Unique constraint:

```text
UNIQUE(user_id, normalized_name)
```

---

# 21. TASK_TAG

Task và Tag có quan hệ:

```text
N:N
```

Do đó cần bảng trung gian.

## Table

`task_tags`

## Fields

| Field   | Type |
| ------- | ---- |
| task_id | UUID |
| tag_id  | UUID |

Primary Key:

```text
(task_id, tag_id)
```

---

# 22. TASK_TAG FOREIGN KEYS

```text
task_id → tasks.id
tag_id → tags.id
```

Recommended:

```text
ON DELETE CASCADE
```

---

# 23. REMINDER

## Table

`reminders`

## Fields

| Field      | Type      | Required |
| ---------- | --------- | -------: |
| id         | UUID      |        ✅ |
| task_id    | UUID      |        ✅ |
| remind_at  | TIMESTAMP |        ✅ |
| status     | ENUM      |        ✅ |
| sent_at    | TIMESTAMP |        ❌ |
| created_at | TIMESTAMP |        ✅ |

---

# 24. REMINDER STATUS

```text
PENDING
SENT
CANCELLED
FAILED
```

Default:

```text
PENDING
```

---

# 25. REMINDER RELATIONSHIP

```text
Task 1 ─── N Reminder
```

MVP có thể UI chỉ hỗ trợ một reminder/task.

Database vẫn nên thiết kế:

```text
1:N
```

để dễ mở rộng.

---

# 26. REMINDER RULE

Task:

```text
COMPLETED
```

hoặc:

```text
CANCELLED
```

thì các Reminder PENDING phải chuyển:

```text
CANCELLED
```

---

# 27. PROPOSED SQL-LIKE SCHEMA

```sql
users
-----
id UUID PK
email VARCHAR UNIQUE
password_hash TEXT
name VARCHAR
role ENUM
status ENUM
created_at TIMESTAMP
updated_at TIMESTAMP
last_login_at TIMESTAMP


projects
--------
id UUID PK
user_id UUID FK
name VARCHAR
description TEXT
created_at TIMESTAMP
updated_at TIMESTAMP


tasks
-----
id UUID PK
user_id UUID FK
project_id UUID FK NULL
title VARCHAR
description TEXT NULL
status ENUM
priority ENUM
deadline TIMESTAMP NULL
completed_at TIMESTAMP NULL
recurrence_type ENUM
recurrence_parent_id UUID NULL
created_at TIMESTAMP
updated_at TIMESTAMP


subtasks
--------
id UUID PK
task_id UUID FK
title VARCHAR
is_completed BOOLEAN
completed_at TIMESTAMP NULL
position INTEGER
created_at TIMESTAMP
updated_at TIMESTAMP


tags
----
id UUID PK
user_id UUID FK
name VARCHAR
normalized_name VARCHAR
created_at TIMESTAMP


task_tags
---------
task_id UUID FK
tag_id UUID FK


reminders
---------
id UUID PK
task_id UUID FK
remind_at TIMESTAMP
status ENUM
sent_at TIMESTAMP NULL
created_at TIMESTAMP
```

---

# 28. INDEX STRATEGY

Không chỉ tạo bảng, phải nghĩ tới query.

---

## IDX-001

```text
tasks(user_id)
```

Hầu hết query Task đều theo user.

---

## IDX-002

```text
tasks(user_id, status)
```

Dùng cho:

```text
TODO
COMPLETED
IN_PROGRESS
```

---

## IDX-003

```text
tasks(user_id, deadline)
```

Dùng cho:

* Today.
* Upcoming.
* Overdue.
* Calendar.

---

## IDX-004

```text
tasks(user_id, priority)
```

Dùng cho priority filtering.

---

## IDX-005

```text
tasks(project_id)
```

Dùng Project Detail.

---

## IDX-006

```text
projects(user_id)
```

---

## IDX-007

```text
tags(user_id, normalized_name)
```

Unique index.

---

## IDX-008

```text
reminders(status, remind_at)
```

Quan trọng cho background reminder worker.

Query:

```text
WHERE status = PENDING
AND remind_at <= NOW()
```

---

# 29. TASK QUERY EXAMPLES

## Today

```text
user_id = currentUser

AND deadline nằm trong ngày hiện tại

AND status NOT IN (COMPLETED, CANCELLED)
```

---

## Overdue

```text
user_id = currentUser

AND deadline < now

AND status NOT IN (COMPLETED, CANCELLED)
```

---

## Upcoming

```text
user_id = currentUser

AND deadline > endOfToday

AND status NOT IN (COMPLETED, CANCELLED)
```

---

# 30. OWNERSHIP IN DATABASE

Không bao giờ query:

```text
WHERE task.id = taskId
```

rồi sau đó mới hy vọng kiểm tra permission.

Nên query:

```text
WHERE task.id = taskId
AND task.user_id = authenticatedUserId
```

Ví dụ:

```sql
SELECT *
FROM tasks
WHERE id = :taskId
AND user_id = :currentUserId;
```

---

# 31. DATA INTEGRITY

## DI-001

Task.project_id nếu tồn tại phải tham chiếu Project cùng user.

Việc này cần kiểm tra trong application/service layer.

---

## DI-002

Tag gán cho Task phải thuộc cùng user.

---

## DI-003

Reminder luôn thuộc Task tồn tại.

---

## DI-004

Subtask luôn thuộc Task tồn tại.

---

# 32. TIMESTAMP POLICY

Khuyến nghị:

Database lưu toàn bộ timestamp theo:

```text
UTC
```

Frontend chuyển sang timezone người dùng.

Không lưu local time trực tiếp.

---

# 33. SOFT DELETE

MVP:

Không sử dụng soft delete cho Task.

Delete:

```text
permanent
```

Future version có thể thêm:

```text
deleted_at
```

để hỗ trợ Trash.

---

# 34. DATA MODEL RELEASE CHECKLIST

Trước khi xem database hoàn thành phải test:

* User tạo Task.
* Task có project.
* Task không có project.
* Task có nhiều tags.
* Task có nhiều subtasks.
* Task có reminder.
* Task recurring.
* Delete Task cascade Subtask.
* Delete Task cascade TaskTag.
* Delete Project không xóa Task.
* User A không gán Task vào Project User B.
* User A không gắn Tag User B.
* Overdue query chính xác.
* Today query chính xác.
* Reminder query sử dụng index.
