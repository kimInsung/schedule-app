package com.example.schedule.service;

import com.example.schedule.common.exception.CommentLimitExceededException;
import com.example.schedule.common.exception.ScheduleNotFoundException;
import com.example.schedule.common.validation.InputValidator;
import com.example.schedule.dto.CommentCreateRequestDto;
import com.example.schedule.dto.CommentResponseDto;
import com.example.schedule.entity.Comment;
import com.example.schedule.repository.CommentRepository;
import com.example.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 Service 계층.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final long MAX_COMMENTS_PER_SCHEDULE = 10L;
    private static final int MAX_CONTENTS_LENGTH = 100;

    private final CommentRepository commentRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 지정한 일정에 새로운 댓글을 생성하여 저장한 뒤 응답 DTO를 반환한다.
     *
     * @param scheduleId 댓글을 등록할 일정 ID (논리적 외래키)
     * @param requestDto 댓글 생성 요청 정보 (contents, author, password)
     * @return 비밀번호를 제외한 생성된 댓글 응답 DTO
     */
    @Transactional
    public CommentResponseDto createComment(Long scheduleId, CommentCreateRequestDto requestDto) {
        // DB 조회 전에 요청 객체와 필수 입력값 및 내용 최대 길이(100자)를 먼저 검증
        validateCreateRequest(requestDto);

        // 1. 댓글을 달 대상 일정이 존재하는지 확인하고, 없으면 404 예외 발생
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ScheduleNotFoundException(scheduleId);
        }

        // 2. 이미 10개 이상의 댓글이 등록된 경우 신규 생성 시 한도를 초과하므로 400 예외 발생 (경계값 >= 10)
        if (commentRepository.countByScheduleId(scheduleId) >= MAX_COMMENTS_PER_SCHEDULE) {
            throw new CommentLimitExceededException(scheduleId);
        }

        // 3. 논리적 외래키(scheduleId)를 포함한 댓글 엔티티 생성 및 저장
        Comment comment = new Comment(
                requestDto.getContents(),
                requestDto.getAuthor(),
                requestDto.getPassword(),
                scheduleId
        );
        Comment savedComment = commentRepository.save(comment);

        // 비밀번호가 제외된 응답 DTO로 변환하여 반환
        return CommentResponseDto.from(savedComment);
    }

    // 댓글 생성 요청의 null, 공백 및 내용 최대 길이(100자) 검증
    private void validateCreateRequest(CommentCreateRequestDto requestDto) {
        InputValidator.validateRequestNotNull(requestDto);
        InputValidator.validateRequiredWithMaxLength(requestDto.getContents(), "내용", MAX_CONTENTS_LENGTH);
        InputValidator.validateRequired(requestDto.getAuthor(), "작성자");
        InputValidator.validateRequired(requestDto.getPassword(), "비밀번호");
    }
}
