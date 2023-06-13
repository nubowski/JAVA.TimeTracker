package ru.nubowski.timeTracker.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.nubowski.timeTracker.controller.TimeLogControllerTest;

import java.time.LocalDateTime;
@SpringBootTest
@ActiveProfiles("test")
public class ClockProviderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogControllerTest.class);
    @Autowired
    private CustomClockProvider clockProvider;



    @Test
    void timeTest () {
        LOGGER.info("LocalDate: {}", LocalDateTime.now());
        LOGGER.info("CustomDate: {}", clockProvider.now());

    }

}
