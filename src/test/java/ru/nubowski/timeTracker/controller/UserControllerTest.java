package ru.nubowski.timeTracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.nubowski.timeTracker.model.User;
import ru.nubowski.timeTracker.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserControllerTest.class);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @Transactional
    @Test
    void testCreateUser () throws Exception {
        User user = new User();
        // some data added..
        user.setUsername("testAccount");
        user.setEmail("test@test.com");
        user.setDisplayName("test");

        LOGGER.info("About to test user creation with user {}", user.getUsername());

        // form the request..
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
        // assert user creation

        User createdUser = userService.getUser("testAccount");
        LOGGER.info("Created user is {}", createdUser);
        assertEquals(createdUser.getUsername(), "testAccount");
        assertEquals(createdUser.getEmail(), "test@test.com");
        assertEquals(createdUser.getDisplayName(), "test");
    }
}
