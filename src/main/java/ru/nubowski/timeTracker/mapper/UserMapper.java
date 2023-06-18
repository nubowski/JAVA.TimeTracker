package ru.nubowski.timeTracker.mapper;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.request.UserCreateRequest;
import ru.nubowski.timeTracker.dto.request.UserUpdateRequest;
import ru.nubowski.timeTracker.dto.response.UsersGetResponse;
import ru.nubowski.timeTracker.model.User;

@Service
public class UserMapper {

    public User mapToUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        // set other..
        return user;
    }

    public User mapToUpgradeUser(UserUpdateRequest request, String username) {
        User user = new User();
        user.setUsername(username);
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getDisplayName() != null && !request.getDisplayName().isEmpty()) {
            user.setDisplayName(request.getDisplayName());
        }
        // set other if needed...
        return user;
    }

    public UsersGetResponse mapToUserGetResponse(User user) {
        // setters if needed
        return new UsersGetResponse(user);
    }
}
