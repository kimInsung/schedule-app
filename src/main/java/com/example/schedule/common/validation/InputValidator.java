package com.example.schedule.common.validation;

import com.example.schedule.common.exception.InvalidInputException;

/**
 * Bean Validation 없이 순수 Java 조건문으로 요청 입력값을 검증하는 공통 유틸리티.
 */
public final class InputValidator {

    private InputValidator() {
    }

    /**
     * 요청 DTO 객체 자체가 null인지 검증한다.
     *
     * @param requestDto 검증할 요청 객체
     */
    public static void validateRequestNotNull(Object requestDto) {
        // 요청 본문 객체가 null이면 이후 필드 접근 시 NullPointerException이 발생하므로 먼저 차단
        if (requestDto == null) {
            throw new InvalidInputException("요청 데이터가 null일 수 없습니다.");
        }
    }

    /**
     * 필수 문자열이 null이거나 공백(whitespace)만으로 이루어져 있지 않은지 검증한다.
     *
     * @param value 검증할 문자열 값
     * @param fieldName 오류 메시지에 표시할 필드명
     */
    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(fieldName + " 입력값은 비어 있거나 공백일 수 없습니다.");
        }
    }

    /**
     * 필수 문자열 여부와 최대 글자 수 제한을 함께 검증한다.
     *
     * @param value 검증할 문자열 값
     * @param fieldName 오류 메시지에 표시할 필드명
     * @param maxLength 허용되는 최대 글자 수
     */
    public static void validateRequiredWithMaxLength(String value, String fieldName, int maxLength) {
        validateRequired(value, fieldName);
        if (value.length() > maxLength) {
            throw new InvalidInputException(fieldName + " 입력값은 최대 " + maxLength + "자까지 입력할 수 있습니다.");
        }
    }
}
