package kr.mmv.mjusugangsincheonghelper.sectionsync.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import kr.mmv.mjusugangsincheonghelper.sectionsync.listener.SectionSyncChangeListener;

/**
 * Section 도메인 Redis Pub/Sub 설정
 * 
 * 크롤러가 "mju:section:change" 채널에 메시지를 발행하면 수신하여 처리
 * 
 * 활성화/비활성화:
 * redis.listener.section.enabled=true|false (application.yml)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "redis.listener.section.enabled", havingValue = "true", matchIfMissing = false)
public class SectionSyncRedisConfig {

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
     * Pub/Sub 메시지 리스너 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer sectionRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter sectionChangeListenerAdapter,
            ChannelTopic sectionChangeTopic) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(sectionChangeListenerAdapter, sectionChangeTopic);
        
        log.info("Section Redis Pub/Sub listener registered for topic: {}", sectionChangeTopic.getTopic());
        
        return container;
    }
}
