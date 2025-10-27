package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.PedidoConMercadoPagoRequestDTO;
import com.elbuensabor.dto.response.PedidoConMercadoPagoResponseDTO;
import com.elbuensabor.services.impl.PedidoConMercadoPagoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos-mercadopago")
public class PedidoConMercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoConMercadoPagoController.class);

    @Autowired
    private PedidoConMercadoPagoService pedidoConMercadoPagoService;

    // ==================== ENDPOINT PRINCIPAL ====================

    /**
     * Crear pedido completo con descuentos automáticos y link de MercadoPago
     * - Aplica descuento automático para TAKE_AWAY (configurable)
     * - Agrega gastos de envío para DELIVERY
     * - Crea pedido usando toda tu lógica existente
     * - Genera factura automáticamente
     * - Crea preferencia de MercadoPago
     * - Devuelve todo en una respuesta unificada
     */
    @PostMapping("/crear")
    public ResponseEntity<PedidoConMercadoPagoResponseDTO> crearPedidoConMercadoPago(
            @Valid @RequestBody PedidoConMercadoPagoRequestDTO request) {

        try {
            logger.info("=== NUEVO PEDIDO CON MERCADOPAGO ===");
            logger.info("Cliente: {}, Tipo: {}, Items: {}",
                    request.getIdCliente(),
                    request.getTipoEnvio(),
                    request.getDetalles().size());

            PedidoConMercadoPagoResponseDTO response = pedidoConMercadoPagoService.crearPedidoConMercadoPago(request);

            if (response.getExito()) {
                logger.info("✅ Pedido creado exitosamente: ID {}", response.getPedido().getIdPedido());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.error("❌ Error creando pedido: {}", response.getMensaje());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            logger.error("❌ Error inesperado: {}", e.getMessage(), e);

            PedidoConMercadoPagoResponseDTO errorResponse = PedidoConMercadoPagoResponseDTO.conError(
                    "Error interno del servidor: " + e.getMessage(),
                    0L
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ENDPOINT PARA CÁLCULOS PREVIOS ====================

    /**
     * Calcular totales con descuentos SIN crear el pedido
     * Útil para mostrar totales actualizados en tiempo real en el frontend
     */
    @PostMapping("/calcular-totales")
    public ResponseEntity<PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO> calcularTotales(
            @Valid @RequestBody PedidoConMercadoPagoRequestDTO request) {

        try {
            logger.info("Calculando totales preview para tipo: {}", request.getTipoEnvio());

            PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO totales =
                    pedidoConMercadoPagoService.calcularTotalesPreview(request);

            logger.info("Totales calculados: Subtotal ${}, Total final: ${}",
                    totales.getSubtotalProductos(), totales.getTotalFinal());

            return ResponseEntity.ok(totales);

        } catch (Exception e) {
            logger.error("Error calculando totales: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ==================== ENDPOINTS DE INFORMACIÓN ====================

    /**
     * Obtener configuración de descuentos y gastos
     */
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Object>> getConfiguracion() {
        Map<String, Object> config = Map.of(
                "descuentoTakeAwayPorDefecto", 10.0,
                "gastosEnvioDeliveryPorDefecto", 200.0,
                "moneda", "ARS",
                "tiposEnvioPermitidos", new String[]{"TAKE_AWAY", "DELIVERY"},
                "descripcion", "Sistema de pedidos con descuentos automáticos y MercadoPago integrado"
        );

        return ResponseEntity.ok(config);
    }

    /**
     * Health check del sistema integrado
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "OK",
                "serviciosDisponibles", new String[]{
                        "PedidoService",
                        "FacturaService",
                        "MercadoPagoService",
                        "CalculadoraDescuentos"
                },
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0"
        );

        return ResponseEntity.ok(health);
    }

    // ==================== ENDPOINT DE EJEMPLO PARA TESTING ====================

    /**
     * Generar request de ejemplo para testing
     */
    @GetMapping("/ejemplo")
    public ResponseEntity<PedidoConMercadoPagoRequestDTO> generarEjemplo() {
        PedidoConMercadoPagoRequestDTO ejemplo = new PedidoConMercadoPagoRequestDTO();

        // Datos básicos del pedido
        ejemplo.setIdCliente(1L);
        ejemplo.setIdSucursal(1L);
        ejemplo.setTipoEnvio("TAKE_AWAY"); // Para mostrar el descuento
        ejemplo.setObservaciones("Pedido de ejemplo para testing");

        // Datos del comprador para MP
        ejemplo.setEmailComprador("test@elbuensabor.com");
        ejemplo.setNombreComprador("Juan");
        ejemplo.setApellidoComprador("Pérez");

        // Configuración de descuentos
        ejemplo.setPorcentajeDescuentoTakeAway(15.0); // 15% de descuento
        ejemplo.setAplicarDescuentoTakeAway(true);
        ejemplo.setCrearPreferenciaMercadoPago(true);

        // Detalle de ejemplo (necesitas ajustar los IDs según tus productos)
        // ejemplo.setDetalles(Arrays.asList(
        //     new DetallePedidoRequestDTO(19L, 2, "Sin cebolla")
        // ));

        return ResponseEntity.ok(ejemplo);
    }
}