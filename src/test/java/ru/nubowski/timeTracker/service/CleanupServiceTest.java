package ru.nubowski.timeTracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nubowski.timeTracker.config.CleanupProperties;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "cleanup.retentionPeriod=5",
        "cleanup.cronExpression=* * * * * ?"
})
public class CleanupServiceTest {
    @MockBean
    private TaskService taskService;
    @MockBean
    private UserService userService;
    @MockBean
    private TimeLogService timeLogService;
    @Autowired
    private CleanupProperties cleanupProperties;

    @Autowired
    private CleanupService cleanupService;

    @Test
    void testCallAllServices () {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupProperties.getRetentionPeriod());

        cleanupService.cleanup();

        verify(taskService, times(1)).deleteOldTasks(any(LocalDateTime.class));
        verify(userService, times(1)).deleteOldUsers(any(LocalDateTime.class));
        verify(timeLogService, times(1)).deleteOldTimeLogs(any(LocalDateTime.class));
    }
}


