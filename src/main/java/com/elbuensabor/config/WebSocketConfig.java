package com.elbuensabor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");

        logger.info("✅ WebSocket Message Broker configurado");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        logger.info("✅ WebSocket STOMP endpoints registrados");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                try {
                    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                    if (accessor != null) {
                        StompCommand command = accessor.getCommand();

                        // ✅ LOGGING MÍNIMO Y SEGURO
                        if (StompCommand.CONNECT.equals(command)) {
                            logger.info("🔐 WebSocket CONNECT");
                        } else if (StompCommand.SUBSCRIBE.equals(command)) {
                            logger.info("📡 WebSocket SUBSCRIBE: {}", accessor.getDestination());
                        } else if (StompCommand.DISCONNECT.equals(command)) {
                            logger.info("🔌 WebSocket DISCONNECT");
                        }
                    }

                    // ✅ SIEMPRE retornar el mensaje sin modificar
                    return message;

                } catch (Exception e) {
                    logger.error("❌ Error en interceptor WebSocket: {}", e.getMessage());
                    // ✅ CRÍTICO: Retornar mensaje original incluso con error
                    return message;
                }
            }
        });
    }
}