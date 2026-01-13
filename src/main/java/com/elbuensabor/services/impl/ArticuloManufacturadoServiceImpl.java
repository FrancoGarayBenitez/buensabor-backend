package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.request.DetalleManufacturadoRequestDTO;
import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.*;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.*;
import com.elbuensabor.services.IArticuloManufacturadoService;
import com.elbuensabor.services.IImagenService;
import com.elbuensabor.services.mapper.ArticuloManufacturadoMapper;
import com.elbuensabor.services.mapper.DetalleManufacturadoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloManufacturadoServiceImpl implements IArticuloManufacturadoService {

    private static final Logger log = LoggerFactory.getLogger(ArticuloManufacturadoServiceImpl.class);

    @Autowired
    private IArticuloManufacturadoRepository repository;
    @Autowired
    private ArticuloManufacturadoMapper mapper;
    @Autowired
    private DetalleManufacturadoMapper detalleMapper;
    @Autowired
    private ICategoriaRepository categoriaRepository;
    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;
    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;
    @Autowired
    private IArticuloRepository articuloRepository;
    @Autowired
    private IImagenService imagenService;

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findAll() {
        log.info("Buscando todos los productos manufacturados");
        return repository.findAll().stream().map(this::enriquecerDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturadoResponseDTO findById(Long id) {
        log.info("Buscando producto manufacturado con ID: {}", id);
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return enriquecerDTO(manufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO create(ArticuloManufacturadoRequestDTO requestDTO) {
        log.info("Creando nuevo producto manufacturado: {}", requestDTO.getDenominacion());
        log.info("Request DTO recibido: {}", requestDTO.toString());

        // ✅ Unicidad GLOBAL (incluye desactivados). Si existe, informar estado.
        articuloRepository.findByDenominacionIgnoreCaseIncludingEliminado(requestDTO.getDenominacion())
                .ifPresent(a -> {
                    String estado = Boolean.TRUE.equals(a.getEliminado()) ? "desactivado" : "activo";
                    throw new DuplicateResourceException("Ya existe un artículo " + estado +
                            " con la denominación: " + requestDTO.getDenominacion() +
                            (Boolean.TRUE.equals(a.getEliminado()) ? ". Puede reactivarlo." : ""));
                });

        ArticuloManufacturado manufacturado = mapper.toEntity(requestDTO);
        asignarRelaciones(manufacturado, requestDTO);
        validarCategoria(manufacturado.getCategoria());
        actualizarDetalles(manufacturado, requestDTO.getDetalles());
        manufacturado.actualizarCostoProduccion();
        if (requestDTO.getPrecioVenta() == null || requestDTO.getPrecioVenta() == 0) {
            manufacturado.actualizarPrecioVenta();
        }

        // ✅ CORRECCIÓN: Reordenar las operaciones
        // 1️⃣ Guardar la entidad principal PRIMERO para obtener un ID.
        ArticuloManufacturado saved = repository.save(manufacturado);
        log.info("Producto {} pre-guardado con ID {}", saved.getDenominacion(), saved.getIdArticulo());

        // 2️⃣ AHORA, manejar las imágenes, pasando la entidad YA GUARDADA (con ID).
        manejarImagenes(saved, requestDTO.getImagenes());

        // 3️⃣ Volver a guardar para persistir las asociaciones de imágenes.
        ArticuloManufacturado finalManufacturado = repository.save(saved);

        log.info("Producto {} creado y finalizado con ID {}", finalManufacturado.getDenominacion(),
                finalManufacturado.getIdArticulo());
        return enriquecerDTO(finalManufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO update(Long id, ArticuloManufacturadoRequestDTO requestDTO) {
        log.info("Actualizando producto manufacturado con ID: {}", id);

        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // ✅ Unicidad GLOBAL en update (excluye el propio ID)
        articuloRepository.findByDenominacionIgnoreCaseAndIdNotIncludingEliminado(requestDTO.getDenominacion(), id)
                .ifPresent(a -> {
                    String estado = Boolean.TRUE.equals(a.getEliminado()) ? "desactivado" : "activo";
                    throw new DuplicateResourceException("Ya existe otro artículo " + estado +
                            " con la denominación: " + requestDTO.getDenominacion());
                });

        mapper.updateEntityFromDTO(requestDTO, manufacturado);
        asignarRelaciones(manufacturado, requestDTO);
        validarCategoria(manufacturado.getCategoria());
        actualizarDetalles(manufacturado, requestDTO.getDetalles());
        manufacturado.actualizarCostoProduccion();
        if (requestDTO.getPrecioVenta() == null) {
            manufacturado.actualizarPrecioVenta();
        }
        manejarImagenes(manufacturado, requestDTO.getImagenes());

        ArticuloManufacturado updated = repository.save(manufacturado);
        return enriquecerDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Baja lógica (DELETE) de producto con ID: {}", id);
        deactivate(id);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        log.info("Desactivando producto con ID: {}", id);
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        manufacturado.setEliminado(true);
        repository.save(manufacturado);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        log.info("Activando producto con ID: {}", id);
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        manufacturado.setEliminado(false);
        repository.save(manufacturado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> search(String denominacion) {
        log.info("Buscando productos por denominación: {}", denominacion);
        return repository.findByDenominacionContainingIgnoreCase(denominacion).stream()
                .map(this::enriquecerDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria) {
        log.info("Buscando productos por categoría ID: {}", idCategoria);
        return repository.findByCategoriaIdCategoria(idCategoria).stream()
                .map(this::enriquecerDTO).collect(Collectors.toList());
    }

    // --- Métodos privados auxiliares (sin cambios de comportamiento) ---
    private ArticuloManufacturadoResponseDTO enriquecerDTO(ArticuloManufacturado entity) {
        ArticuloManufacturadoResponseDTO dto = mapper.toDTO(entity);
        dto.setStockSuficiente(entity.verificarStockSuficiente(1));
        dto.setCantidadMaximaPreparable(entity.calcularCantidadMaximaPreparable());
        return dto;
    }

    private void asignarRelaciones(ArticuloManufacturado manufacturado, ArticuloManufacturadoRequestDTO dto) {
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getIdUnidadMedida())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidad de medida no encontrada con ID: " + dto.getIdUnidadMedida()));
        manufacturado.setUnidadMedida(unidadMedida);

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getIdCategoria()));
        manufacturado.setCategoria(categoria);

        manufacturado.setMargenGananciaPorcentaje(dto.getMargenGananciaPorcentaje());
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria.getTipoCategoria() != TipoCategoria.COMIDAS) {
            throw new IllegalArgumentException("La categoría de un producto manufacturado debe ser de tipo COMIDAS.");
        }
    }

    private void actualizarDetalles(ArticuloManufacturado manufacturado,
            List<DetalleManufacturadoRequestDTO> detallesDTO) {

        // ✅ LOGGING: Verificar qué lista de detalles llega a este método
        log.info("Actualizando detalles. Recibidos {} detalles.", detallesDTO != null ? detallesDTO.size() : "null");

        manufacturado.getDetalles().clear();
        if (detallesDTO != null) {
            for (DetalleManufacturadoRequestDTO detalleDTO : detallesDTO) {
                // ✅ LOGGING: Imprimir el ID del insumo que se va a buscar
                log.info("Procesando detalle para insumo ID: {}", detalleDTO.getIdArticuloInsumo());

                if (detalleDTO.getIdArticuloInsumo() == null) {
                    log.error("Se encontró un detalle con ID de insumo nulo. Detalle: {}", detalleDTO);
                    throw new IllegalArgumentException("Se encontró un detalle con ID de insumo nulo.");
                }

                ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDTO.getIdArticuloInsumo())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Insumo no encontrado con ID: " + detalleDTO.getIdArticuloInsumo()));

                DetalleManufacturado detalle = detalleMapper.toEntity(detalleDTO);
                detalle.setArticuloInsumo(insumo);
                detalle.setArticuloManufacturado(manufacturado);
                manufacturado.getDetalles().add(detalle);
            }
        }
    }

    private void manejarImagenes(Articulo articulo, List<ImagenDTO> imagenesDTO) {
        if (articulo.getImagenes() == null) {
            articulo.setImagenes(new ArrayList<>());
        }

        // Si viene array vacío, limpiar todas
        if (imagenesDTO == null || imagenesDTO.isEmpty()) {
            articulo.getImagenes().clear();
            return;
        }

        // Recolectar IDs de imágenes existentes en el request
        java.util.Set<Long> idsEnRequest = imagenesDTO.stream()
                .map(ImagenDTO::getIdImagen)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 1️⃣ Eliminar imágenes que NO están en el request
        articulo.getImagenes().removeIf(img -> !idsEnRequest.contains(img.getIdImagen()));

        // 2️⃣ Crear o actualizar imágenes del request
        for (ImagenDTO imgDTO : imagenesDTO) {
            if (imgDTO.getIdImagen() == null) {
                // Crear registro de imagen asociado al artículo
                Imagen newImg = imagenService.createFromExistingUrl(
                        imgDTO.getDenominacion(),
                        imgDTO.getUrl(),
                        articulo.getIdArticulo());
                articulo.getImagenes().add(newImg);
                log.info("✅ Nueva imagen creada y asociada: {}", imgDTO.getDenominacion());
            } else {
                // ✅ EXISTENTE: Actualizar si es necesario
                articulo.getImagenes().stream()
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