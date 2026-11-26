package com.example.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일정 수정 요청 데이터를 전달하는 DTO.
 * 수정 허용 필드인 제목(title), 작성자명(author)과 본인 확인용 비밀번호(password)만 포함한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUpdateRequestDto {

    private String title;
    private String author;
    private String password;
}
