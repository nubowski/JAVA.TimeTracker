package ru.nubowski.timeTracker.dto.response;

import ru.nubowski.timeTracker.model.TimeLog;

import java.time.LocalDateTime;

public class TimeLogByIdResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean endedByUser;
    private String taskName;

    public TimeLogByIdResponse(TimeLog timeLog) {
        this.id = timeLog.getId();
        this.startTime = timeLog.getStartTime();
        this.endTime = timeLog.getEndTime();
        this.endedByUser = timeLog.isEndedByUser();
        this.taskName = timeLog.getTask().getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isEndedByUser() {
        return endedByUser;
    }

    public void setEndedByUser(boolean endedByUser) {
        this.endedByUser = endedByUser;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
