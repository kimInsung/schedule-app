package com.example.schedule.controller;

import com.example.schedule.common.exception.CommentLimitExceededException;
import com.example.schedule.common.exception.InvalidInputException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentCreateRequestDto;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("POST /api/schedules/{scheduleId}/comments 요청 시 201 Created와 비밀번호가 제외된 응답을 반환한다")
    void createComment_withValidRequest_returnsCreatedWithoutPassword() throws Exception {
        // given
        Long scheduleId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "댓글 내용입니다.",
                "이영희",
                "commentPw123"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 15, 0);
        CommentResponseDto responseDto = new CommentResponseDto(
                10L,
                scheduleId,
                "댓글 내용입니다.",
                "이영희",
                now,
                now
        );

        given(commentService.createComment(eq(scheduleId), any(CommentCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/schedules/{scheduleId}/comments", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.scheduleId").value(1L))
                .andExpect(jsonPath("$.contents").value("댓글 내용입니다."))
                .andExpect(jsonPath("$.author").value("이영희"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-26T15:00:00"))
                .andExpect(jsonPath("$.modifiedAt").value("2026-11-26T15:00:00"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(commentService).createComment(eq(scheduleId), any(CommentCreateRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/schedules/{scheduleId}/comments 요청 시 존재하지 않는 일정 ID면 404 Not Found를 반환한다")
    void createComment_withNonExistingSchedule_returnsNotFound() throws Exception {
        // given
        Long nonExistingScheduleId = 999L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "댓글 내용입니다.",
                "이영희",
                "commentPw123"
        );

        given(commentService.createComment(eq(nonExistingScheduleId), any(CommentCreateRequestDto.class)))
                .willThrow(new ScheduleNotFoundException(nonExistingScheduleId));

        // when & then
        mockMvc.perform(post("/api/schedules/{scheduleId}/comments", nonExistingScheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("해당 일정을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("POST /api/schedules/{scheduleId}/comments 요청 시 댓글이 10개 이상이면 400 Bad Request를 반환한다")
    void createComment_whenCommentLimitExceeded_returnsBadRequest() throws Exception {
        // given
        Long scheduleId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                "11번째 댓글 시도",
                "이영희",
                "commentPw123"
        );

        given(commentService.createComment(eq(scheduleId), any(CommentCreateRequestDto.class)))
                .willThrow(new CommentLimitExceededException(scheduleId));

        // when & then
        mockMvc.perform(post("/api/schedules/{scheduleId}/comments", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("하나의 일정에는 댓글을 최대 10개까지만 작성할 수 있습니다. scheduleId=1"));
    }

    @Test
    @DisplayName("POST /api/schedules/{scheduleId}/comments 요청 시 내용 100자(경계값)면 201 Created를 반환한다")
    void createComment_withContentsAt100Chars_returnsCreated() throws Exception {
        // given
        Long scheduleId = 1L;
        String contents100 = "c".repeat(100);
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
                contents100, "이영희", "commentPw123"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 15, 0);
        CommentResponseDto responseDto = new CommentResponseDto(
                10L, scheduleId, contents100, "이영희", now, now
        );

        given(commentService.createComment(eq(scheduleId), any(CommentCreateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/schedules/{scheduleId}/comments", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contents").value(contents100));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateCommentRequests")
    @DisplayName("POST /api/schedules/{scheduleId}/comments 요청 시 길이 초과·null·공백 입력이면 400 Bad Request를 반환한다")
    void createComment_withInvalidInput_returnsBadRequest(
            String caseName,
            CommentCreateRequestDto requestDto,
            String errorMessage
    ) throws Exception {
        // given
        Long scheduleId = 1L;
        given(commentService.createComment(eq(scheduleId), any(CommentCreateRequestDto.class)))
                .willThrow(new InvalidInputException(errorMessage));

        // when & then
        mockMvc.perform(post("/api/schedules/{scheduleId}/comments", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    private static Stream<Arguments> invalidCreateCommentRequests() {
        return Stream.of(
                Arguments.of("contents 101 chars", new CommentCreateRequestDto("c".repeat(101), "이영희", "commentPw123"),
                        "내용 입력값은 최대 100자까지 입력할 수 있습니다."),
                Arguments.of("contents is null", new CommentCreateRequestDto(null, "이영희", "commentPw123"),
                        "내용 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("contents is blank", new CommentCreateRequestDto("   ", "이영희", "commentPw123"),
                        "내용 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is null", new CommentCreateRequestDto("댓글 내용", null, "commentPw123"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is blank", new CommentCreateRequestDto("댓글 내용", "   ", "commentPw123"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is null", new CommentCreateRequestDto("댓글 내용", "이영희", null),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is blank", new CommentCreateRequestDto("댓글 내용", "이영희", "   "),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다.")
        );
    }
}
