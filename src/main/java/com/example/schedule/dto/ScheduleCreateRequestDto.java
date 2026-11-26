package com.example.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일정 생성 요청 데이터를 전달하는 DTO.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequestDto {

    private String title;
    private String contents;
    private String author;
    private String password;
}
