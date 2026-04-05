package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class TaskStatusUpdateRequest {
    @NotNull(message = "Status cannot be null")
    private TaskStatus status;

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
