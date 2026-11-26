package com.example.schedule.repository;

import com.example.schedule.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 댓글 엔티티의 영속성 처리를 담당하는 Repository 인터페이스.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 일정에 등록된 댓글 수를 조회하여 최대 개수 제한 검증에 활용
    long countByScheduleId(Long scheduleId);
}
