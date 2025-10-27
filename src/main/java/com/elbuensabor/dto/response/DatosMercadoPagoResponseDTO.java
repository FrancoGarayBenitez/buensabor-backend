package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosMercadoPagoResponseDTO {
    private Long idMercadoPago;
    private Long paymentId;
    private String status;
    private String statusDetail;
    private String paymentMethodId;
    private String paymentTypeId;
    private LocalDateTime dateCreated;
    private LocalDateTime dateApproved;
    private Long pagoId;
}