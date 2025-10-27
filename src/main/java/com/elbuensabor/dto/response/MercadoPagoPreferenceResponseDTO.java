package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceResponseDTO {
    private String id;
    private String initPoint;
    private String sandboxInitPoint;
    private String clientId;
    private Long collectorId;        // CAMBIO: Long en lugar de String
    private String operationType;
    // REMOVIDO: siteId no existe en Preference
    private String externalReference; // AGREGADO: campo útil que sí existe
    private String notificationUrl;   // AGREGADO: campo útil que sí existe
}
