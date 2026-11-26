package com.example.schedule.common.exception;

import lombok.Getter;

/**
 * 예외 발생 시 클라이언트에 반환하는 공통 에러 응답 DTO.
 */
@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
