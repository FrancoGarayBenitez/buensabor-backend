package com.elbuensabor.dto.response;

import com.elbuensabor.entities.Promocion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionCalculoDTO {
    private Double descuentoTotal = 0.0;
    private List<DetalleDescuentoDTO> detallesDescuentos = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleDescuentoDTO {
        private Long idPromocion;
        private String denominacionPromocion;
        private Long idArticulo;
        private Double montoDescuento;
        private Promocion.TipoDescuento tipoDescuento; // ✅ Usar Promocion.TipoDescuento
        private Double valorDescuento;
    }
}