package com.elbuensabor.dto.response;

import com.elbuensabor.entities.TipoDescuento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionCalculoDTO {

    private Double descuentoTotal;
    private List<DetalleDescuentoDTO> detallesDescuentos;
    private String resumenDescuentos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleDescuentoDTO {
        private Long idPromocion;
        private String denominacionPromocion;
        private Long idArticulo;
        private Double montoDescuento;
        private TipoDescuento tipoDescuento;
        private Double valorDescuento;
        private String descripcion;
    }

    public String generarResumen() {
        if (detallesDescuentos == null || detallesDescuentos.isEmpty()) {
            return "Sin promociones aplicadas";
        }

        StringBuilder resumen = new StringBuilder("Promociones aplicadas: ");
        for (DetalleDescuentoDTO detalle : detallesDescuentos) {
            resumen.append(String.format("%s (-$%.2f), ",
                    detalle.getDenominacionPromocion(),
                    detalle.getMontoDescuento()));
        }

        // Remover última coma y espacio
        if (resumen.length() > 2) {
            resumen.setLength(resumen.length() - 2);
        }

        resumen.append(String.format(". Total descuento: $%.2f", descuentoTotal));
        return resumen.toString();
    }
}