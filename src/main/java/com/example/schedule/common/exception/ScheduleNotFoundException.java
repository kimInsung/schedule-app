package com.example.schedule.common.exception;

/**
 * 요청한 ID에 해당하는 일정을 찾을 수 없을 때 발생하는 예외.
 */
public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(Long id) {
        super("해당 일정을 찾을 수 없습니다. id=" + id);
    }
}
