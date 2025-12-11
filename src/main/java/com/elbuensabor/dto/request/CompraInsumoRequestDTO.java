package com.elbuensabor.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompraInsumoRequestDTO {
    private Long idArticuloInsumo;
    private Double cantidad;
    private Double precioUnitario;
    private LocalDate fechaCompra;
}
