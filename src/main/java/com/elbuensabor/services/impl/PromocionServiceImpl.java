package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.request.PromocionDetalleDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.entities.PromocionDetalle;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
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
import java.util.Map;
import java.util.function.Function;
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
    private IImagenService imagenService; // ✅ Inyectar el servicio

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findAll() {
        log.info("Buscando todas las promociones");
        return repository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO findById(Long id) {
        log.info("Buscando promoción con ID: {}", id);
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        return mapper.toDTO(promocion);
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

        log.info("Promoción {} creada y finalizada con ID {}", finalPromocion.getDenominacion(),
                finalPromocion.getIdPromocion());
        return mapper.toDTO(finalPromocion);
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

        return mapper.toDTO(finalPromocion);
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
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> search(String denominacion) {
        log.info("Buscando promociones por denominación: {}", denominacion);
        return repository.findByDenominacionContainingIgnoreCaseAndEliminadoFalse(denominacion).stream()
                .map(mapper::toDTO).collect(Collectors.toList());
    }

    // --- Métodos privados auxiliares ---

    private void asignarDetalles(Promocion promocion, List<PromocionDetalleDTO> detallesDTO) {
        if (detallesDTO == null || detallesDTO.isEmpty()) {
            promocion.getDetalles().clear();
            return;
        }

        // Crear un mapa de los detalles existentes para una búsqueda eficiente
        Map<Long, PromocionDetalle> detallesExistentes = promocion.getDetalles().stream()
                .collect(Collectors.toMap(d -> d.getArticulo().getIdArticulo(), Function.identity()));

        // Crear un conjunto de IDs de artículos del DTO para detectar eliminaciones
        java.util.Set<Long> idsArticulosDTO = detallesDTO.stream()
                .map(PromocionDetalleDTO::getIdArticulo)
                .collect(Collectors.toSet());

        // 1. Eliminar detalles que ya no están en el DTO
        promocion.getDetalles().removeIf(detalle -> !idsArticulosDTO.contains(detalle.getArticulo().getIdArticulo()));

        // 2. Actualizar detalles existentes y añadir nuevos
        for (PromocionDetalleDTO dto : detallesDTO) {
            Articulo articulo = articuloRepository.findById(dto.getIdArticulo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con ID: " + dto.getIdArticulo()));

            PromocionDetalle detalleExistente = detallesExistentes.get(dto.getIdArticulo());

            if (detalleExistente != null) {
                // Actualizar cantidad si el detalle ya existe
                detalleExistente.setCantidad(dto.getCantidad());
            } else {
                // Crear nuevo detalle si no existe
                PromocionDetalle nuevoDetalle = PromocionDetalle.builder()
                        .promocion(promocion)
                        .articulo(articulo)
                        .cantidad(dto.getCantidad())
                        .build();
                promocion.getDetalles().add(nuevoDetalle);
            }
        }
    }

    private void manejarImagenes(Promocion promocion, List<ImagenDTO> imagenesDTO) {
        if (promocion.getImagenes() == null) {
            promocion.setImagenes(new ArrayList<>());
        }

        if (imagenesDTO == null || imagenesDTO.isEmpty()) {
            promocion.getImagenes().clear();
            return;
        }

        // Recolectar IDs de imágenes que vienen en el request
        java.util.Set<Long> idsEnRequest = imagenesDTO.stream()
                .map(ImagenDTO::getIdImagen)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 1️⃣ Eliminar imágenes que NO están en el request
        promocion.getImagenes().removeIf(img -> !idsEnRequest.contains(img.getIdImagen()));

        // 2️⃣ Crear o actualizar imágenes del request
        for (ImagenDTO imgDTO : imagenesDTO) {
            if (imgDTO.getIdImagen() == null) {
                // ✅ NUEVA: Crear registro de imagen asociado a la promoción
                Imagen newImg = imagenService.createFromExistingUrlPromocion(
                        imgDTO.getDenominacion(),
                        imgDTO.getUrl(),
                        promocion.getIdPromocion());
                promocion.getImagenes().add(newImg);
                log.info("✅ Nueva imagen creada y asociada a promoción: {}", imgDTO.getDenominacion());
            } else {
                // ✅ EXISTENTE: Actualizar si es necesario
                promocion.getImagenes().stream()
                        .filter(img -> img.getIdImagen().equals(imgDTO.getIdImagen()))
                        .findFirst()
                        .ifPresent(img -> {
                            img.setDenominacion(imgDTO.getDenominacion());
                            img.setUrl(imgDTO.getUrl());
                        });
            }
        }
    }
}