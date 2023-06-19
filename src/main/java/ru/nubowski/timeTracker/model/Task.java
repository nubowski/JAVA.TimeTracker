package ru.nubowski.timeTracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a task in the time tracking system
 */
@Entity
@Table(name = "tasks")
public class Task {
    /**
     * The unique identifier of the task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * The name of the task.
     */
    @Column(name = "name")
    private String name;
    /**
     * The description of the task.
     */
    @Column(name = "description")
    private String description;
    /**
     * The timestamp when the task was created.
     */

    @Column
    private LocalDateTime createdAt;
    /**
     * The user associated with the task.
     */
    @ManyToOne(fetch = FetchType.LAZY) // TODO read about one-to-many and vice versa for DB with dif fetch types
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    /**
     * The set of time logs associated with the task.
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<TimeLog> timeLogs = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<TimeLog> getTimeLogs() {
        return timeLogs;
    }

    public void setTimeLogs(Set<TimeLog> timeLogs) {
        this.timeLogs = timeLogs;
    }
}
