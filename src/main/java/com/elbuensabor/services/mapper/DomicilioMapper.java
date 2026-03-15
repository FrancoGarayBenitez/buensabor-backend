package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DomicilioRequestDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.dto.response.pedido.DomicilioResponse; // ✅ NUEVO
import com.elbuensabor.entities.Domicilio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DomicilioMapper extends BaseMapper<Domicilio, DomicilioRequestDTO> {

    // ==================== MAPEOS EXISTENTES ====================

    @Override
    @Mapping(target = "idDomicilio", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Domicilio toEntity(DomicilioRequestDTO dto);

    @Override
    @Mapping(target = "idDomicilio", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void updateEntityFromDTO(DomicilioRequestDTO dto, @MappingTarget Domicilio entity);

    @Named("toResponseDTO")
    @Mapping(source = "idDomicilio", target = "idDomicilio")
    @Mapping(source = "calle", target = "calle")
    @Mapping(source = "numero", target = "numero")
    @Mapping(source = "cp", target = "cp")
    @Mapping(source = "localidad", target = "localidad")
    @Mapping(source = "esPrincipal", target = "esPrincipal")
    @Mapping(target = "direccionCompleta", expression = "java(construirDireccionCompleta(entity))")
    DomicilioResponseDTO toResponseDTO(Domicilio entity);

    @Mapping(target = "idDomicilio", ignore = true)
    @Mapping(target = "direccionCompleta", expression = "java(construirDireccionCompletaFromDTO(dto))")
    DomicilioResponseDTO requestToResponse(DomicilioRequestDTO dto);

    // ==================== NUEVO MAPEO PARA PEDIDOS ====================

    /**
     * ✅ Convierte Domicilio a DomicilioResponse (para pedidos)
     * Formato simplificado para modulo de pedidos
     */
    @Mapping(source = "idDomicilio", target = "idDomicilio")
    @Mapping(source = "calle", target = "calle")
    @Mapping(source = "numero", target = "numero")
    @Mapping(source = "cp", target = "codigoPostal")
    @Mapping(source = "localidad", target = "localidad")
    DomicilioResponse toPedidoResponse(Domicilio entity);

    // ==================== MÉTODOS AUXILIARES ====================

    default String construirDireccionCompleta(Domicilio domicilio) {
        if (domicilio == null)
            return "";

        return String.format("%s %d, %s (CP: %d)",
                domicilio.getCalle(),
                domicilio.getNumero(),
                domicilio.getLocalidad(),
                domicilio.getCp());
    }

    default String construirDireccionCompletaFromDTO(DomicilioRequestDTO dto) {
        if (dto == null)
            return "";

        return String.format("%s %d, %s (CP: %d)",
                dto.getCalle(),
                dto.getNumero(),
                dto.getLocalidad(),
                dto.getCp());
    }
}