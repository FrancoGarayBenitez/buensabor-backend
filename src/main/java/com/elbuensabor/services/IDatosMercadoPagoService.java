package com.elbuensabor.services;

import com.elbuensabor.entities.DatosMercadoPago;
import com.elbuensabor.entities.Pago;

import java.util.Optional;

public interface IDatosMercadoPagoService {

    /**
     * Crear datos iniciales de MercadoPago para un pago (por ID)
     * Método conveniente que busca el pago por ID
     */
    DatosMercadoPago crearDatosInicialesPorPagoId(Long pagoId);

    /**
     * Crear datos iniciales de MercadoPago para un pago
     * Se usa cuando se crea la preferencia pero aún no hay payment_id
     */
    DatosMercadoPago crearDatosIniciales(Pago pago);

    /**
     * Actualizar datos de MercadoPago cuando se recibe información del webhook
     */
    DatosMercadoPago actualizarDatosDelPago(Long paymentId, String status, String statusDetail,
                                            String paymentMethodId, String paymentTypeId);

    /**
     * Buscar datos por payment ID de MercadoPago
     */
    Optional<DatosMercadoPago> buscarPorPaymentId(Long paymentId);

    /**
     * Buscar datos por ID de pago interno
     */
    Optional<DatosMercadoPago> buscarPorPagoId(Long pagoId);

    /**
     * Eliminar datos de MercadoPago por ID de pago
     */
    void eliminarPorPagoId(Long pagoId);
}