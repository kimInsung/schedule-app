package com.example.schedule.dto;

import com.example.schedule.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 일정 단건 상세 조회 전용 응답 DTO.
 * 목록/생성/수정용 ScheduleResponseDto와 분리하여 단건 조회 시에만 댓글 목록(comments)을 포함하며,
 * 보안상 비밀번호(password) 필드는 응답에서 완전히 제외한다.
 */
@Getter
public class ScheduleDetailResponseDto {

    private final Long id;
    private final String title;
    private final String contents;
    private final String author;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final List<CommentResponseDto> comments;

    public ScheduleDetailResponseDto(
            Long id,
            String title,
            String contents,
            String author,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            List<CommentResponseDto> comments
    ) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.author = author;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        // 댓글이 없는 경우에도 null이 아닌 빈 배열([])로 반환되도록 보장
        this.comments = (comments == null) ? List.of() : List.copyOf(comments);
    }

    // 일정 엔티티와 댓글 응답 DTO 목록을 조합하여 단건 상세 응답 DTO 생성
    public static ScheduleDetailResponseDto from(Schedule schedule, List<CommentResponseDto> comments) {
        return new ScheduleDetailResponseDto(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getContents(),
                schedule.getAuthor(),
                schedule.getCreatedAt(),
                schedule.getModifiedAt(),
                comments
        );
    }
}
