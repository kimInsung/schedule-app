package com.example.schedule.controller;

import com.example.schedule.dto.CommentCreateRequestDto;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글 HTTP 요청을 처리하는 Controller 계층.
 */
@RestController
@RequestMapping("/api/schedules/{scheduleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 지정한 일정에 새로운 댓글을 생성한다.
     *
     * @param scheduleId 댓글을 등록할 대상 일정 ID
     * @param requestDto 댓글 생성 요청 본문 (contents, author, password)
     * @return 생성된 댓글 정보와 HTTP 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long scheduleId,
            @RequestBody CommentCreateRequestDto requestDto
    ) {
        // 일정 존재 여부, 댓글 수 제한 검증 및 저장은 서비스 계층에 위임하고 DTO만 201 Created로 반환
        CommentResponseDto responseDto = commentService.createComment(scheduleId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
