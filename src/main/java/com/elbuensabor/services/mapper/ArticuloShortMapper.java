package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.response.ArticuloShortResponseDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.ArticuloManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ArticuloShortMapper {

    @Mapping(source = "idArticulo", target = "id")
    @Mapping(source = "denominacion", target = "denominacion")
    @Mapping(source = "precioVenta", target = "precioVenta")
    @Mapping(expression = "java(getCosto(entity))", target = "costo")
    @Mapping(expression = "java(getTipoArticulo(entity))", target = "tipoArticulo")
    ArticuloShortResponseDTO toDTO(Articulo entity);

    default Double getCosto(Articulo entity) {
        if (entity instanceof ArticuloManufacturado am)
            return am.getCostoProduccion() != null ? am.getCostoProduccion() : 0.0;
        if (entity instanceof ArticuloInsumo ai)
            return ai.getPrecioCompra() != null ? ai.getPrecioCompra() : 0.0;
        return 0.0;
    }

    default String getTipoArticulo(Articulo entity) {
        if (entity instanceof ArticuloManufacturado)
            return "MANUFACTURADO";
        if (entity instanceof ArticuloInsumo)
            return "INSUMO";
        return "DESCONOCIDO";
    }
}
