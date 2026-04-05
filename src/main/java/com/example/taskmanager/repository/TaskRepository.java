package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskRepository {

    private final JdbcClient jdbcClient;

    public TaskRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Task save(Task task) {
        String sql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            RETURNING id, title, description, status, created_at, updated_at
        """;

        return jdbcClient.sql(sql)
                .param("title", task.getTitle())
                .param("description", task.getDescription())
                .param("status", task.getStatus().name())
                .param("createdAt", task.getCreatedAt())
                .param("updatedAt", task.getUpdatedAt())
                .query((rs, rowNum) -> new Task(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        TaskStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ))
                .single();
    }

    public Task findById(Long id) {
        String sql = "SELECT id, title, description, status, created_at, updated_at FROM tasks WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query((rs, rowNum) -> new Task(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        TaskStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ))
                .optional()
                .orElse(null);
    }

    public List<Task> findAll(int page, int size, TaskStatus status) {
        int offset = page * size;
        StringBuilder sql = new StringBuilder("""
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
            WHERE 1=1
        """);
        if (status != null) {
            sql.append(" AND status = :status");
        }
        sql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset");

        var spec = jdbcClient.sql(sql.toString())
                .param("limit", size)
                .param("offset", offset);
        if (status != null) {
            spec = spec.param("status", status.name());
        }
        return spec.query((rs, rowNum) -> new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        )).list();
    }

    public long count(TaskStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM tasks WHERE 1=1");
        if (status != null) {
            sql.append(" AND status = :status");
        }
        var spec = jdbcClient.sql(sql.toString());
        if (status != null) {
            spec = spec.param("status", status.name());
        }
        return spec.query(Long.class).single();
    }

    public boolean updateStatus(Long id, TaskStatus newStatus, LocalDateTime updatedAt) {
        String sql = """
            UPDATE tasks
            SET status = :status, updated_at = :updatedAt
            WHERE id = :id
        """;
        int rowsUpdated = jdbcClient.sql(sql)
                .param("status", newStatus.name())
                .param("updatedAt", updatedAt)
                .param("id", id)
                .update();
        return rowsUpdated == 1;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = :id";
        int rowsDeleted = jdbcClient.sql(sql)
                .param("id", id)
                .update();
        return rowsDeleted == 1;
    }
}
