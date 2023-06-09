package ru.nubowski.timeTracker.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @ManyToOne // TODO read about one-to-many and vice versa for DB
    @JoinColumn(name = "user_id", nullable = false) // TODO check withing the project (was OK)
    private User user;

    @OneToMany(mappedBy = "task")
    private Set<TimeLog> timeLogs = new HashSet<>();

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
