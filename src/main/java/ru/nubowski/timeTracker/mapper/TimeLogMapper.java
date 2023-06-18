package ru.nubowski.timeTracker.mapper;

import org.springframework.stereotype.Service;
import ru.nubowski.timeTracker.dto.response.TimeLogsByTaskResponse;
import ru.nubowski.timeTracker.model.TimeLog;

@Service
public class TimeLogMapper {

    public TimeLogsByTaskResponse mapTimeLogToResponse (TimeLog timeLog) {
        return new TimeLogsByTaskResponse(timeLog);

    }
}
