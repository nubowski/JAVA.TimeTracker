package ru.nubowski.timeTracker.dto.response;

import ru.nubowski.timeTracker.model.Task;

import java.time.LocalDateTime;

/**
 * DTO for response after creating a task.
 */
public class TaskCreateResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String username;

    // getters and setters

    public TaskCreateResponse(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.createdAt = task.getCreatedAt();
        this.username = task.getUser().getUsername();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

