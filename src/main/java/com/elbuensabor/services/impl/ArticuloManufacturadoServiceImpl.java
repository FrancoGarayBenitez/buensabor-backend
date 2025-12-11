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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloManufacturadoServiceImpl extends
        GenericServiceImpl<ArticuloManufacturado, Long, ArticuloManufacturadoResponseDTO, IArticuloManufacturadoRepository, ArticuloManufacturadoMapper>
        implements IArticuloManufacturadoService {

    @Autowired
    private ICategoriaRepository categoriaRepository;
    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;
    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;
    @Autowired
    private DetalleManufacturadoMapper detalleMapper;

    public ArticuloManufacturadoServiceImpl(IArticuloManufacturadoRepository repository,
            ArticuloManufacturadoMapper mapper) {
        super(repository, mapper, ArticuloManufacturado.class, ArticuloManufacturadoResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturadoResponseDTO findById(Long id) {
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Artículo manufacturado con ID " + id + " no encontrado"));
        return mapearManufacturadoCompleto(manufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createManufacturado(ArticuloManufacturadoRequestDTO dto) {
        if (repository.existsByDenominacion(dto.getDenominacion())) {
            throw new DuplicateResourceException("Ya existe un artículo con la denominación: " + dto.getDenominacion());
        }

        ArticuloManufacturado manufacturado = mapper.toEntity(dto);
        asignarRelaciones(manufacturado, dto);
        actualizarDetallesYCalcularCosto(manufacturado, dto.getDetalles());
        manejarImagenes(manufacturado, dto.getImagenes());

        ArticuloManufacturado savedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(savedManufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateManufacturado(Long id, ArticuloManufacturadoRequestDTO dto) {
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Artículo manufacturado con ID " + id + " no encontrado"));

        if (repository.existsByDenominacion(dto.getDenominacion())
                && !manufacturado.getDenominacion().equals(dto.getDenominacion())) {
            throw new DuplicateResourceException(
                    "Ya existe otro artículo con la denominación: " + dto.getDenominacion());
        }

        mapper.updateEntityFromDTO(dto, manufacturado);
        asignarRelaciones(manufacturado, dto);
        actualizarDetallesYCalcularCosto(manufacturado, dto.getDetalles());
        manejarImagenes(manufacturado, dto.getImagenes());

        ArticuloManufacturado updatedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(updatedManufacturado);
    }

    @Override
    @Transactional
    public void bajaLogica(Long id) {
        ArticuloManufacturado manufacturado = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Artículo manufacturado con ID " + id + " no encontrado"));
        manufacturado.setEliminado(true);
        repository.save(manufacturado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria) {
        return repository.findByCategoriaIdCategoria(idCategoria).stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> searchByDenominacion(String denominacion) {
        return repository.findByDenominacionContainingIgnoreCase(denominacion).stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    @Override
    @Transactional(readOnly = true)
    public Double calcularCostoProduccion(ArticuloManufacturado manufacturado) {
        return manufacturado.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getArticuloInsumo().getPrecioCompra())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean verificarStockSuficiente(ArticuloManufacturado manufacturado) {
        return manufacturado.getDetalles().stream()
                .allMatch(detalle -> detalle.getArticuloInsumo().getStockActual() >= detalle.getCantidad());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularCantidadMaximaProduccion(ArticuloManufacturado manufacturado) {
        return manufacturado.getDetalles().stream()
                .mapToInt(detalle -> (int) (detalle.getArticuloInsumo().getStockActual() / detalle.getCantidad()))
                .min()
                .orElse(0);
    }

    // --- MÉTODOS AUXILIARES ---

    private ArticuloManufacturadoResponseDTO mapearManufacturadoCompleto(ArticuloManufacturado manufacturado) {
        ArticuloManufacturadoResponseDTO dto = mapper.toDTO(manufacturado);
        dto.setStockSuficiente(verificarStockSuficiente(manufacturado));
        dto.setCantidadMaximaPreparable(calcularCantidadMaximaProduccion(manufacturado));

        if (!manufacturado.getImagenes().isEmpty()) {
            dto.setImagenes(manufacturado.getImagenes().stream()
                    .map(img -> new ImagenDTO(img.getIdImagen(), img.getDenominacion(), img.getUrl()))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private void asignarRelaciones(ArticuloManufacturado manufacturado, ArticuloManufacturadoRequestDTO dto) {
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getIdUnidadMedida())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidad de medida con ID " + dto.getIdUnidadMedida() + " no encontrada"));
        manufacturado.setUnidadMedida(unidadMedida);

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría con ID " + dto.getIdCategoria() + " no encontrada"));
        manufacturado.setCategoria(categoria);
    }

    private void actualizarDetallesYCalcularCosto(ArticuloManufacturado manufacturado,
            List<DetalleManufacturadoRequestDTO> detallesDTO) {
        manufacturado.getDetalles().clear(); // Limpiar detalles existentes para evitar duplicados
        for (DetalleManufacturadoRequestDTO detalleDTO : detallesDTO) {
            ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDTO.getIdArticuloInsumo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Insumo con ID " + detalleDTO.getIdArticuloInsumo() + " no encontrado"));

            DetalleManufacturado detalle = detalleMapper.toEntity(detalleDTO);
            detalle.setArticuloInsumo(insumo);
            detalle.setArticuloManufacturado(manufacturado);
            manufacturado.getDetalles().add(detalle);
        }
        manufacturado.setCostoProduccion(calcularCostoProduccion(manufacturado));
    }

    private void manejarImagenes(ArticuloManufacturado manufacturado, List<ImagenDTO> imagenesDTO) {
        if (manufacturado.getImagenes() != null) {
            manufacturado.getImagenes().clear();
        } else {
            manufacturado.setImagenes(new ArrayList<>());
        }

        if (imagenesDTO != null && !imagenesDTO.isEmpty()) {
            for (ImagenDTO imagenDTO : imagenesDTO) {
                Imagen imagen = new Imagen();
                imagen.setDenominacion(imagenDTO.getDenominacion());
                imagen.setUrl(imagenDTO.getUrl());
                imagen.setArticulo(manufacturado);
                manufacturado.getImagenes().add(imagen);
            }
        }
    }
}