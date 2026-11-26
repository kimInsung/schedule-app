package com.example.schedule.service;

import com.example.schedule.common.exception.InvalidPasswordException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleDeleteRequestDto;
import com.example.schedule.dto.ScheduleDetailResponseDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.dto.ScheduleUpdateRequestDto;
import com.example.schedule.entity.Comment;
import com.example.schedule.entity.Schedule;
import com.example.schedule.repository.CommentRepository;
import com.example.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private CommentRepository commentRepository;

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
    @DisplayName("존재하는 ID로 단건 조회 시 댓글 목록이 작성일 오름차순으로 포함된 상세 응답 DTO를 반환한다")
    void getSchedule_withExistingIdAndComments_returnsDetailResponseWithComments() {
        // given
        LocalDateTime scheduleTime = LocalDateTime.of(2026, 11, 26, 10, 0);
        LocalDateTime firstCommentTime = LocalDateTime.of(2026, 11, 26, 11, 0);
        LocalDateTime secondCommentTime = LocalDateTime.of(2026, 11, 26, 12, 0);
        Schedule schedule = createScheduleEntity(1L, "단건 일정", "상세 내용", "김인성", "secret1234", scheduleTime, scheduleTime);
        Comment firstComment = createCommentEntity(10L, "첫 번째 댓글", "이영희", "cpw1", 1L, firstCommentTime, firstCommentTime);
        Comment secondComment = createCommentEntity(11L, "두 번째 댓글", "박철수", "cpw2", 1L, secondCommentTime, secondCommentTime);

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));
        given(commentRepository.findAllByScheduleIdOrderByCreatedAtAsc(1L))
                .willReturn(List.of(firstComment, secondComment));

        // when
        ScheduleDetailResponseDto result = scheduleService.getSchedule(1L);

        // then
        verify(scheduleRepository).findById(1L);
        verify(commentRepository).findAllByScheduleIdOrderByCreatedAtAsc(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("단건 일정");
        assertThat(result.getContents()).isEqualTo("상세 내용");
        assertThat(result.getAuthor()).isEqualTo("김인성");
        assertThat(result.getCreatedAt()).isEqualTo(scheduleTime);
        assertThat(result.getModifiedAt()).isEqualTo(scheduleTime);

        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments()).extracting(CommentResponseDto::getId).containsExactly(10L, 11L);
        assertThat(result.getComments()).extracting(CommentResponseDto::getContents)
                .containsExactly("첫 번째 댓글", "두 번째 댓글");
        assertThat(result.getComments()).extracting(CommentResponseDto::getCreatedAt)
                .containsExactly(firstCommentTime, secondCommentTime);
    }

    @Test
    @DisplayName("댓글이 0개인 일정 단건 조회 시 comments는 null이 아닌 빈 리스트를 반환한다")
    void getSchedule_withExistingIdAndNoComments_returnsDetailResponseWithEmptyComments() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(1L, "댓글 없는 일정", "상세 내용", "김인성", "secret1234", now, now);

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));
        given(commentRepository.findAllByScheduleIdOrderByCreatedAtAsc(1L)).willReturn(List.of());

        // when
        ScheduleDetailResponseDto result = scheduleService.getSchedule(1L);

        // then
        verify(scheduleRepository).findById(1L);
        verify(commentRepository).findAllByScheduleIdOrderByCreatedAtAsc(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getComments()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 단건 조회 시 ScheduleNotFoundException이 발생하고 댓글을 조회하지 않는다")
    void getSchedule_withNonExistingId_throwsScheduleNotFoundException() {
        // given
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getSchedule(999L))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("999");

        verify(commentRepository, never()).findAllByScheduleIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    @DisplayName("일정 수정 성공 시 title과 author만 변경하고 contents와 createdAt은 유지하며 flush 후 modifiedAt을 반영한다")
    void updateSchedule_withValidPassword_updatesTitleAndAuthorAndFlushes() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 9, 0);
        LocalDateTime updatedModifiedAt = LocalDateTime.of(2026, 11, 26, 10, 30);
        Schedule schedule = createScheduleEntity(
                1L, "기존 제목", "기존 내용", "김인성", "secret1234", createdAt, createdAt
        );
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "secret1234"
        );

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));
        // flush 호출 시 JPA Auditing이 modifiedAt을 갱신하는 동작을 시뮬레이션
        willAnswer(invocation -> {
            ReflectionTestUtils.setField(schedule, "modifiedAt", updatedModifiedAt);
            return null;
        }).given(scheduleRepository).flush();

        // when
        ScheduleResponseDto result = scheduleService.updateSchedule(1L, requestDto);

        // then
        verify(scheduleRepository).findById(1L);
        verify(scheduleRepository).flush();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getAuthor()).isEqualTo("이영희");
        assertThat(result.getContents()).isEqualTo("기존 내용");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getModifiedAt()).isEqualTo(updatedModifiedAt);
    }

    @Test
    @DisplayName("일정 수정 시 비밀번호가 일치하지 않으면 InvalidPasswordException이 발생한다")
    void updateSchedule_withInvalidPassword_throwsInvalidPasswordException() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(
                1L, "기존 제목", "기존 내용", "김인성", "secret1234", now, now
        );
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "wrong-password"
        );

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(1L, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(scheduleRepository, never()).flush();
        assertThat(schedule.getTitle()).isEqualTo("기존 제목");
        assertThat(schedule.getAuthor()).isEqualTo("김인성");
    }

    @Test
    @DisplayName("일정 수정 시 요청 비밀번호가 null이면 InvalidPasswordException이 발생한다")
    void updateSchedule_withNullPassword_throwsInvalidPasswordException() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(
                1L, "기존 제목", "기존 내용", "김인성", "secret1234", now, now
        );
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", null
        );

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(1L, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(scheduleRepository, never()).flush();
        assertThat(schedule.getTitle()).isEqualTo("기존 제목");
        assertThat(schedule.getAuthor()).isEqualTo("김인성");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 일정 수정 시 ScheduleNotFoundException이 발생한다")
    void updateSchedule_withNonExistingId_throwsScheduleNotFoundException() {
        // given
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "secret1234"
        );
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(999L, requestDto))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("999");

        verify(scheduleRepository, never()).flush();
    }

    @Test
    @DisplayName("일정 삭제 성공 시 비밀번호 검증 후 연관 댓글 일괄 삭제 및 일정 삭제를 순서대로 호출한다")
    void deleteSchedule_withValidPassword_deletesCommentsAndSchedule() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(
                1L, "삭제 대상 일정", "내용", "김인성", "secret1234", now, now
        );
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("secret1234");

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when
        scheduleService.deleteSchedule(1L, requestDto);

        // then
        InOrder inOrder = inOrder(scheduleRepository, commentRepository);
        inOrder.verify(scheduleRepository).findById(1L);
        inOrder.verify(commentRepository).deleteAllByScheduleId(1L);
        inOrder.verify(scheduleRepository).delete(schedule);
    }

    @Test
    @DisplayName("일정 삭제 시 비밀번호가 일치하지 않으면 InvalidPasswordException이 발생하고 댓글 및 일정을 삭제하지 않는다")
    void deleteSchedule_withInvalidPassword_throwsInvalidPasswordException() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(
                1L, "삭제 대상 일정", "내용", "김인성", "secret1234", now, now
        );
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("wrong-password");

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(1L, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(commentRepository, never()).deleteAllByScheduleId(anyLong());
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    @DisplayName("일정 삭제 시 요청 비밀번호가 null이면 InvalidPasswordException이 발생하고 댓글 및 일정을 삭제하지 않는다")
    void deleteSchedule_withNullPassword_throwsInvalidPasswordException() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        Schedule schedule = createScheduleEntity(
                1L, "삭제 대상 일정", "내용", "김인성", "secret1234", now, now
        );
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto(null);

        given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(1L, requestDto))
                .isInstanceOf(InvalidPasswordException.class);

        verify(commentRepository, never()).deleteAllByScheduleId(anyLong());
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    @DisplayName("존재하지 않는 ID로 일정 삭제 시 ScheduleNotFoundException이 발생하고 댓글 및 일정을 삭제하지 않는다")
    void deleteSchedule_withNonExistingId_throwsScheduleNotFoundException() {
        // given
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("secret1234");
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(999L, requestDto))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("999");

        verify(commentRepository, never()).deleteAllByScheduleId(anyLong());
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    private Comment createCommentEntity(
            Long id,
            String contents,
            String author,
            String password,
            Long scheduleId,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        Comment comment = new Comment(contents, author, password, scheduleId);
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        ReflectionTestUtils.setField(comment, "modifiedAt", modifiedAt);
        return comment;
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
