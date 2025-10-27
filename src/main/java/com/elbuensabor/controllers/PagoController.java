package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.dto.request.PagoRequestDTO;
import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.dto.response.MercadoPagoPreferenceResponseDTO;
import com.elbuensabor.dto.response.PagoResponseDTO;
import com.elbuensabor.entities.EstadoPago;
import com.elbuensabor.entities.FormaPago;
import com.elbuensabor.services.IMercadoPagoService;
import com.elbuensabor.services.IPagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.elbuensabor.services.IPedidoService;
import com.elbuensabor.services.IFacturaService;
import com.elbuensabor.dto.response.FacturaResponseDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")

public class PagoController {

    private static final Logger logger = LoggerFactory.getLogger(PagoController.class);

    // ✅ AGREGAR ESTAS DOS DEPENDENCIAS FALTANTES:
    @Autowired
    private IPedidoService pedidoService;

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private IPagoService pagoService;

    @Autowired
    private IMercadoPagoService mercadoPagoService;

    @PostMapping
    public ResponseEntity<PagoResponseDTO> crearPago(@Valid @RequestBody PagoRequestDTO pagoRequestDTO) {
        PagoResponseDTO pago = pagoService.crearPago(pagoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> getPagoById(@PathVariable Long id) {
        PagoResponseDTO pago = pagoService.findById(id);
        return ResponseEntity.ok(pago);
    }

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> getAllPagos() {
        List<PagoResponseDTO> pagos = pagoService.findAll();
        return ResponseEntity.ok(pagos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> updatePago(@PathVariable Long id, @Valid @RequestBody PagoResponseDTO pagoDTO) {
        PagoResponseDTO pagoActualizado = pagoService.update(id, pagoDTO);
        return ResponseEntity.ok(pagoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePago(@PathVariable Long id) {
        pagoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<List<PagoResponseDTO>> getPagosByFactura(@PathVariable Long facturaId) {
        List<PagoResponseDTO> pagos = pagoService.getPagosByFactura(facturaId);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PagoResponseDTO>> getPagosByEstado(@PathVariable EstadoPago estado) {
        List<PagoResponseDTO> pagos = pagoService.getPagosByEstado(estado);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/forma-pago/{formaPago}")
    public ResponseEntity<List<PagoResponseDTO>> getPagosByFormaPago(@PathVariable FormaPago formaPago) {
        List<PagoResponseDTO> pagos = pagoService.getPagosByFormaPago(formaPago);
        return ResponseEntity.ok(pagos);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PagoResponseDTO> actualizarEstadoPago(
            @PathVariable Long id,
            @RequestBody Map<String, EstadoPago> request) {
        EstadoPago nuevoEstado = request.get("estado");
        PagoResponseDTO pago = pagoService.actualizarEstadoPago(id, nuevoEstado);
        return ResponseEntity.ok(pago);
    }

    @PostMapping("/{id}/crear-preferencia-mp")
    public ResponseEntity<MercadoPagoPreferenceResponseDTO> crearPreferenciaMercadoPago(
            @PathVariable Long id,
            @RequestBody MercadoPagoPreferenceDTO preferenceDTO) {
        try {
            // Agregar referencia externa con el ID del pago
            preferenceDTO.setExternalReference("PAGO_" + id);

            MercadoPagoPreferenceResponseDTO preference = mercadoPagoService.crearPreferencia(preferenceDTO);

            // Actualizar el pago con el preference ID
            pagoService.procesarPagoMercadoPago(id, preference.getId());

            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Webhook para Mercado Pago
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<String> webhookMercadoPago(@RequestBody Map<String, Object> notification) {
        try {
            if ("payment".equals(notification.get("type"))) {
                Map<String, Object> data = (Map<String, Object>) notification.get("data");
                Long paymentId = Long.valueOf(data.get("id").toString());

                // Aquí deberías hacer una consulta a la API de MP para obtener los detalles completos
                // Por ahora simulamos la confirmación
                pagoService.confirmarPagoMercadoPago(paymentId, "approved", "accredited");
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando webhook");
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PagoResponseDTO> cancelarPago(@PathVariable Long id) {
        PagoResponseDTO pago = pagoService.cancelarPago(id);
        return ResponseEntity.ok(pago);
    }

    @PutMapping("/{id}/reembolsar")
    public ResponseEntity<PagoResponseDTO> procesarReembolso(@PathVariable Long id) {
        PagoResponseDTO pago = pagoService.procesarReembolso(id);
        return ResponseEntity.ok(pago);
    }

    @GetMapping("/factura/{facturaId}/total-pagado")
    public ResponseEntity<Double> getTotalPagadoFactura(@PathVariable Long facturaId) {
        Double total = pagoService.getTotalPagadoFactura(facturaId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/factura/{facturaId}/saldo-pendiente")
    public ResponseEntity<Double> getSaldoPendienteFactura(@PathVariable Long facturaId) {
        Double saldo = pagoService.getSaldoPendienteFactura(facturaId);
        return ResponseEntity.ok(saldo);
    }

    @GetMapping("/factura/{facturaId}/completamente-pagada")
    public ResponseEntity<Boolean> isFacturaCompletamentePagada(@PathVariable Long facturaId) {
        Boolean completamentePagada = pagoService.isFacturaCompletamentePagada(facturaId);
        return ResponseEntity.ok(completamentePagada);
    }

    /**
     * Simular webhook de MercadoPago para testing en localhost
     * Usar después de pagar en sandbox para actualizar el estado
     */
    @PostMapping("/simular-webhook-mp")
    public ResponseEntity<Map<String, Object>> simularWebhookMercadoPago(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== SIMULANDO WEBHOOK MERCADOPAGO ===");

            // Extraer datos del request
            String externalReference = (String) request.get("externalReference");
            String status = (String) request.getOrDefault("status", "approved");
            String statusDetail = (String) request.getOrDefault("statusDetail", "accredited");
            Long paymentId = Long.valueOf(request.getOrDefault("paymentId", System.currentTimeMillis()).toString());

            logger.info("External Reference: {}, Status: {}, PaymentID: {}", externalReference, status, paymentId);

            if (externalReference == null || !externalReference.startsWith("PEDIDO_")) {
                throw new IllegalArgumentException("External reference debe empezar con 'PEDIDO_'");
            }

            // Extraer ID del pedido desde external reference (formato: "PEDIDO_18_timestamp")
            String pedidoIdStr = externalReference.split("_")[1];
            Long pedidoId = Long.valueOf(pedidoIdStr);

            // Buscar la factura del pedido
            FacturaResponseDTO factura = pedidoService.getFacturaPedido(pedidoId);

            // Crear pago en el sistema
            PagoRequestDTO pagoRequest = new PagoRequestDTO();
            pagoRequest.setFacturaId(factura.getIdFactura());
            pagoRequest.setFormaPago(FormaPago.MERCADO_PAGO);
            pagoRequest.setMonto(factura.getTotalVenta());
            pagoRequest.setDescripcion("Pago simulado desde webhook - PaymentID: " + paymentId);

            // Crear el pago
            PagoResponseDTO pagoCreado = pagoService.crearPago(pagoRequest);

            // Simular confirmación de MercadoPago
            pagoService.confirmarPagoMercadoPago(paymentId, status, statusDetail);

            // Obtener factura actualizada
            FacturaResponseDTO facturaActualizada = facturaService.findById(factura.getIdFactura());

            result.put("success", true);
            result.put("message", "Pago simulado exitosamente");
            result.put("pedidoId", pedidoId);
            result.put("facturaId", factura.getIdFactura());
            result.put("pagoId", pagoCreado.getIdPago());
            result.put("paymentIdMP", paymentId);
            result.put("status", status);
            result.put("facturaActualizada", facturaActualizada);

            logger.info("✅ Webhook simulado exitosamente para pedido: {}", pedidoId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Error simulando webhook: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("suggestion", "Verificar que el external reference sea correcto");

            return ResponseEntity.status(400).body(result);
        }
    }

    /**
     * Confirmar pago usando solo el external reference del pedido
     * Más simple - solo necesitas el external reference
     */
    @PostMapping("/confirmar-pago-pedido")
    public ResponseEntity<Map<String, Object>> confirmarPagoPedido(@RequestBody Map<String, String> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            String externalReference = request.get("externalReference");

            if (externalReference == null || !externalReference.startsWith("PEDIDO_")) {
                throw new IllegalArgumentException("External reference requerido (formato: PEDIDO_XX_timestamp)");
            }

            // Extraer pedido ID
            String pedidoIdStr = externalReference.split("_")[1];
            Long pedidoId = Long.valueOf(pedidoIdStr);

            // Buscar factura
            FacturaResponseDTO factura = pedidoService.getFacturaPedido(pedidoId);

            // Verificar si ya está pagada
            if (factura.getCompletamentePagada()) {
                result.put("success", true);
                result.put("message", "La factura ya estaba completamente pagada");
                result.put("factura", factura);
                return ResponseEntity.ok(result);
            }

            // Crear pago
            PagoRequestDTO pagoRequest = new PagoRequestDTO();
            pagoRequest.setFacturaId(factura.getIdFactura());
            pagoRequest.setFormaPago(FormaPago.MERCADO_PAGO);
            pagoRequest.setMonto(factura.getSaldoPendiente());
            pagoRequest.setDescripcion("Pago confirmado manualmente - Ref: " + externalReference);

            PagoResponseDTO pagoCreado = pagoService.crearPago(pagoRequest);

            // Simular aprobación inmediata
            Long fakePaymentId = System.currentTimeMillis();
            pagoService.confirmarPagoMercadoPago(fakePaymentId, "approved", "accredited");

            // Obtener factura actualizada
            FacturaResponseDTO facturaFinal = facturaService.findById(factura.getIdFactura());

            result.put("success", true);
            result.put("message", "¡Pago confirmado exitosamente!");
            result.put("pedidoId", pedidoId);
            result.put("pagoCreado", pagoCreado);
            result.put("facturaFinal", facturaFinal);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error confirmando pago: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(400).body(result);
        }
    }

    /**
     * Confirmar pago manualmente usando external reference
     * Para usar cuando el webhook no llega por localhost
     */
    @PostMapping("/confirmar-pago-manual")
    public ResponseEntity<Map<String, Object>> confirmarPagoManual(@RequestBody Map<String, String> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            String externalReference = request.get("externalReference");

            if (externalReference == null || !externalReference.contains("PEDIDO_")) {
                result.put("success", false);
                result.put("error", "External reference requerido (debe contener 'PEDIDO_')");
                return ResponseEntity.status(400).body(result);
            }

            logger.info("=== CONFIRMANDO PAGO MANUAL ===");
            logger.info("External Reference: {}", externalReference);

            // Extraer pedido ID del external reference (formato: PEDIDO_18_timestamp)
            String[] parts = externalReference.split("_");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Formato de external reference incorrecto");
            }

            Long pedidoId = Long.valueOf(parts[1]);
            logger.info("Pedido ID extraído: {}", pedidoId);

            // Buscar factura del pedido
            FacturaResponseDTO factura = pedidoService.getFacturaPedido(pedidoId);
            logger.info("Factura encontrada: ID {}, Total: ${}", factura.getIdFactura(), factura.getTotalVenta());

            // Verificar si ya está pagada
            if (factura.getCompletamentePagada()) {
                result.put("success", true);
                result.put("message", "✅ La factura ya estaba completamente pagada");
                result.put("factura", factura);
                return ResponseEntity.ok(result);
            }

            // Crear pago por el saldo pendiente
            PagoRequestDTO pagoRequest = new PagoRequestDTO();
            pagoRequest.setFacturaId(factura.getIdFactura());
            pagoRequest.setFormaPago(FormaPago.MERCADO_PAGO);
            pagoRequest.setMonto(factura.getSaldoPendiente());
            pagoRequest.setDescripcion("Pago confirmado manualmente - " + externalReference);

            PagoResponseDTO pagoCreado = pagoService.crearPago(pagoRequest);
            logger.info("Pago creado: ID {}", pagoCreado.getIdPago());

            // Aprobar el pago inmediatamente
            PagoResponseDTO pagoAprobado = pagoService.actualizarEstadoPago(pagoCreado.getIdPago(), EstadoPago.APROBADO);
            logger.info("Pago aprobado exitosamente");

            // Obtener factura actualizada
            FacturaResponseDTO facturaFinal = facturaService.findById(factura.getIdFactura());

            result.put("success", true);
            result.put("message", "🎉 ¡Pago confirmado exitosamente!");
            result.put("pedidoId", pedidoId);
            result.put("pagoCreado", pagoAprobado);
            result.put("facturaAntes", factura);
            result.put("facturaDespues", facturaFinal);
            result.put("diferencia", Map.of(
                    "totalPagadoAntes", factura.getTotalPagado(),
                    "totalPagadoDespues", facturaFinal.getTotalPagado(),
                    "saldoPendienteAntes", factura.getSaldoPendiente(),
                    "saldoPendienteDespues", facturaFinal.getSaldoPendiente()
            ));

            logger.info("✅ Confirmación manual completada para pedido: {}", pedidoId);

            return ResponseEntity.ok(result);

        } catch (NumberFormatException e) {
            logger.error("Error parseando pedido ID: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "ID de pedido inválido en external reference");
            return ResponseEntity.status(400).body(result);

        } catch (Exception e) {
            logger.error("❌ Error confirmando pago manual: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("suggestion", "Verificar que el pedido y factura existan");

            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/debug/factura/{facturaId}")
    public ResponseEntity<Map<String, Object>> debugPagosFactura(@PathVariable Long facturaId) {

        Map<String, Object> debug = new HashMap<>();

        try {
            logger.info("=== DEBUG: VERIFICANDO PAGOS FACTURA {} ===", facturaId);

            // 1. Verificar que la factura existe
            FacturaResponseDTO factura = facturaService.findById(facturaId);
            debug.put("facturaExiste", true);
            debug.put("factura", factura);

            // 2. Buscar pagos directamente por factura
            List<PagoResponseDTO> pagosFactura = pagoService.getPagosByFactura(facturaId);
            debug.put("cantidadPagosEncontrados", pagosFactura.size());
            debug.put("pagosDetalle", pagosFactura);

            // 3. Buscar todos los pagos del sistema
            List<PagoResponseDTO> todosPagos = pagoService.findAll();
            debug.put("totalPagosEnSistema", todosPagos.size());

            // 4. Filtrar pagos por estado
            long pagosAprobados = pagosFactura.stream()
                    .filter(p -> "APROBADO".equals(p.getEstado().name()))
                    .count();
            debug.put("pagosAprobados", pagosAprobados);

            // 5. Calcular total manual
            double totalManual = pagosFactura.stream()
                    .filter(p -> "APROBADO".equals(p.getEstado().name()))
                    .mapToDouble(PagoResponseDTO::getMonto)
                    .sum();
            debug.put("totalCalculadoManual", totalManual);

            // 6. Obtener total desde service
            Double totalDesdeService = pagoService.getTotalPagadoFactura(facturaId);
            debug.put("totalDesdeService", totalDesdeService);

            // 7. Verificar estado de completamente pagada
            Boolean completamentePagada = pagoService.isFacturaCompletamentePagada(facturaId);
            debug.put("completamentePagadaDesdeService", completamentePagada);

            // 8. Resumen final
            debug.put("resumen", Map.of(
                    "facturaTotal", factura.getTotalVenta(),
                    "totalPagado", totalDesdeService,
                    "saldoPendiente", factura.getTotalVenta() - totalDesdeService,
                    "estadoFactura", completamentePagada ? "PAGADA" : "PENDIENTE"
            ));

            logger.info("✅ Debug completado para factura {}", facturaId);
            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            logger.error("❌ Error en debug: {}", e.getMessage(), e);
            debug.put("error", e.getMessage());
            return ResponseEntity.status(500).body(debug);
        }
    }

    // AGREGAR también este método para ver todos los pagos:
    @GetMapping("/debug/todos-los-pagos")
    public ResponseEntity<Map<String, Object>> debugTodosLosPagos() {

        Map<String, Object> debug = new HashMap<>();

        try {
            List<PagoResponseDTO> todosPagos = pagoService.findAll();

            debug.put("totalPagos", todosPagos.size());
            debug.put("pagos", todosPagos);

            // Agrupar por estado
            Map<String, Long> porEstado = todosPagos.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getEstado().name(),
                            Collectors.counting()
                    ));
            debug.put("pagosPorEstado", porEstado);

            // Agrupar por forma de pago
            Map<String, Long> porFormaPago = todosPagos.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getFormaPago().name(),
                            Collectors.counting()
                    ));
            debug.put("pagosPorFormaPago", porFormaPago);

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            return ResponseEntity.status(500).body(debug);
        }
    }
}