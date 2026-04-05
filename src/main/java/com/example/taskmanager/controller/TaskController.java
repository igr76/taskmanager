package com.example.taskmanager.controller;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Mono<ResponseEntity<TaskResponse>> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request)
                .map(task -> ResponseEntity.status(HttpStatus.CREATED).body(task));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskResponse>> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<PageResponse<TaskResponse>>> getTasks(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) TaskStatus status) {
        return taskService.getTasks(page, size, status)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<TaskResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        return taskService.updateStatus(id, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
