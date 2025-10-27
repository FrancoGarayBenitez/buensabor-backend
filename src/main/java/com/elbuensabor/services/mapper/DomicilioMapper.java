package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.DomicilioRequestDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.entities.Domicilio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DomicilioMapper extends BaseMapper<Domicilio, DomicilioRequestDTO> {

    // ==================== MAPEOS PARA DomicilioRequestDTO ====================

    /**
     * Convierte DomicilioRequestDTO a entidad
     * Usado para crear nuevos domicilios
     */
    @Override
    @Mapping(target = "idDomicilio", ignore = true)
    @Mapping(target = "sucursalEmpresa", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "cliente", ignore = true) // Se asigna manualmente en el servicio
    Domicilio toEntity(DomicilioRequestDTO dto);

    /**
     * Actualiza entidad desde DomicilioRequestDTO
     * Usado para editar domicilios existentes
     */
    @Override
    @Mapping(target = "idDomicilio", ignore = true)
    @Mapping(target = "sucursalEmpresa", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "cliente", ignore = true) // No se cambia el cliente propietario
    void updateEntityFromDTO(DomicilioRequestDTO dto, @MappingTarget Domicilio entity);

    // ==================== MAPEOS PARA DomicilioResponseDTO ====================

    /**
     * Convierte entidad a DomicilioResponseDTO
     * Incluye dirección completa calculada y campo esPrincipal
     */
    @Named("toResponseDTO")
    @Mapping(source = "idDomicilio", target = "idDomicilio")
    @Mapping(source = "calle", target = "calle")
    @Mapping(source = "numero", target = "numero")
    @Mapping(source = "cp", target = "cp")
    @Mapping(source = "localidad", target = "localidad")
    @Mapping(source = "esPrincipal", target = "esPrincipal") // ✅ NUEVO CAMPO
    @Mapping(target = "direccionCompleta", expression = "java(construirDireccionCompleta(entity))")
    DomicilioResponseDTO toResponseDTO(Domicilio entity);

    /**
     * Convierte DomicilioRequestDTO a DomicilioResponseDTO
     * Útil para respuestas inmediatas sin guardar en BD
     */
    @Mapping(target = "idDomicilio", ignore = true) // No tiene ID hasta que se guarde
    @Mapping(target = "direccionCompleta", expression = "java(construirDireccionCompletaFromDTO(dto))")
    DomicilioResponseDTO requestToResponse(DomicilioRequestDTO dto);

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Construye la dirección completa desde la entidad
     */
    default String construirDireccionCompleta(Domicilio domicilio) {
        if (domicilio == null) return "";

        return String.format("%s %d, %s (CP: %d)",
                domicilio.getCalle(),
                domicilio.getNumero(),
                domicilio.getLocalidad(),
                domicilio.getCp());
    }

    /**
     * Construye la dirección completa desde el DTO
     */
    default String construirDireccionCompletaFromDTO(DomicilioRequestDTO dto) {
        if (dto == null) return "";

        return String.format("%s %d, %s (CP: %d)",
                dto.getCalle(),
                dto.getNumero(),
                dto.getLocalidad(),
                dto.getCp());
    }
}