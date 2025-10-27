package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ClienteRegisterDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.entities.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {DomicilioMapper.class})
public interface ClienteMapper extends BaseMapper<Cliente, ClienteResponseDTO> {

    /**
     * Convierte entidad Cliente a DTO de respuesta.
     * Los datos de identificación (nombre, apellido, email, rol) provienen de la entidad Usuario.
     */
    @Override
    @Mapping(source = "usuario.idUsuario", target = "idUsuario")
    @Mapping(source = "usuario.email", target = "email")
    @Mapping(source = "usuario.rol", target = "rol")
    @Mapping(source = "usuario.nombre", target = "nombre")
    @Mapping(source = "usuario.apellido", target = "apellido")
    @Mapping(source = "domicilios", target = "domicilios", qualifiedByName = "toResponseDTO")
    ClienteResponseDTO toDTO(Cliente entity);

    /**
     * Convierte DTO de respuesta a entidad Cliente.
     * Solo mapea campos de negocio (telefono, fechaNacimiento).
     * Ignora todas las relaciones y campos de Usuario.
     */
    @Override
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "domicilios", ignore = true)
    Cliente toEntity(ClienteResponseDTO dto);

    /**
     * Convierte DTO de registro a entidad Cliente.
     * Solo mapea campos de negocio (telefono, fechaNacimiento).
     */
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "domicilios", ignore = true)
    Cliente toEntity(ClienteRegisterDTO registerDTO);

    /**
     * Actualiza una entidad Cliente existente con datos del DTO.
     * MapStruct solo aplicará los campos que existen en Cliente (telefono, fechaNacimiento).
     * La actualización de Usuario (nombre, apellido, email) DEBE manejarse en el servicio.
     */
    @Override
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "domicilios", ignore = true)
    void updateEntityFromDTO(ClienteResponseDTO dto, @MappingTarget Cliente entity);
}