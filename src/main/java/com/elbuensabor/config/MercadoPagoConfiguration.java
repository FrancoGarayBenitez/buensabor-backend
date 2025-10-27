package com.elbuensabor.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource("classpath:mercadopago.properties")
@Slf4j
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.sandbox.mode:true}")
    private boolean sandboxMode;

    @Value("${mercadopago.success.url}")
    private String successUrl;

    @Value("${mercadopago.failure.url}")
    private String failureUrl;

    @Value("${mercadopago.pending.url}")
    private String pendingUrl;

    @PostConstruct
    public void initialize() {
        log.info("=== INICIALIZANDO MERCADOPAGO ===");

        // Validar que el access token esté configurado
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("ERROR: Access token de MercadoPago no configurado!");
            throw new IllegalStateException("mercadopago.access.token no está configurado en mercadopago.properties");
        }

        // Validar formato del access token
        if (!accessToken.startsWith("TEST-") && !accessToken.startsWith("APP_USR-")) {
            log.warn("ADVERTENCIA: El access token podría no tener el formato correcto");
            log.warn("Tokens de test deben empezar con 'TEST-' y de producción con 'APP_USR-'");
        }

        // Configurar token de acceso
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Access token configurado: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));

        // Configurar modo sandbox
        if (sandboxMode) {
            System.setProperty("mercadopago.sdk.environment", "sandbox");
            log.info("Modo SANDBOX activado");
        } else {
            log.info("Modo PRODUCCIÓN activado");
        }

        // Validar URLs
        log.info("URLs configuradas:");
        log.info("- Success: {}", successUrl);
        log.info("- Failure: {}", failureUrl);
        log.info("- Pending: {}", pendingUrl);

        // Verificar que las URLs sean accesibles (opcional)
        if (successUrl == null || !successUrl.startsWith("http")) {
            log.warn("URL de éxito podría no ser válida: {}", successUrl);
        }

        log.info("=== MERCADOPAGO CONFIGURADO CORRECTAMENTE ===");
    }

    // Getters sin cambios
    public String getSuccessUrl() { return successUrl; }
    public String getFailureUrl() { return failureUrl; }
    public String getPendingUrl() { return pendingUrl; }
    public boolean isSandboxMode() { return sandboxMode; }

    // Getter adicional para debugging
    public String getAccessTokenInfo() {
        return accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "NO CONFIGURADO";
    }
}