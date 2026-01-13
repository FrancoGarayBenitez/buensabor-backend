package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.ArticuloManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = { DetalleManufacturadoMapper.class })
public interface ArticuloManufacturadoMapper {

        @Mappings({
                        @Mapping(source = "unidadMedida.idUnidadMedida", target = "idUnidadMedida"),
                        @Mapping(source = "unidadMedida.denominacion", target = "denominacionUnidadMedida"),
                        @Mapping(source = "categoria.idCategoria", target = "idCategoria"),
                        @Mapping(source = "categoria.denominacion", target = "denominacionCategoria"),
                        @Mapping(source = "categoria.esSubcategoria", target = "esSubcategoria"),
                        @Mapping(source = "categoria.categoriaPadre.denominacion", target = "denominacionCategoriaPadre"),
                        @Mapping(source = "categoria.tipoCategoria", target = "tipoCategoria"),
                        @Mapping(source = "margenGananciaPorcentaje", target = "margenGananciaPorcentaje"),
                        // Ignorar campos calculados, se poblarán en el service
                        @Mapping(target = "stockSuficiente", ignore = true),
                        @Mapping(target = "cantidadMaximaPreparable", ignore = true)
        })
        ArticuloManufacturadoResponseDTO toDTO(ArticuloManufacturado entity);

        @Mappings({
                        @Mapping(target = "idArticulo", ignore = true),
                        @Mapping(target = "eliminado", constant = "false"),
                        @Mapping(target = "costoProduccion", constant = "0.0"),
                        // Ignorar relaciones, se asignan en el service
                        @Mapping(target = "unidadMedida", ignore = true),
                        @Mapping(target = "categoria", ignore = true),
                        @Mapping(target = "imagenes", ignore = true),
                        @Mapping(target = "detallesPromocion", ignore = true),
                        // El margen se setea desde el porcentaje en el service
                        @Mapping(target = "margenGanancia", ignore = true)
        })
        ArticuloManufacturado toEntity(ArticuloManufacturadoRequestDTO dto);

        @Mappings({
                        @Mapping(target = "idArticulo", ignore = true),
                        @Mapping(target = "eliminado", ignore = true),
                        @Mapping(target = "costoProduccion", ignore = true),
                        @Mapping(target = "unidadMedida", ignore = true),
                        @Mapping(target = "categoria", ignore = true),
                        @Mapping(target = "imagenes", ignore = true),
                        @Mapping(target = "detallesPromocion", ignore = true),
                        @Mapping(target = "detalles", ignore = true), // Se manejan manualmente
                        @Mapping(target = "margenGanancia", ignore = true)
        })
        void updateEntityFromDTO(ArticuloManufacturadoRequestDTO dto, @MappingTarget ArticuloManufacturado entity);
}