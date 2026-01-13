package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.response.PromocionDetalleResponseDTO;
import com.elbuensabor.entities.PromocionDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ArticuloShortMapper.class })
public interface PromocionDetalleMapper {

    @Mapping(source = "articulo", target = "articulo")
    PromocionDetalleResponseDTO toDTO(PromocionDetalle entity);

}