package com.elbuensabor.dto.response;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPaymentResponseDTO {
    private Long id;
    private String status;
    private String statusDetail;
    private String operationType;
    private String paymentMethodId;
    private String paymentTypeId;
    private Double transactionAmount;
    private String currencyId;
    private String dateCreated;
    private String dateApproved;
    private String externalReference;
    private PayerResponseDTO payer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerResponseDTO {
        private String email;
        private String firstName;
        private String lastName;
    }
}