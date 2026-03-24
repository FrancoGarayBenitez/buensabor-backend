package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.cliente.CatalogoArticuloDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Promocion;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CatalogoArticuloMapper {

    // ✅ MapStruct no soporta polimorfismo directo → usamos default methods

    default CatalogoArticuloDTO toDTO(Articulo entity) {
        if (entity instanceof ArticuloManufacturado) {
            return toDTOManufacturado((ArticuloManufacturado) entity);
        } else if (entity instanceof ArticuloInsumo) {
            return toDTOInsumo((ArticuloInsumo) entity);
        }
        throw new IllegalArgumentException(
                "Tipo de artículo no soportado: " + entity.getClass().getSimpleName());
    }

    default List<CatalogoArticuloDTO> toDTOList(List<Articulo> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== MANUFACTURADO ====================

    default CatalogoArticuloDTO toDTOManufacturado(ArticuloManufacturado entity) {
        CatalogoArticuloDTO dto = new CatalogoArticuloDTO();

        dto.setIdArticulo(entity.getIdArticulo());
        dto.setDenominacion(entity.getDenominacion());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecioOriginal(entity.getPrecioVenta());
        dto.setTiempoEstimadoEnMinutos(entity.getTiempoEstimadoEnMinutos());

        if (entity.getCategoria() != null) {
            dto.setIdCategoria(entity.getCategoria().getIdCategoria());
            dto.setNombreCategoria(entity.getCategoria().getDenominacion());
        }

        // Disponibilidad
        dto.setDisponible(!entity.getEliminado() && entity.verificarStockSuficiente(1));

        // Promoción vigente
        Promocion promocion = entity.getPromocionVigente();
        if (promocion != null) {
            dto.setTienePromocion(true);
            dto.setIdPromocion(promocion.getIdPromocion());
            Double precioFinal = calcularPrecioConDescuento(entity.getPrecioVenta(), promocion);
            dto.setPrecioFinal(precioFinal);
            dto.setPorcentajeDescuento(
                    calcularPorcentajeDescuento(entity.getPrecioVenta(), precioFinal).intValue());
            dto.setEtiquetaPromocion(generarEtiquetaPromocion(promocion));
        } else {
            dto.setTienePromocion(false);
            dto.setPrecioFinal(entity.getPrecioVenta());
            dto.setPorcentajeDescuento(0);
        }

        // Imagen principal
        setImagenPrincipal(dto, entity.getImagenes());

        dto.setTipoArticulo("MANUFACTURADO");

        return dto;
    }

    // ==================== INSUMO DE VENTA DIRECTA ====================

    default CatalogoArticuloDTO toDTOInsumo(ArticuloInsumo entity) {
        CatalogoArticuloDTO dto = new CatalogoArticuloDTO();

        dto.setIdArticulo(entity.getIdArticulo());
        dto.setDenominacion(entity.getDenominacion());
        // ✅ Insumo de venta directa usa precioVenta (no precioCompra)
        dto.setPrecioOriginal(entity.getPrecioVenta());
        dto.setPrecioFinal(entity.getPrecioVenta());
        dto.setTipoArticulo("INSUMO");

        // Los insumos no tienen tiempo de preparación
        dto.setTiempoEstimadoEnMinutos(0);

        if (entity.getCategoria() != null) {
            dto.setIdCategoria(entity.getCategoria().getIdCategoria());
            dto.setNombreCategoria(entity.getCategoria().getDenominacion());
        }

        // ✅ Disponibilidad: stock actual > 0
        dto.setDisponible(
                !entity.getEliminado() &&
                        entity.getStockActual() != null &&
                        entity.getStockActual() > 0);

        // Los insumos no tienen promociones propias
        dto.setTienePromocion(false);
        dto.setPorcentajeDescuento(0);

        // Imagen principal
        setImagenPrincipal(dto, entity.getImagenes());

        return dto;
    }

    // ==================== HELPERS ====================

    default void setImagenPrincipal(CatalogoArticuloDTO dto, List<Imagen> imagenes) {
        if (imagenes != null && !imagenes.isEmpty()) {
            Imagen imagen = imagenes.get(0);
            ImagenDTO imagenDTO = new ImagenDTO();
            imagenDTO.setIdImagen(imagen.getIdImagen());
            imagenDTO.setDenominacion(imagen.getDenominacion());
            imagenDTO.setUrl(imagen.getUrl());
            dto.setImagenPrincipal(imagenDTO);
        }
    }

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

    default String generarEtiquetaPromocion(Promocion promocion) {
        switch (promocion.getTipoDescuento()) {
            case PORCENTUAL:
                return promocion.getValorDescuento().intValue() + "% OFF";
            case MONTO_FIJO:
                return "$" + promocion.getValorDescuento().intValue() + " OFF";
            default:
                return "PROMO";
        }
    }
}