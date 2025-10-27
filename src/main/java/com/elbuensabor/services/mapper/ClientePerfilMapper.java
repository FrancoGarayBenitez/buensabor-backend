package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.entities.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper específico para operaciones de perfil de cliente
 * Maneja solo información personal, sin domicilios ni datos de autenticación
 */
@Mapper(componentModel = "spring")
public interface ClientePerfilMapper {

    /**
     * Convierte Cliente entity a ClientePerfilDTO
     * Para mostrar datos actuales en formulario de edición
     */
    @Mapping(target = "imagen", ignore = true) // Se maneja por separado si es necesario
    ClientePerfilDTO toPerfilDTO(Cliente entity);

    /**
     * Convierte ClienteResponseDTO a ClientePerfilDTO
     * Útil para transformar respuesta completa a formulario editable
     */
    @Mapping(target = "imagen", source = "imagen")
    ClientePerfilDTO responseToPerfilDTO(ClienteResponseDTO responseDTO);

    /**
     * Actualiza campos de Cliente desde ClientePerfilDTO
     * Preserva relaciones y campos críticos
     */
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)      // Usuario/Auth0 no se toca
    @Mapping(target = "pedidos", ignore = true)      // Historial se mantiene
    @Mapping(target = "domicilios", ignore = true)   // Domicilios se manejan por separado
    @Mapping(target = "imagen", ignore = true)       // Se maneja por separado en el servicio
    void updateEntityFromPerfilDTO(ClientePerfilDTO perfilDTO, @MappingTarget Cliente entity);

    /**
     * Convierte ClientePerfilDTO a Cliente entity
     * Para casos donde se necesite la entidad completa (raro, pero útil)
     */
    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "pedidos", ignore = true)
    @Mapping(target = "domicilios", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    Cliente perfilDTOToEntity(ClientePerfilDTO perfilDTO);
}