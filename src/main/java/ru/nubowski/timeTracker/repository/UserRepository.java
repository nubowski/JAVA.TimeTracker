package ru.nubowski.timeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nubowski.timeTracker.model.User;

public interface UserRepository extends JpaRepository <User, Long> {
}
