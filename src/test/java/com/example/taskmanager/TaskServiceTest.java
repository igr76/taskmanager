package com.example.taskmanager;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void createTask_shouldSaveAndReturnTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");
        request.setDescription("Description");

        Task task = new Task(1L, "Test Task", "Description", TaskStatus.NEW, now, now);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Mono<TaskResponse> result = taskService.createTask(request);

        StepVerifier.create(result)
                .expectNextMatches(res -> res.getId() == 1L &&
                        res.getTitle().equals("Test Task") &&
                        res.getDescription().equals("Description") &&
                        res.getStatus() == TaskStatus.NEW)
                .verifyComplete();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_shouldReturnTask_whenExists() {
        Task task = new Task(1L, "Task", null, TaskStatus.NEW, now, now);
        when(taskRepository.findById(1L)).thenReturn(task);

        Mono<TaskResponse> result = taskService.getTaskById(1L);

        StepVerifier.create(result)
                .expectNextMatches(res -> res.getId() == 1L)
                .verifyComplete();
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_shouldThrowNotFound_whenNotExists() {
        when(taskRepository.findById(1L)).thenReturn(null);

        Mono<TaskResponse> result = taskService.getTaskById(1L);

        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();
    }

    @Test
    void updateStatus_shouldUpdateAndReturnTask() {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest();
        request.setStatus(TaskStatus.DONE);

        Task updatedTask = new Task(1L, "Task", null, TaskStatus.DONE, now, now.plusSeconds(1));
        when(taskRepository.updateStatus(eq(1L), eq(TaskStatus.DONE), any(LocalDateTime.class))).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(updatedTask);

        Mono<TaskResponse> result = taskService.updateStatus(1L, request);

        StepVerifier.create(result)
                .expectNextMatches(res -> res.getStatus() == TaskStatus.DONE)
                .verifyComplete();
        verify(taskRepository, times(1)).updateStatus(anyLong(), any(), any());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void updateStatus_shouldThrowNotFound_whenUpdateFails() {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest();
        request.setStatus(TaskStatus.DONE);
        when(taskRepository.updateStatus(anyLong(), any(), any())).thenReturn(false);

        Mono<TaskResponse> result = taskService.updateStatus(1L, request);

        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();
        verify(taskRepository, never()).findById(anyLong());
    }

    @Test
    void deleteTask_shouldDelete_whenExists() {
        when(taskRepository.deleteById(1L)).thenReturn(true);

        Mono<Void> result = taskService.deleteTask(1L);

        StepVerifier.create(result)
                .verifyComplete();
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_shouldThrowNotFound_whenNotExists() {
        when(taskRepository.deleteById(1L)).thenReturn(false);

        Mono<Void> result = taskService.deleteTask(1L);

        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();
    }

    @Test
    void getTasks_shouldReturnPageWithTasks() {
        List<Task> tasks = List.of(
                new Task(1L, "Task1", null, TaskStatus.NEW, now, now),
                new Task(2L, "Task2", null, TaskStatus.NEW, now, now)
        );
        when(taskRepository.findAll(0, 10, TaskStatus.NEW)).thenReturn(tasks);
        when(taskRepository.count(TaskStatus.NEW)).thenReturn(2L);

        Mono<PageResponse<TaskResponse>> result = taskService.getTasks(0, 10, TaskStatus.NEW);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getContent().size() == 2 &&
                        page.getPage() == 0 &&
                        page.getSize() == 10 &&
                        page.getTotalElements() == 2L &&
                        page.getTotalPages() == 1)
                .verifyComplete();
    }
}
