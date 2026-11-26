package com.example.schedule.repository;

import com.example.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 일정 엔티티의 영속성 처리를 담당하는 Repository 인터페이스.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
