USER STORIES — TASKFLOW
1. AUTHENTICATION
   US-001 — Register

As a guest,
I want to create an account,
so that I can save and manage my tasks.

Acceptance Criteria

AC-001

Given người dùng chưa đăng nhập
When nhập email và password hợp lệ
Then hệ thống tạo tài khoản.

AC-002

Given email đã tồn tại
When người dùng đăng ký
Then hệ thống từ chối yêu cầu.

US-002 — Login

As a registered user,
I want to login,
so that I can access my personal workspace.

US-003 — Logout

As a logged-in user,
I want to logout,
so that my session is terminated safely.

2. TASK
   US-004 — Create Task

As a user,
I want to create a task,
so that I can remember the work I need to complete.

Acceptance Criteria

Given user đã đăng nhập
When user nhập title hợp lệ và chọn Create
Then task được tạo
And task xuất hiện trong danh sách.

US-005 — View Task

As a user,
I want to view task details,
so that I can see all information about my task.

US-006 — Edit Task

As a user,
I want to edit my task,
so that I can update its information when plans change.

US-007 — Delete Task

As a user,
I want to delete a task,
so that I can remove tasks that are no longer needed.

US-008 — Complete Task

As a user,
I want to mark a task completed,
so that I can track my progress.

Acceptance Criteria

Given task chưa completed
When user chọn Complete
Then:

status = COMPLETED

And:

completedAt != null

3. DEADLINE
   US-009 — Set Deadline

As a user,
I want to assign a deadline,
so that I know when a task must be completed.

US-010 — View Overdue Tasks

As a user,
I want to see overdue tasks,
so that I can identify missed deadlines.

4. PRIORITY
   US-011 — Set Priority

As a user,
I want to assign a priority level,
so that I can determine which tasks should be done first.

Priority:

LOW.
MEDIUM.
HIGH.
URGENT.
5. PROJECT
   US-012 — Create Project

As a user,
I want to create projects,
so that I can group related tasks.

US-013 — Assign Task to Project

As a user,
I want to assign tasks to projects,
so that my tasks remain organized.

US-014 — View Project Tasks

As a user,
I want to view all tasks inside a project,
so that I can monitor that project.

6. TAG
   US-015 — Create Tag

As a user,
I want to create tags,
so that I can classify tasks across multiple projects.

US-016 — Assign Tag

As a user,
I want to assign multiple tags to a task,
so that I can categorize it flexibly.

7. SUBTASK
   US-017 — Create Subtask

As a user,
I want to divide a task into smaller subtasks,
so that large tasks are easier to complete.

US-018 — Complete Subtask

As a user,
I want to complete individual subtasks,
so that I can track partial progress.

8. SEARCH & FILTER
   US-019 — Search Task

As a user,
I want to search tasks by title,
so that I can find a task quickly.

US-020 — Filter Task

As a user,
I want to filter tasks,
so that I can focus on relevant work.

Filters:

Status.
Priority.
Project.
Tag.
Deadline.
US-021 — Sort Task

As a user,
I want to sort tasks,
so that important or urgent tasks appear first.

9. REMINDER
   US-022 — Set Reminder

As a user,
I want to set a reminder,
so that I am reminded before a deadline.

10. RECURRING TASK
    US-023 — Create Recurring Task

As a user,
I want to create recurring tasks,
so that I do not have to recreate repetitive work manually.

Examples:

DAILY.
WEEKLY.
MONTHLY.
11. CALENDAR
    US-024 — Calendar View

As a user,
I want to view tasks on a calendar,
so that I can understand my workload over time.

12. DASHBOARD
    US-025 — Today Dashboard

As a user,
I want to see today's tasks,
so that I immediately know what I need to do.

US-026 — Upcoming Tasks

As a user,
I want to see upcoming deadlines,
so that I can plan ahead.

13. STATISTICS
    US-027 — Productivity Statistics

As a user,
I want to see basic task statistics,
so that I can understand my productivity.

Statistics:

Total tasks.
Completed.
Pending.
Overdue.
Completion rate.