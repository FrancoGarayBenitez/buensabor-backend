package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.Categoria;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.UnidadMedida;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.ICategoriaRepository;
import com.elbuensabor.repository.IUnidadMedidaRepository;
import com.elbuensabor.services.IArticuloInsumoService;
import com.elbuensabor.services.mapper.ArticuloInsumoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloInsumoServiceImpl extends GenericServiceImpl<ArticuloInsumo, Long, ArticuloInsumoResponseDTO, IArticuloInsumoRepository, ArticuloInsumoMapper>
        implements IArticuloInsumoService {

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    public ArticuloInsumoServiceImpl(IArticuloInsumoRepository repository, ArticuloInsumoMapper mapper) {
        super(repository, mapper, ArticuloInsumo.class, ArticuloInsumoResponseDTO.class);
    }

    // ==================== SOBRESCRIBIR MÉTODOS GENÉRICOS ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO findById(Long id) {
        ArticuloInsumo insumo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + id + " no encontrado"));
        return mapearInsumoCompleto(insumo);
    }

    // ==================== MÉTODOS ESPECÍFICOS ====================

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createInsumo(ArticuloInsumoRequestDTO insumoRequestDTO) {
        // Validar que no exista un insumo con el mismo nombre
        if (repository.existsByDenominacion(insumoRequestDTO.getDenominacion())) {
            throw new DuplicateResourceException("Ya existe un artículo con la denominación: " + insumoRequestDTO.getDenominacion());
        }

        // Validar stock máximo >= stock actual
        if (insumoRequestDTO.getStockMaximo() < insumoRequestDTO.getStockActual()) {
            throw new IllegalArgumentException("El stock máximo no puede ser menor al stock actual");
        }

        // Mapear DTO a Entity
        ArticuloInsumo insumo = mapper.toEntity(insumoRequestDTO);

        // Asignar relaciones
        asignarRelaciones(insumo, insumoRequestDTO);

        // ==================== MANEJO DE IMAGEN - NUEVO ====================
        // Inicializar la lista de imágenes
        insumo.setImagenes(new ArrayList<>());

        // Solo agregar imagen si NO es para elaborar (es para venta)
        if (!insumoRequestDTO.getEsParaElaborar() && insumoRequestDTO.getImagen() != null) {
            Imagen imagen = crearImagen(insumoRequestDTO.getImagen());
            // CLAVE: Establecer la relación bidireccional
            imagen.setArticulo(insumo);
            insumo.getImagenes().add(imagen);
        }
        // ================================================================


        ArticuloInsumo savedInsumo = repository.save(insumo);
        return mapearInsumoCompleto(savedInsumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateInsumo(Long id, ArticuloInsumoRequestDTO insumoRequestDTO) {
        ArticuloInsumo existingInsumo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + id + " no encontrado"));

        // Validar nombre duplicado (excluyendo el actual)
        if (repository.existsByDenominacion(insumoRequestDTO.getDenominacion()) &&
                !existingInsumo.getDenominacion().equals(insumoRequestDTO.getDenominacion())) {
            throw new DuplicateResourceException("Ya existe otro artículo con la denominación: " + insumoRequestDTO.getDenominacion());
        }

        // Validar stock máximo >= stock actual
        if (insumoRequestDTO.getStockMaximo() < insumoRequestDTO.getStockActual()) {
            throw new IllegalArgumentException("El stock máximo no puede ser menor al stock actual");
        }

        // Actualizar campos básicos
        mapper.updateEntityFromDTO(insumoRequestDTO, existingInsumo);

        // Actualizar relaciones
        asignarRelaciones(existingInsumo, insumoRequestDTO);

        // ==================== MANEJO DE IMAGEN - NUEVO ====================
        // Limpiar imágenes existentes
        if (existingInsumo.getImagenes() != null) {
            existingInsumo.getImagenes().clear();
        } else {
            existingInsumo.setImagenes(new ArrayList<>());
        }

        // Solo agregar imagen si NO es para elaborar (es para venta)
        if (!insumoRequestDTO.getEsParaElaborar() && insumoRequestDTO.getImagen() != null) {

            Imagen imagen = crearImagen(insumoRequestDTO.getImagen());
            // CLAVE: Establecer la relación bidireccional
            imagen.setArticulo(existingInsumo);
            existingInsumo.getImagenes().add(imagen);
        }
        // ================================================================

        ArticuloInsumo updatedInsumo = repository.save(existingInsumo);
        return mapearInsumoCompleto(updatedInsumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByCategoria(Long idCategoria) {
        List<ArticuloInsumo> insumos = repository.findByCategoriaIdCategoria(idCategoria);
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByUnidadMedida(Long idUnidadMedida) {
        List<ArticuloInsumo> insumos = repository.findByUnidadMedidaIdUnidadMedida(idUnidadMedida);
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findIngredientes() {
        List<ArticuloInsumo> ingredientes = repository.findByEsParaElaborarTrue();
        return ingredientes.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findProductosNoManufacturados() {
        List<ArticuloInsumo> productos = repository.findByEsParaElaborarFalse();
        return productos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> searchByDenominacion(String denominacion) {
        List<ArticuloInsumo> insumos = repository.findByDenominacionContainingIgnoreCase(denominacion);
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findStockCritico() {
        List<ArticuloInsumo> insumos = repository.findStockCritico();
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findStockBajo() {
        List<ArticuloInsumo> insumos = repository.findStockBajo();
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findInsuficientStock(Integer cantidadRequerida) {
        List<ArticuloInsumo> insumos = repository.findInsuficientStock(cantidadRequerida);
        return insumos.stream()
                .map(this::mapearInsumoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO actualizarStock(Long id, Integer nuevoStock) {
        ArticuloInsumo insumo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + id + " no encontrado"));

        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        if (nuevoStock > insumo.getStockMaximo()) {
            throw new IllegalArgumentException("el stock no puede superar el stock máximo (" + insumo.getStockMaximo() + ")");
        }

        insumo.setStockActual(nuevoStock);
        ArticuloInsumo updatedInsumo = repository.save(insumo);
        return mapearInsumoCompleto(updatedInsumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO incrementarStock(Long id, Integer cantidad) {
        ArticuloInsumo insumo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + id + " no encontrado"));

        Integer nuevoStock = insumo.getStockActual() + cantidad;
        if (nuevoStock > insumo.getStockMaximo()) {
            throw new IllegalArgumentException("El incremento supera el stock máximo permitido");
        }

        insumo.setStockActual(nuevoStock);
        ArticuloInsumo updatedInsumo = repository.save(insumo);
        return mapearInsumoCompleto(updatedInsumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO decrementarStock(Long id, Integer cantidad) {
        ArticuloInsumo insumo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + id + " no encontrado"));

        Integer nuevoStock = insumo.getStockActual() - cantidad;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }

        insumo.setStockActual(nuevoStock);
        ArticuloInsumo updatedInsumo = repository.save(insumo);
        return mapearInsumoCompleto(updatedInsumo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDenominacion(String denominacion) {
        return repository.existsByDenominacion(denominacion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStockAvailable(Long idInsumo, Integer cantidad) {
        return repository.hasStockAvailable(idInsumo, cantidad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsedInProducts(Long idInsumo) {
        Integer count = repository.countProductosQueUsan(idInsumo);
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPorcentajeStock(Long idInsumo) {
        ArticuloInsumo insumo = repository.findById(idInsumo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo insumo con ID " + idInsumo + " no encontrado"));

        if (insumo.getStockMaximo() == 0) return 0.0;
        return (insumo.getStockActual() * 100.0) / insumo.getStockMaximo();
    }

    @Override
    @Transactional(readOnly = true)
    public String determinarEstadoStock(Long idInsumo) {
        Double porcentaje = calcularPorcentajeStock(idInsumo);

        if (porcentaje <= 25) return "CRITICO";
        if (porcentaje <= 50) return "BAJO";
        if (porcentaje <= 75) return "NORMAL";
        return "ALTO";
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private ArticuloInsumoResponseDTO mapearInsumoCompleto(ArticuloInsumo insumo) {
        ArticuloInsumoResponseDTO dto = mapper.toDTO(insumo);

        // Calcular información adicional
        dto.setPorcentajeStock(calcularPorcentajeStock(insumo.getIdArticulo()));
        dto.setEstadoStock(determinarEstadoStock(insumo.getIdArticulo()));
        dto.setCantidadProductosQueLoUsan(repository.countProductosQueUsan(insumo.getIdArticulo()));

        // Mapear imágenes
        if (!insumo.getImagenes().isEmpty()) {
            List<ImagenDTO> imagenesDTO = insumo.getImagenes().stream()
                    .map(imagen -> new ImagenDTO(imagen.getIdImagen(), imagen.getDenominacion(), imagen.getUrl()))
                    .collect(Collectors.toList());
            dto.setImagenes(imagenesDTO);
        }

        return dto;
    }

    private void asignarRelaciones(ArticuloInsumo insumo, ArticuloInsumoRequestDTO dto) {
        // Asignar unidad de medida (DEBE existir previamente)
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getIdUnidadMedida())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida con ID " + dto.getIdUnidadMedida() + " no encontrada"));
        insumo.setUnidadMedida(unidadMedida);
        unidadMedida.getArticulos().add(insumo);

        // Asignar categoría (DEBE existir previamente)
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + dto.getIdCategoria() + " no encontrada"));
        insumo.setCategoria(categoria);
    }

    private Imagen crearImagen(ImagenDTO imagenDTO) {
        Imagen imagen = new Imagen();
        imagen.setDenominacion(imagenDTO.getDenominacion());
        imagen.setUrl(imagenDTO.getUrl());
        // Con cascada, no necesitamos guardar la imagen por separado
        return imagen;
    }

}