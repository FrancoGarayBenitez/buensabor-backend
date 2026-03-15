package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.cliente.DetalleArticuloDTO;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.entities.Promocion;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { PromocionClienteMapper.class })
public interface DetalleArticuloMapper {

    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "denominacion", target = "denominacion")
    @Mapping(source = "descripcion", target = "descripcion")
    @Mapping(source = "precioVenta", target = "precioOriginal")
    @Mapping(source = "tiempoEstimadoEnMinutos", target = "tiempoEstimadoEnMinutos")
    @Mapping(source = "categoria.idCategoria", target = "idCategoria")
    @Mapping(source = "categoria.denominacion", target = "nombreCategoria")
    // Campos calculados en @AfterMapping
    @Mapping(target = "precioFinal", ignore = true)
    @Mapping(target = "porcentajeDescuento", ignore = true)
    @Mapping(target = "ahorroEnPesos", ignore = true)
    @Mapping(target = "disponible", ignore = true)
    @Mapping(target = "mensajeDisponibilidad", ignore = true)
    @Mapping(target = "promocionActiva", ignore = true)
    @Mapping(target = "ingredientesPrincipales", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "tipoArticulo", ignore = true)
    // ✅ Campos exclusivos de INSUMO — ignorados aquí, los setea
    // buildDetalleInsumo()
    @Mapping(target = "unidadMedida", ignore = true)
    @Mapping(target = "stockActual", ignore = true)
    DetalleArticuloDTO toDTO(ArticuloManufacturado entity);

    @AfterMapping
    default void calcularCamposDetalle(ArticuloManufacturado entity,
            @MappingTarget DetalleArticuloDTO dto) {

        // ✅ Tipo
        dto.setTipoArticulo("MANUFACTURADO");

        // ✅ Campos de INSUMO siempre null en manufacturado
        dto.setUnidadMedida(null);
        dto.setStockActual(null);

        // 1. Disponibilidad
        boolean disponible = !entity.getEliminado() && entity.verificarStockSuficiente(1);
        dto.setDisponible(disponible);

        if (!disponible) {
            dto.setMensajeDisponibilidad("Agotado");
        } else {
            Integer cantidadMaxima = entity.calcularCantidadMaximaPreparable();
            if (cantidadMaxima != null && cantidadMaxima <= 5) {
                dto.setMensajeDisponibilidad("¡Últimas unidades!");
            } else {
                dto.setMensajeDisponibilidad("Disponible");
            }
        }

        // 2. Promoción y precios
        Promocion promocion = entity.getPromocionVigente();
        if (promocion != null) {
            Double precioFinal = calcularPrecioConDescuento(entity.getPrecioVenta(), promocion);
            dto.setPrecioFinal(precioFinal);
            dto.setPorcentajeDescuento(
                    calcularPorcentajeDescuento(entity.getPrecioVenta(), precioFinal).intValue());
            dto.setAhorroEnPesos(entity.getPrecioVenta() - precioFinal);
        } else {
            dto.setPrecioFinal(entity.getPrecioVenta());
            dto.setPorcentajeDescuento(0);
            dto.setAhorroEnPesos(0.0);
        }

        // 3. Ingredientes principales (máximo 5)
        if (entity.getDetalles() != null && !entity.getDetalles().isEmpty()) {
            List<String> ingredientes = entity.getDetalles().stream()
                    .limit(5)
                    .map(detalle -> detalle.getArticuloInsumo().getDenominacion())
                    .collect(Collectors.toList());
            dto.setIngredientesPrincipales(ingredientes);
        } else {
            dto.setIngredientesPrincipales(new ArrayList<>());
        }

        // 4. Imágenes
        if (entity.getImagenes() != null && !entity.getImagenes().isEmpty()) {
            List<ImagenDTO> imagenesDTO = entity.getImagenes().stream()
                    .map(img -> {
                        ImagenDTO imgDto = new ImagenDTO();
                        imgDto.setIdImagen(img.getIdImagen());
                        imgDto.setDenominacion(img.getDenominacion());
                        imgDto.setUrl(img.getUrl());
                        return imgDto;
                    })
                    .collect(Collectors.toList());
            dto.setImagenes(imagenesDTO);
        } else {
            dto.setImagenes(new ArrayList<>());
        }
    }

    // ==================== HELPERS ====================

    default Double calcularPrecioConDescuento(Double precioOriginal, Promocion promocion) {
        if (promocion == null)
            return precioOriginal;
        switch (promocion.getTipoDescuento()) {
            case PORCENTUAL:
                return precioOriginal * (1 - promocion.getValorDescuento() / 100);
            case MONTO_FIJO:
                return Math.max(precioOriginal - promocion.getValorDescuento(), 0.0);
            default:
                return precioOriginal;
        }
    }

    default Double calcularPorcentajeDescuento(Double precioOriginal, Double precioFinal) {
        if (precioOriginal == null || precioOriginal == 0)
            return 0.0;
        return ((precioOriginal - precioFinal) / precioOriginal) * 100;
    }
}