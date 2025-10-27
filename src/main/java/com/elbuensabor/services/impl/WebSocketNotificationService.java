package com.elbuensabor.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notifica cuando se crea un nuevo pedido
     */
    public void notificarNuevoPedido(Long pedidoId, String clienteNombre) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "PEDIDO_NUEVO",
                "pedidoId", pedidoId,
                "cliente", clienteNombre,
                "timestamp", LocalDateTime.now(),
                "mensaje", "Nuevo pedido recibido"
        );

        try {
            // Notificar a cocineros
            messagingTemplate.convertAndSend("/topic/cocina/nuevos", notificacion);

            // Notificar a cajeros
            messagingTemplate.convertAndSend("/topic/cajero/pedidos", notificacion);

            // Notificar a admin
            messagingTemplate.convertAndSend("/topic/pedidos/nuevos", notificacion);

            logger.info("✅ Nuevo pedido notificado: #{}", pedidoId);

        } catch (Exception e) {
            logger.error("❌ Error notificando nuevo pedido #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica cambio de estado de pedido
     */
    public void notificarCambioEstado(Long pedidoId, String nuevoEstado, String clienteEmail) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "CAMBIO_ESTADO",
                "pedidoId", pedidoId,
                "estado", nuevoEstado,
                "timestamp", LocalDateTime.now(),
                "mensaje", generarMensajeEstado(nuevoEstado)
        );

        try {
            // Notificar al cliente específico
            if (clienteEmail != null && !clienteEmail.isEmpty()) {
                messagingTemplate.convertAndSendToUser(
                        clienteEmail, // ✅ Email del cliente (Spring lo asocia a la sesión del WebSocket)
                        "/queue/pedido/estado",
                        notificacion
                );
            }

            // Broadcast a todos los roles
            messagingTemplate.convertAndSend("/topic/pedidos/estados", notificacion);

            logger.info("✅ Cambio de estado notificado - Pedido #{}: {}", pedidoId, nuevoEstado);

        } catch (Exception e) {
            logger.error("❌ Error notificando cambio de estado #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica específicamente cancelaciones a cocina
     */
    public void notificarCancelacionPedido(Long pedidoId, String clienteNombre, String clienteAuthId) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "PEDIDO_CANCELADO",
                "pedidoId", pedidoId,
                "cliente", clienteNombre,
                "timestamp", LocalDateTime.now(),
                "mensaje", "Pedido cancelado - detener preparación"
        );

        try {
            // Notificar específicamente a cocina
            messagingTemplate.convertAndSend("/topic/cocina/cancelaciones", notificacion);

            // Notificar al cajero
            messagingTemplate.convertAndSend("/topic/cajero/pedidos", notificacion);

            // Notificar cambio de estado general
            notificarCambioEstado(pedidoId, "CANCELADO", clienteAuthId);

            logger.info("✅ Cancelación notificada - Pedido #{}", pedidoId);

        } catch (Exception e) {
            logger.error("❌ Error notificando cancelación #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica cuando un pedido está listo para delivery
     */
    public void notificarPedidoListoParaDelivery(Long pedidoId, String clienteNombre) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "PEDIDO_LISTO_DELIVERY",
                "pedidoId", pedidoId,
                "cliente", clienteNombre,
                "timestamp", LocalDateTime.now(),
                "mensaje", "Pedido listo para delivery"
        );

        try {
            // Notificar específicamente a delivery
            messagingTemplate.convertAndSend("/topic/delivery/disponibles", notificacion);

            logger.info("✅ Pedido listo para delivery notificado - Pedido #{}", pedidoId);

        } catch (Exception e) {
            logger.error("❌ Error notificando pedido listo para delivery #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica cuando se confirma un pago
     */
    public void notificarPagoConfirmado(Long pedidoId, String tipoPago) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "PAGO_CONFIRMADO",
                "pedidoId", pedidoId,
                "tipoPago", tipoPago,
                "timestamp", LocalDateTime.now(),
                "mensaje", "Pago confirmado - proceder con preparación"
        );

        try {
            // Notificar a cocina que puede empezar a preparar
            messagingTemplate.convertAndSend("/topic/cocina/pagos", notificacion);

            // Notificar a cajero
            messagingTemplate.convertAndSend("/topic/cajero/pagos", notificacion);

            logger.info("✅ Pago confirmado notificado - Pedido #{}", pedidoId);

        } catch (Exception e) {
            logger.error("❌ Error notificando pago confirmado #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica cuando se extiende el tiempo de preparación
     */
    public void notificarTiempoExtendido(Long pedidoId, Integer minutosExtra) {
        Map<String, Object> notificacion = Map.of(
                "tipo", "TIEMPO_EXTENDIDO",
                "pedidoId", pedidoId,
                "minutosExtra", minutosExtra,
                "timestamp", LocalDateTime.now(),
                "mensaje", "Tiempo de preparación extendido +" + minutosExtra + " min"
        );

        try {
            // Notificar a todos los roles
            messagingTemplate.convertAndSend("/topic/pedidos/tiempos", notificacion);

            logger.info("✅ Tiempo extendido notificado - Pedido #{}: +{}min", pedidoId, minutosExtra);

        } catch (Exception e) {
            logger.error("❌ Error notificando tiempo extendido #{}: {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Genera mensaje amigable según el estado
     */
    private String generarMensajeEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "Tu pedido ha sido recibido";
            case "EN_PREPARACION" -> "Tu pedido está siendo preparado";
            case "LISTO" -> "Tu pedido está listo para entregar";
            case "EN_CAMINO" -> "Tu pedido está en camino";
            case "ENTREGADO" -> "Tu pedido ha sido entregado";
            case "CANCELADO" -> "Tu pedido ha sido cancelado";
            default -> "Estado del pedido actualizado";
        };
    }
}