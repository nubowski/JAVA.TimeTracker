package ru.nubowski.timeTracker.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskCreateRequest {
    @NotBlank(message = "Task name is required")
    private String name;

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

    @NotBlank(message = "Description is required")
    private String description;
}
