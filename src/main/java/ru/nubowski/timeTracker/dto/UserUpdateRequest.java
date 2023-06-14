package ru.nubowski.timeTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class UserUpdateRequest {
    @Email(message = "Invalid email format")
    private String email;
    private String displayName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

