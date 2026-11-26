package com.example.schedule.service;

import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.entity.Schedule;
import com.example.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
