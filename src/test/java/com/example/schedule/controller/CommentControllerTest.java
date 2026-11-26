package com.example.schedule.controller;

import com.example.schedule.common.exception.CommentLimitExceededException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentCreateRequestDto;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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
}
