package com.elbuensabor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {
    private Long idImagen;

    @NotBlank(message = "La denominación es obligatoria")
    private String denominacion;

    @NotBlank(message = "La URL es obligatoria")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
            message = "URL de imagen inválida")
    private String url;
}
