package com.example.schedule.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 일관된 HTTP 응답으로 변환하는 전역 예외 처리기.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 존재하지 않는 일정 조회 시 404 Not Found 응답을 반환한다.
     *
     * @param exception 일정 미존재 예외
     * @return 에러 정보와 HTTP 404 상태 코드
     */
    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScheduleNotFoundException(ScheduleNotFoundException exception) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * 비밀번호 불일치 시 401 Unauthorized 응답을 반환한다.
     *
     * @param exception 비밀번호 불일치 예외
     * @return 에러 정보와 HTTP 401 상태 코드
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException exception) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    /**
     * 일정당 최대 댓글 개수(10개) 초과 시 400 Bad Request 응답을 반환한다.
     *
     * @param exception 댓글 개수 초과 예외
     * @return 에러 정보와 HTTP 400 상태 코드
     */
    @ExceptionHandler(CommentLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCommentLimitExceededException(CommentLimitExceededException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * 필수 입력값 누락, 공백 입력, 최대 길이 초과 등 입력값 검증 실패 시 400 Bad Request 응답을 반환한다.
     *
     * @param exception 입력값 검증 예외
     * @return 에러 정보와 HTTP 400 상태 코드
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
