package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioResponseDTO {
    private Long idDomicilio;
    private String calle;
    private Integer numero;
    private Integer cp;
    private String localidad;
    private Boolean esPrincipal;
    private String direccionCompleta; // calculada
}