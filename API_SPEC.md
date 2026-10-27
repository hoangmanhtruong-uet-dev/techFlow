# 08_API_SPEC.md

# API SPECIFICATION — TASKFLOW

**Version:** 1.0
**API Style:** REST
**Base Path:** `/api/v1`

---

# 1. API PRINCIPLES

TaskFlow API tuân theo các nguyên tắc:

1. RESTful resource naming.
2. JSON request/response.
3. Server-side validation.
4. Authentication bắt buộc cho private resource.
5. Ownership validation trên backend.
6. Error response thống nhất.
7. Pagination cho collection lớn.
8. API versioning.

---

# 2. BASE URL

Development:

```text
http://localhost:3000/api/v1
```

Production:

```text
https://your-domain.com/api/v1
```

---

# 3. CONTENT TYPE

Request:

```http
Content-Type: application/json
```

Response:

```http
Content-Type: application/json
```

---

# 4. AUTHENTICATION

Private endpoints yêu cầu authenticated session.

Có thể sử dụng:

* HttpOnly Cookie Session.

hoặc:

* Access Token + Refresh Token.

Khuyến nghị web app:

```text
HttpOnly
Secure
SameSite
```

cookie.

Frontend không tự truyền:

```text
userId
```

để xác định ownership.

Backend lấy User ID từ session.

---

# 5. STANDARD SUCCESS RESPONSE

Có thể dùng format:

```json
{
  "success": true,
  "data": {}
}
```

Collection:

```json
{
  "success": true,
  "data": [],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 50,
    "totalPages": 3
  }
}
```

---

# 6. STANDARD ERROR RESPONSE

```json
{
  "success": false,
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "Task not found."
  }
}
```

Validation:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data.",
    "fields": {
      "title": "Task title is required."
    }
  }
}
```

---

# 7. HTTP STATUS POLICY

| Status | Meaning                   |
| ------ | ------------------------- |
| 200    | Request thành công        |
| 201    | Resource được tạo         |
| 204    | Thành công, không có body |
| 400    | Invalid request           |
| 401    | Not authenticated         |
| 403    | Not authorized            |
| 404    | Resource not found        |
| 409    | Conflict                  |
| 422    | Validation error          |
| 429    | Rate limited              |
| 500    | Internal server error     |

---

# 8. AUTH API

# POST /auth/register

Tạo account.

## Request

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123",
  "name": "Minh"
}
```

## Success

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "name": "Minh",
      "role": "USER"
    }
  }
}
```

---

## Errors

```text
EMAIL_ALREADY_EXISTS
INVALID_EMAIL
WEAK_PASSWORD
VALIDATION_ERROR
```

---

# POST /auth/login

## Request

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

## Success

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "name": "Minh",
      "role": "USER"
    }
  }
}
```

Authenticated session/token được tạo.

---

## Errors

```text
INVALID_CREDENTIALS
ACCOUNT_DISABLED
```

Không nên tiết lộ:

```text
Email đúng nhưng password sai
```

hay:

```text
Email không tồn tại
```

Nên trả chung:

```text
Invalid email or password.
```

---

# POST /auth/logout

Hủy authenticated session.

## Success

```http
204 No Content
```

---

# GET /auth/me

Lấy authenticated user.

## Response

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Minh",
    "role": "USER"
  }
}
```

---

# 9. TASK API

# POST /tasks

Tạo task.

## Auth

Required.

## Request

```json
{
  "title": "Làm bài Văn học",
  "description": "Hoàn thành bài trước tối thứ Sáu",
  "deadline": "2026-07-30T15:00:00Z",
  "priority": "HIGH",
  "projectId": "project-uuid",
  "tagIds": [
    "tag-uuid-1",
    "tag-uuid-2"
  ],
  "recurrenceType": "NONE"
}
```

---

## Required

```text
title
```

---

## Optional

```text
description
deadline
priority
projectId
tagIds
recurrenceType
```

---

## Default Values

```text
status = TODO
priority = MEDIUM
recurrenceType = NONE
```

---

## Success

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "title": "Làm bài Văn học",
    "description": "Hoàn thành bài trước tối thứ Sáu",
    "status": "TODO",
    "priority": "HIGH",
    "deadline": "2026-07-30T15:00:00Z",
    "project": {
      "id": "project-uuid",
      "name": "University"
    },
    "tags": [],
    "completedAt": null,
    "createdAt": "2026-07-24T16:00:00Z",
    "updatedAt": "2026-07-24T16:00:00Z"
  }
}
```

---

## Errors

```text
TASK_TITLE_REQUIRED
INVALID_DEADLINE
INVALID_PRIORITY
PROJECT_NOT_FOUND
TAG_NOT_FOUND
FORBIDDEN
```

---

# GET /tasks

Lấy danh sách Task.

## Example

```http
GET /api/v1/tasks
```

---

# 10. TASK QUERY PARAMETERS

## Pagination

```text
page
limit
```

Example:

```text
?page=1&limit=20
```

---

## Status

```text
?status=TODO
```

Multiple:

```text
?status=TODO,IN_PROGRESS
```

---

## Priority

```text
?priority=HIGH
```

Multiple:

```text
?priority=HIGH,URGENT
```

---

## Project

```text
?projectId=uuid
```

---

## Tag

```text
?tagId=uuid
```

---

## Search

```text
?q=van%20hoc
```

Search ít nhất theo:

```text
title
```

---

## Deadline Range

```text
?deadlineFrom=2026-07-24T00:00:00Z
&deadlineTo=2026-07-31T23:59:59Z
```

---

## Sort

```text
?sortBy=deadline
&sortOrder=asc
```

Supported:

```text
deadline
priority
createdAt
updatedAt
```

Order:

```text
asc
desc
```

---

# 11. GET TASK COLLECTION RESPONSE

```json
{
  "success": true,
  "data": [
    {
      "id": "task-uuid",
      "title": "Làm bài",
      "status": "TODO",
      "priority": "HIGH",
      "deadline": "2026-07-30T15:00:00Z"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

# GET /tasks/:id

Lấy chi tiết task.

Example:

```http
GET /api/v1/tasks/task-uuid
```

---

## Success

```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "title": "Làm bài Văn học",
    "description": "...",
    "status": "TODO",
    "priority": "HIGH",
    "deadline": "2026-07-30T15:00:00Z",
    "completedAt": null,
    "project": {},
    "tags": [],
    "subtasks": [],
    "reminders": []
  }
}
```

---

## Ownership

Backend query phải chứa:

```text
task.id = params.id
AND
task.userId = authenticatedUser.id
```

---

# PATCH /tasks/:id

Update Task.

Chỉ gửi field cần sửa.

## Request

```json
{
  "title": "Làm bài Dẫn luận Văn học",
  "priority": "URGENT"
}
```

---

## Success

```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "title": "Làm bài Dẫn luận Văn học",
    "priority": "URGENT"
  }
}
```

---

# DELETE /tasks/:id

Delete Task.

## Success

```http
204 No Content
```

---

## Side Effects

Delete:

* Subtasks.
* TaskTag relations.
* Reminders.

Không delete:

* Project.
* Tag.

---

# 12. COMPLETE TASK

# POST /tasks/:id/complete

Thay vì frontend tự PATCH nhiều field:

```text
status
completedAt
```

nên có command endpoint.

---

## Success

```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "status": "COMPLETED",
    "completedAt": "2026-07-24T16:30:00Z"
  }
}
```

---

## Side Effects

System:

```text
status = COMPLETED
completedAt = now()
```

Cancel pending reminders.

Nếu recurring:

Tạo occurrence tiếp theo.

---

# POST /tasks/:id/reopen

Reopen Task.

## Result

```text
status = TODO
completedAt = null
```

---

# POST /tasks/:id/cancel

Cancel Task.

## Result

```text
status = CANCELLED
```

Pending Reminder:

```text
CANCELLED
```

---

# 13. TODAY API

# GET /tasks/views/today

Return:

* Task deadline hôm nay.
* Task overdue chưa completed/cancelled.

---

## Example Response

```json
{
  "success": true,
  "data": {
    "overdue": [],
    "today": []
  }
}
```

---

# GET /tasks/views/upcoming

Lấy upcoming Tasks.

Possible query:

```text
?days=7
```

Default:

```text
7 days
```

---

# GET /tasks/views/overdue

Return Tasks:

```text
deadline < now
AND
status NOT IN (COMPLETED, CANCELLED)
```

---

# 14. PROJECT API

# POST /projects

## Request

```json
{
  "name": "University",
  "description": "Các bài tập và deadline đại học"
}
```

## Success

```http
201
```

---

# GET /projects

Lấy Project của current user.

---

# GET /projects/:id

Lấy Project Detail.

Có thể kèm task summary:

```json
{
  "id": "uuid",
  "name": "University",
  "taskCount": 10,
  "completedTaskCount": 4
}
```

---

# PATCH /projects/:id

Update Project.

---

# DELETE /projects/:id

Delete Project.

Side effect:

```text
Task.projectId = null
```

Task không bị delete.

---

# 15. TAG API

# POST /tags

## Request

```json
{
  "name": "Exam"
}
```

---

## Duplicate

Nếu current user đã có:

```text
exam
```

thì:

```http
409 Conflict
```

Error:

```text
TAG_ALREADY_EXISTS
```

---

# GET /tags

Return Tags của user.

---

# PATCH /tags/:id

Rename Tag.

---

# DELETE /tags/:id

Delete Tag.

Task không bị delete.

TaskTag relationship bị remove.

---

# 16. SUBTASK API

# POST /tasks/:taskId/subtasks

## Request

```json
{
  "title": "Tìm tài liệu"
}
```

---

## Response

```json
{
  "success": true,
  "data": {
    "id": "subtask-uuid",
    "title": "Tìm tài liệu",
    "isCompleted": false,
    "position": 1
  }
}
```

---

# PATCH /tasks/:taskId/subtasks/:subtaskId

Update Subtask.

Example:

```json
{
  "title": "Tìm 5 tài liệu"
}
```

---

# POST /tasks/:taskId/subtasks/:subtaskId/complete

Result:

```text
isCompleted = true
completedAt = now()
```

---

# POST /tasks/:taskId/subtasks/:subtaskId/reopen

Result:

```text
isCompleted = false
completedAt = null
```

---

# DELETE /tasks/:taskId/subtasks/:subtaskId

Delete Subtask.

---

# 17. REMINDER API

# POST /tasks/:taskId/reminders

## Request

```json
{
  "remindAt": "2026-07-30T13:00:00Z"
}
```

---

## Success

```json
{
  "success": true,
  "data": {
    "id": "reminder-uuid",
    "remindAt": "2026-07-30T13:00:00Z",
    "status": "PENDING"
  }
}
```

---

# DELETE /tasks/:taskId/reminders/:id

Cancel Reminder.

Recommendation:

Không hard delete ngay.

Có thể:

```text
status = CANCELLED
```

---

# 18. RECURRENCE

Recurrence config có thể được update qua:

```http
PATCH /tasks/:id
```

Example:

```json
{
  "recurrenceType": "WEEKLY"
}
```

---

## Allowed

```text
NONE
DAILY
WEEKLY
MONTHLY
```

---

# 19. CALENDAR API

# GET /calendar

Query:

```text
?from=2026-07-01T00:00:00Z
&to=2026-07-31T23:59:59Z
```

---

## Response

```json
{
  "success": true,
  "data": [
    {
      "id": "task-uuid",
      "title": "Nộp tiểu luận",
      "deadline": "2026-07-26T15:00:00Z",
      "priority": "HIGH",
      "status": "TODO"
    }
  ]
}
```

Chỉ trả task có:

```text
deadline != null
```

---

# 20. STATISTICS API

# GET /statistics/summary

Example:

```text
?from=2026-07-01
&to=2026-07-31
```

---

## Response

```json
{
  "success": true,
  "data": {
    "totalTasks": 35,
    "completedTasks": 21,
    "pendingTasks": 11,
    "overdueTasks": 3,
    "cancelledTasks": 2,
    "completionRate": 63.64
  }
}
```

Formula:

```text
completedTasks
--------------------------- × 100
totalNonCancelledTasks
```

---

# 21. API ENDPOINT SUMMARY

## Auth

```text
POST   /auth/register
POST   /auth/login
POST   /auth/logout
GET    /auth/me
```

## Tasks

```text
POST   /tasks
GET    /tasks
GET    /tasks/:id
PATCH  /tasks/:id
DELETE /tasks/:id
```

## Task Commands

```text
POST /tasks/:id/complete
POST /tasks/:id/reopen
POST /tasks/:id/cancel
```

## Views

```text
GET /tasks/views/today
GET /tasks/views/upcoming
GET /tasks/views/overdue
```

## Projects

```text
POST   /projects
GET    /projects
GET    /projects/:id
PATCH  /projects/:id
DELETE /projects/:id
```

## Tags

```text
POST   /tags
GET    /tags
PATCH  /tags/:id
DELETE /tags/:id
```

## Subtasks

```text
POST   /tasks/:taskId/subtasks
PATCH  /tasks/:taskId/subtasks/:id
DELETE /tasks/:taskId/subtasks/:id

POST /tasks/:taskId/subtasks/:id/complete
POST /tasks/:taskId/subtasks/:id/reopen
```

## Reminders

```text
POST   /tasks/:taskId/reminders
DELETE /tasks/:taskId/reminders/:id
```

## Calendar

```text
GET /calendar
```

## Statistics

```text
GET /statistics/summary
```

---

# 22. VALIDATION RULES

## Task

```text
title:
required
string
trim
max 150
```

```text
priority:
LOW | MEDIUM | HIGH | URGENT
```

```text
status:
TODO | IN_PROGRESS | COMPLETED | CANCELLED
```

```text
recurrenceType:
NONE | DAILY | WEEKLY | MONTHLY
```

---

# 23. SECURITY RULES

Mọi private endpoint phải:

1. Verify authentication.
2. Lấy user ID từ session.
3. Validate request.
4. Validate ownership.
5. Thực thi business rule.
6. Query/update DB.
7. Trả response.

Không tin:

```text
userId
role
ownerId
```

do client tự gửi.

---

# 24. RATE LIMIT RECOMMENDATION

Auth endpoint:

```text
POST /login
POST /register
```

nên rate-limit mạnh hơn.

Ví dụ:

```text
5–10 attempts / minute / IP
```

Mức chính xác tùy infrastructure.

---

# 25. PAGINATION RULES

Default:

```text
page = 1
limit = 20
```

Maximum:

```text
limit = 100
```

Client không được request:

```text
limit = 100000
```

---

# 26. API PROCESSING PIPELINE

Ví dụ Create Task:

```text
HTTP Request
     ↓
Authentication
     ↓
Validation
     ↓
Authorization
     ↓
Business Rules
     ↓
Service Layer
     ↓
Database
     ↓
Response Mapper
     ↓
HTTP Response
```

---

# 27. CONTROLLER RESPONSIBILITY

Controller không nên chứa toàn bộ logic.

Controller chỉ:

```text
Parse Request
↓
Call Service
↓
Return Response
```

---

# 28. SERVICE RESPONSIBILITY

Service xử lý:

* Business Rule.
* Ownership.
* Transaction.
* Recurrence.
* Reminder side effect.

Ví dụ:

```text
TaskService.completeTask()
```

thực hiện:

```text
Find Task
↓
Check Ownership
↓
Set COMPLETED
↓
Set completedAt
↓
Cancel Reminder
↓
Generate Recurring Task
↓
Commit Transaction
```

---

# 29. TRANSACTION REQUIREMENTS

Một số operation nên chạy trong transaction.

Ví dụ Complete Recurring Task:

```text
BEGIN

Update Task → COMPLETED

Cancel Pending Reminders

Create Next Recurring Task

COMMIT
```

Nếu bước cuối lỗi:

```text
ROLLBACK
```

để tránh dữ liệu nửa hoàn thành.

---

# 30. API RELEASE CHECKLIST

Trước khi API MVP release:

* Register hoạt động.
* Login hoạt động.
* Logout hoạt động.
* CRUD Task hoạt động.
* Ownership test pass.
* Project CRUD hoạt động.
* Tag CRUD hoạt động.
* Subtask hoạt động.
* Reminder hoạt động.
* Recurrence hoạt động.
* Search hoạt động.
* Filter hoạt động.
* Pagination hoạt động.
* Today query đúng.
* Upcoming query đúng.
* Overdue query đúng.
* Calendar đúng.
* Statistics đúng.
* Validation error format thống nhất.
* Không leak password hash.
* Không leak private Task giữa users.
* Transaction critical flow được test.
