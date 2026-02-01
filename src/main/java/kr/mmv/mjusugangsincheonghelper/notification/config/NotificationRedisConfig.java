package kr.mmv.mjusugangsincheonghelper.notification.config;

import org.springframework.context.annotation.Configuration;

/**
 * Notification 도메인 Redis 설정
 * 
 * 알림 수신(Worker) 관련 설정 관리
 * 실제 Worker 로직은 com.example.demo.notification.worker.NotificationWorker 참조
 */
@Configuration
public class NotificationRedisConfig {
    // 필요한 경우 Worker 관련 빈이나 설정을 이곳에 추가
}
