package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.pedido.*;
import com.elbuensabor.dto.response.pedido.*;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.services.IPedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    private final IPedidoService service;

    @Autowired
    public PedidoController(IPedidoService service) {
        this.service = service;
    }

    // ==================== CREACIÓN DE PEDIDOS ====================

    /**
     * Crea un nuevo pedido (CLIENTE)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<PedidoClienteResponse> crearPedido(
            @Valid @RequestBody CrearPedidoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 POST /api/pedidos - Cliente {} creando pedido", usuario.getEmail());

        try {
            PedidoClienteResponse pedido = service.crearPedido(request, usuario);
            logger.info("✅ Pedido {} creado exitosamente para cliente {}", pedido.getIdPedido(), usuario.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al crear pedido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al crear pedido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CONSULTAS GENERALES ====================

    /**
     * Obtiene todos los pedidos (ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        logger.debug("📥 GET /api/pedidos - Listando todos los pedidos (ADMIN)");

        List<PedidoResponse> pedidos = service.listarTodosPedidos();
        logger.info("✅ Se encontraron {} pedidos", pedidos.size());
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene un pedido por ID (Todos los roles - vista según rol)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        logger.debug("📥 GET /api/pedidos/{} - Usuario {} obteniendo pedido", id, usuario.getEmail());

        try {
            Object pedido = service.obtenerPedidoPorId(id, usuario);
            logger.info("✅ Pedido {} obtenido exitosamente", id);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al obtener pedido {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error inesperado al obtener pedido {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CONSULTAS POR ROL ====================

    /**
     * Obtiene pedidos del día actual (CAJERO/ADMIN)
     */
    @GetMapping("/del-dia")
    @PreAuthorize("hasAnyAuthority('CAJERO', 'ADMIN')")
    public ResponseEntity<List<PedidoCajeroResponse>> listarPedidosDelDia() {
        logger.debug("📥 GET /api/pedidos/del-dia - Listando pedidos del día");

        List<PedidoCajeroResponse> pedidos = service.listarPedidosDelDia();
        logger.info("✅ Se encontraron {} pedidos del día", pedidos.size());
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene pedidos por estado (ADMIN/CAJERO)
     */
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO')")
    public ResponseEntity<List<PedidoResponse>> listarPorEstado(@PathVariable String estado) {
        logger.debug("📥 GET /api/pedidos/estado/{} - Listando pedidos por estado", estado);

        try {
            List<PedidoResponse> pedidos = service.listarPedidosPorEstado(estado);
            logger.info("✅ Se encontraron {} pedidos con estado {}", pedidos.size(), estado);
            return ResponseEntity.ok(pedidos);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Estado inválido: {}", estado);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene pedidos por fecha (ADMIN/CAJERO)
     */
    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO')")
    public ResponseEntity<List<PedidoResponse>> listarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.debug("📥 GET /api/pedidos/fecha/{} - Listando pedidos por fecha", fecha);

        List<PedidoResponse> pedidos = service.listarPedidosPorFecha(fecha);
        logger.info("✅ Se encontraron {} pedidos para la fecha {}", pedidos.size(), fecha);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene pedidos para cocina (COCINERO)
     */
    @GetMapping("/cocina")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<List<PedidoCocineroResponse>> listarPedidosCocina() {
        logger.debug("📥 GET /api/pedidos/cocina - Listando pedidos para cocina");

        List<PedidoCocineroResponse> pedidos = service.listarPedidosCocina();
        logger.info("✅ Se encontraron {} pedidos para cocina", pedidos.size());
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene pedidos para delivery (DELIVERY)
     */
    @GetMapping("/delivery")
    @PreAuthorize("hasAuthority('DELIVERY')")
    public ResponseEntity<List<PedidoDeliveryResponse>> listarPedidosDelivery() {
        logger.debug("📥 GET /api/pedidos/delivery - Listando pedidos para delivery");

        List<PedidoDeliveryResponse> pedidos = service.listarPedidosDelivery();
        logger.info("✅ Se encontraron {} pedidos para delivery", pedidos.size());
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene pedidos de un cliente (CLIENTE - sus propios pedidos)
     */
    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<List<PedidoClienteResponse>> listarMisPedidos(
            @AuthenticationPrincipal Usuario usuario) {
        logger.debug("📥 GET /api/pedidos/mis-pedidos - Cliente {} obteniendo sus pedidos", usuario.getEmail());

        try {
            // Obtener el ID del cliente desde el usuario autenticado
            Long idCliente = usuario.getCliente().getIdCliente();
            List<PedidoClienteResponse> pedidos = service.listarPedidosCliente(idCliente);
            logger.info("✅ Se encontraron {} pedidos para el cliente {}", pedidos.size(), usuario.getEmail());
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("❌ Error al obtener pedidos del cliente {}", usuario.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GESTIÓN DE PAGOS ====================

    /**
     * Confirma el pago en efectivo de un pedido (CAJERO/ADMIN)
     */
    @PostMapping("/confirmar-pago")
    @PreAuthorize("hasAnyAuthority('CAJERO', 'ADMIN')")
    public ResponseEntity<PedidoResponse> confirmarPago(
            @Valid @RequestBody ConfirmarPagoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 POST /api/pedidos/confirmar-pago - {} confirmando pago del pedido {}",
                usuario.getRol(), request.getIdPedido());

        try {
            PedidoResponse pedido = service.confirmarPago(request, usuario);
            logger.info("✅ Pago del pedido {} confirmado por {}", request.getIdPedido(), usuario.getEmail());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al confirmar pago: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al confirmar pago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GESTIÓN DE ESTADOS ====================

    /**
     * Cambia el estado de un pedido (Según rol)
     */
    @PutMapping("/cambiar-estado")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CAJERO', 'COCINERO', 'DELIVERY')")
    public ResponseEntity<?> cambiarEstado(
            @Valid @RequestBody CambiarEstadoPedidoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 PUT /api/pedidos/cambiar-estado - {} cambiando estado del pedido {} a {}",
                usuario.getRol(), request.getIdPedido(), request.getNuevoEstado());

        try {
            Object pedido = service.cambiarEstado(request, usuario);
            logger.info("✅ Estado del pedido {} cambiado a {} por {}",
                    request.getIdPedido(), request.getNuevoEstado(), usuario.getEmail());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al cambiar estado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error inesperado al cambiar estado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancela un pedido (CLIENTE/CAJERO/ADMIN)
     */
    @PutMapping("/cancelar")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'CAJERO', 'ADMIN', 'COCINERO')")
    public ResponseEntity<?> cancelarPedido(
            @Valid @RequestBody CancelarPedidoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 PUT /api/pedidos/cancelar - {} cancelando pedido {}",
                usuario.getRol(), request.getIdPedido());

        try {
            Object pedido = service.cancelarPedido(request, usuario);
            logger.info("✅ Pedido {} cancelado por {}", request.getIdPedido(), usuario.getEmail());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al cancelar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error inesperado al cancelar pedido", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GESTIÓN DE COCINA ====================

    /**
     * Inicia la preparación de un pedido (COCINERO)
     */
    @PutMapping("/{id}/iniciar-preparacion")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<PedidoCocineroResponse> iniciarPreparacion(@PathVariable("id") Long id) {
        logger.info("📥 PUT /api/pedidos/{}/iniciar-preparacion - Iniciando preparación", id);

        try {
            PedidoCocineroResponse pedido = service.iniciarPreparacion(id);
            logger.info("✅ Preparación del pedido {} iniciada", id);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al iniciar preparación del pedido {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al iniciar preparación del pedido {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marca un pedido como listo (COCINERO)
     */
    @PutMapping("/{id}/marcar-listo")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<PedidoCocineroResponse> marcarListo(@PathVariable("id") Long id) {
        logger.info("📥 PUT /api/pedidos/{}/marcar-listo - Marcando pedido como listo", id);

        try {
            PedidoCocineroResponse pedido = service.marcarListo(id);
            logger.info("✅ Pedido {} marcado como listo", id);
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al marcar pedido {} como listo: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al marcar pedido {} como listo", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extiende el tiempo de preparación de un pedido (COCINERO)
     */
    @PutMapping("/extender-tiempo")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<PedidoCocineroResponse> extenderTiempo(
            @Valid @RequestBody ExtenderTiempoRequest request) {
        logger.info("📥 PUT /api/pedidos/extender-tiempo - Extendiendo tiempo del pedido {} en {} minutos",
                request.getIdPedido(), request.getMinutosExtension());

        try {
            PedidoCocineroResponse pedido = service.extenderTiempo(request);
            logger.info("✅ Tiempo del pedido {} extendido en {} minutos",
                    request.getIdPedido(), request.getMinutosExtension());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al extender tiempo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al extender tiempo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GESTIÓN DE DELIVERY ====================

    /**
     * Marca un pedido como entregado (DELIVERY)
     */
    @PutMapping("/{id}/marcar-entregado")
    @PreAuthorize("hasAuthority('DELIVERY')")
    public ResponseEntity<PedidoDeliveryResponse> marcarEntregado(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 PUT /api/pedidos/{}/marcar-entregado - Delivery {} marcando como entregado",
                id, usuario.getEmail());

        try {
            PedidoDeliveryResponse pedido = service.marcarEntregado(id, usuario);
            logger.info("✅ Pedido {} marcado como entregado por {}", id, usuario.getEmail());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al marcar pedido {} como entregado: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al marcar pedido {} como entregado", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Asigna un delivery a un pedido (CAJERO/ADMIN)
     */
    @PutMapping("/asignar-delivery")
    @PreAuthorize("hasAnyAuthority('CAJERO', 'ADMIN')")
    public ResponseEntity<PedidoResponse> asignarDelivery(
            @Valid @RequestBody AsignarDeliveryRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        logger.info("📥 PUT /api/pedidos/asignar-delivery - Asignando delivery {} al pedido {}",
                request.getIdUsuarioDelivery(), request.getIdPedido());

        try {
            PedidoResponse pedido = service.asignarDelivery(request, usuario);
            logger.info("✅ Delivery {} asignado al pedido {} por {}",
                    request.getIdUsuarioDelivery(), request.getIdPedido(), usuario.getEmail());
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al asignar delivery: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ Error inesperado al asignar delivery", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}