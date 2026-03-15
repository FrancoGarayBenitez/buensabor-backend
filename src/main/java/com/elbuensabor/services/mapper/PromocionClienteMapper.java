package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.cliente.ArticuloPromocionDTO;
import com.elbuensabor.dto.response.cliente.PromocionClienteDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.entities.PromocionDetalle;
import com.elbuensabor.entities.TipoDescuento;

import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromocionClienteMapper {

    Logger log = LoggerFactory.getLogger(PromocionClienteMapper.class);

    // ==================== MAPEO PRINCIPAL ====================

    @Mapping(source = "idPromocion", target = "idPromocion")
    @Mapping(source = "denominacion", target = "nombre")
    @Mapping(source = "descripcionDescuento", target = "descripcion")
    @Mapping(source = "fechaDesde", target = "fechaDesde")
    @Mapping(source = "fechaHasta", target = "fechaHasta")
    @Mapping(source = "horaDesde", target = "horaDesde")
    @Mapping(source = "horaHasta", target = "horaHasta")
    @Mapping(source = "tipoDescuento", target = "tipoDescuento")
    @Mapping(source = "valorDescuento", target = "valorDescuento")
    @Mapping(source = "cantidadMinima", target = "cantidadMinima")
    // Campos calculados en @AfterMapping
    @Mapping(target = "textoDescuento", ignore = true)
    @Mapping(target = "mensajeCondiciones", ignore = true)
    @Mapping(target = "esCombo", ignore = true)
    @Mapping(target = "articulosIncluidos", ignore = true)
    @Mapping(target = "imagenPromocion", ignore = true)
    @Mapping(target = "precioOriginal", ignore = true)
    @Mapping(target = "precioFinal", ignore = true)
    @Mapping(target = "porcentajeDescuento", ignore = true)
    @Mapping(target = "disponibleAhora", ignore = true) // ✅ NUEVO
    @Mapping(target = "textoHorario", ignore = true) // ✅ NUEVO
    PromocionClienteDTO toDTO(Promocion entity);

    // ==================== POST-PROCESAMIENTO ====================

    @AfterMapping
    default void calcularCamposPromocion(Promocion entity, @MappingTarget PromocionClienteDTO dto) {
        // 1. Texto del descuento
        if (entity.getTipoDescuento() == TipoDescuento.PORCENTUAL) {
            dto.setTextoDescuento(entity.getValorDescuento().intValue() + "% OFF");
        } else { // MONTO_FIJO
            dto.setTextoDescuento("$" + entity.getValorDescuento().intValue() + " OFF");
        }

        // 2. Mensaje de condiciones (incluir horario)
        StringBuilder condiciones = new StringBuilder();
        if (entity.getFechaDesde() != null && entity.getFechaHasta() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            condiciones.append("Válido del ")
                    .append(entity.getFechaDesde().format(formatter))
                    .append(" al ")
                    .append(entity.getFechaHasta().format(formatter));
        }
        // ✅ NUEVO: Agregar horario al mensaje
        if (entity.getHoraDesde() != null && entity.getHoraHasta() != null) {
            if (condiciones.length() > 0)
                condiciones.append(". ");
            condiciones.append("Horario: ")
                    .append(entity.getHoraDesde().toString())
                    .append(" a ")
                    .append(entity.getHoraHasta().toString());
        }
        if (entity.getCantidadMinima() != null && entity.getCantidadMinima() > 1) {
            if (condiciones.length() > 0)
                condiciones.append(". ");
            condiciones.append("Compra mínima: ")
                    .append(entity.getCantidadMinima())
                    .append(" unidades");
        }
        dto.setMensajeCondiciones(condiciones.toString());

        // 3. Mapear artículos incluidos
        if (entity.getDetalles() != null && !entity.getDetalles().isEmpty()) {
            List<ArticuloPromocionDTO> articulos = entity.getDetalles().stream()
                    .map(this::mapearArticuloPromocion)
                    .collect(Collectors.toList());
            dto.setArticulosIncluidos(articulos);

            // 4. CALCULAR PRECIOS
            double precioOriginalTotal = calcularPrecioOriginal(entity.getDetalles());
            double precioFinalTotal = calcularPrecioFinal(precioOriginalTotal, entity);
            int porcentajeAhorro = calcularPorcentajeDescuento(precioOriginalTotal, precioFinalTotal);

            dto.setPrecioOriginal(precioOriginalTotal);
            dto.setPrecioFinal(precioFinalTotal);
            dto.setPorcentajeDescuento(porcentajeAhorro);

            // 5. Es combo?
            dto.setEsCombo(entity.getDetalles().size() > 1);
        } else {
            dto.setArticulosIncluidos(new ArrayList<>());
            dto.setPrecioOriginal(0.0);
            dto.setPrecioFinal(0.0);
            dto.setPorcentajeDescuento(0);
            dto.setEsCombo(false);
        }

        // 6. Imagen principal
        if (entity.getImagenes() != null && !entity.getImagenes().isEmpty()) {
            Imagen imagen = entity.getImagenes().get(0);
            ImagenDTO imagenDTO = new ImagenDTO();
            imagenDTO.setIdImagen(imagen.getIdImagen());
            imagenDTO.setDenominacion(imagen.getDenominacion());
            imagenDTO.setUrl(imagen.getUrl());
            dto.setImagenPromocion(imagenDTO);
        }

        // ✅ 7. NUEVO: Estado de disponibilidad
        dto.setDisponibleAhora(entity.estaDisponibleAhora());
        dto.setTextoHorario(entity.getTextoHorario());
    }

    // ==================== MAPEO DE ARTÍCULOS (POLIMÓRFICO) ====================

    /**
     * ✅ Mapea artículos de forma polimórfica: soporta ArticuloManufacturado y
     * ArticuloInsumo.
     * Maneja correctamente los proxies de Hibernate.
     */
    default ArticuloPromocionDTO mapearArticuloPromocion(PromocionDetalle detalle) {
        Articulo articulo = detalle.getArticulo();

        // ✅ CRÍTICO: Desproxyficar el artículo para obtener la clase concreta
        if (!Hibernate.isInitialized(articulo)) {
            log.debug("🔄 Inicializando proxy de artículo ID: {}", articulo.getIdArticulo());
            Hibernate.initialize(articulo);
        }

        // Obtener la entidad real (no el proxy)
        articulo = (Articulo) Hibernate.unproxy(articulo);

        log.debug("📦 Mapeando artículo: {} (Tipo: {})",
                articulo.getDenominacion(),
                articulo.getClass().getSimpleName());

        ArticuloPromocionDTO dto = new ArticuloPromocionDTO();

        // ==================== CAMPOS COMUNES (de Articulo) ====================
        dto.setIdArticulo(articulo.getIdArticulo());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioOriginal(articulo.getPrecioVenta());

        // Categoría
        if (articulo.getCategoria() != null) {
            dto.setNombreCategoria(articulo.getCategoria().getDenominacion());
        }

        // Imagen principal
        if (articulo.getImagenes() != null && !articulo.getImagenes().isEmpty()) {
            Imagen imagen = articulo.getImagenes().get(0);
            ImagenDTO imagenDTO = new ImagenDTO();
            imagenDTO.setIdImagen(imagen.getIdImagen());
            imagenDTO.setDenominacion(imagen.getDenominacion());
            imagenDTO.setUrl(imagen.getUrl());
            dto.setImagenPrincipal(imagenDTO);
        }

        // ==================== CAMPOS ESPECÍFICOS POR TIPO ====================
        if (articulo instanceof ArticuloManufacturado) {
            // ✅ Producto manufacturado (hamburguesa, pizza, etc.)
            ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
            dto.setDescripcion(manufacturado.getDescripcion());
            dto.setTiempoEstimadoEnMinutos(manufacturado.getTiempoEstimadoEnMinutos());

            log.debug("  ↳ Manufacturado - Tiempo: {}min", manufacturado.getTiempoEstimadoEnMinutos());

        } else if (articulo instanceof ArticuloInsumo) {
            // ✅ Insumo de venta directa (bebida, etc.)
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;

            // Los insumos no tienen descripción larga
            dto.setDescripcion("Producto de venta directa");

            // Los insumos no requieren tiempo de preparación
            dto.setTiempoEstimadoEnMinutos(0);

            log.debug("  ↳ Insumo - Venta directa (sin tiempo de preparación)");

        } else {
            // ⚠️ Tipo de artículo desconocido (no debería pasar con el desproxyficar)
            String errorMsg = String.format(
                    "Tipo de artículo no soportado: %s (ID: %d, Clase: %s)",
                    articulo.getDenominacion(),
                    articulo.getIdArticulo(),
                    articulo.getClass().getName());
            log.error("❌ {}", errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        return dto;
    }

    // ==================== CÁLCULOS DE PRECIOS ====================

    /**
     * ✅ Calcula el precio total original (suma de artículos x cantidad).
     * Funciona para cualquier tipo de Articulo (polimórfico).
     */
    default double calcularPrecioOriginal(List<PromocionDetalle> detalles) {
        return detalles.stream()
                .mapToDouble(d -> d.getArticulo().getPrecioVenta() * d.getCantidad())
                .sum();
    }

    /**
     * ✅ Calcula el precio final con descuento aplicado.
     */
    default double calcularPrecioFinal(double precioOriginal, Promocion promocion) {
        if (promocion.getTipoDescuento() == TipoDescuento.PORCENTUAL) {
            // Descuento porcentual: precio * (1 - descuento/100)
            return precioOriginal * (1 - promocion.getValorDescuento() / 100);
        } else { // MONTO_FIJO
            // Descuento fijo: precio - descuento
            return Math.max(precioOriginal - promocion.getValorDescuento(), 0.0);
        }
    }

    /**
     * ✅ Calcula el porcentaje de descuento.
     */
    default int calcularPorcentajeDescuento(double precioOriginal, double precioFinal) {
        if (precioOriginal == 0)
            return 0;
        double porcentaje = ((precioOriginal - precioFinal) / precioOriginal) * 100;
        return (int) Math.round(porcentaje);
    }
}