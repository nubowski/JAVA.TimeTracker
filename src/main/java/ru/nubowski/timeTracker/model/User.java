package ru.nubowski.timeTracker.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "display_name")
    private String displayName;  // dummy if we ll need a display name (aka, names, etc)
    @Column(name = "email")
    private String email;

    @Column
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "user")
    private Set<Task> tasks = new HashSet<>();

    // Constructors ?


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

}
