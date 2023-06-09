package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.User;

public interface TimeLogRepository extends JpaRepository <User, Long> {
}
