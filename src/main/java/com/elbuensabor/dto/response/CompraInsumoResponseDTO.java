package com.elbuensabor.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CompraInsumoResponseDTO {
    // Identificadores
    private Long id;
    private Long idArticuloInsumo;
    private String denominacionInsumo;

    // Normalizado (unidad técnica: g/ml/unidad)
    private Double cantidad; // cantidad técnica (ej: 1500 g)
    private Double precioUnitario; // precio por unidad técnica (ej: $/g)
    private LocalDate fechaCompra;

    private String imagenUrl; // Opcional

    // Nuevo: datos de la operación “por paquete”
    private Double paquetes; // ej: 3
    private Double precioPorPaquete; // ej: 1000
    private Double contenidoPorPaquete; // ej: 500
    private String unidadContenido; // "g" | "kg" | "ml" | "l" | "unidad"
}
