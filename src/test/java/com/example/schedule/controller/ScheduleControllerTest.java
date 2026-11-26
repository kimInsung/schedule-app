package com.example.schedule.controller;

import com.example.schedule.common.exception.InvalidInputException;
import com.example.schedule.common.exception.InvalidPasswordException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleDeleteRequestDto;
import com.example.schedule.dto.ScheduleDetailResponseDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.dto.ScheduleUpdateRequestDto;
import com.example.schedule.service.ScheduleService;
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
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    @DisplayName("POST /api/schedules 요청 시 201 Created와 비밀번호가 제외된 응답을 반환한다")
    void createSchedule_returnsCreatedWithoutPassword() throws Exception {
        // given
        ScheduleCreateRequestDto requestDto = new ScheduleCreateRequestDto(
                "스프링 과제",
                "Lv 1 일정 생성 API 구현",
                "김인성",
                "secret1234"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleResponseDto responseDto = new ScheduleResponseDto(
                1L,
                "스프링 과제",
                "Lv 1 일정 생성 API 구현",
                "김인성",
                now,
                now
        );

        given(scheduleService.createSchedule(any(ScheduleCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("스프링 과제"))
                .andExpect(jsonPath("$.contents").value("Lv 1 일정 생성 API 구현"))
                .andExpect(jsonPath("$.author").value("김인성"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.modifiedAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.comments").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/schedules 요청 시 200 OK와 수정일 내림차순 전체 일정 목록(비밀번호·댓글 제외)을 반환한다")
    void getSchedules_withoutAuthor_returnsOkWithSortedListWithoutPassword() throws Exception {
        // given
        LocalDateTime olderTime = LocalDateTime.of(2026, 11, 25, 9, 0);
        LocalDateTime newerTime = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleResponseDto newerDto = new ScheduleResponseDto(
                2L, "최신 일정", "내용2", "이영희", olderTime, newerTime
        );
        ScheduleResponseDto olderDto = new ScheduleResponseDto(
                1L, "이전 일정", "내용1", "김인성", olderTime, olderTime
        );

        given(scheduleService.getSchedules(isNull())).willReturn(List.of(newerDto, olderDto));

        // when & then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].title").value("최신 일정"))
                .andExpect(jsonPath("$[0].author").value("이영희"))
                .andExpect(jsonPath("$[0].modifiedAt").value("2026-11-26T10:00:00"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].comments").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(1L))
                .andExpect(jsonPath("$[1].title").value("이전 일정"))
                .andExpect(jsonPath("$[1].author").value("김인성"))
                .andExpect(jsonPath("$[1].modifiedAt").value("2026-11-25T09:00:00"))
                .andExpect(jsonPath("$[1].password").doesNotExist())
                .andExpect(jsonPath("$[1].comments").doesNotExist());

        verify(scheduleService).getSchedules(null);
    }

    @Test
    @DisplayName("GET /api/schedules?author=김인성 요청 시 200 OK와 해당 작성자 일정 목록(비밀번호 제외)을 반환한다")
    void getSchedules_withAuthor_returnsOkWithFilteredListWithoutPassword() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleResponseDto filteredDto = new ScheduleResponseDto(
                1L, "김인성 일정", "작성자 필터 테스트", "김인성", now, now
        );

        given(scheduleService.getSchedules("김인성")).willReturn(List.of(filteredDto));

        // when & then
        mockMvc.perform(get("/api/schedules")
                        .param("author", "김인성"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("김인성 일정"))
                .andExpect(jsonPath("$[0].author").value("김인성"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        verify(scheduleService).getSchedules("김인성");
    }

    @Test
    @DisplayName("GET /api/schedules/{id} 요청 시 댓글이 포함된 일정이면 200 OK와 작성일 오름차순 댓글 목록(비밀번호 제외)을 반환한다")
    void getSchedule_withExistingIdAndComments_returnsOkWithCommentsWithoutPassword() throws Exception {
        // given
        LocalDateTime scheduleTime = LocalDateTime.of(2026, 11, 26, 10, 0);
        LocalDateTime firstCommentTime = LocalDateTime.of(2026, 11, 26, 11, 0);
        LocalDateTime secondCommentTime = LocalDateTime.of(2026, 11, 26, 12, 0);
        List<CommentResponseDto> comments = List.of(
                new CommentResponseDto(10L, 1L, "첫 번째 댓글", "이영희", firstCommentTime, firstCommentTime),
                new CommentResponseDto(11L, 1L, "두 번째 댓글", "박철수", secondCommentTime, secondCommentTime)
        );
        ScheduleDetailResponseDto responseDto = new ScheduleDetailResponseDto(
                1L, "단건 일정", "상세 조회 테스트", "김인성", scheduleTime, scheduleTime, comments
        );

        given(scheduleService.getSchedule(1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("단건 일정"))
                .andExpect(jsonPath("$.contents").value("상세 조회 테스트"))
                .andExpect(jsonPath("$.author").value("김인성"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-26T10:00:00"))
                .andExpect(jsonPath("$.modifiedAt").value("2026-11-26T10:00:00"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2))
                .andExpect(jsonPath("$.comments[0].id").value(10L))
                .andExpect(jsonPath("$.comments[0].scheduleId").value(1L))
                .andExpect(jsonPath("$.comments[0].contents").value("첫 번째 댓글"))
                .andExpect(jsonPath("$.comments[0].author").value("이영희"))
                .andExpect(jsonPath("$.comments[0].createdAt").value("2026-11-26T11:00:00"))
                .andExpect(jsonPath("$.comments[0].modifiedAt").value("2026-11-26T11:00:00"))
                .andExpect(jsonPath("$.comments[0].password").doesNotExist())
                .andExpect(jsonPath("$.comments[1].id").value(11L))
                .andExpect(jsonPath("$.comments[1].scheduleId").value(1L))
                .andExpect(jsonPath("$.comments[1].contents").value("두 번째 댓글"))
                .andExpect(jsonPath("$.comments[1].author").value("박철수"))
                .andExpect(jsonPath("$.comments[1].createdAt").value("2026-11-26T12:00:00"))
                .andExpect(jsonPath("$.comments[1].modifiedAt").value("2026-11-26T12:00:00"))
                .andExpect(jsonPath("$.comments[1].password").doesNotExist());

        verify(scheduleService).getSchedule(1L);
    }

    @Test
    @DisplayName("GET /api/schedules/{id} 요청 시 댓글이 0개인 일정이면 200 OK와 빈 배열([]) comments를 반환한다")
    void getSchedule_withExistingIdAndNoComments_returnsOkWithEmptyCommentsArray() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleDetailResponseDto responseDto = new ScheduleDetailResponseDto(
                1L, "댓글 없는 일정", "상세 조회 테스트", "김인성", now, now, List.of()
        );

        given(scheduleService.getSchedule(1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(0));

        verify(scheduleService).getSchedule(1L);
    }

    @Test
    @DisplayName("GET /api/schedules/{id} 요청 시 존재하지 않는 ID면 404 Not Found를 반환한다")
    void getSchedule_withNonExistingId_returnsNotFound() throws Exception {
        // given
        given(scheduleService.getSchedule(999L)).willThrow(new ScheduleNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/schedules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("해당 일정을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("PATCH /api/schedules/{id} 요청 시 수정 성공하면 200 OK와 비밀번호가 제외된 수정 응답을 반환한다")
    void updateSchedule_withValidRequest_returnsOkWithoutPassword() throws Exception {
        // given
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "secret1234"
        );
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 9, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 11, 26, 10, 30);
        ScheduleResponseDto responseDto = new ScheduleResponseDto(
                1L, "수정된 제목", "기존 내용", "이영희", createdAt, modifiedAt
        );

        given(scheduleService.updateSchedule(eq(1L), any(ScheduleUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.contents").value("기존 내용"))
                .andExpect(jsonPath("$.author").value("이영희"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-25T09:00:00"))
                .andExpect(jsonPath("$.modifiedAt").value("2026-11-26T10:30:00"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.comments").doesNotExist());

        verify(scheduleService).updateSchedule(eq(1L), any(ScheduleUpdateRequestDto.class));
    }

    @Test
    @DisplayName("PATCH /api/schedules/{id} 요청 시 비밀번호가 일치하지 않으면 401 Unauthorized를 반환한다")
    void updateSchedule_withInvalidPassword_returnsUnauthorized() throws Exception {
        // given
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "wrong-password"
        );
        given(scheduleService.updateSchedule(eq(1L), any(ScheduleUpdateRequestDto.class)))
                .willThrow(new InvalidPasswordException());

        // when & then
        mockMvc.perform(patch("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("PATCH /api/schedules/{id} 요청 시 존재하지 않는 ID면 404 Not Found를 반환한다")
    void updateSchedule_withNonExistingId_returnsNotFound() throws Exception {
        // given
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                "수정된 제목", "이영희", "secret1234"
        );
        given(scheduleService.updateSchedule(eq(999L), any(ScheduleUpdateRequestDto.class)))
                .willThrow(new ScheduleNotFoundException(999L));

        // when & then
        mockMvc.perform(patch("/api/schedules/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("해당 일정을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("DELETE /api/schedules/{id} 요청 시 삭제 성공하면 204 No Content와 빈 본문을 반환한다")
    void deleteSchedule_withValidPassword_returnsNoContentWithoutBody() throws Exception {
        // given
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("secret1234");
        willDoNothing().given(scheduleService).deleteSchedule(eq(1L), any(ScheduleDeleteRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(scheduleService).deleteSchedule(eq(1L), any(ScheduleDeleteRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /api/schedules/{id} 요청 시 비밀번호가 일치하지 않으면 401 Unauthorized를 반환한다")
    void deleteSchedule_withInvalidPassword_returnsUnauthorized() throws Exception {
        // given
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("wrong-password");
        willThrow(new InvalidPasswordException())
                .given(scheduleService).deleteSchedule(eq(1L), any(ScheduleDeleteRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("DELETE /api/schedules/{id} 요청 시 존재하지 않는 ID면 404 Not Found를 반환한다")
    void deleteSchedule_withNonExistingId_returnsNotFound() throws Exception {
        // given
        ScheduleDeleteRequestDto requestDto = new ScheduleDeleteRequestDto("secret1234");
        willThrow(new ScheduleNotFoundException(999L))
                .given(scheduleService).deleteSchedule(eq(999L), any(ScheduleDeleteRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/schedules/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("해당 일정을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("POST /api/schedules 요청 시 제목 30자, 내용 200자(경계값)면 201 Created를 반환한다")
    void createSchedule_withBoundaryLengths_returnsCreated() throws Exception {
        // given
        String title30 = "a".repeat(30);
        String contents200 = "b".repeat(200);
        ScheduleCreateRequestDto requestDto = new ScheduleCreateRequestDto(
                title30, contents200, "김인성", "secret1234"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleResponseDto responseDto = new ScheduleResponseDto(
                1L, title30, contents200, "김인성", now, now
        );

        given(scheduleService.createSchedule(any(ScheduleCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title30))
                .andExpect(jsonPath("$.contents").value(contents200));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateScheduleRequests")
    @DisplayName("POST /api/schedules 요청 시 길이 초과·null·공백 입력이면 400 Bad Request를 반환한다")
    void createSchedule_withInvalidInput_returnsBadRequest(
            String caseName,
            ScheduleCreateRequestDto requestDto,
            String errorMessage
    ) throws Exception {
        // given
        given(scheduleService.createSchedule(any(ScheduleCreateRequestDto.class)))
                .willThrow(new InvalidInputException(errorMessage));

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("PATCH /api/schedules/{id} 요청 시 제목 30자(경계값)면 200 OK를 반환한다")
    void updateSchedule_withTitleAt30Chars_returnsOk() throws Exception {
        // given
        String title30 = "a".repeat(30);
        ScheduleUpdateRequestDto requestDto = new ScheduleUpdateRequestDto(
                title30, "이영희", "secret1234"
        );
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 10, 0);
        ScheduleResponseDto responseDto = new ScheduleResponseDto(
                1L, title30, "기존 내용", "이영희", now, now
        );

        given(scheduleService.updateSchedule(eq(1L), any(ScheduleUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title30));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateScheduleRequests")
    @DisplayName("PATCH /api/schedules/{id} 요청 시 길이 초과·null·공백 입력이면 400 Bad Request를 반환한다")
    void updateSchedule_withInvalidInput_returnsBadRequest(
            String caseName,
            ScheduleUpdateRequestDto requestDto,
            String errorMessage
    ) throws Exception {
        // given
        given(scheduleService.updateSchedule(eq(1L), any(ScheduleUpdateRequestDto.class)))
                .willThrow(new InvalidInputException(errorMessage));

        // when & then
        mockMvc.perform(patch("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDeleteScheduleRequests")
    @DisplayName("DELETE /api/schedules/{id} 요청 시 비밀번호가 null 또는 공백이면 400 Bad Request를 반환한다")
    void deleteSchedule_withInvalidInput_returnsBadRequest(
            String caseName,
            ScheduleDeleteRequestDto requestDto,
            String errorMessage
    ) throws Exception {
        // given
        willThrow(new InvalidInputException(errorMessage))
                .given(scheduleService).deleteSchedule(eq(1L), any(ScheduleDeleteRequestDto.class));

        // when & then
        mockMvc.perform(delete("/api/schedules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    private static Stream<Arguments> invalidCreateScheduleRequests() {
        return Stream.of(
                Arguments.of("title 31 chars", new ScheduleCreateRequestDto("a".repeat(31), "내용", "김인성", "secret1234"),
                        "제목 입력값은 최대 30자까지 입력할 수 있습니다."),
                Arguments.of("contents 201 chars", new ScheduleCreateRequestDto("제목", "b".repeat(201), "김인성", "secret1234"),
                        "내용 입력값은 최대 200자까지 입력할 수 있습니다."),
                Arguments.of("title is null", new ScheduleCreateRequestDto(null, "내용", "김인성", "secret1234"),
                        "제목 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("title is blank", new ScheduleCreateRequestDto("   ", "내용", "김인성", "secret1234"),
                        "제목 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("contents is null", new ScheduleCreateRequestDto("제목", null, "김인성", "secret1234"),
                        "내용 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("contents is blank", new ScheduleCreateRequestDto("제목", "   ", "김인성", "secret1234"),
                        "내용 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is null", new ScheduleCreateRequestDto("제목", "내용", null, "secret1234"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is blank", new ScheduleCreateRequestDto("제목", "내용", "   ", "secret1234"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is null", new ScheduleCreateRequestDto("제목", "내용", "김인성", null),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is blank", new ScheduleCreateRequestDto("제목", "내용", "김인성", "   "),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다.")
        );
    }

    private static Stream<Arguments> invalidUpdateScheduleRequests() {
        return Stream.of(
                Arguments.of("title 31 chars", new ScheduleUpdateRequestDto("a".repeat(31), "이영희", "secret1234"),
                        "제목 입력값은 최대 30자까지 입력할 수 있습니다."),
                Arguments.of("title is null", new ScheduleUpdateRequestDto(null, "이영희", "secret1234"),
                        "제목 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("title is blank", new ScheduleUpdateRequestDto("   ", "이영희", "secret1234"),
                        "제목 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is null", new ScheduleUpdateRequestDto("수정 제목", null, "secret1234"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("author is blank", new ScheduleUpdateRequestDto("수정 제목", "   ", "secret1234"),
                        "작성자 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is null", new ScheduleUpdateRequestDto("수정 제목", "이영희", null),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is blank", new ScheduleUpdateRequestDto("수정 제목", "이영희", "   "),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다.")
        );
    }

    private static Stream<Arguments> invalidDeleteScheduleRequests() {
        return Stream.of(
                Arguments.of("password is null", new ScheduleDeleteRequestDto(null),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다."),
                Arguments.of("password is blank", new ScheduleDeleteRequestDto("   "),
                        "비밀번호 입력값은 비어 있거나 공백일 수 없습니다.")
        );
    }
}
