package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoResponseDTO {
    private Long idDetallePedido;
    private Long idArticulo;
    private String denominacionArticulo;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private String unidadMedida;
    private Integer tiempoPreparacion; // minutos
    private String observaciones;

    // ✅ NUEVOS: Campos para promociones
    private Double precioUnitarioOriginal;    // Precio sin promoción
    private Double descuentoPromocion;        // Monto del descuento aplicado
    private Double precioUnitarioFinal;       // Precio después del descuento
    private Boolean tienePromocion;           // Si tiene promoción aplicada

    // Detalle de la promoción aplicada
    private PromocionAplicadaDTO promocionAplicada;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromocionAplicadaDTO {
        private Long idPromocion;
        private String denominacion;
        private String descripcion;
        private String tipoDescuento; // "PORCENTUAL" o "MONTO_FIJO"
        private Double valorDescuento;
        private String resumenDescuento; // "15% descuento - Ahorro: $150"
    }

    // ✅ MÉTODOS DE CONVENIENCIA (calculados automáticamente)
    public Double getSubtotalOriginal() {
        if (precioUnitarioOriginal == null || cantidad == null) {
            // Fallback: usar el precio actual si no hay precio original
            return (precioUnitario != null && cantidad != null)
                    ? precioUnitario * cantidad
                    : 0.0;
        }
        return precioUnitarioOriginal * cantidad;
    }


    public Double getAhorroTotal() {
        return descuentoPromocion != null ? descuentoPromocion : 0.0;
    }

    public Double getPorcentajeAhorro() {
        if (precioUnitarioOriginal == null || precioUnitarioOriginal == 0 || descuentoPromocion == null) {
            return 0.0;
        }
        return (descuentoPromocion / (precioUnitarioOriginal * cantidad)) * 100;
    }
    // ✅ MÉTODO ADICIONAL: Verificar si tiene datos válidos
    public Boolean tieneDatosCompletos() {
        return precioUnitario != null &&
                cantidad != null &&
                cantidad > 0;
    }

    // ✅ MÉTODO ADICIONAL: Obtener subtotal actual (con o sin promoción)
    public Double getSubtotalActual() {
        if (precioUnitarioFinal != null && cantidad != null) {
            return precioUnitarioFinal * cantidad;
        }
        return (precioUnitario != null && cantidad != null)
                ? precioUnitario * cantidad
                : 0.0;
    }
}