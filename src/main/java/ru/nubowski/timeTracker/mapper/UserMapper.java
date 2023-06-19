package ru.nubowski.timeTracker.mapper;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.request.UserCreateRequest;
import ru.nubowski.timeTracker.dto.request.UserUpdateRequest;
import ru.nubowski.timeTracker.dto.response.UsersGetResponse;
import ru.nubowski.timeTracker.model.User;

/**
 * Mapper service for User related objects.
 */
@Service
public class UserMapper {

    /**
     * Maps a UserCreateRequest to a User object.
     *
     * @param request the UserCreateRequest
     * @return the created User object
     */
    public User mapToUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        // set other..
        return user;
    }

    /**
     * Maps a UserUpdateRequest and a username to a User object.
     *
     * @param request the UserUpdateRequest
     * @param username the username of the user to be updated
     * @return the updated User object
     */
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

    /**
     * Maps a User object to a UsersGetResponse object.
     *
     * @param user the User object
     * @return the created UsersGetResponse object
     */
    public UsersGetResponse mapToUserGetResponse(User user) {
        // setters if needed
        return new UsersGetResponse(user);
    }
}
