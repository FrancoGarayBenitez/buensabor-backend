package com.elbuensabor.dto.response.pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioResponse {

    private Long idDomicilio;
    private String calle;
    private Integer numero;
    private String localidad;
    private Integer codigoPostal;
    private Integer piso;
    private String dpto;
}
