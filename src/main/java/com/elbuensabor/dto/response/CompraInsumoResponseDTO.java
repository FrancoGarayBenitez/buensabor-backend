package com.elbuensabor.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CompraInsumoResponseDTO {
    private Long id;
    private Long idArticuloInsumo;
    private String denominacionInsumo;
    private Double cantidad;
    private Double precioUnitario;
    private LocalDate fechaCompra;
    private String imagenUrl; // Opcional, para mostrar la imagen principal en el front
}
