package com.elbuensabor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosMercadoPagoRequestDTO {
    private Long paymentId;
    private String status;
    private String statusDetail;
    private String paymentMethodId;
    private String paymentTypeId;
    private LocalDateTime dateCreated;
    private LocalDateTime dateApproved;
    private Long pagoId;
}