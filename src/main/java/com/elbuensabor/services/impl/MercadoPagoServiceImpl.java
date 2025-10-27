package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.dto.response.MercadoPagoPreferenceResponseDTO;
import com.elbuensabor.dto.response.MercadoPagoPaymentResponseDTO;
import com.elbuensabor.services.IMercadoPagoService;
import com.elbuensabor.services.IPagoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MercadoPagoServiceImpl implements IMercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoServiceImpl.class);

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.public.key}")
    private String publicKey;

    @Value("${app.base.url}")
    private String baseUrl;

    @Autowired
    private IPagoService pagoService;

    private PreferenceClient preferenceClient;
    private PaymentClient paymentClient;

    private void initializeClients() {
        try {
            logger.info("=== INICIALIZANDO CLIENTES MERCADOPAGO ===");

            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new RuntimeException("Access token no configurado");
            }

            logger.info("Access Token: {}...", accessToken.substring(0, Math.min(15, accessToken.length())));
            logger.info("Base URL: {}", baseUrl);

            MercadoPagoConfig.setAccessToken(accessToken);

            preferenceClient = new PreferenceClient();
            paymentClient = new PaymentClient();

            logger.info("✅ Clientes inicializados correctamente");

        } catch (Exception e) {
            logger.error("❌ Error inicializando clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error inicializando MercadoPago: " + e.getMessage(), e);
        }
    }

    @Override
    public MercadoPagoPreferenceResponseDTO crearPreferencia(MercadoPagoPreferenceDTO preferenceDTO) {
        logger.info("=== INICIANDO CREACIÓN DE PREFERENCIA SANDBOX ===");

        try {
            // Validaciones básicas ANTES de llamar a MP
            validatePreferenceDTO(preferenceDTO);

            // Inicializar clientes
            initializeClients();

            // Log de datos de entrada
            logger.info("DTO recibido: {}", preferenceDTO);
            logger.info("Cantidad de items: {}", preferenceDTO.getItems().size());
            logger.info("Email del payer: {}", preferenceDTO.getPayer().getEmail());

            // Convertir items con validación individual
            List<PreferenceItemRequest> items = convertItems(preferenceDTO.getItems());
            logger.info("✅ Items convertidos exitosamente: {}", items.size());

            // Convertir payer con validación
            PreferencePayerRequest payer = convertPayer(preferenceDTO.getPayer());
            logger.info("✅ Payer convertido: {} {} - {}", payer.getName(), payer.getSurname(), payer.getEmail());

            // ✅ REQUEST SIMPLIFICADO - Igual al que funciona en diagnostic controller
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .externalReference(preferenceDTO.getExternalReference() != null ?
                            preferenceDTO.getExternalReference() : "PAGO_" + System.currentTimeMillis())
                    .build();

            // ❌ NO CONFIGURAR para sandbox (evitar URLs problemáticas):
            // - notificationUrl (causa problemas con localhost)
            // - backUrls (causa problemas con localhost)
            // - autoReturn (requiere backUrls válidas)

            logger.info("=== ENVIANDO REQUEST SIMPLIFICADO A MERCADOPAGO ===");
            logger.info("🔧 Modo: SANDBOX (sin notification URL ni back URLs)");

            // LLAMADA CRÍTICA A MERCADOPAGO
            Preference preference = preferenceClient.create(request);

            logger.info("🎉 ¡PREFERENCIA SANDBOX CREADA EXITOSAMENTE!");
            logger.info("Preference ID: {}", preference.getId());
            logger.info("Init Point: {}", preference.getInitPoint());
            logger.info("Sandbox Init Point: {}", preference.getSandboxInitPoint());

            return convertToPreferenceResponse(preference);

        } catch (MPApiException apiException) {
            // ERROR ESPECÍFICO DE LA API DE MERCADOPAGO
            logger.error("❌ ERROR DE API MERCADOPAGO ❌");
            logger.error("Status Code: {}", apiException.getStatusCode());
            logger.error("Message: {}", apiException.getMessage());

            // Intentar obtener el contenido de la respuesta
            String responseContent = "No disponible";
            try {
                if (apiException.getApiResponse() != null) {
                    responseContent = apiException.getApiResponse().getContent();
                    logger.error("API Response Content: {}", responseContent);
                }
            } catch (Exception e) {
                logger.error("No se pudo obtener contenido de respuesta: {}", e.getMessage());
            }

            // Crear mensaje de error detallado para el frontend
            String detailedMessage = String.format(
                    "Error de Mercado Pago - Status: %d, Mensaje: %s, Respuesta: %s",
                    apiException.getStatusCode(),
                    apiException.getMessage(),
                    responseContent
            );

            throw new RuntimeException(detailedMessage, apiException);

        } catch (MPException mpException) {
            // ERROR GENERAL DE MERCADOPAGO
            logger.error("❌ ERROR GENERAL MERCADOPAGO ❌");
            logger.error("Message: {}", mpException.getMessage());

            String detailedMessage = String.format(
                    "Error de MercadoPago SDK - Mensaje: %s",
                    mpException.getMessage()
            );

            throw new RuntimeException(detailedMessage, mpException);

        } catch (Exception e) {
            // ERROR INESPERADO
            logger.error("❌ ERROR INESPERADO ❌");
            logger.error("Class: {}", e.getClass().getSimpleName());
            logger.error("Message: {}", e.getMessage());
            logger.error("Stack trace completo:", e);

            String detailedMessage = String.format(
                    "Error inesperado - Tipo: %s, Mensaje: %s",
                    e.getClass().getSimpleName(),
                    e.getMessage()
            );

            throw new RuntimeException(detailedMessage, e);
        }
    }

    // MÉTODOS DE VALIDACIÓN Y CONVERSIÓN

    private void validatePreferenceDTO(MercadoPagoPreferenceDTO dto) {
        logger.info("Validando DTO...");

        if (dto == null) {
            throw new IllegalArgumentException("PreferenceDTO no puede ser null");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos un item");
        }

        if (dto.getPayer() == null) {
            throw new IllegalArgumentException("Información del payer es obligatoria");
        }

        if (dto.getPayer().getEmail() == null || !dto.getPayer().getEmail().contains("@")) {
            throw new IllegalArgumentException("Email del payer es inválido: " + dto.getPayer().getEmail());
        }

        // Validar cada item
        for (int i = 0; i < dto.getItems().size(); i++) {
            MercadoPagoPreferenceDTO.ItemDTO item = dto.getItems().get(i);
            if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Item " + i + ": título es obligatorio");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item " + i + ": cantidad debe ser mayor a 0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Item " + i + ": precio debe ser mayor a 0");
            }
        }

        logger.info("✅ DTO validado correctamente");
    }

    private List<PreferenceItemRequest> convertItems(List<MercadoPagoPreferenceDTO.ItemDTO> items) {
        return items.stream()
                .map(this::convertToPreferenceItem)
                .collect(Collectors.toList());
    }

    private PreferenceItemRequest convertToPreferenceItem(MercadoPagoPreferenceDTO.ItemDTO item) {
        logger.info("Convirtiendo item: {} - Cantidad: {} - Precio: {}",
                item.getTitle(), item.getQuantity(), item.getUnitPrice());

        return PreferenceItemRequest.builder()
                .title(item.getTitle())
                .quantity(item.getQuantity())
                .unitPrice(BigDecimal.valueOf(item.getUnitPrice()))
                .currencyId(item.getCurrencyId() != null ? item.getCurrencyId() : "ARS")
                .description(item.getDescription())
                .build();
    }

    private PreferencePayerRequest convertPayer(MercadoPagoPreferenceDTO.PayerDTO payer) {
        logger.info("Convirtiendo payer: {} {} - {}", payer.getName(), payer.getSurname(), payer.getEmail());

        return PreferencePayerRequest.builder()
                .name(payer.getName())
                .surname(payer.getSurname())
                .email(payer.getEmail())
                .build();
    }

    private PreferenceBackUrlsRequest createBackUrls() {
        String successUrl = baseUrl + "/payment/success";
        String failureUrl = baseUrl + "/payment/failure";
        String pendingUrl = baseUrl + "/payment/pending";

        logger.info("URLs de retorno:");
        logger.info("- Success: {}", successUrl);
        logger.info("- Failure: {}", failureUrl);
        logger.info("- Pending: {}", pendingUrl);

        return PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();
    }

    // RESTO DE MÉTODOS (sin cambios significativos)

    @Override
    public MercadoPagoPaymentResponseDTO obtenerPago(Long paymentId) {
        try {
            initializeClients();
            Payment payment = paymentClient.get(paymentId);
            return convertToPaymentResponse(payment);
        } catch (MPException | MPApiException e) {
            logger.error("Error obteniendo pago de Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error obteniendo información del pago", e);
        }
    }

    @Override
    public void procesarWebhook(String topic, String id) {
        try {
            logger.info("Procesando webhook - Topic: {}, ID: {}", topic, id);
            if ("payment".equals(topic)) {
                Long paymentId = Long.valueOf(id);
                MercadoPagoPaymentResponseDTO payment = obtenerPago(paymentId);
                pagoService.confirmarPagoMercadoPago(paymentId, payment.getStatus(), payment.getStatusDetail());
                logger.info("Webhook procesado exitosamente para payment ID: {}", paymentId);
            }
        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando webhook de Mercado Pago", e);
        }
    }

    @Override
    public void cancelarPreferencia(String preferenceId) {
        logger.info("Preference {} marcada para cancelación (expira automáticamente)", preferenceId);
    }

    @Override
    public void procesarReembolso(Long paymentId, Double amount) {
        try {
            initializeClients();
            logger.info("Procesando reembolso para payment {} por monto {}", paymentId, amount);
        } catch (Exception e) {
            logger.error("Error procesando reembolso: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando reembolso", e);
        }
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    private MercadoPagoPreferenceResponseDTO convertToPreferenceResponse(Preference preference) {
        MercadoPagoPreferenceResponseDTO response = new MercadoPagoPreferenceResponseDTO();
        response.setId(preference.getId());
        response.setInitPoint(preference.getInitPoint());
        response.setSandboxInitPoint(preference.getSandboxInitPoint());
        response.setClientId(preference.getClientId());
        response.setCollectorId(preference.getCollectorId());
        response.setOperationType(preference.getOperationType());
        response.setExternalReference(preference.getExternalReference());
        response.setNotificationUrl(preference.getNotificationUrl());
        return response;
    }

    private MercadoPagoPaymentResponseDTO convertToPaymentResponse(Payment payment) {
        MercadoPagoPaymentResponseDTO response = new MercadoPagoPaymentResponseDTO();
        response.setId(payment.getId());
        response.setStatus(payment.getStatus());
        response.setStatusDetail(payment.getStatusDetail());
        response.setOperationType(payment.getOperationType());
        response.setPaymentMethodId(payment.getPaymentMethodId());
        response.setPaymentTypeId(payment.getPaymentTypeId());
        response.setTransactionAmount(payment.getTransactionAmount().doubleValue());
        response.setCurrencyId(payment.getCurrencyId());
        response.setDateCreated(payment.getDateCreated().toString());
        response.setDateApproved(payment.getDateApproved() != null ? payment.getDateApproved().toString() : null);
        response.setExternalReference(payment.getExternalReference());

        if (payment.getPayer() != null) {
            MercadoPagoPaymentResponseDTO.PayerResponseDTO payer = new MercadoPagoPaymentResponseDTO.PayerResponseDTO();
            payer.setEmail(payment.getPayer().getEmail());
            payer.setFirstName(payment.getPayer().getFirstName());
            payer.setLastName(payment.getPayer().getLastName());
            response.setPayer(payer);
        }

        return response;
    }
}