package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.request.PromocionDetalleDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.entities.PromocionDetalle;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloManufacturadoRepository;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IImagenRepository;
import com.elbuensabor.repository.IPromocionRepository;
import com.elbuensabor.services.IImagenService;
import com.elbuensabor.services.IPromocionService;
import com.elbuensabor.services.mapper.PromocionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionServiceImpl implements IPromocionService {

    private static final Logger log = LoggerFactory.getLogger(PromocionServiceImpl.class);

    @Autowired
    private IPromocionRepository repository;
    @Autowired
    private PromocionMapper mapper;
    @Autowired
    private IArticuloRepository articuloRepository;
    @Autowired
    private IImagenService imagenService;
    @Autowired
    private IArticuloManufacturadoRepository articuloManufacturadoRepository;
    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;
    @Autowired
    private IImagenRepository imagenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findAll() {
        log.info("Buscando todas las promociones");
        return repository.findAll().stream()
                .map(this::toDTOConResumen) // ✅ usar wrapper con resumen
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO findById(Long id) {
        log.info("Buscando promoción con ID: {}", id);
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        return toDTOConResumen(promocion); // ✅ usar wrapper con resumen
    }

    @Override
    @Transactional
    public PromocionResponseDTO create(PromocionRequestDTO requestDTO) {
        log.info("Creando nueva promoción: {}", requestDTO.getDenominacion());

        repository.findByDenominacionIgnoreCaseIncludingEliminado(requestDTO.getDenominacion())
                .ifPresent(p -> {
                    String estado = Boolean.TRUE.equals(p.getEliminado()) ? "desactivada" : "activa";
                    throw new DuplicateResourceException("Ya existe una promoción " + estado +
                            " con la denominación: " + requestDTO.getDenominacion());
                });

        Promocion promocion = mapper.toEntity(requestDTO);

        // ✅ Guardar la promoción primero para obtener un ID
        Promocion savedPromocion = repository.save(promocion);
        log.info("Promoción {} pre-guardada con ID {}", savedPromocion.getDenominacion(),
                savedPromocion.getIdPromocion());

        // ✅ Asignar detalles y manejar imágenes
        asignarDetalles(savedPromocion, requestDTO.getDetalles());
        manejarImagenes(savedPromocion, requestDTO.getImagenes());

        // ✅ Guardar de nuevo para persistir las relaciones
        Promocion finalPromocion = repository.save(savedPromocion);
        log.info("Promoción {} creada con ID {}", finalPromocion.getDenominacion(), finalPromocion.getIdPromocion());
        return toDTOConResumen(finalPromocion); // ✅
    }

    @Override
    @Transactional
    public PromocionResponseDTO update(Long id, PromocionRequestDTO requestDTO) {
        log.info("Actualizando promoción con ID: {}", id);

        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));

        repository.findByDenominacionIgnoreCaseAndIdNotIncludingEliminado(requestDTO.getDenominacion(), id)
                .ifPresent(p -> {
                    String estado = Boolean.TRUE.equals(p.getEliminado()) ? "desactivada" : "activa";
                    throw new DuplicateResourceException("Ya existe otra promoción " + estado +
                            " con la denominación: " + requestDTO.getDenominacion());
                });

        mapper.updateFromDTO(requestDTO, promocion);

        // ✅ Usar la nueva lógica para detalles e imágenes
        asignarDetalles(promocion, requestDTO.getDetalles());
        manejarImagenes(promocion, requestDTO.getImagenes());

        Promocion finalPromocion = repository.save(promocion);
        return toDTOConResumen(finalPromocion); // ✅
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Baja lógica (DELETE) de promoción con ID: {}", id);
        deactivate(id);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        log.info("Desactivando promoción con ID: {}", id);
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        promocion.setEliminado(true);
        repository.save(promocion);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activando promoción con ID: {}", id);
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        promocion.setEliminado(false);
        repository.save(promocion);
    }

    @Override
    @Transactional
    public PromocionResponseDTO toggleActivo(Long id) {
        log.info("Cambiando estado 'activo' de la promoción con ID: {}", id);
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        promocion.setActivo(!promocion.getActivo());
        Promocion updated = repository.save(promocion);
        return toDTOConResumen(updated); // ✅
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> search(String denominacion) {
        return repository.findByDenominacionContainingIgnoreCaseAndEliminadoFalse(denominacion).stream()
                .map(this::toDTOConResumen) // ✅
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * ✅ Convierte Promocion a DTO e inyecta el resumen financiero calculado.
     * Alineado con el mismo cálculo que PedidoServiceImpl.
     */
    private PromocionResponseDTO toDTOConResumen(Promocion promocion) {
        PromocionResponseDTO dto = mapper.toDTO(promocion);

        if (promocion.getDetalles() == null || promocion.getDetalles().isEmpty()) {
            dto.setPrecioOriginal(0.0);
            dto.setPrecioFinal(0.0);
            dto.setAhorro(0.0);
            dto.setTotalCosto(0.0);
            dto.setGananciaEstimada(0.0);
            dto.setMargenGanancia(0.0);
            return dto;
        }

        // ✅ Igual que PedidoServiceImpl: cargar subclase concreta para tener los campos
        // de costo
        double precioOriginal = 0.0;
        double totalCosto = 0.0;

        for (PromocionDetalle pd : promocion.getDetalles()) {
            Articulo art = cargarArticuloReal(pd.getArticulo().getIdArticulo());
            double cantidad = pd.getCantidad();

            precioOriginal += art.getPrecioVenta() * cantidad;
            totalCosto += getCostoArticulo(art) * cantidad;

            log.debug("   📊 '{}' ({}): precioVenta=${} | costo=${} | x{}",
                    art.getDenominacion(),
                    art.getClass().getSimpleName(),
                    art.getPrecioVenta(),
                    getCostoArticulo(art),
                    cantidad);
        }

        // ✅ Mismo cálculo que procesarDetallesPedido
        double precioFinal = switch (promocion.getTipoDescuento()) {
            case PORCENTUAL -> precioOriginal * (1 - promocion.getValorDescuento() / 100.0);
            case MONTO_FIJO -> Math.max(0, precioOriginal - promocion.getValorDescuento());
        };

        double ahorro = precioOriginal - precioFinal;
        double gananciaEstimada = precioFinal - totalCosto;
        double margen = precioFinal > 0
                ? Math.round((gananciaEstimada / precioFinal) * 1000.0) / 10.0
                : 0.0;

        dto.setPrecioOriginal(precioOriginal);
        dto.setPrecioFinal(precioFinal);
        dto.setAhorro(ahorro);
        dto.setTotalCosto(totalCosto);
        dto.setGananciaEstimada(gananciaEstimada);
        dto.setMargenGanancia(margen);

        log.info("📊 Promo '{}' → Original: ${} | Final: ${} | Costo: ${} | Ganancia: ${} | Margen: {}%",
                promocion.getDenominacion(),
                String.format("%.2f", precioOriginal),
                String.format("%.2f", precioFinal),
                String.format("%.2f", totalCosto),
                String.format("%.2f", gananciaEstimada),
                margen);

        return dto;
    }

    // ✅ Igual que PedidoServiceImpl — fuente única de verdad
    private Articulo cargarArticuloReal(Long idArticulo) {
        var manufacturado = articuloManufacturadoRepository.findById(idArticulo);
        if (manufacturado.isPresent())
            return manufacturado.get();

        var insumo = articuloInsumoRepository.findById(idArticulo);
        if (insumo.isPresent())
            return insumo.get();

        return articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado: " + idArticulo));
    }

    private double getCostoArticulo(Articulo articulo) {
        if (articulo instanceof ArticuloManufacturado am)
            return am.getCostoProduccion() != null ? am.getCostoProduccion() : 0.0;
        if (articulo instanceof ArticuloInsumo ai)
            return ai.getPrecioCompra() != null ? ai.getPrecioCompra() : 0.0;
        return 0.0;
    }

    /**
     * ✅ Asigna los detalles (artículos + cantidades) a la promoción.
     * Reemplaza la lista existente para soportar updates correctamente.
     */
    private void asignarDetalles(Promocion promocion, List<PromocionDetalleDTO> detallesDTO) {
        if (detallesDTO == null || detallesDTO.isEmpty()) {
            log.warn("⚠️  La promoción '{}' no tiene detalles", promocion.getDenominacion());
            return;
        }

        // Limpiar detalles existentes (para updates)
        if (promocion.getDetalles() == null) {
            promocion.setDetalles(new ArrayList<>());
        } else {
            promocion.getDetalles().clear();
        }

        for (PromocionDetalleDTO detalleDTO : detallesDTO) {
            Articulo articulo = articuloRepository.findById(detalleDTO.getIdArticulo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con ID: " + detalleDTO.getIdArticulo()));

            PromocionDetalle detalle = PromocionDetalle.builder()
                    .promocion(promocion)
                    .articulo(articulo)
                    .cantidad(detalleDTO.getCantidad())
                    .build();

            promocion.getDetalles().add(detalle);

            log.debug("   ✅ Detalle asignado: '{}' x{}", articulo.getDenominacion(), detalleDTO.getCantidad());
        }

        log.info("✅ {} detalles asignados a la promoción '{}'",
                detallesDTO.size(), promocion.getDenominacion());
    }

    /**
     * ✅ Maneja la sincronización de imágenes de la promoción.
     * - Elimina las que ya no están en la nueva lista.
     * - Agrega las nuevas.
     */
    private void manejarImagenes(Promocion promocion, List<ImagenDTO> imagenesDTO) {
        if (imagenesDTO == null) {
            log.debug("No se enviaron imágenes para la promoción '{}'", promocion.getDenominacion());
            return;
        }

        if (promocion.getImagenes() == null) {
            promocion.setImagenes(new ArrayList<>());
        }

        // URLs que vienen en el request
        List<String> urlsNuevas = imagenesDTO.stream()
                .map(ImagenDTO::getUrl)
                .collect(Collectors.toList());

        // ✅ Eliminar imágenes que ya no están en la lista nueva
        // Usar deleteCompletely(id) que elimina BD + archivo físico
        List<Imagen> imagenesAEliminar = promocion.getImagenes().stream()
                .filter(img -> !urlsNuevas.contains(img.getUrl()))
                .collect(Collectors.toList());

        imagenesAEliminar.forEach(img -> {
            log.debug("   🗑️ Eliminando imagen: {}", img.getUrl());
            try {
                imagenService.deleteCompletely(img.getIdImagen());
            } catch (Exception e) {
                // Si no existe en disco, continuar igual
                log.warn("⚠️ No se pudo eliminar completamente la imagen {}: {}",
                        img.getUrl(), e.getMessage());
            }
        });
        promocion.getImagenes().removeAll(imagenesAEliminar);

        // URLs actuales tras la limpieza
        List<String> urlsActuales = promocion.getImagenes().stream()
                .map(Imagen::getUrl)
                .collect(Collectors.toList());

        // ✅ Agregar imágenes nuevas que aún no existen
        imagenesDTO.stream()
                .filter(dto -> !urlsActuales.contains(dto.getUrl()))
                .forEach(dto -> {
                    // ✅ Si ya existe en BD (subida previamente), reutilizarla
                    // Si no existe, crearla asociada a la promoción
                    Imagen imagen = imagenRepository.findByUrl(dto.getUrl())
                            .map(existente -> {
                                // Reasociar a esta promoción si estaba huérfana
                                existente.setPromocion(promocion);
                                log.debug("   🔗 Imagen reutilizada: {}", dto.getUrl());
                                return existente;
                            })
                            .orElseGet(() -> {
                                Imagen nueva = new Imagen();
                                nueva.setUrl(dto.getUrl());
                                nueva.setDenominacion(promocion.getDenominacion());
                                nueva.setPromocion(promocion);
                                log.debug("   ✅ Imagen nueva agregada: {}", dto.getUrl());
                                return nueva;
                            });

                    promocion.getImagenes().add(imagen);
                });

        log.info("✅ Imágenes sincronizadas para '{}': {} imagen(es) activa(s)",
                promocion.getDenominacion(), promocion.getImagenes().size());
    }
}