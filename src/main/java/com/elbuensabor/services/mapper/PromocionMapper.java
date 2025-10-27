package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Promocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromocionMapper extends BaseMapper<Promocion, PromocionResponseDTO> {

    // ==================== ENTITY → RESPONSE DTO ====================
    @Override
    @Mapping(source = "articulos", target = "articulos", qualifiedByName = "mapArticulosToSimpleDTO")
    @Mapping(source = "imagenes", target = "urlsImagenes", qualifiedByName = "mapImagenesToUrls")
    @Mapping(target = "estaVigente", expression = "java(entity.estaVigente())")
    @Mapping(target = "estadoDescripcion", expression = "java(generateEstadoDescripcion(entity))")
    PromocionResponseDTO toDTO(Promocion entity);

    // ==================== REQUEST DTO → ENTITY ====================
    @Mapping(target = "idPromocion", ignore = true)
    @Mapping(target = "articulos", ignore = true) // Se asigna en el service
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "sucursales", ignore = true) // Se asigna en el service
    // ✅ FIX: Mapear precioPromocional en lugar de ignorarlo
    @Mapping(source = "precioPromocional", target = "precioPromocional")
    Promocion toEntity(PromocionRequestDTO dto);

    // ==================== UPDATE FROM DTO ====================
    @Mapping(target = "idPromocion", ignore = true)
    @Mapping(target = "articulos", ignore = true) // Se maneja en el service
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    // ✅ FIX: También mapear en updates
    @Mapping(source = "precioPromocional", target = "precioPromocional")
    void updateEntityFromDTO(PromocionRequestDTO dto, @MappingTarget Promocion entity);

    // ==================== RESPONSE DTO → ENTITY ====================
    @Override
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Promocion toEntity(PromocionResponseDTO dto);

    // ==================== MÉTODOS AUXILIARES ====================

    @Named("mapArticulosToSimpleDTO")
    default List<PromocionResponseDTO.ArticuloSimpleDTO> mapArticulosToSimpleDTO(List<com.elbuensabor.entities.Articulo> articulos) {
        if (articulos == null) return null;

        return articulos.stream()
                .map(articulo -> {
                    PromocionResponseDTO.ArticuloSimpleDTO dto = new PromocionResponseDTO.ArticuloSimpleDTO();
                    dto.setIdArticulo(articulo.getIdArticulo());
                    dto.setDenominacion(articulo.getDenominacion());
                    dto.setPrecioVenta(articulo.getPrecioVenta());

                    // Obtener primera imagen si existe
                    if (articulo.getImagenes() != null && !articulo.getImagenes().isEmpty()) {
                        dto.setUrlImagen(articulo.getImagenes().get(0).getUrl());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Named("mapImagenesToUrls")
    default List<String> mapImagenesToUrls(List<com.elbuensabor.entities.Imagen> imagenes) {
        if (imagenes == null) return null;

        return imagenes.stream()
                .map(com.elbuensabor.entities.Imagen::getUrl)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE EXPRESIÓN ====================

    default String generateEstadoDescripcion(Promocion promocion) {
        if (!promocion.getActivo()) {
            return "Inactiva";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        // Verificar fechas
        if (ahora.isBefore(promocion.getFechaDesde())) {
            return "Programada (inicia el " + promocion.getFechaDesde().toLocalDate() + ")";
        }

        if (ahora.isAfter(promocion.getFechaHasta())) {
            return "Expirada";
        }

        // Verificar horarios
        if (horaActual.isBefore(promocion.getHoraDesde()) || horaActual.isAfter(promocion.getHoraHasta())) {
            return "Fuera de horario (válida de " + promocion.getHoraDesde() + " a " + promocion.getHoraHasta() + ")";
        }

        return "Vigente";
    }
}