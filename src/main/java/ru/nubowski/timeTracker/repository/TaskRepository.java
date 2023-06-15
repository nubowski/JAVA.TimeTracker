package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.Task;

import java.time.LocalDateTime;
import java.util.List;


// ALREADY has save(), findALL(), findById() methods !!!

public interface TaskRepository extends JpaRepository <Task, Long> {
    List<Task> findByCreatedAtBefore(LocalDateTime cutoff);

    List<Task> findByUserId (Long id);
}
