package com.example.schedule.dto;

import com.example.schedule.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 댓글 정보를 클라이언트에 반환하는 응답 DTO.
 * 보안상 비밀번호(password) 필드는 응답에서 완전히 제외한다.
 */
@Getter
public class CommentResponseDto {

    private final Long id;
    private final Long scheduleId;
    private final String contents;
    private final String author;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public CommentResponseDto(
            Long id,
            Long scheduleId,
            String contents,
            String author,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.contents = contents;
        this.author = author;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    // 엔티티에서 민감 정보(password)를 제외하고 응답 DTO로 변환
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getScheduleId(),
                comment.getContents(),
                comment.getAuthor(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
