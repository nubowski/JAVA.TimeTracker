package ru.nubowski.timeTracker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.exception.UserNotFoundException;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing users.
 */
@Service // TODO do not fockup with @annotation as the previous time
public class UserService {
    private static final  Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    /**
     * Constructor for UserService.
     *
     * @param userRepository repository for handling users
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns all users.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        LOGGER.debug("Getting all users");
        return userRepository.findAll();
    }

    /**
     * Find a user by id.
     *
     * @param id the id of the user to be returned
     * @return the user with the specified id
     * @throws UserNotFoundException if no user is found with the specified id
     */
    public User getUserById(Long id) { // findById is not a bad workaround, just let it be dummy
        LOGGER.debug("Getting user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Find a user by username.
     *
     * @param username the username of the user to be returned
     * @return the user with the specified username
     * @throws UserNotFoundException if no user is found with the specified username
     */
    public User getUser(String username) {
        LOGGER.debug("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    /**
     * Saves a user. If the user does not exist, it creates a new one.
     * If the user exists, it updates the fields.
     *
     * @param user the user to be saved
     * @return the saved user
     * @throws IllegalArgumentException if the user or the username is null
     */
    public User saveUser(User user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Username can't be null");
        }
        LOGGER.info("Saving user: {}", user.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            // if user exists, update fields
            User userToUpdate = existingUser.get();
            BeanUtils.copyProperties(user, userToUpdate, getNullPropertyNames(user));
            return userRepository.save(userToUpdate);
        } else {
            // if user doesn't exist - create
            if (user.getId() == null) {
                user.setCreatedAt(LocalDateTime.now()); // first time created add timestamp
            }
            return userRepository.save(user);
        }
    }

    //

    /**
     * Utility method of handling null properties in a bean.
     * Returns an array of the null properties.
     *
     * @param source the object to check
     * @return a string array of the null properties
     */
    private static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            // check if value of this property is null then add it to the collection
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username of the user to be deleted
     * @throws UserNotFoundException if no user is found with the specified username
     */
    public void deleteUser(String username) {
        LOGGER.info("Deleting user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        userRepository.delete(user);
    }

    /**
     * Returns an Optional of a User by username.
     *
     * @param username the username of the user to be returned
     * @return an Optional of the user with the specified username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Returns a User by username, not Optional.
     *
     * @param username the username of the user to be returned
     * @return the user with the specified username
     */
    public User getUserByUsernameNotOptional (String username) {
        return userRepository.findUserByUsername(username);
    }

    /**
     * Check if user is present.
     *
     * @param username the username of the user to be checked
     * @return TRUE if it is present, FALSE otherwise
     */
    public boolean userIsPresent(String username) {
        Optional<User> checkUser = getUserByUsername(username);
        return checkUser.isPresent();
    }

}
