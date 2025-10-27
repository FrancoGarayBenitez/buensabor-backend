package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.dto.response.MercadoPagoPreferenceResponseDTO;
import com.elbuensabor.dto.response.MercadoPagoPaymentResponseDTO;
import com.elbuensabor.services.IMercadoPagoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.resources.preference.Preference;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/mercadopago")

public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    @Autowired
    private IMercadoPagoService mercadoPagoService;

    // ENDPOINT DE PRUEBA PARA VERIFICAR QUE FUNCIONA
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("MercadoPago Controller funcionando correctamente");
    }

    // ENDPOINT PARA VERIFICAR CREDENCIALES SIMPLE
    @GetMapping("/test-token")
    public ResponseEntity<Map<String, Object>> testToken() {
        try {
            logger.info("=== PROBANDO TOKEN DE MP ===");

            String token = mercadoPagoService.getAccessToken();
            logger.info("Token configurado: {}...", token.substring(0, Math.min(15, token.length())));

            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(token);

            // Crear el cliente más simple posible
            PreferenceClient client = new PreferenceClient();

            // Crear request mínimo
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Token")
                    .quantity(1)
                    .unitPrice(new java.math.BigDecimal("100.00"))
                    .currencyId("ARS")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .build();

            // Intentar crear preferencia
            Preference preference = client.create(request);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS ✅",
                    "preferenceId", preference.getId(),
                    "initPoint", preference.getInitPoint(),
                    "message", "Token válido - MercadoPago conectado correctamente"
            ));

        } catch (Exception e) {
            logger.error("Error probando token: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "status", "ERROR ❌",
                    "message", e.getMessage(),
                    "class", e.getClass().getSimpleName(),
                    "suggestion", "Verifica tu access token en application.properties"
            ));
        }
    }

    @PostMapping("/crear-preferencia")
    public ResponseEntity<MercadoPagoPreferenceResponseDTO> crearPreferencia(
            @Valid @RequestBody MercadoPagoPreferenceDTO preferenceDTO) {
        try {
            logger.info("=== CREANDO PREFERENCIA MP ===");
            logger.info("Preference DTO recibido: {}", preferenceDTO);

            MercadoPagoPreferenceResponseDTO response = mercadoPagoService.crearPreferencia(preferenceDTO);
            logger.info("Preferencia creada exitosamente: {}", response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creando preferencia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pago/{paymentId}")
    public ResponseEntity<MercadoPagoPaymentResponseDTO> obtenerPago(@PathVariable Long paymentId) {
        try {
            MercadoPagoPaymentResponseDTO payment = mercadoPagoService.obtenerPago(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            logger.error("Error obteniendo pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestParam(name = "topic", required = false) String topic,
            @RequestParam(name = "id", required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            logger.info("Webhook recibido - Topic: {}, ID: {}", topic, id);

            // Mercado Pago puede enviar el webhook de diferentes formas
            if (topic != null && id != null) {
                mercadoPagoService.procesarWebhook(topic, id);
            } else if (body != null && body.containsKey("type") && body.containsKey("data")) {
                // Formato alternativo del webhook
                String webhookTopic = (String) body.get("type");
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String webhookId = data.get("id").toString();
                mercadoPagoService.procesarWebhook(webhookTopic, webhookId);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
        }
    }

    @PostMapping("/reembolso/{paymentId}")
    public ResponseEntity<String> procesarReembolso(
            @PathVariable Long paymentId,
            @RequestBody Map<String, Double> request) {
        try {
            Double amount = request.get("amount");
            mercadoPagoService.procesarReembolso(paymentId, amount);
            return ResponseEntity.ok("Reembolso procesado exitosamente");
        } catch (Exception e) {
            logger.error("Error procesando reembolso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando reembolso");
        }
    }

    @PostMapping("/sandbox-test")
    public ResponseEntity<Map<String, Object>> crearPreferenciaSandboxTest() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== CREANDO PREFERENCIA TEST PARA SANDBOX ===");

            String token = mercadoPagoService.getAccessToken();
            logger.info("Token configurado: {}...", token.substring(0, Math.min(15, token.length())));

            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(token);

            // Crear el cliente
            PreferenceClient client = new PreferenceClient();

            // ✅ ITEM SIMPLIFICADO
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Sandbox El Buen Sabor")
                    .quantity(1)
                    .unitPrice(new java.math.BigDecimal("100.00"))
                    .currencyId("ARS")
                    .description("Prueba sandbox sin URLs problemáticas")
                    .build();

            // ✅ PAYER SIMPLIFICADO
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name("Test")
                    .surname("Sandbox")
                    .email("test@sandbox.com")
                    .build();

            // ✅ REQUEST ULTRA-SIMPLIFICADO - Sin URLs problemáticas
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .externalReference("SANDBOX_TEST_" + System.currentTimeMillis())
                    .build();

            // ❌ NO agregamos:
            // - .notificationUrl()
            // - .backUrls()
            // - .autoReturn()

            logger.info("Enviando request sandbox sin URLs...");

            // Intentar crear preferencia
            Preference preference = client.create(request);

            logger.info("✅ ¡PREFERENCIA SANDBOX CREADA!");

            result.put("status", "SUCCESS ✅");
            result.put("preferenceId", preference.getId());
            result.put("sandboxLink", preference.getSandboxInitPoint());
            result.put("productionLink", preference.getInitPoint());
            result.put("message", "¡Preferencia sandbox creada exitosamente!");
            result.put("instructions", "Usa el sandboxLink para probar el pago");
            result.put("testCard", Map.of(
                    "number", "4509 9535 6623 3704",
                    "cvv", "123",
                    "expiry", "11/25",
                    "name", "APRO"
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Error en test sandbox: {}", e.getMessage(), e);

            result.put("status", "ERROR ❌");
            result.put("error", e.getMessage());
            result.put("class", e.getClass().getSimpleName());
            result.put("suggestion", "Verificar credenciales de sandbox y configuración");

            return ResponseEntity.status(500).body(result);
        }
    }

}