package com.example.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 생성 요청 데이터를 전달하는 DTO.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {

    private String contents;
    private String author;
    private String password;
}
