package com.elbuensabor.dto.response;

import com.elbuensabor.entities.EstadoPago;
import com.elbuensabor.entities.FormaPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {

    private Long idPago;
    private Long facturaId;
    private FormaPago formaPago;
    private EstadoPago estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Double monto;
    private String moneda;

    // ✅ NUEVO: Observaciones generales
    private String observaciones;

    // Datos de Mercado Pago (si aplica)
    private DatosMercadoPagoDTO datosMercadoPago;

    // Los detalles ya incluirán sus observaciones automáticamente
    private List<DetallePedidoResponseDTO> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatosMercadoPagoDTO {
        private Long paymentId;
        private String status;
        private String statusDetail;
        private String paymentMethodId;
        private String paymentTypeId;
        private LocalDateTime dateCreated;
        private LocalDateTime dateApproved;
    }
}