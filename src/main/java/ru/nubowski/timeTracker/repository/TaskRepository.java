package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.Task;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for performing database operations on {@link Task} entities.
 * It extends Spring Data JPA {@link JpaRepository}, gaining methods like save(), findAll(), and findById().
 */
public interface TaskRepository extends JpaRepository <Task, Long> {

    /**
     * Finds all tasks created before the specified cutoff time.
     *
     * @param cutoff the time to check tasks against
     * @return a list of tasks created before the cutoff time
     */
    List<Task> findByCreatedAtBefore(LocalDateTime cutoff);

    /**
     * Finds all tasks associated with a specific user.
     *
     * @param id the user's id
     * @return a list of tasks associated with the user
     */
    List<Task> findByUserId (Long id);

}
