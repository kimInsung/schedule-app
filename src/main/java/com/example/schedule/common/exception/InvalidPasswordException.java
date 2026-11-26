package com.example.schedule.common.exception;

/**
 * 일정 수정 등 비밀번호 검증이 필요한 요청에서 비밀번호가 일치하지 않을 때 발생하는 예외.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}
