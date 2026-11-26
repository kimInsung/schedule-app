package com.example.schedule.common.exception;

/**
 * 요청 입력값이 필수 조건이나 최대 길이 제한 등 유효성 검증을 통과하지 못했을 때 발생하는 예외.
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
