package ru.nubowski.timeTracker.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in the time tracking system.
 */
@Entity
@Table(name = "app_user")
public class User {
    /**
     * The unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * The username of the user.
     */
    @Column(name = "username", unique = true)
    private String username;
    /**
     * The display name of the user.
     */
    @Column(name = "display_name")
    private String displayName;  // dummy if we ll need a display name (aka, names, etc)
    /**
     * The email of the user
     */
    @Column(name = "email")
    private String email;
    /**
     * The timestamp when the user was created.
     */
    @Column
    private LocalDateTime createdAt;
    /**
     * The set of tasks associated with the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private Set<Task> tasks = new HashSet<>();


    @Override
    public String toString() {
        return "User { " +
                "id = " + id +
                ", username = '" + username + '\'' +
                ", displayName = '" + displayName + '\'' +
                ", email = '" + email + '\'' +
                ", createdAt = " + createdAt +
                ", tasks size = " + (tasks != null ? tasks.size() : 0) +
                " }";
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
