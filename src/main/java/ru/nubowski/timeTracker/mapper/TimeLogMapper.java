package ru.nubowski.timeTracker.mapper;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.response.TimeLogsByTaskResponse;
import ru.nubowski.timeTracker.model.TimeLog;

/**
 * Mapper service for TimeLog related objects.
 */
@Service
public class TimeLogMapper {
    /**
     * Maps a TimeLog object to a TimeLogsByTaskResponse object.
     *
     * @param timeLog the TimeLog object
     * @return the created TimeLogsByTaskResponse object
     */
    public TimeLogsByTaskResponse mapTimeLogToResponse (TimeLog timeLog) {
        return new TimeLogsByTaskResponse(timeLog);

    }
}
