package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.Task;


// ALREADY has save(), findALL(), findById() methods !!!

public interface TaskRepository extends JpaRepository <Task, Long> {
}
