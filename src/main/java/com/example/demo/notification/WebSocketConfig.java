package com.example.demo.notification;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Включаем внешний брокер (Redis)
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("redis-host")
                .setRelayPort(6379)
                .setSystemLogin("redis-user")
                .setSystemPasscode("redis-password");

        // Префикс для приложений
        registry.setApplicationDestinationPrefixes("/app");
    }
}
