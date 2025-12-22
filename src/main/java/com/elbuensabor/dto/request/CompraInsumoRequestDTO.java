package com.elbuensabor.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CompraInsumoRequestDTO {
    private Long idArticuloInsumo;

    // Flujo único: compra por paquetes
    private Double paquetes; // cantidad de paquetes
    private Double precioPorPaquete; // precio de cada paquete
    private String unidadContenido; // "g" | "kg" | "ml" | "l" | "unidad"
    private Double contenidoPorPaquete; // contenido dentro de cada paquete (en unidadContenido)
    private LocalDate fechaCompra;
}
