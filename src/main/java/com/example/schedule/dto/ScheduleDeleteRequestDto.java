package com.example.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일정 삭제 요청 데이터를 전달하는 DTO.
 * 본인 확인용 비밀번호(password)를 포함한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDeleteRequestDto {

    private String password;
}
