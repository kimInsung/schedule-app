package com.example.schedule.common.exception;

/**
 * 하나의 일정에 허용된 최대 댓글 개수(10개)를 초과하여 등록을 시도할 때 발생하는 예외.
 */
public class CommentLimitExceededException extends RuntimeException {

    public CommentLimitExceededException(Long scheduleId) {
        super("하나의 일정에는 댓글을 최대 10개까지만 작성할 수 있습니다. scheduleId=" + scheduleId);
    }

    public CommentLimitExceededException(String message) {
        super(message);
    }
}
