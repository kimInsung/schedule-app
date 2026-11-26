package com.example.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일정에 달린 댓글 정보를 저장하는 JPA 엔티티.
 * BaseEntity를 상속받아 생성일시(createdAt)와 수정일시(modifiedAt)를 JPA Auditing으로 자동 관리한다.
 */
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String password;

    // JPA 연관관계 어노테이션 없이 일정 ID를 일반 컬럼으로 보관하는 논리적 외래키
    @Column(nullable = false)
    private Long scheduleId;

    // 필수 입력값과 대상 일정 ID를 받아 일관된 상태로 댓글을 생성
    public Comment(String contents, String author, String password, Long scheduleId) {
        this.contents = contents;
        this.author = author;
        this.password = password;
        this.scheduleId = scheduleId;
    }
}
