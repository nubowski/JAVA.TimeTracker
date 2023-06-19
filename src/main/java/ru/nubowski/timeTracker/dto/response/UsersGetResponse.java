package ru.nubowski.timeTracker.dto.response;

import ru.nubowski.timeTracker.model.User;

import java.time.LocalDateTime;

/**
 * DTO for response after getting users
 */
public class UsersGetResponse {
    private Long id;
    private String username;
    private String mail;
    private String displayName;

    private LocalDateTime createdAt;

    public UsersGetResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.mail = user.getEmail();
        this.displayName = user.getDisplayName();
        this.createdAt = user.getCreatedAt();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
