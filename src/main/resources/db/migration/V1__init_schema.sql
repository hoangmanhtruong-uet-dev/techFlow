-- Flyway Migration: V1__init_schema.sql
-- Description: Initialize TaskFlow database schema

-- Enable pgcrypto or uuid-ossp for UUID generation if needed
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. Table: users
-- =============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash TEXT NOT NULL,
    name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

-- =============================================================================
-- 2. Table: projects
-- =============================================================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================================================
-- 3. Table: tasks
-- =============================================================================
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    project_id UUID,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    deadline TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    recurrence_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    recurrence_parent_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_recurrence_parent FOREIGN KEY (recurrence_parent_id) REFERENCES tasks(id) ON DELETE SET NULL,
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_tasks_recurrence_type CHECK (recurrence_type IN ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY'))
);

-- =============================================================================
-- 4. Table: subtasks
-- =============================================================================
CREATE TABLE subtasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subtasks_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- =============================================================================
-- 5. Table: tags
-- =============================================================================
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    normalized_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_tags_user_normalized_name UNIQUE (user_id, normalized_name)
);

-- =============================================================================
-- 6. Table: task_tags (Join table)
-- =============================================================================
CREATE TABLE task_tags (
    task_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- =============================================================================
-- 7. Table: reminders
-- =============================================================================
CREATE TABLE reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    remind_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT chk_reminders_status CHECK (status IN ('PENDING', 'SENT', 'CANCELLED', 'FAILED'))
);

-- =============================================================================
-- 8. Indexes (as defined in DATA_MODEL.md & Requirements)
-- =============================================================================

-- IDX-001: tasks(user_id)
CREATE INDEX idx_tasks_user_id ON tasks(user_id);

-- IDX-002: tasks(user_id, status)
CREATE INDEX idx_tasks_user_id_status ON tasks(user_id, status);

-- IDX-003: tasks(user_id, deadline)
CREATE INDEX idx_tasks_user_id_deadline ON tasks(user_id, deadline);

-- IDX-004: tasks(user_id, priority)
CREATE INDEX idx_tasks_user_id_priority ON tasks(user_id, priority);

-- IDX-005: tasks(project_id)
CREATE INDEX idx_tasks_project_id ON tasks(project_id);

-- Recurrence parent index for fast tracking
CREATE INDEX idx_tasks_recurrence_parent_id ON tasks(recurrence_parent_id);

-- IDX-006: projects(user_id)
CREATE INDEX idx_projects_user_id ON projects(user_id);

-- Subtasks task_id index
CREATE INDEX idx_subtasks_task_id ON subtasks(task_id);

-- Subtasks position sorting
CREATE INDEX idx_subtasks_task_id_position ON subtasks(task_id, position);

-- Tags user_id index
CREATE INDEX idx_tags_user_id ON tags(user_id);

-- Task_tags tag_id index for reverse lookup
CREATE INDEX idx_task_tags_tag_id ON task_tags(tag_id);

-- Reminders task_id index
CREATE INDEX idx_reminders_task_id ON reminders(task_id);

-- IDX-008: reminders(status, remind_at) - for background reminder worker
CREATE INDEX idx_reminders_status_remind_at ON reminders(status, remind_at);