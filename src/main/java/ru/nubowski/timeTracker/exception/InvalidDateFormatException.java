package ru.nubowski.timeTracker.exception;

public class InvalidDateFormatException extends RuntimeException{
    public InvalidDateFormatException(String startDate, String endDate) {
        super("Invalid date range. The start date " + startDate + " should be before the end date " + endDate);
    }
}
