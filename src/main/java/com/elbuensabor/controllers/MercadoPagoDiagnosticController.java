package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.services.IMercadoPagoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mp-debug")
public class MercadoPagoDiagnosticController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoDiagnosticController.class);

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.public.key}")
    private String publicKey;

    @Value("${app.base.url}")
    private String baseUrl;

    @Autowired
    private IMercadoPagoService mercadoPagoService;

    // ========== TEST BÁSICO SIN COMPLICACIONES ==========

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Controller funcionando correctamente ✅");
    }

    // ========== TESTS DE CONFIGURACIÓN ==========

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> checkConfig() {
        Map<String, Object> config = new HashMap<>();

        try {
            config.put("accessToken", accessToken != null ?
                    accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "NO CONFIGURADO");
            config.put("publicKey", publicKey != null ?
                    publicKey.substring(0, Math.min(20, publicKey.length())) + "..." : "NO CONFIGURADO");
            config.put("baseUrl", baseUrl);
            config.put("tokenFormat", accessToken != null && accessToken.startsWith("APP_USR-") ? "CORRECTO" : "INCORRECTO");
            config.put("status", "OK");

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            config.put("error", e.getMessage());
            config.put("status", "ERROR");
            return ResponseEntity.status(500).body(config);
        }
    }

    // ========== TEST BÁSICO DE SDK ==========

    @GetMapping("/test-sdk")
    public ResponseEntity<Map<String, Object>> testSDK() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== TESTING SDK BÁSICO ===");

            // Configurar token
            MercadoPagoConfig.setAccessToken(accessToken);
            result.put("tokenConfigured", "✅ Token configurado");

            // Crear cliente
            PreferenceClient client = new PreferenceClient();
            result.put("clientCreated", "✅ Cliente creado");

            result.put("status", "SUCCESS");
            result.put("message", "SDK funcionando correctamente");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error en test SDK: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("status", "ERROR");
            return ResponseEntity.status(500).body(result);
        }
    }

    // ========== TEST SUPER SIMPLE ==========

    @PostMapping("/test-simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== EJECUTANDO TEST SUPER SIMPLE ===");

            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PreferenceClient client = new PreferenceClient();

            // Crear item mínimo
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Item Simple")
                    .quantity(1)
                    .unitPrice(new BigDecimal("100.00"))
                    .currencyId("ARS")
                    .build();

            // Crear payer mínimo
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email("test@example.com")
                    .build();

            // Request mínimo
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .build();

            logger.info("Enviando request mínimo a MercadoPago...");

            // LLAMADA CRÍTICA
            Preference preference = client.create(request);

            logger.info("✅ ¡Preferencia creada exitosamente!");

            result.put("status", "SUCCESS");
            result.put("preferenceId", preference.getId());
            result.put("initPoint", preference.getInitPoint());
            result.put("sandboxInitPoint", preference.getSandboxInitPoint());
            result.put("message", "¡Test simple exitoso! MercadoPago funciona correctamente");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Error en test simple:", e);

            result.put("status", "ERROR");
            result.put("errorType", e.getClass().getSimpleName());
            result.put("errorMessage", e.getMessage());

            // Información adicional para errores de MP
            if (e instanceof com.mercadopago.exceptions.MPApiException) {
                com.mercadopago.exceptions.MPApiException apiEx = (com.mercadopago.exceptions.MPApiException) e;
                result.put("statusCode", apiEx.getStatusCode());

                try {
                    if (apiEx.getApiResponse() != null) {
                        result.put("apiResponse", apiEx.getApiResponse().getContent());
                    }
                } catch (Exception ex) {
                    result.put("apiResponseError", "No se pudo obtener respuesta de API");
                }
            }

            return ResponseEntity.status(500).body(result);
        }
    }

    // ========== TEST CON DATOS COMPLETOS ==========

    @PostMapping("/test-complete")
    public ResponseEntity<Map<String, Object>> testComplete(@RequestBody Map<String, Object> testData) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== EJECUTANDO TEST COMPLETO ===");
            logger.info("Datos recibidos: {}", testData);

            // Extraer datos del request
            String title = (String) testData.getOrDefault("title", "Test Product");
            Double price = Double.valueOf(testData.getOrDefault("price", 100.0).toString());
            String email = (String) testData.getOrDefault("email", "test@example.com");
            String name = (String) testData.getOrDefault("name", "Test");
            String surname = (String) testData.getOrDefault("surname", "User");

            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PreferenceClient client = new PreferenceClient();

            // Crear item
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(1)
                    .unitPrice(new BigDecimal(price))
                    .currencyId("ARS")
                    .description("Test desde El Buen Sabor")
                    .build();

            // Crear payer
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(name)
                    .surname(surname)
                    .email(email)
                    .build();

            // Request completo
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .externalReference("TEST_" + System.currentTimeMillis())
                    .build();

            logger.info("Enviando request completo a MercadoPago...");

            // LLAMADA CRÍTICA
            Preference preference = client.create(request);

            logger.info("✅ ¡Preferencia completa creada exitosamente!");

            result.put("status", "SUCCESS");
            result.put("preferenceId", preference.getId());
            result.put("initPoint", preference.getInitPoint());
            result.put("sandboxInitPoint", preference.getSandboxInitPoint());
            result.put("externalReference", preference.getExternalReference());
            result.put("clientId", preference.getClientId());
            result.put("collectorId", preference.getCollectorId());
            result.put("message", "¡Test completo exitoso!");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Error en test completo:", e);

            result.put("status", "ERROR");
            result.put("errorType", e.getClass().getSimpleName());
            result.put("errorMessage", e.getMessage());
            result.put("requestData", testData);

            // Información adicional para errores de MP
            if (e instanceof com.mercadopago.exceptions.MPApiException) {
                com.mercadopago.exceptions.MPApiException apiEx = (com.mercadopago.exceptions.MPApiException) e;
                result.put("statusCode", apiEx.getStatusCode());

                try {
                    if (apiEx.getApiResponse() != null) {
                        result.put("apiResponse", apiEx.getApiResponse().getContent());
                    }
                } catch (Exception ex) {
                    result.put("apiResponseError", "No se pudo obtener respuesta de API");
                }
            }

            return ResponseEntity.status(500).body(result);
        }
    }

    // ========== TEST DE CONECTIVIDAD ==========

    @GetMapping("/test-connectivity")
    public ResponseEntity<Map<String, Object>> testConnectivity() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== TESTING CONECTIVIDAD ===");

            // Test de configuración de SDK
            MercadoPagoConfig.setAccessToken(accessToken);

            // Verificar que el token no esté vacío
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new RuntimeException("Access token no configurado");
            }

            // Verificar formato del token
            if (!accessToken.startsWith("APP_USR-") && !accessToken.startsWith("TEST-")) {
                result.put("warning", "Token no tiene formato esperado (APP_USR- o TEST-)");
            }

            result.put("tokenFormat", accessToken.startsWith("APP_USR-") ? "PRODUCTION" :
                    accessToken.startsWith("TEST-") ? "TEST" : "UNKNOWN");
            result.put("tokenLength", accessToken.length());
            result.put("baseUrl", baseUrl);
            result.put("status", "OK");
            result.put("message", "Configuración básica OK");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error en test de conectividad: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
            result.put("status", "ERROR");
            return ResponseEntity.status(500).body(result);
        }
    }
}