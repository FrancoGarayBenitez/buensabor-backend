package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.PagoRequestDTO;
import com.elbuensabor.dto.response.PagoResponseDTO;
import com.elbuensabor.entities.Pago;
import com.elbuensabor.entities.DatosMercadoPago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PagoMapper extends BaseMapper<Pago, PagoResponseDTO> {

    // ==================== ENTITY → DTO ====================

    @Override
    @Mapping(source = "factura.idFactura", target = "facturaId")
    @Mapping(source = "datosMercadoPago", target = "datosMercadoPago")
    PagoResponseDTO toDTO(Pago entity);

    @Mapping(source = "paymentId", target = "paymentId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "statusDetail", target = "statusDetail")
    @Mapping(source = "paymentMethodId", target = "paymentMethodId")
    @Mapping(source = "paymentTypeId", target = "paymentTypeId")
    @Mapping(source = "dateCreated", target = "dateCreated")
    @Mapping(source = "dateApproved", target = "dateApproved")
    PagoResponseDTO.DatosMercadoPagoDTO toDatosMercadoPagoDTO(DatosMercadoPago entity);

    // ==================== DTO → ENTITY ====================

    @Override
    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "factura", ignore = true)           // Se asigna en el service
    @Mapping(target = "estado", ignore = true)            // Se asigna en el service (PENDIENTE por defecto)
    @Mapping(target = "fechaCreacion", ignore = true)     // Se asigna en el service
    @Mapping(target = "fechaActualizacion", ignore = true) // Se asigna en el service
    @Mapping(target = "datosMercadoPago", ignore = true)  // Se asigna en el service si es necesario
    @Mapping(target = "mercadoPagoPreferenceId", ignore = true) // No viene del ResponseDTO
    Pago toEntity(PagoResponseDTO dto);

    // ==================== UPDATE ENTITY FROM DTO ====================

    @Override
    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "factura", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "datosMercadoPago", ignore = true)
    @Mapping(target = "mercadoPagoPreferenceId", ignore = true)
    void updateEntityFromDTO(PagoResponseDTO dto, @MappingTarget Pago entity);

    // ==================== REQUEST DTO → ENTITY ====================

    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "factura", ignore = true)           // Se asigna en el service
    @Mapping(target = "estado", ignore = true)            // Se asigna en el service (PENDIENTE por defecto)
    @Mapping(target = "fechaCreacion", ignore = true)     // Se asigna en el service
    @Mapping(target = "fechaActualizacion", ignore = true) // Se asigna en el service
    @Mapping(target = "datosMercadoPago", ignore = true)  // Se asigna en el service si es necesario
    @Mapping(target = "mercadoPagoPreferenceId", source = "mercadoPagoPreferenceId")
    @Mapping(target = "moneda", defaultValue = "ARS")
    Pago toEntityFromRequest(PagoRequestDTO dto);
}