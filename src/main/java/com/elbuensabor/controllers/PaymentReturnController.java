package com.elbuensabor.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")

public class PaymentReturnController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentReturnController.class);

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(
            @RequestParam Map<String, String> params) {

        logger.info("=== PAGO EXITOSO ===");
        logger.info("Parámetros recibidos: {}", params);

        // Extraer información importante
        String collectionId = params.get("collection_id");
        String collectionStatus = params.get("collection_status");
        String paymentId = params.get("payment_id");
        String status = params.get("status");
        String externalReference = params.get("external_reference");
        String paymentType = params.get("payment_type");
        String merchantOrderId = params.get("merchant_order_id");
        String preferenceId = params.get("preference_id");
        String siteId = params.get("site_id");
        String processingMode = params.get("processing_mode");
        String merchantAccountId = params.get("merchant_account_id");

        logger.info("Payment ID: {}", paymentId);
        logger.info("Status: {}", status);
        logger.info("External Reference: {}", externalReference);

        // Aquí puedes procesar la confirmación del pago
        // Por ejemplo, actualizar el estado del pedido

        return ResponseEntity.ok("¡Pago realizado exitosamente! Payment ID: " + paymentId);
    }

    @GetMapping("/failure")
    public ResponseEntity<String> paymentFailure(
            @RequestParam Map<String, String> params) {

        logger.warn("=== PAGO FALLIDO ===");
        logger.warn("Parámetros recibidos: {}", params);

        String collectionId = params.get("collection_id");
        String collectionStatus = params.get("collection_status");
        String paymentId = params.get("payment_id");
        String status = params.get("status");
        String externalReference = params.get("external_reference");

        logger.warn("Payment ID: {}", paymentId);
        logger.warn("Status: {}", status);
        logger.warn("External Reference: {}", externalReference);

        return ResponseEntity.ok("El pago no pudo ser procesado. Intente nuevamente.");
    }

    @GetMapping("/pending")
    public ResponseEntity<String> paymentPending(
            @RequestParam Map<String, String> params) {

        logger.info("=== PAGO PENDIENTE ===");
        logger.info("Parámetros recibidos: {}", params);

        String collectionId = params.get("collection_id");
        String collectionStatus = params.get("collection_status");
        String paymentId = params.get("payment_id");
        String status = params.get("status");
        String externalReference = params.get("external_reference");

        logger.info("Payment ID: {}", paymentId);
        logger.info("Status: {}", status);
        logger.info("External Reference: {}", externalReference);

        return ResponseEntity.ok("Su pago está siendo procesado. Le notificaremos cuando esté aprobado.");
    }
}