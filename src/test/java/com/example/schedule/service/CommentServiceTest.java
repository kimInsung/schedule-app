package com.example.schedule.service;

import com.example.schedule.common.exception.CommentLimitExceededException;
import com.example.schedule.common.exception.InvalidInputException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentCreateRequestDto;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.entity.Comment;
import com.example.schedule.repository.CommentRepository;
import com.example.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("일정이 존재하고 댓글이 9개 이하이면 댓글을 생성하여 저장하고 응답 DTO를 반환한다")
    void createComment_withValidScheduleAndUnderLimit_savesCommentAndReturnsResponse() {
        // given
        Long scheduleId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "일정 확인했습니다.",
                "이영희",
                "commentPw123"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 15, 0);
        Comment savedComment = createCommentEntity(
                10L,
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword(),
                scheduleId,
                now,
                now
        );

        given(scheduleRepository.existsById(scheduleId)).willReturn(true);
        given(commentRepository.countByScheduleId(scheduleId)).willReturn(9L);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponseDto responseDto = commentService.createComment(scheduleId, requestDto);

        // then
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(scheduleRepository).existsById(scheduleId);
        verify(commentRepository).countByScheduleId(scheduleId);
        verify(commentRepository).save(captor.capture());

        Comment captured = captor.getValue();
        assertThat(captured.getContents()).isEqualTo("일정 확인했습니다.");
        assertThat(captured.getAuthor()).isEqualTo("이영희");
        assertThat(captured.getPassword()).isEqualTo("commentPw123");
        assertThat(captured.getScheduleId()).isEqualTo(scheduleId);

        assertThat(responseDto.getId()).isEqualTo(10L);
        assertThat(responseDto.getScheduleId()).isEqualTo(scheduleId);
        assertThat(responseDto.getContents()).isEqualTo("일정 확인했습니다.");
        assertThat(responseDto.getAuthor()).isEqualTo("이영희");
        assertThat(responseDto.getCreatedAt()).isEqualTo(now);
        assertThat(responseDto.getModifiedAt()).isEqualTo(now);
        assertThat(responseDto.getCreatedAt()).isEqualTo(responseDto.getModifiedAt());
    }

    @Test
    @DisplayName("존재하지 않는 일정 ID로 댓글 생성 시 ScheduleNotFoundException이 발생한다")
    void createComment_withNonExistingSchedule_throwsScheduleNotFoundException() {
        // given
        Long nonExistingScheduleId = 999L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "일정 확인했습니다.",
                "이영희",
                "commentPw123"
        );

        given(scheduleRepository.existsById(nonExistingScheduleId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(nonExistingScheduleId, requestDto))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessageContaining("999");

        verify(commentRepository, never()).countByScheduleId(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("해당 일정의 댓글이 이미 10개(경계값)이면 CommentLimitExceededException이 발생한다")
    void createComment_whenCommentCountIsTen_throwsCommentLimitExceededException() {
        // given
        Long scheduleId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "11번째 댓글 시도",
                "이영희",
                "commentPw123"
        );

        given(scheduleRepository.existsById(scheduleId)).willReturn(true);
        given(commentRepository.countByScheduleId(scheduleId)).willReturn(10L);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(scheduleId, requestDto))
                .isInstanceOf(CommentLimitExceededException.class)
                .hasMessageContaining("10");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("해당 일정의 댓글이 10개를 초과해도 CommentLimitExceededException이 발생한다")
    void createComment_whenCommentCountExceedsTen_throwsCommentLimitExceededException() {
        // given
        Long scheduleId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "초과 댓글 시도",
                "이영희",
                "commentPw123"
        );

        given(scheduleRepository.existsById(scheduleId)).willReturn(true);
        given(commentRepository.countByScheduleId(scheduleId)).willReturn(11L);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(scheduleId, requestDto))
                .isInstanceOf(CommentLimitExceededException.class)
                .hasMessageContaining("10");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 시 요청 객체가 null이면 DB 조회 없이 InvalidInputException이 발생한다")
    void createComment_withNullRequest_throwsInvalidInputException() {
        assertThatThrownBy(() -> commentService.createComment(1L, null))
                .isInstanceOf(InvalidInputException.class);

        verify(scheduleRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).countByScheduleId(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 시 내용이 100자(최대 길이 경계값)면 정상 저장된다")
    void createComment_withContentsAt100Chars_savesComment() {
        // given
        Long scheduleId = 1L;
        String contents100 = "c".repeat(100);
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                contents100, "이영희", "commentPw123"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 15, 0);
        Comment savedComment = createCommentEntity(
                10L, contents100, "이영희", "commentPw123", scheduleId, now, now
        );

        given(scheduleRepository.existsById(scheduleId)).willReturn(true);
        given(commentRepository.countByScheduleId(scheduleId)).willReturn(0L);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponseDto responseDto = commentService.createComment(scheduleId, requestDto);

        // then
        verify(commentRepository).save(any(Comment.class));
        assertThat(responseDto.getContents()).hasSize(100);
    }

    @Test
    @DisplayName("댓글 생성 시 내용이 101자(최대 길이 초과)면 DB 조회 없이 InvalidInputException이 발생한다")
    void createComment_withContentsExceeding100Chars_throwsInvalidInputException() {
        // given
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "c".repeat(101), "이영희", "commentPw123"
        );

        // when & then
        assertThatThrownBy(() -> commentService.createComment(1L, requestDto))
                .isInstanceOf(InvalidInputException.class);

        verify(scheduleRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).countByScheduleId(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateCommentRequests")
    @DisplayName("댓글 생성 시 필수 입력값이 null 또는 공백이면 DB 조회 없이 InvalidInputException이 발생한다")
    void createComment_withNullOrBlankRequiredFields_throwsInvalidInputException(
            String caseName,
            CommentCreateRequestDto requestDto
    ) {
        assertThatThrownBy(() -> commentService.createComment(1L, requestDto))
                .isInstanceOf(InvalidInputException.class);

        verify(scheduleRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).countByScheduleId(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    private static Stream<Arguments> invalidCreateCommentRequests() {
        return Stream.of(
                Arguments.of("contents is null", new CommentCreateRequestDto(null, "이영희", "commentPw123")),
                Arguments.of("contents is blank", new CommentCreateRequestDto("   ", "이영희", "commentPw123")),
                Arguments.of("author is null", new CommentCreateRequestDto("댓글 내용", null, "commentPw123")),
                Arguments.of("author is blank", new CommentCreateRequestDto("댓글 내용", "   ", "commentPw123")),
                Arguments.of("password is null", new CommentCreateRequestDto("댓글 내용", "이영희", null)),
                Arguments.of("password is blank", new CommentCreateRequestDto("댓글 내용", "이영희", "   "))
        );
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
}
