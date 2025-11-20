package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrecioVentaSugeridoDTO {
    /**
     * Precio promedio histórico de COMPRA
     */
    private Double precioCompraPromedio;

    /**
     * Precio sugerido de VENTA basado en margen
     */
    private Double precioVentaSugerido;

    /**
     * Margen de ganancia aplicado (1.2 = 20% ganancia)
     */
    private Double margenGanancia;

    /**
     * Ganancia aproximada por unidad
     */
    private Double gananciaUnitaria;

    /**
     * Total de compras registradas en historial
     */
    private Integer totalCompras;

    /**
     * Rango histórico de precios de compra
     */
    private Double precioMinimoCompra;
    private Double precioMaximoCompra;

    /**
     * Mensaje descriptivo
     */
    private String mensaje;
}