package com.example.schedule.service;

import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.entity.Schedule;
import com.example.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("일정 생성 시 엔티티를 저장하고 생성된 일정 정보를 반환한다")
    void createSchedule_savesScheduleAndReturnsResponse() {
        // given
        ScheduleCreateRequestDto requestDto = new ScheduleCreateRequestDto(
                "스프링 과제",
                "Lv 1 일정 생성 API 구현",
                "김인성",
                "secret1234"
        );

        Schedule savedSchedule = new Schedule(
                requestDto.getTitle(),
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword()
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ReflectionTestUtils.setField(savedSchedule, "id", 1L);
        ReflectionTestUtils.setField(savedSchedule, "createdAt", now);
        ReflectionTestUtils.setField(savedSchedule, "modifiedAt", now);

        given(scheduleRepository.save(any(Schedule.class))).willReturn(savedSchedule);

        // when
        ScheduleResponseDto responseDto = scheduleService.createSchedule(requestDto);

        // then
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(captor.capture());

        Schedule captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("스프링 과제");
        assertThat(captured.getContents()).isEqualTo("Lv 1 일정 생성 API 구현");
        assertThat(captured.getAuthor()).isEqualTo("김인성");
        assertThat(captured.getPassword()).isEqualTo("secret1234");

        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getTitle()).isEqualTo("스프링 과제");
        assertThat(responseDto.getContents()).isEqualTo("Lv 1 일정 생성 API 구현");
        assertThat(responseDto.getAuthor()).isEqualTo("김인성");
        assertThat(responseDto.getCreatedAt()).isEqualTo(now);
        assertThat(responseDto.getModifiedAt()).isEqualTo(now);
    }
}
