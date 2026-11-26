package com.example.schedule.controller;

import com.example.schedule.dto.ScheduleCreateRequestDto;
import com.example.schedule.dto.ScheduleResponseDto;
import com.example.schedule.service.ScheduleService;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
