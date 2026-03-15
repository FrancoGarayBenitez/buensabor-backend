package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.cliente.CatalogoArticuloDTO;
import com.elbuensabor.dto.response.cliente.DetalleArticuloDTO;
import com.elbuensabor.dto.response.cliente.PromocionClienteDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.entities.EstadoStock;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IArticuloManufacturadoRepository;
import com.elbuensabor.repository.IPromocionRepository;
import com.elbuensabor.services.ICatalogoClienteService;
import com.elbuensabor.services.mapper.CatalogoArticuloMapper;
import com.elbuensabor.services.mapper.DetalleArticuloMapper;
import com.elbuensabor.services.mapper.PromocionClienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoClienteServiceImpl implements ICatalogoClienteService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClienteServiceImpl.class);

    // ✅ Repositorio base: cubre ArticuloManufacturado + ArticuloInsumo
    @Autowired
    private IArticuloRepository articuloRepository;

    // ✅ Solo para métodos exclusivos de manufacturados
    @Autowired
    private IArticuloManufacturadoRepository manufacturadoRepository;

    @Autowired
    private IPromocionRepository promocionRepository;

    @Autowired
    private CatalogoArticuloMapper catalogoMapper;

    @Autowired
    private DetalleArticuloMapper detalleMapper;

    @Autowired
    private PromocionClienteMapper promocionMapper;

    // ==================== ARTÍCULOS ====================

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoArticuloDTO> obtenerArticulosDisponibles() {
        log.info("🛍️ Cliente consultando catálogo de productos disponibles");

        List<Articulo> articulos = articuloRepository.findDisponiblesParaCatalogo();

        log.info("📊 Total artículos para catálogo: {}", articulos.size());
        return catalogoMapper.toDTOList(articulos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoArticuloDTO> obtenerArticulosPorCategoria(Long idCategoria) {
        log.info("🔍 Cliente filtrando por categoría ID: {}", idCategoria);

        List<Articulo> articulos = articuloRepository.findDisponiblesParaCatalogoPorCategoria(idCategoria);

        log.info("📊 Artículos en categoría {}: {}", idCategoria, articulos.size());
        return catalogoMapper.toDTOList(articulos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoArticuloDTO> buscarArticulos(String query) {
        log.info("🔍 Cliente buscando: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            return obtenerArticulosDisponibles();
        }

        List<Articulo> articulos = articuloRepository.findDisponiblesParaCatalogoPorNombre(query.trim());

        log.info("📊 Búsqueda '{}': {} resultados", query, articulos.size());
        return catalogoMapper.toDTOList(articulos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoArticuloDTO> obtenerArticulosEnPromocion() {
        log.info("🎁 Cliente consultando artículos en promoción");

        // Las promociones solo aplican a manufacturados
        List<ArticuloManufacturado> articulos = manufacturadoRepository.findByEliminadoFalse();

        return articulos.stream()
                .filter(a -> !a.getEliminado() && a.verificarStockSuficiente(1))
                .filter(ArticuloManufacturado::tienePromocionVigente)
                .map(catalogoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleArticuloDTO obtenerDetalleArticulo(Long idArticulo) {
        log.info("🔍 Cliente consultando detalle del artículo ID: {}", idArticulo);

        // ✅ Buscar en repositorio base (cubre ambos tipos)
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artículo no encontrado con ID: " + idArticulo));

        if (articulo.getEliminado()) {
            throw new ResourceNotFoundException("El artículo no está disponible");
        }

        if (articulo instanceof ArticuloManufacturado) {
            log.info("✅ Artículo {} es MANUFACTURADO", idArticulo);
            return detalleMapper.toDTO((ArticuloManufacturado) articulo);
        }

        if (articulo instanceof ArticuloInsumo) {
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;
            if (insumo.getEsParaElaborar()) {
                log.warn("❌ Artículo {} es insumo de elaboración, no disponible para venta", idArticulo);
                throw new ResourceNotFoundException(
                        "El artículo no está disponible para venta directa");
            }
            log.info("✅ Artículo {} es INSUMO de venta directa (bebida)", idArticulo);
            return buildDetalleInsumo(insumo);
        }

        throw new ResourceNotFoundException("Tipo de artículo no soportado");
    }

    // ==================== PROMOCIONES (sin cambios) ====================

    @Override
    @Transactional(readOnly = true)
    public List<PromocionClienteDTO> obtenerPromocionesVigentes() {
        log.info("🎁 ========== INICIO: obtenerPromocionesVigentes ==========");

        // Cargar promociones con detalles
        List<Promocion> promocionesQuery = promocionRepository.findAllVigentesConDetalles();
        log.info("📊 Promociones de findAllVigentesConDetalles(): {}", promocionesQuery.size());

        if (promocionesQuery.isEmpty()) {
            log.warn("⚠️ La query findAllVigentesConDetalles() no devolvió resultados");
            return List.of();
        }

        // Cargar imágenes
        try {
            promocionRepository.fetchImagenesPromocion(promocionesQuery);
            log.debug("🖼️ Imágenes de promociones cargadas");
        } catch (Exception e) {
            log.error("❌ Error cargando imágenes de promociones: {}", e.getMessage());
        }

        try {
            promocionRepository.fetchImagenesArticulos(promocionesQuery);
            log.debug("🖼️ Imágenes de artículos cargadas");
        } catch (Exception e) {
            log.error("❌ Error cargando imágenes de artículos: {}", e.getMessage());
        }

        // ✅ CAMBIO: Usar estaVigenteParaMostrar() en lugar de estaVigente()
        // Esto muestra TODAS las promos del día, sin importar la hora actual
        log.info("📊 Aplicando filtro estaVigenteParaMostrar() en Java...");

        List<PromocionClienteDTO> resultado = promocionesQuery.stream()
                .filter(promo -> {
                    boolean mostrar = promo.estaVigenteParaMostrar();
                    log.info("  {} ID: {} - '{}' | estaVigenteParaMostrar(): {} | estaDisponibleAhora(): {}",
                            mostrar ? "✅" : "❌",
                            promo.getIdPromocion(),
                            promo.getDenominacion(),
                            mostrar,
                            promo.estaDisponibleAhora());

                    if (!mostrar) {
                        log.info("     ↳ Razón: activo={}, eliminado={}, enRangoFechas={}",
                                promo.getActivo(),
                                promo.getEliminado(),
                                promo.estaEnRangoFechas());
                    }
                    return mostrar;
                })
                .map(promo -> {
                    try {
                        PromocionClienteDTO dto = promocionMapper.toDTO(promo);
                        log.debug("  ✅ Mapeado OK: '{}' (${} -> ${}, {} artículos)",
                                dto.getNombre(),
                                dto.getPrecioOriginal(),
                                dto.getPrecioFinal(),
                                dto.getArticulosIncluidos().size());
                        return dto;
                    } catch (Exception e) {
                        log.error("  ❌ Error mapeando promoción ID {}: {}",
                                promo.getIdPromocion(), e.getMessage(), e);
                        throw e;
                    }
                })
                .collect(Collectors.toList());

        log.info("🎁 ========== FIN: {} promociones vigentes de {} consultadas ==========",
                resultado.size(), promocionesQuery.size());

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionClienteDTO obtenerDetallePromocion(Long idPromocion) {
        log.info("🎁 Cliente consultando detalle de promoción ID: {}", idPromocion);

        // ✅ PASO 1: Cargar promoción con detalles
        Promocion promocion = promocionRepository.findByIdConDetalles(idPromocion)
                .orElseThrow(() -> {
                    log.warn("❌ Promoción no encontrada: ID {}", idPromocion);
                    return new ResourceNotFoundException(
                            "Promoción no encontrada con ID: " + idPromocion);
                });

        // ✅ PASO 2: Cargar imágenes
        promocionRepository.fetchImagenesPorId(idPromocion);
        promocionRepository.fetchImagenesArticulos(List.of(promocion));

        // Validaciones básicas
        if (promocion.getEliminado()) {
            log.warn("❌ Promoción {} está eliminada", idPromocion);
            throw new ResourceNotFoundException("La promoción no está disponible");
        }

        if (!promocion.getActivo()) {
            log.warn("❌ Promoción {} está inactiva", idPromocion);
            throw new ResourceNotFoundException("La promoción no está activa");
        }

        // ✅ CAMBIO: Usar estaVigenteParaMostrar() en lugar de estaVigente()
        // Esto permite ver el detalle de promos fuera de horario pero dentro del rango
        // de fechas
        if (!promocion.estaVigenteParaMostrar()) {
            log.warn("❌ Promoción {} no está vigente para mostrar - Estado: {}",
                    idPromocion, promocion.getEstado());
            throw new ResourceNotFoundException(
                    "La promoción no está vigente. Estado: " + promocion.getEstado());
        }

        PromocionClienteDTO dto = promocionMapper.toDTO(promocion);

        // ✅ Log adicional para debug
        log.info("✅ Detalle de promoción '{}' devuelto (${} -> ${}) | disponibleAhora: {}",
                dto.getNombre(),
                dto.getPrecioOriginal(),
                dto.getPrecioFinal(),
                dto.getDisponibleAhora());

        return dto;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Construye el DTO de detalle para un ArticuloInsumo de venta directa (bebida).
     * No usa mapper porque los campos difieren completamente de
     * ArticuloManufacturado.
     */
    private DetalleArticuloDTO buildDetalleInsumo(ArticuloInsumo insumo) {
        DetalleArticuloDTO dto = new DetalleArticuloDTO();

        dto.setTipoArticulo("INSUMO");
        dto.setIdArticulo(insumo.getIdArticulo());
        dto.setDenominacion(insumo.getDenominacion());
        dto.setPrecioOriginal(insumo.getPrecioVenta());
        dto.setPrecioFinal(insumo.getPrecioVenta());
        dto.setPorcentajeDescuento(0);
        dto.setAhorroEnPesos(0.0);
        dto.setTiempoEstimadoEnMinutos(0);

        if (insumo.getCategoria() != null) {
            dto.setIdCategoria(insumo.getCategoria().getIdCategoria());
            dto.setNombreCategoria(insumo.getCategoria().getDenominacion());
        }

        // ✅ Disponibilidad y mensaje usando estadoStock (sin stockMinimo)
        EstadoStock estado = resolverEstadoStock(insumo.getEstadoStock());
        boolean disponible = !insumo.getEliminado()
                && insumo.getStockActual() != null
                && insumo.getStockActual() > 0
                && estado != EstadoStock.CRITICO;

        dto.setDisponible(disponible);
        dto.setMensajeDisponibilidad(resolverMensajeDisponibilidad(disponible, estado));

        // Campos específicos de insumo
        dto.setStockActual(insumo.getStockActual());
        dto.setUnidadMedida(
                insumo.getUnidadMedida() != null
                        ? insumo.getUnidadMedida().getDenominacion()
                        : null);

        // Sin promoción ni ingredientes
        dto.setPromocionActiva(null);
        dto.setIngredientesPrincipales(new ArrayList<>());

        // Imágenes
        if (insumo.getImagenes() != null && !insumo.getImagenes().isEmpty()) {
            List<ImagenDTO> imagenesDTO = insumo.getImagenes().stream()
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

        return dto;
    }

    /**
     * Convierte el String estadoStock al enum, con fallback seguro.
     */
    private EstadoStock resolverEstadoStock(String estadoStock) {
        if (estadoStock == null)
            return EstadoStock.CRITICO;
        try {
            return EstadoStock.valueOf(estadoStock.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ EstadoStock desconocido: '{}', se asume CRITICO", estadoStock);
            return EstadoStock.CRITICO;
        }
    }

    /**
     * Resuelve el mensaje de disponibilidad según estado de stock.
     *
     * CRITICO (0-25%) → no disponible → "Agotado"
     * BAJO (26-50%) → disponible → "¡Últimas unidades!"
     * NORMAL (51-75%) → disponible → "Disponible"
     * ALTO (76-100%)→ disponible → "Disponible"
     */
    private String resolverMensajeDisponibilidad(boolean disponible, EstadoStock estado) {
        if (!disponible)
            return "Agotado";
        switch (estado) {
            case BAJO:
                return "¡Últimas unidades!";
            case NORMAL:
            case ALTO:
            default:
                return "Disponible";
        }
    }
}