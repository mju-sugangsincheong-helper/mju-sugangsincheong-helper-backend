package kr.mmv.mjusugangsincheonghelper.sectionsync.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import kr.mmv.mjusugangsincheonghelper.sectionsync.listener.SectionSyncChangeListener;
import kr.mmv.mjusugangsincheonghelper.sectionsync.service.SectionSyncService;

/**
 * Section 도메인 Redis Pub/Sub 설정
 * 
 * 크롤러가 "mju:section:change" 채널에 메시지를 발행하면 수신하여 처리
 * 
 * 활성화/비활성화:
 * app.section-sync.enabled=true|false
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.section-sync.enabled", havingValue = "true", matchIfMissing = false)
public class SectionSyncRedisConfig {

    /**
     * 강의 변경 이벤트 리스너 빈 등록
     */
    @Bean
    public SectionSyncChangeListener sectionSyncChangeListener(SectionSyncService sectionSyncService) {
        return new SectionSyncChangeListener(sectionSyncService);
    }

    /**
     * 강의 변경 이벤트 토픽
     */
    @Bean
    public ChannelTopic sectionChangeTopic() {
        return new ChannelTopic("mju:section:change");
    }

    /**
     * 메시지 리스너 어댑터
     * SectionChangeListener.handleMessage() 메서드로 메시지 전달
     */
    @Bean
    public MessageListenerAdapter sectionChangeListenerAdapter(SectionSyncChangeListener listener) {
        log.info("Section change listener adapter initialized");
        return new MessageListenerAdapter(listener, "handleMessage");
    }

    /**
     * Redis Pub/Sub 메시지 처리를 위한 전용 스레드 풀
     * 스레드 번호가 무한정 늘어나는 것을 방지하고 재사용하기 위해 설정
     */
    @Bean
    public TaskExecutor sectionSyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("section-sync-listener-");
        executor.initialize();
        return executor;
    }

    /**
     * Pub/Sub 메시지 리스너 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer sectionRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter sectionChangeListenerAdapter,
            ChannelTopic sectionChangeTopic,
            TaskExecutor sectionSyncTaskExecutor) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(sectionSyncTaskExecutor);
        container.addMessageListener(sectionChangeListenerAdapter, sectionChangeTopic);
        
        log.info("Section Redis Pub/Sub listener registered for topic: {} with dedicated task executor", sectionChangeTopic.getTopic());
        
        return container;
    }
}
