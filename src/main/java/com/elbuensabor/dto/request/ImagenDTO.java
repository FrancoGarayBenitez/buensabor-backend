package com.elbuensabor.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {
    private Long idImagen;

    // Permitir que sea nullable para nuevas imágenes
    private String denominacion;

    // Permitir que sea nullable, pero validar cuando no sea null
    @Pattern(regexp = "^(https?://|/img/).+", message = "URL de imagen inválida")
    private String url;
}
