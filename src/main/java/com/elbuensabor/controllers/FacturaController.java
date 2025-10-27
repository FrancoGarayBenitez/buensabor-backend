package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.services.IFacturaPdfService;
import com.elbuensabor.services.IFacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private IFacturaPdfService facturaPdfService;

    // ==================== ENDPOINTS BÁSICOS ====================

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> getAllFacturas() {
        List<FacturaResponseDTO> facturas = facturaService.findAll();
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> getFacturaById(@PathVariable Long id) {
        FacturaResponseDTO factura = facturaService.findById(id);
        return ResponseEntity.ok(factura);
    }

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<FacturaResponseDTO> getFacturaByPedido(@PathVariable Long pedidoId) {
        FacturaResponseDTO factura = facturaService.findByPedidoId(pedidoId);
        return ResponseEntity.ok(factura);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<FacturaResponseDTO>> getFacturasByCliente(@PathVariable Long clienteId) {
        List<FacturaResponseDTO> facturas = facturaService.findByClienteId(clienteId);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/fecha-rango")
    public ResponseEntity<List<FacturaResponseDTO>> getFacturasByFechaRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<FacturaResponseDTO> facturas = facturaService.findByFechaRange(fechaInicio, fechaFin);
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/pendientes-pago")
    public ResponseEntity<List<FacturaResponseDTO>> getFacturasPendientesPago() {
        List<FacturaResponseDTO> facturas = facturaService.findFacturasPendientesPago();
        return ResponseEntity.ok(facturas);
    }

    // ==================== OPERACIONES ESPECÍFICAS ====================

    @GetMapping("/exists/pedido/{pedidoId}")
    public ResponseEntity<Boolean> existeFacturaParaPedido(@PathVariable Long pedidoId) {
        boolean existe = facturaService.existeFacturaParaPedido(pedidoId);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/generar-numero-comprobante")
    public ResponseEntity<String> generarNumeroComprobante() {
        String numeroComprobante = facturaService.generarNumeroComprobante();
        return ResponseEntity.ok(numeroComprobante);
    }

    // ==================== 🎯 NUEVOS ENDPOINTS PARA PDF ====================

    /**
     * Descargar factura en formato PDF por ID de factura
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable Long id) {
        try {
            logger.info("Solicitud de descarga PDF para factura ID: {}", id);

            // Obtener datos de la factura para el nombre del archivo
            FacturaResponseDTO factura = facturaService.findById(id);

            // Generar PDF
            byte[] pdfBytes = facturaPdfService.generarFacturaPdf(id);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "factura_" + factura.getNroComprobante() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            logger.info("PDF generado exitosamente para factura {}: {} bytes",
                    factura.getNroComprobante(), pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generando PDF para factura {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Descargar factura en formato PDF por ID de pedido
     */
    @GetMapping("/pedido/{pedidoId}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdfByPedido(@PathVariable Long pedidoId) {
        try {
            logger.info("Solicitud de descarga PDF para pedido ID: {}", pedidoId);

            // Obtener datos de la factura para el nombre del archivo
            FacturaResponseDTO factura = facturaService.findByPedidoId(pedidoId);

            // Generar PDF
            byte[] pdfBytes = facturaPdfService.generarFacturaPdfByPedidoId(pedidoId);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "factura_pedido_" + pedidoId + "_" + factura.getNroComprobante() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            logger.info("PDF generado exitosamente para pedido {}: {} bytes",
                    pedidoId, pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generando PDF para pedido {}: {}", pedidoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Vista previa de factura PDF (inline en navegador)
     */
    @GetMapping("/{id}/pdf/preview")
    public ResponseEntity<byte[]> previsualizarFacturaPdf(@PathVariable Long id) {
        try {
            logger.info("Solicitud de preview PDF para factura ID: {}", id);

            // Generar PDF
            byte[] pdfBytes = facturaPdfService.generarFacturaPdf(id);

            // Configurar headers para mostrar en navegador
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "inline; filename=factura_preview.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generando preview PDF para factura {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}