package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DatosMercadoPagoRequestDTO;
import com.elbuensabor.dto.response.DatosMercadoPagoResponseDTO;
import com.elbuensabor.entities.DatosMercadoPago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DatosMercadoPagoMapper extends BaseMapper<DatosMercadoPago, DatosMercadoPagoResponseDTO> {

    // ==================== ENTITY → DTO ====================

    @Override
    @Mapping(source = "pago.idPago", target = "pagoId")
    DatosMercadoPagoResponseDTO toDTO(DatosMercadoPago entity);

    // ==================== DTO → ENTITY ====================

    @Override
    @Mapping(target = "idMercadoPago", ignore = true)
    @Mapping(target = "pago", ignore = true) // Se asigna en el service
    DatosMercadoPago toEntity(DatosMercadoPagoResponseDTO dto);

    // ==================== UPDATE ENTITY FROM DTO ====================

    @Override
    @Mapping(target = "idMercadoPago", ignore = true)
    @Mapping(target = "pago", ignore = true)
    void updateEntityFromDTO(DatosMercadoPagoResponseDTO dto, @MappingTarget DatosMercadoPago entity);

    // ==================== REQUEST DTO → ENTITY ====================

    @Mapping(target = "idMercadoPago", ignore = true)
    @Mapping(target = "pago", ignore = true) // Se asigna en el service
    DatosMercadoPago toEntityFromRequest(DatosMercadoPagoRequestDTO dto);
}