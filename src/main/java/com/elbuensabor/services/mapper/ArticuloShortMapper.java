package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.response.ArticuloShortResponseDTO;
import com.elbuensabor.entities.Articulo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticuloShortMapper {

    @Mapping(source = "idArticulo", target = "id")
    ArticuloShortResponseDTO toDTO(Articulo entity);
}
