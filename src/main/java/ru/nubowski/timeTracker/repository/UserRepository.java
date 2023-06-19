package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository is an interface for performing database operations on {@link User} entities.
 * It extends Spring Data JPA {@link JpaRepository}, gaining methods like save(), findAll(), and findById().
 */
public interface UserRepository extends JpaRepository <User, Long> {

    /**
     * Finds a user with the specified username.
     * @param username The username of the user.
     * @return An OPTIONAL user that matches the specified username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user with the specified username.
     * @param username The username of the user.
     * @return A User that matches the specified username.
     */
    User findUserByUsername (String username);

    /**
     * Finds all users that were created before the specified cutoff time.
     * @param cutoff The cutoff time to check users against.
     * @return A list of users that were created before the cutoff time.
     */
    List<User> findByCreatedAtBefore(LocalDateTime cutoff);
}
