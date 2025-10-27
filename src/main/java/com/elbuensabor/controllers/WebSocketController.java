package com.elbuensabor.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Maneja mensajes de prueba desde el cliente
     */
    @MessageMapping("/test")
    public void handleTestMessage(@Payload Map<String, Object> message, Principal principal) {
        try {
            String usuario = principal != null ? principal.getName() : "Anónimo";

            Map<String, Object> response = Map.of(
                    "tipo", "TEST_RESPONSE",
                    "mensaje", "Mensaje recibido correctamente",
                    "usuario", usuario,
                    "timestamp", LocalDateTime.now(),
                    "originalMessage", message
            );

            // Responder al usuario que envió el mensaje
            messagingTemplate.convertAndSendToUser(
                    usuario,
                    "/queue/test/response",
                    response
            );

            logger.info("✅ Mensaje de prueba procesado para usuario: {}", usuario);

        } catch (Exception e) {
            logger.error("❌ Error procesando mensaje de prueba: {}", e.getMessage());
        }
    }

    /**
     * Evento cuando un cliente se conecta
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        try {
            String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
            logger.info("🔌 Cliente WebSocket conectado - Session: {}", sessionId);

        } catch (Exception e) {
            logger.error("❌ Error manejando conexión WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Evento cuando un cliente se desconecta
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            String sessionId = event.getSessionId();
            logger.info("🔌 Cliente WebSocket desconectado - Session: {}", sessionId);

        } catch (Exception e) {
            logger.error("❌ Error manejando desconexión WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Endpoint para ping/pong - mantener conexión activa
     */
    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        try {
            String usuario = principal != null ? principal.getName() : "Anónimo";

            Map<String, Object> pong = Map.of(
                    "tipo", "PONG",
                    "timestamp", LocalDateTime.now()
            );

            messagingTemplate.convertAndSendToUser(
                    usuario,
                    "/queue/pong",
                    pong
            );

        } catch (Exception e) {
            logger.error("❌ Error procesando ping: {}", e.getMessage());
        }
    }
}