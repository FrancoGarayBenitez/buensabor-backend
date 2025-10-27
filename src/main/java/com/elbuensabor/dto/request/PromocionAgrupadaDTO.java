package com.elbuensabor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionAgrupadaDTO {
    private Long idPromocion;
    private String denominacion;
    private String tipoDescuento; // "PORCENTUAL" o "FIJO"
    private Double valorDescuento;
    private String descripcion;
    private Double descuentoAplicado;
    private Double subtotalOriginal;
    private Double subtotalConDescuento;
}