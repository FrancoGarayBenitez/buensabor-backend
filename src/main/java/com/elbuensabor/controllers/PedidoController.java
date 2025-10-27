package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.PedidoRequestDTO;
import com.elbuensabor.dto.response.HorarioStatusResponseDTO;
import com.elbuensabor.dto.response.PedidoResponseDTO;
import com.elbuensabor.services.IHorarioService;
import com.elbuensabor.services.IPedidoService;
import com.elbuensabor.services.impl.HorarioServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.services.impl.PromocionPedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final IPedidoService pedidoService;
    private final IHorarioService horarioService;
    private final PromocionPedidoService promocionPedidoService;
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    public PedidoController(IPedidoService pedidoService, IHorarioService horarioService, PromocionPedidoService promocionPedidoService) {
        this.pedidoService = pedidoService;
        this.horarioService = horarioService;
        this.promocionPedidoService = promocionPedidoService; // NUEVO
    }

    // ==================== CREAR PEDIDO ====================
    @PostMapping
    public ResponseEntity<?> crearPedido(@Valid @RequestBody PedidoRequestDTO pedidoRequest) {

        // ✅ NUEVO: Log para debugging promociones
        System.out.println("🔍 REQUEST RECIBIDO:");
        System.out.println("📝 Cliente: " + pedidoRequest.getIdCliente());
        System.out.println("📝 Tipo envío: " + pedidoRequest.getTipoEnvio());
        System.out.println("📝 Cantidad productos: " + pedidoRequest.getDetalles().size());

        // ✅ NUEVO: Verificar promoción agrupada
        if (pedidoRequest.getPromocionAgrupada() != null) {
            System.out.println("🎁 PROMOCIÓN AGRUPADA RECIBIDA:");
            System.out.println("   - ID: " + pedidoRequest.getPromocionAgrupada().getIdPromocion());
            System.out.println("   - Nombre: " + pedidoRequest.getPromocionAgrupada().getDenominacion());
            System.out.println("   - Tipo: " + pedidoRequest.getPromocionAgrupada().getTipoDescuento());
            System.out.println("   - Valor: " + pedidoRequest.getPromocionAgrupada().getValorDescuento() + "%");
            System.out.println("   - Descuento: $" + pedidoRequest.getPromocionAgrupada().getDescuentoAplicado());
        } else {
            System.out.println("ℹ️ Sin promoción agrupada");
        }

        // El controlador ahora recibe un objeto de estado completo
        HorarioStatusResponseDTO estadoHorario = horarioService.getEstadoHorario();

        if (!estadoHorario.isAbierto()) {
            // Usamos el mensaje que viene del DTO
            Map<String, String> errorResponse = Map.of(
                    "error", "Fuera de horario de atención",
                    "message", estadoHorario.getMensaje()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        PedidoResponseDTO pedidoCreado = pedidoService.crearPedido(pedidoRequest);
        return new ResponseEntity<>(pedidoCreado, HttpStatus.CREATED);
    }

    // ==================== OBTENER PEDIDOS - AL FINAL ====================


    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> getPedidoById(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.findById(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosByCliente(@PathVariable Long idCliente) {
        List<PedidoResponseDTO> pedidos = pedidoService.findByCliente(idCliente);
        return ResponseEntity.ok(pedidos);
    }

    // ==================== OPERACIONES DE ESTADO ====================
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedidoConfirmado = pedidoService.confirmarPedido(id);
        return ResponseEntity.ok(pedidoConfirmado);
    }

    @PutMapping("/{id}/preparacion")
    public ResponseEntity<PedidoResponseDTO> marcarEnPreparacion(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.marcarEnPreparacion(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{id}/listo")
    public ResponseEntity<PedidoResponseDTO> marcarListo(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.marcarListo(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{id}/entregado")
    public ResponseEntity<PedidoResponseDTO> marcarEntregado(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.marcarEntregado(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    // ==================== VALIDACIONES PREVIAS - PRIMERO ====================
    @PostMapping("/validar")
    public ResponseEntity<Boolean> validarPedido(@Valid @RequestBody PedidoRequestDTO pedidoRequest) {
        Boolean esValido = pedidoService.validarStockDisponible(pedidoRequest);
        return ResponseEntity.ok(esValido);
    }

    @PostMapping("/calcular-total")
    public ResponseEntity<Double> calcularTotal(@Valid @RequestBody PedidoRequestDTO pedidoRequest) {
        Double total = pedidoService.calcularTotal(pedidoRequest);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/tiempo-estimado")
    public ResponseEntity<Integer> calcularTiempoEstimado(@Valid @RequestBody PedidoRequestDTO pedidoRequest) {
        Integer tiempoMinutos = pedidoService.calcularTiempoEstimado(pedidoRequest);
        return ResponseEntity.ok(tiempoMinutos);
    }

    // ==================== FILTROS PARA DIFERENTES ROLES ====================

    // Para administradores/gerentes - ver todos los pedidos por estado
    @GetMapping("/pendientes")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosPendientes() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosPendientes();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/en-preparacion")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosEnPreparacion() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosEnPreparacion();
        return ResponseEntity.ok(pedidos);
    }


    // Para delivery - pedidos listos para entregar
    @GetMapping("/listos-para-entrega")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosListosParaEntrega() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosListosParaEntrega();
        return ResponseEntity.ok(pedidos);
    }

    // Para mostrador/caja - pedidos listos para retirar
    @GetMapping("/listos-para-retiro")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosListosParaRetiro() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosListosParaRetiro();
        return ResponseEntity.ok(pedidos);
    }

    // ==================== ENDPOINTS ESPECÍFICOS PARA COCINA ====================

    // Dashboard de cocina - pedidos que necesitan atención
    @GetMapping("/cocina/pendientes-confirmacion")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosPendientesConfirmacion() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosPendientes();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/cocina/en-proceso")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosEnProceso() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosEnPreparacion();
        return ResponseEntity.ok(pedidos);
    }

    // ==================== ENDPOINTS PARA DELIVERY ====================

    // Solo pedidos de delivery listos para entregar
    @GetMapping("/delivery/pendientes")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosDeliveryPendientes() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosListosParaEntrega();
        return ResponseEntity.ok(pedidos);
    }

    // ==================== ENDPOINTS PARA MOSTRADOR ====================

    // Solo pedidos de take away listos para retirar
    @GetMapping("/mostrador/listos")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosMostradorListos() {
        List<PedidoResponseDTO> pedidos = pedidoService.findPedidosListosParaRetiro();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Obtiene todos los pedidos listos (para vista de cocina)
     */
    @GetMapping("/listos")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosListos() {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.findPedidosListos();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("Error al obtener pedidos listos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene la factura asociada a un pedido específico
     */
    @GetMapping("/{id}/factura")
    public ResponseEntity<FacturaResponseDTO> getFacturaPedido(@PathVariable Long id) {
        try {
            FacturaResponseDTO factura = pedidoService.getFacturaPedido(id);
            return ResponseEntity.ok(factura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los pedidos (endpoint básico para gestión)
     */

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.findAll();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("Error al obtener todos los pedidos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * POST /api/pedidos/preview-carrito
     * Preview del carrito con promociones aplicadas
     */
    @PostMapping("/preview-carrito")
    public ResponseEntity<CarritoPreviewDTO> previewCarrito(@Valid @RequestBody PedidoRequestDTO pedidoRequest) {
        try {
            // Usar el service de promociones para calcular
            PromocionPedidoService.PromocionesAplicadasDTO promocionesAplicadas =
                    promocionPedidoService.aplicarPromocionesAPedido(pedidoRequest);

            // Calcular totales
            Double subtotalOriginal = promocionesAplicadas.getSubtotalOriginal();
            Double descuentoTotal = promocionesAplicadas.getDescuentoTotal();
            Double subtotalConDescuentos = promocionesAplicadas.getSubtotalFinal();

            // Agregar costo de envío si es delivery
            Double gastosEnvio = 0.0;
            if ("DELIVERY".equals(pedidoRequest.getTipoEnvio())) {
                gastosEnvio = 200.0;
            }

            Double totalFinal = subtotalConDescuentos + gastosEnvio;

            // Crear response
            CarritoPreviewDTO preview = new CarritoPreviewDTO();
            preview.setSubtotalOriginal(subtotalOriginal);
            preview.setDescuentoTotal(descuentoTotal);
            preview.setSubtotalConDescuentos(subtotalConDescuentos);
            preview.setGastosEnvio(gastosEnvio);
            preview.setTotalFinal(totalFinal);
            preview.setTipoEnvio(pedidoRequest.getTipoEnvio());
            preview.setResumenPromociones(promocionesAplicadas.getResumenPromociones());

            // Mapear detalles con promociones
            List<CarritoPreviewDTO.DetallePreviewDTO> detallesPreview =
                    promocionesAplicadas.getDetallesConPromociones().stream()
                            .map(detalle -> {
                                CarritoPreviewDTO.DetallePreviewDTO detalleDto = new CarritoPreviewDTO.DetallePreviewDTO();
                                detalleDto.setIdArticulo(detalle.getIdArticulo());
                                detalleDto.setDenominacionArticulo(detalle.getDenominacionArticulo());
                                detalleDto.setCantidad(detalle.getCantidad());
                                detalleDto.setPrecioUnitarioOriginal(detalle.getPrecioUnitarioOriginal());
                                detalleDto.setPrecioUnitarioFinal(detalle.getPrecioUnitarioFinal());
                                detalleDto.setSubtotalOriginal(detalle.getSubtotalOriginal());
                                detalleDto.setSubtotalFinal(detalle.getSubtotalFinal());
                                detalleDto.setDescuentoAplicado(detalle.getDescuentoAplicado());
                                detalleDto.setTienePromocion(detalle.getTienePromocion());
                                detalleDto.setObservaciones(detalle.getObservaciones());

                                if (detalle.getPromocionAplicada() != null) {
                                    detalleDto.setNombrePromocion(detalle.getPromocionAplicada().getDenominacion());
                                    detalleDto.setResumenDescuento(detalle.getPromocionAplicada().getResumenDescuento());
                                }

                                return detalleDto;
                            })
                            .collect(Collectors.toList());

            preview.setDetalles(detallesPreview);

            return ResponseEntity.ok(preview);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

// ===============================================
// AGREGAR ESTA CLASE INTERNA AL FINAL DEL CONTROLLER
// ===============================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarritoPreviewDTO {
        private Double subtotalOriginal;      // Total sin promociones
        private Double descuentoTotal;        // Total de descuentos aplicados
        private Double subtotalConDescuentos; // Subtotal después de promociones
        private Double gastosEnvio;           // Gastos de envío (si es delivery)
        private Double totalFinal;            // Total final a pagar
        private String tipoEnvio;             // DELIVERY o TAKE_AWAY
        private String resumenPromociones;    // Resumen de promociones aplicadas
        private List<DetallePreviewDTO> detalles; // Detalles con promociones

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DetallePreviewDTO {
            private Long idArticulo;
            private String denominacionArticulo;
            private Integer cantidad;
            private Double precioUnitarioOriginal;
            private Double precioUnitarioFinal;
            private Double subtotalOriginal;
            private Double subtotalFinal;
            private Double descuentoAplicado;
            private Boolean tienePromocion;
            private String observaciones;
            private String nombrePromocion;
            private String resumenDescuento;
        }
    }
}