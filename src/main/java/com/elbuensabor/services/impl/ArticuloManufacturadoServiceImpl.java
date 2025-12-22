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
        if (requestDTO.getPrecioVenta() == null) {
            manufacturado.actualizarPrecioVenta();
        }
        manejarImagenes(manufacturado, requestDTO.getImagenes());

        ArticuloManufacturado saved = repository.save(manufacturado);
        log.info("Producto {} creado con ID {}", saved.getDenominacion(), saved.getIdArticulo());
        return enriquecerDTO(saved);
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
        if (entity.getImagenes() != null) {
            dto.setImagenes(entity.getImagenes().stream()
                    .map(img -> new ImagenDTO(img.getIdImagen(), img.getDenominacion(), img.getUrl()))
                    .collect(Collectors.toList()));
        }
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
        manufacturado.getDetalles().clear();
        if (detallesDTO != null) {
            for (DetalleManufacturadoRequestDTO detalleDTO : detallesDTO) {
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

    private void manejarImagenes(ArticuloManufacturado manufacturado, List<ImagenDTO> imagenesDTO) {
        if (manufacturado.getImagenes() == null)
            manufacturado.setImagenes(new ArrayList<>());
        manufacturado.getImagenes().clear();
        if (imagenesDTO != null) {
            imagenesDTO.forEach(imgDTO -> {
                Imagen img = new Imagen();
                img.setUrl(imgDTO.getUrl());
                img.setDenominacion(imgDTO.getDenominacion());
                img.setArticulo(manufacturado);
                manufacturado.getImagenes().add(img);
            });
        }
    }
}