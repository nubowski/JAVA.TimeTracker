package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.exception.TaskNotFoundException;
import ru.nubowski.timeTracker.model.Task;
import ru.nubowski.timeTracker.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TaskServiceTest {

    // TRYING  AssertJ library syntax HERE (instead of JUnit5)

    @MockBean
    private TaskRepository taskRepository;

    @Autowired // another way is @BeforeEach and constructor injection
    private TaskService taskService;

    @Test
    void testGetAllTasks() {
        Task task1 = new Task();
        task1.setName("test task n1");
        task1.setDescription("This is a first test task");
        Task task2 = new Task();
        task2.setName("test task n2");
        task2.setDescription("This is a second test task");
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<Task> tasks = taskService.getAllTasks();
        assertThat(tasks).hasSize(2); // magic numbers =_=
        assertThat(tasks.get(0).getName()).isEqualTo("test task n1");
        assertThat(tasks.get(1).getName()).isEqualTo("test task n2");

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTask () {
        Task task = new Task();
        task.setId(1L);
        task.setName("test task");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task found = taskService.getTask(1L);
        assertThat(found.getName()).isEqualTo("test task");

        verify(taskRepository, times(1)).findById(any());
    }

    @Test
    void testTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(1L))
                .isInstanceOf(TaskNotFoundException.class); // .hasMessage() if we need to make sure about the line or several based exs in one class
        verify(taskRepository, times(1)).findById(any());
    }

    @Test
    void testSaveTask() {
        Task task = new Task();
        task.setName("test task");
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task savedTask = taskService.saveTask(task);
        assertThat(savedTask.getName()).isEqualTo("test task");

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskRepository).deleteById(any());
        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).deleteById(any());
    }

    @Test
    void testDeleteOldTask () {
        Task oldTask = new Task();
        oldTask.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(taskRepository.findByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(List.of(oldTask));

        taskService.deleteOldTasks(LocalDateTime.now().minusDays(1));
        verify(taskRepository, times(1)).deleteAll(anyList());
    }
}
