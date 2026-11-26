package com.example.schedule.controller;

import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 일정 HTTP 요청을 처리하는 Controller 계층.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 새로운 일정을 생성한다.
     *
     * @param requestDto 일정 생성 요청 본문
     * @return 생성된 일정 정보와 HTTP 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<ScheduleResponseDto> createSchedule(@RequestBody ScheduleCreateRequestDto requestDto) {
        // 서비스 계층에 일정 생성 위임 후 201 Created 상태 코드와 함께 응답
        ScheduleResponseDto responseDto = scheduleService.createSchedule(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
