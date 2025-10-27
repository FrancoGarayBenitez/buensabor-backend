package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoConMercadoPagoResponseDTO {

    // ==================== INFORMACIÓN DEL PEDIDO CREADO ====================
    private PedidoResponseDTO pedido;

    // ==================== INFORMACIÓN DE LA FACTURA GENERADA ====================
    private FacturaResponseDTO factura;

    // ==================== DESGLOSE DE TOTALES CON DESCUENTOS ====================
    private CalculoTotalesDTO calculoTotales;

    // ==================== INFORMACIÓN DE MERCADO PAGO ====================
    private MercadoPagoInfoDTO mercadoPago;

    // ==================== ESTADO GENERAL ====================
    private Boolean exito;
    private String mensaje;
    private Long tiempoProcesamientoMs;

    // ==================== DTOs AUXILIARES ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculoTotalesDTO {
        private Double subtotalProductos;      // Total de productos sin descuentos
        private Double descuentoTakeAway;      // Monto del descuento aplicado (si aplica)
        private Double porcentajeDescuento;    // % de descuento aplicado
        private Double gastosEnvio;            // Gastos de envío (si es DELIVERY)
        private Double totalFinal;             // Total final a pagar
        private String tipoEnvio;              // TAKE_AWAY o DELIVERY
        private String resumenCalculo;         // Descripción del cálculo
        private Boolean seAplicoDescuento;     // Si se aplicó descuento
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MercadoPagoInfoDTO {
        private Boolean preferenciaCreada;     // Si se creó la preferencia
        private String preferenceId;           // ID de la preferencia
        private String linkPago;               // URL para pagar (initPoint)
        private String linkPagoSandbox;        // URL de sandbox para testing
        private String externalReference;      // Referencia externa
        private String qrCodeUrl;              // URL del QR (si está disponible)
        private String errorMercadoPago;       // Error si falló la creación
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    public static PedidoConMercadoPagoResponseDTO exitoso(
            PedidoResponseDTO pedido,
            FacturaResponseDTO factura,
            CalculoTotalesDTO totales,
            MercadoPagoInfoDTO mercadoPago,
            Long tiempoProcesamiento) {

        return new PedidoConMercadoPagoResponseDTO(
                pedido,
                factura,
                totales,
                mercadoPago,
                true,
                "Pedido creado exitosamente con link de pago",
                tiempoProcesamiento
        );
    }

    public static PedidoConMercadoPagoResponseDTO conError(
            String mensaje,
            Long tiempoProcesamiento) {

        return new PedidoConMercadoPagoResponseDTO(
                null,
                null,
                null,
                null,
                false,
                mensaje,
                tiempoProcesamiento
        );
    }

    public static PedidoConMercadoPagoResponseDTO parcialmenteExitoso(
            PedidoResponseDTO pedido,
            FacturaResponseDTO factura,
            CalculoTotalesDTO totales,
            String errorMercadoPago,
            Long tiempoProcesamiento) {

        MercadoPagoInfoDTO mpInfo = new MercadoPagoInfoDTO();
        mpInfo.setPreferenciaCreada(false);
        mpInfo.setErrorMercadoPago(errorMercadoPago);

        return new PedidoConMercadoPagoResponseDTO(
                pedido,
                factura,
                totales,
                mpInfo,
                true,
                "Pedido creado exitosamente, pero falló la creación del link de pago: " + errorMercadoPago,
                tiempoProcesamiento
        );
    }
}