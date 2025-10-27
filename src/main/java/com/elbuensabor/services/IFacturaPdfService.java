package com.elbuensabor.services;

import com.elbuensabor.dto.response.FacturaResponseDTO;

public interface IFacturaPdfService {

    /**
     * Genera un PDF de la factura basado en el ID de la factura
     * @param facturaId ID de la factura
     * @return Array de bytes del PDF generado
     */
    byte[] generarFacturaPdf(Long facturaId);

    /**
     * Genera un PDF de la factura basado en el DTO de respuesta
     * @param facturaDTO DTO con datos de la factura
     * @return Array de bytes del PDF generado
     */
    byte[] generarFacturaPdf(FacturaResponseDTO facturaDTO);

    /**
     * Genera un PDF de la factura por ID de pedido
     * @param pedidoId ID del pedido
     * @return Array de bytes del PDF generado
     */
    byte[] generarFacturaPdfByPedidoId(Long pedidoId);
}