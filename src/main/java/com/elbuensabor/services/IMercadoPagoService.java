package com.elbuensabor.services;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.dto.response.MercadoPagoPreferenceResponseDTO;
import com.elbuensabor.dto.response.MercadoPagoPaymentResponseDTO;

public interface IMercadoPagoService {

    /**
     * Crear una preferencia de pago en Mercado Pago
     */
    MercadoPagoPreferenceResponseDTO crearPreferencia(MercadoPagoPreferenceDTO preferenceDTO);

    /**
     * Obtener información de un pago desde Mercado Pago
     */
    MercadoPagoPaymentResponseDTO obtenerPago(Long paymentId);

    /**
     * Procesar webhook de Mercado Pago
     */
    void procesarWebhook(String topic, String id);

    /**
     * Cancelar una preferencia de pago
     */
    void cancelarPreferencia(String preferenceId);

    /**
     * Procesar reembolso
     */
    void procesarReembolso(Long paymentId, Double amount);

    String getAccessToken();
}