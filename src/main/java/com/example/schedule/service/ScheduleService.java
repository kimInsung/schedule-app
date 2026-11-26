package com.example.schedule.service;

import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.entity.Schedule;
import com.example.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 일정 관련 비즈니스 로직을 처리하는 Service 계층.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    /**
     * 새로운 일정을 생성하여 저장한 뒤 응답 DTO를 반환한다.
     *
     * @param requestDto 일정 생성 요청 정보
     * @return 비밀번호를 제외한 생성된 일정 정보
     */
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleCreateRequestDto requestDto) {
        // 요청 DTO를 기반으로 일정 엔티티 생성
        Schedule schedule = new Schedule(
                requestDto.getTitle(),
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword()
        );

        // DB 저장 후 생성된 ID 및 Auditing 시간 정보가 포함된 엔티티 획득
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 비밀번호가 제외된 응답 DTO로 변환하여 반환
        return ScheduleResponseDto.from(savedSchedule);
    }

    /**
     * 전체 일정 또는 특정 작성자의 일정을 수정일 기준 내림차순으로 조회한다.
     *
     * @param author 선택적 작성자명 필터 (null 또는 공백이면 전체 조회)
     * @return 비밀번호를 제외한 일정 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(String author) {
        // 작성자명이 없거나 공백이면 전체 일정을, 값이 있으면 해당 작성자의 일정만 조회
        List<Schedule> schedules = (author == null || author.isBlank())
                ? scheduleRepository.findAllByOrderByModifiedAtDesc()
                : scheduleRepository.findAllByAuthorOrderByModifiedAtDesc(author);

        return schedules.stream()
                .map(ScheduleResponseDto::from)
                .toList();
    }

    /**
     * 지정한 ID의 단건 일정을 조회한다.
     *
     * @param id 조회할 일정 ID
     * @return 비밀번호를 제외한 단건 일정 응답 DTO
     */
    @Transactional(readOnly = true)
    public ScheduleResponseDto getSchedule(Long id) {
        // 존재하지 않는 ID 조회 시 커스텀 예외를 발생시켜 전역 예외 처리기에서 404 응답 처리
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));

        return ScheduleResponseDto.from(schedule);
    }
}
