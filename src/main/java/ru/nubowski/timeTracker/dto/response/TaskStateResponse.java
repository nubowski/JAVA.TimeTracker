package ru.nubowski.timeTracker.dto.response;

import ru.nubowski.timeTracker.model.TimeLog;

import java.time.LocalDateTime;

/**
 * DTO for response containing task state details.
 */
public class TaskStateResponse {
    private Long taskId;
    private String taskName;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private String taskState;

    public TaskStateResponse (TimeLog timeLog) {
        this.taskId = timeLog.getTask().getId();
        this.taskName = timeLog.getTask().getName();
        this.startedAt = timeLog.getStartTime();
        this.stoppedAt = timeLog.getEndTime();
        this.taskState = timeLog.getTaskState().name();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(LocalDateTime stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    public String getTaskState() {
        return taskState;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }
}
