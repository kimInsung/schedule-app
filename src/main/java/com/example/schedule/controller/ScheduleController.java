package com.example.schedule.controller;

import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.dto.ScheduleUpdateRequestDto;
import com.example.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 전체 일정 또는 특정 작성자의 일정 목록을 조회한다.
     *
     * @param author 선택적 작성자명 쿼리 파라미터
     * @return 수정일 기준 내림차순 정렬된 일정 목록과 HTTP 200 OK 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
            @RequestParam(required = false) String author
    ) {
        // 작성자명 필터링 및 정렬은 서비스 계층에 위임하고 DTO 목록만 반환
        List<ScheduleResponseDto> responseDtos = scheduleService.getSchedules(author);
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 지정한 ID의 단건 일정을 조회한다.
     *
     * @param id 조회할 일정 ID
     * @return 조회된 일정 정보와 HTTP 200 OK 상태 코드
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> getSchedule(@PathVariable Long id) {
        // 단건 조회 및 미존재 예외 처리는 서비스 계층에 위임하고 DTO만 반환
        ScheduleResponseDto responseDto = scheduleService.getSchedule(id);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 지정한 ID의 일정을 수정한다.
     *
     * @param id 수정할 일정 ID
     * @param requestDto 일정 수정 요청 본문 (title, author, password)
     * @return 수정된 일정 정보와 HTTP 200 OK 상태 코드
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateRequestDto requestDto
    ) {
        // 비밀번호 검증, 제한 수정, flush 반영은 서비스 계층에 위임하고 응답 DTO만 반환
        ScheduleResponseDto responseDto = scheduleService.updateSchedule(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
