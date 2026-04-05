package com.example.taskmanager;

import com.example.taskmanager.controller.TaskController;
import com.example.taskmanager.dto.*;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    private TaskService taskService;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void createTask_shouldReturn201() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");
        request.setDescription("Desc");

        TaskResponse response = new TaskResponse(1L, "Test Task", "Desc", TaskStatus.NEW, now, now);
        when(taskService.createTask(any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Test Task");
    }

    @Test
    void createTask_withInvalidTitle_shouldReturn400() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("ab"); // too short

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getTaskById_shouldReturn200_whenExists() {
        TaskResponse response = new TaskResponse(1L, "Task", null, TaskStatus.NEW, now, now);
        when(taskService.getTaskById(1L)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/tasks/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);
    }

    @Test
    void getTaskById_shouldReturn404_whenNotFound() {
        when(taskService.getTaskById(1L)).thenReturn(Mono.error(new TaskNotFoundException(1L)));

        webTestClient.get()
                .uri("/api/tasks/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTasks_shouldReturn200WithPage() {
        PageResponse<TaskResponse> pageResponse = new PageResponse<>(
                List.of(new TaskResponse(1L, "Task", null, TaskStatus.NEW, now, now)),
                0, 10, 1, 1
        );
        when(taskService.getTasks(0, 10, TaskStatus.NEW)).thenReturn(Mono.just(pageResponse));

        webTestClient.get()
                .uri("/api/tasks?page=0&size=10&status=NEW")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.page").isEqualTo(0);
    }

    @Test
    void updateStatus_shouldReturn200() {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest();
        request.setStatus(TaskStatus.DONE);
        TaskResponse response = new TaskResponse(1L, "Task", null, TaskStatus.DONE, now, now);
        when(taskService.updateStatus(any(), any())).thenReturn(Mono.just(response));

        webTestClient.patch()
                .uri("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DONE");
    }

    @Test
    void deleteTask_shouldReturn204() {
        when(taskService.deleteTask(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/tasks/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
