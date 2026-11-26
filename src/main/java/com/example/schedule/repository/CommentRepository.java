package com.example.schedule.repository;

import com.example.schedule.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 댓글 엔티티의 영속성 처리를 담당하는 Repository 인터페이스.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 일정에 등록된 댓글 수를 조회하여 최대 개수 제한 검증에 활용
    long countByScheduleId(Long scheduleId);

    // 특정 일정의 댓글을 작성일 오름차순으로 조회
    List<Comment> findAllByScheduleIdOrderByCreatedAtAsc(Long scheduleId);

    // 일정 삭제 시 연관된 댓글을 일괄 삭제하여 데이터 정합성 유지
    void deleteAllByScheduleId(Long scheduleId);
}
