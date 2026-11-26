package com.example.schedule.service;

import com.example.schedule.common.exception.ScheduleNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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

        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule savedSchedule = createScheduleEntity(
                1L,
                requestDto.getTitle(),
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword(),
                now,
                now
        );

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

    @Test
    @DisplayName("author가 null이면 전체 일정을 수정일 내림차순으로 조회한다")
    void getSchedules_withoutAuthor_returnsAllOrderedByModifiedAtDesc() {
        // given
        LocalDateTime olderTime = LocalDateTime.of(2026, 11, 25, 9, 0);
        LocalDateTime newerTime = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule newerSchedule = createScheduleEntity(2L, "최신 일정", "내용2", "이영희", "pw2", olderTime, newerTime);
        Schedule olderSchedule = createScheduleEntity(1L, "이전 일정", "내용1", "김인성", "pw1", olderTime, olderTime);

        given(scheduleRepository.findAllByOrderByModifiedAtDesc())
                .willReturn(List.of(newerSchedule, olderSchedule));

        // when
        List<ScheduleResponseDto> result = scheduleService.getSchedules(null);

        // then
        verify(scheduleRepository).findAllByOrderByModifiedAtDesc();
        verify(scheduleRepository, never()).findAllByAuthorOrderByModifiedAtDesc(anyString());
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ScheduleResponseDto::getId).containsExactly(2L, 1L);
        assertThat(result).extracting(ScheduleResponseDto::getModifiedAt).containsExactly(newerTime, olderTime);
    }

    @Test
    @DisplayName("author가 공백(blank)이면 전체 일정을 수정일 내림차순으로 조회한다")
    void getSchedules_withBlankAuthor_returnsAllOrderedByModifiedAtDesc() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(1L, "일정", "내용", "김인성", "pw1", now, now);

        given(scheduleRepository.findAllByOrderByModifiedAtDesc())
                .willReturn(List.of(schedule));

        // when
        List<ScheduleResponseDto> result = scheduleService.getSchedules("   ");

        // then
        verify(scheduleRepository).findAllByOrderByModifiedAtDesc();
        verify(scheduleRepository, never()).findAllByAuthorOrderByModifiedAtDesc(anyString());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("author가 주어지면 해당 작성자의 일정만 수정일 내림차순으로 조회한다")
    void getSchedules_withAuthor_returnsFilteredOrderedByModifiedAtDesc() {
        // given
        LocalDateTime firstTime = LocalDateTime.of(2026, 11, 25, 11, 0);
        LocalDateTime secondTime = LocalDateTime.of(2026, 11, 26, 12, 0);
        Schedule secondSchedule = createScheduleEntity(3L, "두 번째 일정", "내용2", "김인성", "pw2", firstTime, secondTime);
        Schedule firstSchedule = createScheduleEntity(1L, "첫 번째 일정", "내용1", "김인성", "pw1", firstTime, firstTime);

        given(scheduleRepository.findAllByAuthorOrderByModifiedAtDesc("김인성"))
                .willReturn(List.of(secondSchedule, firstSchedule));

        // when
        List<ScheduleResponseDto> result = scheduleService.getSchedules("김인성");

        // then
        verify(scheduleRepository).findAllByAuthorOrderByModifiedAtDesc("김인성");
        verify(scheduleRepository, never()).findAllByOrderByModifiedAtDesc();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ScheduleResponseDto::getAuthor).containsOnly("김인성");
        assertThat(result).extracting(ScheduleResponseDto::getId).containsExactly(3L, 1L);
    }

    @Test
    @DisplayName("존재하는 ID로 단건 조회 시 해당 일정의 응답 DTO를 반환한다")
    void getSchedule_withExistingId_returnsResponse() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(1L, "단건 일정", "상세 내용", "김인성", "secret1234", now, now);

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when
        ScheduleResponseDto result = scheduleService.getSchedule(1L);

        // then
        verify(scheduleRepository).findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("단건 일정");
        assertThat(result.getContents()).isEqualTo("상세 내용");
        assertThat(result.getAuthor()).isEqualTo("김인성");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getModifiedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 단건 조회 시 ScheduleNotFoundException이 발생한다")
    void getSchedule_withNonExistingId_throwsScheduleNotFoundException() {
        // given
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedule(999L))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("999");
    }

    private Schedule createScheduleEntity(
            Long id,
            String title,
            String contents,
            String author,
            String password,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        Schedule schedule = new Schedule(title, contents, author, password);
        ReflectionTestUtils.setField(schedule, "id", id);
        ReflectionTestUtils.setField(schedule, "createdAt", createdAt);
        ReflectionTestUtils.setField(schedule, "modifiedAt", modifiedAt);
        return schedule;
    }
}
