package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setDisplayName("test");
        when(userRepository.save(any(User.class))).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals(user.getDisplayName(), savedUser.getDisplayName());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }


    @Test
    public void testGetUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setDisplayName("test");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User userFromDb = userService.getUserById(1L);
        assertNotNull(userFromDb);
        assertEquals(userFromDb.getDisplayName(), user.getDisplayName());
        assertEquals(userFromDb.getEmail(), user.getEmail());
    }

    @Test
    public void testUpdateUserEmail(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setDisplayName("testUser");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        user.setEmail("updated@test.com");
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUserName(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setDisplayName("testUser");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        user.setDisplayName("updatedUser");
        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }
}
