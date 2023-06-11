package ru.nubowski.timeTracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TimeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "ended_by_user")
    private boolean endedByUser;

    @Enumerated(EnumType.STRING)
    private TaskState taskState;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isEndedByUser() {
        return endedByUser;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public void setEndedByUser(boolean endedByUser) {
        this.endedByUser = endedByUser;
    }
}
