package com.elbuensabor.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioRequestDTO {
    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotNull(message = "El número es obligatorio")
    @Positive(message = "El número debe ser positivo")
    private Integer numero;

    @NotNull(message = "El código postal es obligatorio")
    @Min(value = 1000, message = "Código postal inválido")
    @Max(value = 9999, message = "Código postal inválido")
    private Integer cp;

    @NotBlank(message = "La localidad es obligatoria")
    private String localidad;

    @NotNull(message = "Debe especificar si es domicilio principal")
    private Boolean esPrincipal = false;
}
