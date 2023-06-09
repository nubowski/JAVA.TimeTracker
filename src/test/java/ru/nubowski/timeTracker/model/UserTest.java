package ru.nubowski.timeTracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testNameBehavior() {
        // Arrange
        String expectedName = "TestName";
        // Act
        user.setName(expectedName);
        // Assert
        assertEquals(expectedName, user.getName());
    }

    @Test
    public void testTaskAssociation() {
        User user = new User();
        Task task1 = new Task();
        Task task2 = new Task();

        // empty
        assertTrue(user.getTasks().isEmpty());

        // add and check the set
        user.getTasks().add(task1);
        assertTrue(user.getTasks().contains(task1));
        assertEquals(1, user.getTasks().size());

        // add addition and check the set
        user.getTasks().add(task2);
        assertTrue(user.getTasks().contains(task1));
        assertTrue(user.getTasks().contains(task2));
        assertEquals(2, user.getTasks().size());

        // remove
        user.getTasks().remove(task1);
        assertFalse(user.getTasks().contains(task1));
        assertTrue(user.getTasks().contains(task2));
        assertEquals(1, user.getTasks().size());

    }
}
