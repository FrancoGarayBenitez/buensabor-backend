

        package com.elbuensabor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceDTO {

    @NotNull(message = "Los items son obligatorios")
    private List<ItemDTO> items;

    @NotNull(message = "La información del pagador es obligatoria")
    private PayerDTO payer;

    private String backUrls;
    private String notificationUrl;
    private String externalReference;
    private Boolean autoReturn;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        @NotNull(message = "El título del item es obligatorio")
        private String title;

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        private Integer quantity;

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        private Double unitPrice;

        private String currencyId = "ARS";
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerDTO {
        @NotNull(message = "El nombre es obligatorio")
        private String name;

        @NotNull(message = "El apellido es obligatorio")
        private String surname;

        @Email(message = "El email debe ser válido")
        @NotNull(message = "El email es obligatorio")
        private String email;

        private PhoneDTO phone;
        private AddressDTO address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneDTO {
        private String areaCode;
        private String number;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private String zipCode;
        private String streetName;
        private String streetNumber;
    }
}
