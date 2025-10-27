package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long idPedido;
    private LocalDateTime fecha;
    private LocalTime horaEstimadaFinalizacion;
    private Double total;
    private String estado;
    private String tipoEnvio;
    private String observaciones;

    // Información del cliente
    private Long idCliente;
    private String nombreCliente;
    private String apellidoCliente;
    private String telefonoCliente;

    // Información del domicilio (si es delivery)
    private DomicilioResponseDTO domicilio;

    // Detalles del pedido
    private List<DetallePedidoResponseDTO> detalles;

    // Información adicional
    private Integer tiempoEstimadoTotal; // en minutos
    private Boolean stockSuficiente;

    // ✅ NUEVOS: Resumen de promociones del pedido
    private ResumenPromocionesDTO resumenPromociones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenPromocionesDTO {
        private Double subtotalOriginal;      // Total sin promociones
        private Double totalDescuentos;       // Total de descuentos aplicados
        private Double subtotalConDescuentos; // Total después de promociones
        private Integer cantidadPromociones;  // Número de promociones aplicadas
        private List<String> nombresPromociones; // Lista de promociones aplicadas
        private String resumenTexto;          // "3 promociones aplicadas - Ahorro: $450"
    }
}