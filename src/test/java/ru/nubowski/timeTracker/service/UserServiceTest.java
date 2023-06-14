package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.exception.UserNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {



    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TaskService taskService;
    @MockBean
    private TimeLogService timeLogService;

    @Autowired
    private UserService userService;

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        var users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("test");
        user.setEmail("test@test.com");
        user.setDisplayName("test");
        when(userRepository.save(any(User.class))).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals(user.getDisplayName(), savedUser.getDisplayName());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }


    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("test");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User userFromDb = userService.getUserById(1L);
        assertNotNull(userFromDb);
        assertEquals(userFromDb.getDisplayName(), user.getDisplayName());
        assertEquals(userFromDb.getEmail(), user.getEmail());
    }

    @Test
    void testUpdateUserEmail(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testUser");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        user.setEmail("updated@test.com");
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUsername(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testUser");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        user.setUsername("updatedUser");
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });
    }

    @Test
    void testDeleteUserAndTasksUserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUserAndTasks("test_username");
        });
    }

    @Test // isolated context of cleanup service
    void testDeleteOldUsers(){
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        User oldUser1 = new User();
        oldUser1.setUsername("old1");
        User oldUser2 = new User();
        oldUser2.setUsername("old2");
        when(userRepository.findByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(Arrays.asList(oldUser1, oldUser2));
        userService.deleteOldUsers(cutoff);
        verify(userRepository, times(1)).deleteAll(Arrays.asList(oldUser1, oldUser2));
    }
}
