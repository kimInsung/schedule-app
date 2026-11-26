package com.example.schedule.repository;

import com.example.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 일정 엔티티의 영속성 처리를 담당하는 Repository 인터페이스.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 전체 일정을 수정일(modifiedAt) 기준 내림차순으로 조회
    List<Schedule> findAllByOrderByModifiedAtDesc();

    // 특정 작성자의 일정을 수정일(modifiedAt) 기준 내림차순으로 조회
    List<Schedule> findAllByAuthorOrderByModifiedAtDesc(String author);
}
