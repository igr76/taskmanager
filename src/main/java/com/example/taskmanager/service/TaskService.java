package com.example.taskmanager.service;

import org.springframework.stereotype.Service;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Mono<TaskResponse> createTask(TaskCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return Mono.fromCallable(() -> taskRepository.save(task))
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::toResponse);
    }

    public Mono<TaskResponse> getTaskById(Long id) {
        return Mono.fromCallable(() -> taskRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(task -> {
                    if (task != null) {
                        return Mono.just(toResponse(task));
                    } else {
                        return Mono.error(new TaskNotFoundException(id));
                    }
                });
    }

    public Mono<PageResponse<TaskResponse>> getTasks(int page, int size, TaskStatus status) {
        return Mono.fromCallable(() -> {
            List<Task> tasks = taskRepository.findAll(page, size, status);
            long total = taskRepository.count(status);
            int totalPages = (size > 0) ? (int) ((total + size - 1) / size) : 0;
            return new PageResponse<>(
                    tasks.stream().map(this::toResponse).collect(Collectors.toList()),
                    page,
                    size,
                    total,
                    totalPages
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TaskResponse> updateStatus(Long id, TaskStatusUpdateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return Mono.fromCallable(() -> {
                    boolean updated = taskRepository.updateStatus(id, request.getStatus(), now);
                    if (!updated) {
                        throw new TaskNotFoundException(id);
                    }
                    Task task = taskRepository.findById(id);
                    if (task == null) {
                        throw new TaskNotFoundException(id);
                    }
                    return task;
                }).subscribeOn(Schedulers.boundedElastic())
                .map(this::toResponse);
    }

    public Mono<Void> deleteTask(Long id) {
        return Mono.fromCallable(() -> {
            boolean deleted = taskRepository.deleteById(id);
            if (!deleted) {
                throw new TaskNotFoundException(id);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
