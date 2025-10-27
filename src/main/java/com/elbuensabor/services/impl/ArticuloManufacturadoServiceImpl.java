package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.request.ManufacturadoDetalleDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.dto.response.CategoriaInfo;
import com.elbuensabor.entities.*;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.*;
import com.elbuensabor.services.IArticuloManufacturadoService;
import com.elbuensabor.services.mapper.ArticuloManufacturadoMapper;
import com.elbuensabor.services.mapper.ManufacturadoDetalleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloManufacturadoServiceImpl extends GenericServiceImpl<ArticuloManufacturado, Long, ArticuloManufacturadoResponseDTO, IArticuloManufacturadoRepository, ArticuloManufacturadoMapper>
        implements IArticuloManufacturadoService {

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;

    @Autowired
    private IManufacturadoDetalleRepository detalleRepository;

    @Autowired
    private ManufacturadoDetalleMapper detalleMapper;

    @Autowired
    public ArticuloManufacturadoServiceImpl(IArticuloManufacturadoRepository repository, ArticuloManufacturadoMapper mapper) {
        super(repository, mapper, ArticuloManufacturado.class, ArticuloManufacturadoResponseDTO.class);
    }

    // ==================== SOBRESCRIBIR MÉTODOS GENÉRICOS ====================

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
                .orElseThrow(() -> new ResourceNotFoundException("Artículo manufacturado con ID " + id + " no encontrado"));
        return mapearManufacturadoCompleto(manufacturado);
    }

    // ==================== MÉTODOS ESPECÍFICOS DE CREACIÓN Y ACTUALIZACIÓN ====================

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createManufacturado(ArticuloManufacturadoRequestDTO manufacturadoRequestDTO) {
        // Validaciones iniciales
        validarDatosManufacturado(manufacturadoRequestDTO);

        // Verificar stock de ingredientes
        verificarStockIngredientes(manufacturadoRequestDTO.getDetalles());

        // Mapear DTO a Entity
        ArticuloManufacturado manufacturado = mapper.toEntity(manufacturadoRequestDTO);

        // Asignar relaciones básicas
        asignarRelacionesBasicas(manufacturado, manufacturadoRequestDTO);

        // Crear y asignar detalles
        crearDetalles(manufacturado, manufacturadoRequestDTO.getDetalles());
        // Guardar el margen en la entidad (por si después recalculás el precio)
        manufacturado.setMargenGanancia(manufacturadoRequestDTO.getMargenGanancia());


        // Calcular precio si no se proporcionó
        if (manufacturadoRequestDTO.getPrecioVenta() == null) {
            Double costoTotal = calcularCostoTotal(manufacturado);
            Double margen = manufacturadoRequestDTO.getMargenGanancia() != null ?
                    manufacturadoRequestDTO.getMargenGanancia() : 2.0; // Margen por defecto
            manufacturado.setPrecioVenta(costoTotal * margen);
        } else {
            manufacturado.setPrecioVenta(manufacturadoRequestDTO.getPrecioVenta());
        }

        // Manejar imagen si existe
        manufacturado.setImagenes(new ArrayList<>());
        if (manufacturadoRequestDTO.getImagen() != null) {
            Imagen imagen = crearImagen(manufacturadoRequestDTO.getImagen());
            imagen.setArticulo(manufacturado);
            manufacturado.getImagenes().add(imagen);

        }

        ArticuloManufacturado savedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(savedManufacturado);
    }
    @Override
    public void bajaLogica(Long id) {
        ArticuloManufacturado producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto con id " + id));
        producto.setEliminado(true);
        repository.save(producto);
    }

    @Override
    public void altaLogica(Long id) {
        ArticuloManufacturado producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto con id " + id));
        producto.setEliminado(false);
        repository.save(producto);
    }
    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateManufacturado(Long id, ArticuloManufacturadoRequestDTO manufacturadoRequestDTO) {
        ArticuloManufacturado existingManufacturado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo manufacturado con ID " + id + " no encontrado"));

        // Validaciones
        validarDatosManufacturado(manufacturadoRequestDTO, id);
        verificarStockIngredientes(manufacturadoRequestDTO.getDetalles());

        // Actualizar campos básicos
        mapper.updateEntityFromDTO(manufacturadoRequestDTO, existingManufacturado);

        // Actualizar relaciones básicas
        asignarRelacionesBasicas(existingManufacturado, manufacturadoRequestDTO);

        // ==================== MANEJO DE IMÁGENES - NUEVO ====================
        // Limpiar imágenes existentes
        if (existingManufacturado.getImagenes() != null) {
            existingManufacturado.getImagenes().clear();
        } else {
            existingManufacturado.setImagenes(new ArrayList<>());
        }

        // Agregar nueva imagen si existe
        if (manufacturadoRequestDTO.getImagen() != null) {
            Imagen imagen = crearImagen(manufacturadoRequestDTO.getImagen());
            // CLAVE: Establecer la relación bidireccional
            imagen.setArticulo(existingManufacturado);
            existingManufacturado.getImagenes().add(imagen);
        }
        // ================================================================

        // Actualizar detalles
        detalleRepository.deleteByArticuloManufacturadoId(id);
        detalleRepository.flush(); // Forzar eliminación inmediata

        existingManufacturado.getDetalles().clear();
        crearDetalles(existingManufacturado, manufacturadoRequestDTO.getDetalles());

        // Guardar el margen en la entidad (por si después recalculás el precio)
        existingManufacturado.setMargenGanancia(manufacturadoRequestDTO.getMargenGanancia());

        // Recalcular precio si es necesario
        if (manufacturadoRequestDTO.getPrecioVenta() == null) {
            Double costoTotal = calcularCostoTotal(existingManufacturado);
            Double margen = manufacturadoRequestDTO.getMargenGanancia() != null ?
                    manufacturadoRequestDTO.getMargenGanancia() : 2.0;
            existingManufacturado.setPrecioVenta(costoTotal * margen);
        } else {
            existingManufacturado.setPrecioVenta(manufacturadoRequestDTO.getPrecioVenta());
        }

        ArticuloManufacturado updatedManufacturado = repository.save(existingManufacturado);
        return mapearManufacturadoCompleto(updatedManufacturado);
    }

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria) {
        List<ArticuloManufacturado> manufacturados = repository.findByCategoriaIdCategoria(idCategoria);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByTiempoMaximo(Integer tiempoMaximo) {
        List<ArticuloManufacturado> manufacturados = repository.findByTiempoEstimadoEnMinutosLessThanEqual(tiempoMaximo);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByIngrediente(Long idInsumo) {
        List<ArticuloManufacturado> manufacturados = repository.findByIngrediente(idInsumo);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByPrecioRango(Double precioMin, Double precioMax) {
        List<ArticuloManufacturado> manufacturados = repository.findByPrecioVentaBetween(precioMin, precioMax);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findByMinimoIngredientes(Integer cantidadMinima) {
        List<ArticuloManufacturado> manufacturados = repository.findByMinimoIngredientes(cantidadMinima);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> searchByDenominacion(String denominacion) {
        List<ArticuloManufacturado> manufacturados = repository.findByDenominacionContainingIgnoreCase(denominacion);
        return manufacturados.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    // ==================== CONTROL DE PREPARABILIDAD Y STOCK ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findPreparables() {
        List<ArticuloManufacturado> preparables = repository.findPreparables();
        return preparables.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findNoPreparables() {
        List<ArticuloManufacturado> todos = repository.findAll();
        List<ArticuloManufacturado> preparables = repository.findPreparables();

        List<ArticuloManufacturado> noPreparables = todos.stream()
                .filter(m -> !preparables.contains(m))
                .collect(Collectors.toList());

        return noPreparables.stream()
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularMaximoPreparable(Long idProducto) {
        Integer maximo = repository.calcularMaximoPreparable(idProducto);
        return maximo != null ? maximo : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean puedePrepararse(Long idProducto, Integer cantidad) {
        Integer maximoPreparable = calcularMaximoPreparable(idProducto);
        return maximoPreparable >= cantidad;
    }

    // ==================== CÁLCULOS DE COSTOS Y PRECIOS ====================

    @Override
    @Transactional(readOnly = true)
    public Double calcularCostoTotal(Long idProducto) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return calcularCostoTotal(manufacturado);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularMargenGanancia(Long idProducto) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Double costoTotal = calcularCostoTotal(manufacturado);
        if (costoTotal == 0) return 0.0;

        return manufacturado.getPrecioVenta() / costoTotal;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPrecioSugerido(Long idProducto, Double margen) {
        Double costoTotal = calcularCostoTotal(idProducto);
        return costoTotal * margen;
    }

    // ==================== GESTIÓN DE RECETAS (DETALLES) ====================

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO agregarIngrediente(Long idProducto, Long idInsumo, Double cantidad) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        ArticuloInsumo insumo = articuloInsumoRepository.findById(idInsumo)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado"));

        // Verificar que el ingrediente no esté ya en la receta
        boolean yaExiste = manufacturado.getDetalles().stream()
                .anyMatch(d -> d.getArticuloInsumo().getIdArticulo().equals(idInsumo));

        if (yaExiste) {
            throw new IllegalArgumentException("El ingrediente ya está en la receta");
        }

        // Crear nuevo detalle
        ArticuloManufacturadoDetalle detalle = new ArticuloManufacturadoDetalle();
        detalle.setArticuloManufacturado(manufacturado);
        detalle.setArticuloInsumo(insumo);
        detalle.setCantidad(cantidad);

        manufacturado.getDetalles().add(detalle);

        ArticuloManufacturado savedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(savedManufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO actualizarIngrediente(Long idProducto, Long idDetalle, Double nuevaCantidad) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        ArticuloManufacturadoDetalle detalle = manufacturado.getDetalles().stream()
                .filter(d -> d.getIdDetalleManufacturado().equals(idDetalle))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado"));

        detalle.setCantidad(nuevaCantidad);

        ArticuloManufacturado savedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(savedManufacturado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO eliminarIngrediente(Long idProducto, Long idDetalle) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        boolean removed = manufacturado.getDetalles().removeIf(d -> d.getIdDetalleManufacturado().equals(idDetalle));

        if (!removed) {
            throw new ResourceNotFoundException("Detalle no encontrado");
        }

        if (manufacturado.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Un producto debe tener al menos un ingrediente");
        }

        ArticuloManufacturado savedManufacturado = repository.save(manufacturado);
        return mapearManufacturadoCompleto(savedManufacturado);
    }

    // ==================== SIMULACIONES PARA PRODUCCIÓN ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> simularProduccion(Integer cantidadAProducir) {
        List<ArticuloManufacturado> todos = repository.findAll();

        return todos.stream()
                .filter(m -> calcularMaximoPreparable(m.getIdArticulo()) >= cantidadAProducir)
                .map(this::mapearManufacturadoCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean verificarStockParaProduccion(Long idProducto, Integer cantidadAProducir) {
        return puedePrepararse(idProducto, cantidadAProducir);
    }

    // ==================== VALIDACIONES ====================

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDenominacion(String denominacion) {
        return repository.existsByDenominacion(denominacion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneIngredientes(Long idProducto) {
        ArticuloManufacturado manufacturado = repository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return !manufacturado.getDetalles().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean seUsaEnPedidos(Long idProducto) {
        // TODO: Implementar cuando tengas la entidad Pedido
        return false;
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    private ArticuloManufacturadoResponseDTO mapearManufacturadoCompleto(ArticuloManufacturado manufacturado) {
        ArticuloManufacturadoResponseDTO dto = mapper.toDTO(manufacturado);

        // Mapear información de categoría de forma más clara
        CategoriaInfo categoriaInfo = new CategoriaInfo();
        categoriaInfo.setIdCategoria(manufacturado.getCategoria().getIdCategoria());
        categoriaInfo.setDenominacion(manufacturado.getCategoria().getDenominacion());
        categoriaInfo.setEsSubcategoria(manufacturado.getCategoria().isEsSubcategoria());
        if (manufacturado.getCategoria().getCategoriaPadre() != null) {
            categoriaInfo.setCategoriaPadre(manufacturado.getCategoria().getCategoriaPadre().getDenominacion());
        }
        dto.setCategoria(categoriaInfo);

        // Mapear detalles con información completa
        List<ManufacturadoDetalleDTO> detallesDTO = manufacturado.getDetalles().stream()
                .map(detalle -> {
                    ManufacturadoDetalleDTO detalleDTO = detalleMapper.toDTO(detalle);
                    detalleDTO.setSubtotal(detalle.getCantidad() * detalle.getArticuloInsumo().getPrecioCompra());
                    return detalleDTO;
                })
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

        // Calcular información adicional
        dto.setCostoTotal(calcularCostoTotal(manufacturado));
        dto.setMargenGanancia(dto.getCostoTotal() > 0 ? manufacturado.getPrecioVenta() / dto.getCostoTotal() : 0.0);
        dto.setCantidadIngredientes(manufacturado.getDetalles().size());
        dto.setCantidadMaximaPreparable(calcularMaximoPreparable(manufacturado.getIdArticulo()));
        dto.setStockSuficiente(dto.getCantidadMaximaPreparable() > 0);

        // Mapear imágenes
        if (!manufacturado.getImagenes().isEmpty()) {
            List<ImagenDTO> imagenesDTO = manufacturado.getImagenes().stream()
                    .map(imagen -> new ImagenDTO(imagen.getIdImagen(), imagen.getDenominacion(), imagen.getUrl()))
                    .collect(Collectors.toList());
            dto.setImagenes(imagenesDTO);
        }

        return dto;
    }

    private void validarDatosManufacturado(ArticuloManufacturadoRequestDTO dto) {
        validarDatosManufacturado(dto, null);
    }

    private void validarDatosManufacturado(ArticuloManufacturadoRequestDTO dto, Long idExcluir) {
        // Validar denominación única
        if (repository.existsByDenominacion(dto.getDenominacion())) {
            if (idExcluir == null) {
                throw new DuplicateResourceException("Ya existe un producto con la denominación: " + dto.getDenominacion());
            } else {
                ArticuloManufacturado existente = repository.findByDenominacion(dto.getDenominacion()).orElse(null);
                if (existente != null && !existente.getIdArticulo().equals(idExcluir)) {
                    throw new DuplicateResourceException("Ya existe otro producto con la denominación: " + dto.getDenominacion());
                }
            }
        }

        // Validar que tenga ingredientes
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El producto debe tener al menos un ingrediente");
        }

        // Validar ingredientes únicos
        List<Long> idsInsumos = dto.getDetalles().stream()
                .map(ManufacturadoDetalleDTO::getIdArticuloInsumo)
                .collect(Collectors.toList());

        if (idsInsumos.size() != idsInsumos.stream().distinct().count()) {
            throw new IllegalArgumentException("No se pueden repetir ingredientes en la receta");
        }
    }

    private void verificarStockIngredientes(List<ManufacturadoDetalleDTO> detalles) {
        for (ManufacturadoDetalleDTO detalle : detalles) {
            ArticuloInsumo insumo = articuloInsumoRepository.findById(detalle.getIdArticuloInsumo())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrediente con ID " + detalle.getIdArticuloInsumo() + " no encontrado"));

            if (insumo.getStockActual() < detalle.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente del ingrediente: " + insumo.getDenominacion() +
                        ". Stock disponible: " + insumo.getStockActual() +
                        ", cantidad requerida: " + detalle.getCantidad());
            }
        }
    }

    private void asignarRelacionesBasicas(ArticuloManufacturado manufacturado, ArticuloManufacturadoRequestDTO dto) {
        // Asignar unidad de medida
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getIdUnidadMedida())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida con ID " + dto.getIdUnidadMedida() + " no encontrada"));
        manufacturado.setUnidadMedida(unidadMedida);

        // Asignar categoría
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + dto.getIdCategoria() + " no encontrada"));
        manufacturado.setCategoria(categoria);
    }

    private void crearDetalles(ArticuloManufacturado manufacturado, List<ManufacturadoDetalleDTO> detallesDTO) {
        List<ArticuloManufacturadoDetalle> detalles = new ArrayList<>();

        for (ManufacturadoDetalleDTO detalleDTO : detallesDTO) {
            ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDTO.getIdArticuloInsumo())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado"));

            ArticuloManufacturadoDetalle detalle = new ArticuloManufacturadoDetalle();
            detalle.setArticuloManufacturado(manufacturado);
            detalle.setArticuloInsumo(insumo);
            detalle.setCantidad(detalleDTO.getCantidad());

            detalles.add(detalle);
        }

        manufacturado.setDetalles(detalles);
    }

    private Double calcularCostoTotal(ArticuloManufacturado manufacturado) {
        return manufacturado.getDetalles().stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getArticuloInsumo().getPrecioCompra())
                .sum();
    }

    private Imagen crearImagen(ImagenDTO imagenDTO) {
        Imagen imagen = new Imagen();
        imagen.setDenominacion(imagenDTO.getDenominacion());
        imagen.setUrl(imagenDTO.getUrl());
        return imagen;
    }
}