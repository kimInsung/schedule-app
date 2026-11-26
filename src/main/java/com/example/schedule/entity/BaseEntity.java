package com.example.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 생성일시(createdAt)와 수정일시(modifiedAt)를 자동 관리하는 공통 상위 엔티티.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // 엔티티 최초 저장 시 생성일시 자동 기록 (수정 불가)
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // 엔티티 저장 및 변경 시 수정일시 자동 갱신
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;
}
