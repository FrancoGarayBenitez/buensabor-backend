package com.elbuensabor.dto.request;

import com.elbuensabor.entities.FormaPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {

    @NotNull(message = "El ID de la factura es obligatorio")
    private Long facturaId;

    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    @Size(max = 3, message = "La moneda no puede exceder 3 caracteres")
    private String moneda = "ARS";

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    // Campos específicos para Mercado Pago (opcionales)
    private String mercadoPagoPreferenceId;
}