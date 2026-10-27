package com.techflow.e2e;

import com.jayway.jsonpath.JsonPath;
import com.techflow.BaseIntegrationTest;
import com.techflow.auth.dto.LoginRequest;
import com.techflow.auth.dto.RegisterRequest;
import com.techflow.project.dto.CreateProjectRequest;
import com.techflow.task.dto.CreateSubtaskRequest;
import com.techflow.task.dto.CreateTaskRequest;
import com.techflow.task.dto.UpdateSubtaskRequest;
import com.techflow.task.model.TaskPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("E2E Flow: Đăng ký -> Đăng nhập lấy JWT -> Tạo Project -> Tạo Task -> Tạo Subtasks -> Hoàn thành Subtasks -> Task cha hoàn thành")
    void fullTaskFlowLifecycle() throws Exception {
        String uniqueEmail = "user_" + UUID.randomUUID() + "@example.com";
        String password = "StrongPassword@123";

        // 1. Đăng ký
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(password);
        registerRequest.setName("Integration User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail));

        // 2. Đăng nhập lấy JWT
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(uniqueEmail);
        loginRequest.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
        String authHeader = "Bearer " + accessToken;

        // 3. Tạo Project
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("End to End Project");
        projectRequest.setDescription("Project for integration test");

        MvcResult projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("End to End Project"))
                .andReturn();

        String projectId = JsonPath.read(projectResult.getResponse().getContentAsString(), "$.data.id");

        // 4. Tạo Task
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("End to End Parent Task");
        taskRequest.setDescription("Parent task description");
        taskRequest.setProjectId(UUID.fromString(projectId));
        taskRequest.setPriority(TaskPriority.HIGH);

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("End to End Parent Task"))
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andReturn();

        String taskId = JsonPath.read(taskResult.getResponse().getContentAsString(), "$.data.id");

        // 5. Tạo Subtask 1 và Subtask 2
        CreateSubtaskRequest subtask1Req = new CreateSubtaskRequest();
        subtask1Req.setTitle("Subtask 1");
        MvcResult subtask1Result = mockMvc.perform(post("/api/v1/tasks/" + taskId + "/subtasks")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subtask1Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.completed").value(false))
                .andReturn();
        String subtask1Id = JsonPath.read(subtask1Result.getResponse().getContentAsString(), "$.data.id");

        CreateSubtaskRequest subtask2Req = new CreateSubtaskRequest();
        subtask2Req.setTitle("Subtask 2");
        MvcResult subtask2Result = mockMvc.perform(post("/api/v1/tasks/" + taskId + "/subtasks")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subtask2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.completed").value(false))
                .andReturn();
        String subtask2Id = JsonPath.read(subtask2Result.getResponse().getContentAsString(), "$.data.id");

        // 6. Hoàn thành Subtask 1 -> Task cha chuyển sang IN_PROGRESS (BR-TASK-001)
        UpdateSubtaskRequest updateSubtask1 = new UpdateSubtaskRequest();
        updateSubtask1.setIsCompleted(true);
        mockMvc.perform(put("/api/v1/tasks/" + taskId + "/subtasks/" + subtask1Id)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSubtask1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 7. Hoàn thành Subtask 2 -> Tất cả hoàn thành -> Task cha chuyển sang COMPLETED (BR-TASK-001)
        UpdateSubtaskRequest updateSubtask2 = new UpdateSubtaskRequest();
        updateSubtask2.setIsCompleted(true);
        mockMvc.perform(put("/api/v1/tasks/" + taskId + "/subtasks/" + subtask2Id)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSubtask2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());
    }
}