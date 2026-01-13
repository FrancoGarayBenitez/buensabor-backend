package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Promocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = { ImagenMapper.class, PromocionDetalleMapper.class })
public interface PromocionMapper {

        @Mappings({
                        @Mapping(source = "idPromocion", target = "id"),
                        @Mapping(source = "estado", target = "estado"), // El método getEstado() se invoca
                                                                        // automáticamente
                        @Mapping(source = "tipoPromocion", target = "tipoPromocion"),
                        @Mapping(source = "detalles", target = "detalles"),
                        @Mapping(source = "imagenes", target = "imagenes")
        })
        PromocionResponseDTO toDTO(Promocion entity);

        @Mappings({
                        @Mapping(target = "idPromocion", ignore = true),
                        @Mapping(target = "eliminado", constant = "false"),
                        @Mapping(source = "tipoPromocion", target = "tipoPromocion"),
                        // Ignorar relaciones, se asignarán en el servicio a partir de los IDs
                        @Mapping(target = "detalles", ignore = true),
                        @Mapping(target = "imagenes", ignore = true)
        })
        Promocion toEntity(PromocionRequestDTO dto);

        /**
         * Actualiza una entidad Promocion existente desde un DTO.
         * Ignora campos que no deben ser modificados en una actualización estándar.
         */
        @Mappings({
                        @Mapping(target = "idPromocion", ignore = true),
                        @Mapping(target = "eliminado", ignore = true),
                        @Mapping(source = "tipoPromocion", target = "tipoPromocion"),
                        @Mapping(target = "detalles", ignore = true),
                        @Mapping(target = "imagenes", ignore = true)
        })
        void updateFromDTO(PromocionRequestDTO dto, @MappingTarget Promocion entity);
}