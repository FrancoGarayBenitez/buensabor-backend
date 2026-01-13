// package com.elbuensabor.dto.request;

// import jakarta.validation.Valid;
// import jakarta.validation.constraints.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// import java.util.List;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class PedidoRequestDTO {

// @NotNull(message = "El cliente es obligatorio")
// private Long idCliente;

// @NotNull(message = "El tipo de envío es obligatorio")
// private String tipoEnvio; // "DELIVERY" o "TAKE_AWAY"

// private Long idDomicilio; // Solo si es DELIVERY

// @NotNull(message = "La sucursal es obligatoria")
// private Long idSucursal;

// // ✅ NUEVO: Observaciones generales del pedido
// @Size(max = 500, message = "Las observaciones no pueden exceder 500
// caracteres")
// private String observaciones;

// @Valid
// @NotEmpty(message = "El pedido debe tener al menos un producto")
// private List<DetallePedidoRequestDTO> detalles;

// // ✅ NUEVO: Promoción agrupada aplicada
// @Valid
// private PromocionAgrupadaDTO promocionAgrupada;
// }