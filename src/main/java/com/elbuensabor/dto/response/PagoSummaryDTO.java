package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoSummaryDTO {
    private Long idPago;
    private String formaPago;
    private String estado;
    private Double monto;
    private String fechaCreacion;
}