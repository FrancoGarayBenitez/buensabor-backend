package com.elbuensabor.services;

import com.elbuensabor.entities.Pago;
import com.elbuensabor.entities.EstadoPago;
import com.elbuensabor.entities.FormaPago;
import com.elbuensabor.dto.request.PagoRequestDTO;
import com.elbuensabor.dto.response.PagoResponseDTO;

import java.util.List;

public interface IPagoService extends IGenericService<Pago, Long, PagoResponseDTO> {

    // Crear un nuevo pago usando RequestDTO
    PagoResponseDTO crearPago(PagoRequestDTO pagoRequestDTO);

    // Actualizar estado de pago
    PagoResponseDTO actualizarEstadoPago(Long pagoId, EstadoPago nuevoEstado);

    // Procesar pago con Mercado Pago
    PagoResponseDTO procesarPagoMercadoPago(Long pagoId, String preferenceId);

    // Confirmar pago de Mercado Pago (webhook)
    PagoResponseDTO confirmarPagoMercadoPago(Long paymentId, String status, String statusDetail);

    // Obtener pagos por factura
    List<PagoResponseDTO> getPagosByFactura(Long facturaId);

    // Obtener pagos por estado
    List<PagoResponseDTO> getPagosByEstado(EstadoPago estado);

    // Obtener pagos por forma de pago
    List<PagoResponseDTO> getPagosByFormaPago(FormaPago formaPago);

    // Verificar si una factura está completamente pagada
    boolean isFacturaCompletamentePagada(Long facturaId);

    // Obtener total pagado de una factura
    Double getTotalPagadoFactura(Long facturaId);

    // Obtener saldo pendiente de una factura
    Double getSaldoPendienteFactura(Long facturaId);

    // Cancelar pago
    PagoResponseDTO cancelarPago(Long pagoId);

    // Procesar reembolso
    PagoResponseDTO procesarReembolso(Long pagoId);
}