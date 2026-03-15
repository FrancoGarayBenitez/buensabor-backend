package com.elbuensabor.dto.response.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoResponse {

    private Long idDetallePedido;

    private Long idArticulo;
    private String nombreArticulo;
    private String imagenArticulo;

    private Integer cantidad;

    private Double precioUnitarioOriginal;
    private Double descuentoPromocion;
    private Double precioUnitarioFinal;
    private Double subtotal;

    // ✅ AGREGADO: idPromocion para el frontend
    private Long idPromocion;
    private String nombrePromocion;

    // ✅ AGREGADO: artículos del combo
    private List<ArticuloComboResponse> articulosCombo;

    private String observaciones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArticuloComboResponse {
        private Long idArticulo;
        private String denominacion;
        private Integer cantidad;
    }
}