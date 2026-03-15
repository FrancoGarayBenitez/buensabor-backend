package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.pedido.DetallePedidoRequest;
import com.elbuensabor.dto.response.pedido.DetallePedidoResponse;
import com.elbuensabor.entities.DetallePedido;
import com.elbuensabor.entities.PromocionDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DetallePedidoMapper {

    @Mappings({
            @Mapping(source = "idDetallePedido", target = "idDetallePedido"),

            @Mapping(source = "articulo.idArticulo", target = "idArticulo"),
            @Mapping(source = "articulo.denominacion", target = "nombreArticulo"),
            @Mapping(expression = "java(getImagenArticulo(entity))", target = "imagenArticulo"),

            @Mapping(source = "cantidad", target = "cantidad"),

            @Mapping(source = "precioUnitarioOriginal", target = "precioUnitarioOriginal"),
            @Mapping(source = "descuentoPromocion", target = "descuentoPromocion"),
            @Mapping(expression = "java(entity.getPrecioUnitarioFinal())", target = "precioUnitarioFinal"),
            @Mapping(source = "subtotal", target = "subtotal"),

            // ✅ AGREGADO: idPromocion
            @Mapping(source = "promocionAplicada.idPromocion", target = "idPromocion"),
            @Mapping(source = "promocionAplicada.denominacion", target = "nombrePromocion"),

            // ✅ AGREGADO: articulosCombo desde los detalles de la promoción
            @Mapping(source = "promocionAplicada.detalles", target = "articulosCombo", qualifiedByName = "mapArticulosCombo"),

            @Mapping(source = "observaciones", target = "observaciones")
    })
    DetallePedidoResponse toDTO(DetallePedido entity);

    @Mappings({
            @Mapping(target = "idDetallePedido", ignore = true),

            @Mapping(source = "cantidad", target = "cantidad"),
            @Mapping(source = "observaciones", target = "observaciones"),

            @Mapping(target = "subtotal", constant = "0.0"),
            @Mapping(target = "precioUnitarioOriginal", constant = "0.0"),
            @Mapping(target = "descuentoPromocion", constant = "0.0"),

            @Mapping(target = "articulo", ignore = true),
            @Mapping(target = "pedido", ignore = true),
            @Mapping(target = "promocionAplicada", ignore = true)
    })
    DetallePedido toEntity(DetallePedidoRequest request);

    // ✅ Convierte List<PromocionDetalle> → List<ArticuloComboResponse>
    @Named("mapArticulosCombo")
    default List<DetallePedidoResponse.ArticuloComboResponse> mapArticulosCombo(
            List<PromocionDetalle> detalles) {
        if (detalles == null || detalles.isEmpty())
            return Collections.emptyList();
        return detalles.stream()
                .filter(pd -> pd.getArticulo() != null)
                .map(pd -> DetallePedidoResponse.ArticuloComboResponse.builder()
                        .idArticulo(pd.getArticulo().getIdArticulo())
                        .denominacion(pd.getArticulo().getDenominacion())
                        .cantidad(pd.getCantidad())
                        .build())
                .collect(Collectors.toList());
    }

    default String getImagenArticulo(DetallePedido entity) {
        if (entity.getArticulo() == null ||
                entity.getArticulo().getImagenes() == null ||
                entity.getArticulo().getImagenes().isEmpty()) {
            return null;
        }
        return entity.getArticulo().getImagenes().get(0).getUrl();
    }
}