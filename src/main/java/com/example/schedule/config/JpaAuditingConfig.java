package com.example.schedule.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정.
 * 슬라이스 테스트(@WebMvcTest 등) 시 JPA 메타모델 빈 충돌을 방지하기 위해 애플리케이션 진입점과 분리한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
