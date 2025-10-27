package com.elbuensabor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoConMercadoPagoRequestDTO {

    // ==================== DATOS DEL PEDIDO (REUTILIZAR TU LÓGICA) ====================

    @NotNull(message = "El cliente es obligatorio")
    private Long idCliente;

    @NotNull(message = "El tipo de envío es obligatorio")
    @Pattern(regexp = "DELIVERY|TAKE_AWAY", message = "Tipo de envío debe ser DELIVERY o TAKE_AWAY")
    private String tipoEnvio;

    private Long idDomicilio; // Solo si es DELIVERY

    @NotNull(message = "La sucursal es obligatoria")
    private Long idSucursal;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @Valid
    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<DetallePedidoRequestDTO> detalles;

    // ==================== CONFIGURACIÓN DE DESCUENTOS ====================

    @DecimalMin(value = "0.0", message = "El porcentaje de descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje de descuento no puede exceder 100%")
    private Double porcentajeDescuentoTakeAway = 10.0; // 10% por defecto

    @DecimalMin(value = "0.0", message = "Los gastos de envío no pueden ser negativos")
    private Double gastosEnvioDelivery = 200.0; // $200 por defecto

    // ==================== DATOS PARA MERCADO PAGO ====================

    @NotNull(message = "El email del comprador es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String emailComprador;

    @NotBlank(message = "El nombre del comprador es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombreComprador;

    @NotBlank(message = "El apellido del comprador es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    private String apellidoComprador;

    // ==================== CONFIGURACIÓN OPCIONAL ====================

    private Boolean aplicarDescuentoTakeAway = true; // Si aplicar descuento automático
    private Boolean crearPreferenciaMercadoPago = true; // Si crear link de MP
    private String externalReference; // Referencia externa opcional
}