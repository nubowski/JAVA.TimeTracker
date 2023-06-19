package ru.nubowski.timeTracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a time log in the time tracking system
 */
@Entity
@Table(name = "time_logs")
public class TimeLog {
    /**
     * The unique identifier of the time log
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * The start time of the time log.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    /**
     * The end time of the time log.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    /**
     * Indicates whether the time log was ended by the user.
     * (DEPRECATED: This field may no longer be used and left as a tag.)
     */
    @Column(name = "ended_by_user") // DEPRECATED
    private boolean endedByUser;
    /**
     * The state of the task associated with the time log.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "task_state")
    private TaskState taskState;
    /**
     * The task associated with the time log.
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonBackReference
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
