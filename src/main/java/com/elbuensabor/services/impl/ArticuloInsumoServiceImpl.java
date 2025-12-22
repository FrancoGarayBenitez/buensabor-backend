package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.Categoria;
import com.elbuensabor.entities.TipoCategoria;
import com.elbuensabor.entities.UnidadMedida;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.ICategoriaRepository;
import com.elbuensabor.repository.IUnidadMedidaRepository;
import com.elbuensabor.repository.ICompraInsumoRepository;
import com.elbuensabor.repository.IHistoricoPrecioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import com.elbuensabor.services.IArticuloInsumoService;
import com.elbuensabor.services.mapper.ArticuloInsumoMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticuloInsumoServiceImpl extends
        GenericServiceImpl<ArticuloInsumo, Long, ArticuloInsumoResponseDTO, IArticuloInsumoRepository, ArticuloInsumoMapper>
        implements IArticuloInsumoService {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloInsumoServiceImpl.class);

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private IHistoricoPrecioRepository historicoPrecioRepository;
    @Autowired
    private ICompraInsumoRepository compraInsumoRepository;

    @Autowired
    public ArticuloInsumoServiceImpl(
            IArticuloInsumoRepository repository,
            ArticuloInsumoMapper mapper) {
        super(repository, mapper, ArticuloInsumo.class, ArticuloInsumoResponseDTO.class);
    }

    // ==================== CRUD ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findAll() {
        logger.debug("📋 Obteniendo todos los insumos");

        return repository.findAll().stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO findById(Long id) {
        logger.debug("🔍 Buscando insumo ID: {}", id);

        ArticuloInsumo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insumo con ID " + id + " no encontrado"));

        return enriquecerResponseDTO(entity);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO create(ArticuloInsumoRequestDTO requestDTO) {
        logger.info("📝 Creando nuevo insumo: {}", requestDTO.getDenominacion());

        // 1️⃣ Validar denominación duplicada
        if (repository.existsByDenominacion(requestDTO.getDenominacion())) {
            throw new DuplicateResourceException(
                    "Ya existe un insumo con la denominación: " + requestDTO.getDenominacion());
        }

        // 2️⃣ Mapear DTO a entidad (sin relaciones)
        ArticuloInsumo entity = mapper.toEntity(requestDTO);
        entity.setEsParaElaborar(
                requestDTO.getEsParaElaborar() != null ? requestDTO.getEsParaElaborar() : Boolean.TRUE);

        // 3️⃣ Asignar y validar relaciones
        asignarRelaciones(entity, requestDTO);

        // 4️⃣ Validar que categoría sea apta para insumos (INGREDIENTES o BEBIDAS)
        validarCategoriaAptaParaInsumos(entity.getCategoria());

        // 5️⃣ Inicializar colecciones
        entity.setImagenes(new ArrayList<>());
        entity.setDetallesManufacturados(new ArrayList<>());
        entity.setHistoricosPrecios(new ArrayList<>());
        entity.setCompras(new ArrayList<>());

        // 6️⃣ Guardar
        ArticuloInsumo saved = repository.save(entity);
        logger.info("✅ Insumo creado exitosamente: {} (ID: {}) - Sin estado hasta primera compra",
                saved.getDenominacion(), saved.getIdArticulo());

        return enriquecerResponseDTO(saved);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO update(Long id, ArticuloInsumoRequestDTO requestDTO) {
        logger.info("📝 Actualizando insumo ID: {}", id);

        // 1️⃣ Obtener entidad existente
        ArticuloInsumo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insumo con ID " + id + " no encontrado"));

        // 2️⃣ Validar denominación duplicada (excluyendo el actual)
        if (!entity.getDenominacion().equals(requestDTO.getDenominacion()) &&
                repository.existsByDenominacion(requestDTO.getDenominacion())) {
            throw new DuplicateResourceException(
                    "Ya existe otro insumo con la denominación: " + requestDTO.getDenominacion());
        }

        // 3️⃣ Actualizar desde DTO
        mapper.updateEntityFromDTO(requestDTO, entity);
        if (requestDTO.getEsParaElaborar() != null) {
            entity.setEsParaElaborar(requestDTO.getEsParaElaborar());
        }

        // 4️⃣ Actualizar relaciones si cambiaron
        asignarRelaciones(entity, requestDTO);

        // 5️⃣ Validar que categoría sea apta para insumos (INGREDIENTES o BEBIDAS)
        validarCategoriaAptaParaInsumos(entity.getCategoria());

        // ✅ NO RECALCULAR ESTADO - Solo se modifica con compras
        // entity.setEstadoStock() ← NO TOCAR

        // 6️⃣ Guardar
        ArticuloInsumo updated = repository.save(entity);
        logger.info("✅ Insumo actualizado: {}", updated.getDenominacion());

        return enriquecerResponseDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("🗑️ Eliminando insumo ID: {}", id);

        ArticuloInsumo entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insumo con ID " + id + " no encontrado"));

        if (estaEnUso(id)) {
            Integer cantidadProductos = countProductosQueLoUsan(id);
            throw new IllegalArgumentException(
                    "No se puede eliminar este insumo. Está en uso en " + cantidadProductos + " productos");
        }

        try {
            // ✅ eliminar dependencias (por si el JPA cascade no ejecuta antes)
            historicoPrecioRepository.deleteByArticuloInsumoId(id);
            compraInsumoRepository.deleteByArticuloInsumoId(id);

            repository.delete(entity);
            logger.info("✅ Insumo eliminado permanentemente: {}", entity.getDenominacion());
        } catch (DataIntegrityViolationException ex) {
            logger.error("❌ Violación de integridad al eliminar insumo {}: {}", id, ex.getMessage());
            throw new DataIntegrityViolationException("No se pudo eliminar el insumo: existen referencias aún vigentes",
                    ex);
        }
    }

    // ==================== BÚSQUEDAS POR FILTRO ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByCategoria(Long idCategoria) {
        logger.debug("🔍 Buscando insumos por categoría: {}", idCategoria);

        return repository.findByCategoriaIdCategoria(idCategoria).stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByUnidadMedida(Long idUnidadMedida) {
        logger.debug("🔍 Buscando insumos por unidad de medida: {}", idUnidadMedida);

        return repository.findByUnidadMedidaIdUnidadMedida(idUnidadMedida).stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByDenominacion(String denominacion) {
        logger.debug("🔍 Buscando insumos por denominación: {}", denominacion);

        return repository.findByDenominacionContainingIgnoreCase(denominacion).stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR TIPO ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findParaElaborar() {
        logger.debug("🔍 Buscando insumos para elaborar");

        return repository.findByEsParaElaborarTrue().stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findNoParaElaborar() {
        logger.debug("🔍 Buscando insumos no para elaborar");

        return repository.findByEsParaElaborarFalse().stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR ESTADO DE STOCK ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByCriticoStock() {
        logger.debug("🔍 Buscando insumos con stock crítico");

        return repository.findAll().stream()
                .filter(insumo -> "CRITICO".equals(insumo.getEstadoStock()))
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByBajoStock() {
        logger.debug("🔍 Buscando insumos con stock bajo");

        return repository.findAll().stream()
                .filter(insumo -> "BAJO".equals(insumo.getEstadoStock()))
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByAltoStock() {
        logger.debug("🔍 Buscando insumos con stock alto");

        return repository.findAll().stream()
                .filter(insumo -> "ALTO".equals(insumo.getEstadoStock()))
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== BÚSQUEDAS POR PRECIO ====================

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findByPrecioCompraBetween(Double precioMin, Double precioMax) {
        logger.debug("🔍 Buscando insumos con precio de compra entre ${} y ${}", precioMin, precioMax);

        if (precioMin == null || precioMax == null) {
            throw new IllegalArgumentException("Los precios mínimo y máximo son obligatorios");
        }

        if (precioMin < 0 || precioMax < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }

        if (precioMin > precioMax) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }

        return repository.findByPrecioCompraBetween(precioMin, precioMax).stream()
                .map(this::enriquecerResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== VALIDACIONES ====================

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDenominacion(String denominacion) {
        return repository.existsByDenominacion(denominacion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneStockDisponible(Long idInsumo, Double cantidad) {
        ArticuloInsumo insumo = repository.findById(idInsumo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insumo con ID " + idInsumo + " no encontrado"));

        return insumo.tieneStockDisponible(cantidad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaEnUso(Long idInsumo) {
        return countProductosQueLoUsan(idInsumo) > 0;
    }

    // ==================== INFORMACIÓN ====================

    @Override
    @Transactional(readOnly = true)
    public Integer countProductosQueLoUsan(Long idInsumo) {
        Integer count = repository.countProductosQueUsan(idInsumo);
        return count != null ? count : 0;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * ✅ Enriquecer DTO sin calcular estado
     * El estado solo se calcula con compras (CompraInsumoServiceImpl)
     */
    private ArticuloInsumoResponseDTO enriquecerResponseDTO(ArticuloInsumo entity) {
        // 1️⃣ Mapeo básico
        ArticuloInsumoResponseDTO dto = mapper.toDTO(entity);

        // 2️⃣ Calcular porcentaje de stock
        dto.setPorcentajeStock(entity.getPorcentajeStock());

        // ✅ NO RECALCULAR ESTADO - Solo viene del entity (asignado por
        // CompraInsumoServiceImpl)
        dto.setEstadoStock(entity.getEstadoStock());

        // 3️⃣ Calcular costo total del inventario
        dto.setCostoTotalInventario(entity.getCostoTotalInventario());

        // 4️⃣ Calcular margen de ganancia
        dto.setMargenGanancia(entity.getMargenGanancia());

        // 5️⃣ Contar productos que lo usan
        dto.setCantidadProductosQueLoUsan(
                entity.getDetallesManufacturados() != null
                        ? entity.getDetallesManufacturados().size()
                        : 0);

        // 6️⃣ Mapear imágenes
        if (entity.getImagenes() != null && !entity.getImagenes().isEmpty()) {
            dto.setImagenes(entity.getImagenes().stream()
                    .map(imagen -> new ImagenDTO(
                            imagen.getIdImagen(),
                            imagen.getDenominacion(),
                            imagen.getUrl()))
                    .collect(Collectors.toList()));
        } else {
            dto.setImagenes(new ArrayList<>());
        }

        return dto;
    }

    /**
     * ✅ Asignar relaciones FK desde DTO
     */
    private void asignarRelaciones(ArticuloInsumo entity, ArticuloInsumoRequestDTO requestDTO) {
        // Asignar unidad de medida
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(requestDTO.getIdUnidadMedida())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidad de medida con ID " + requestDTO.getIdUnidadMedida() + " no encontrada"));
        entity.setUnidadMedida(unidadMedida);

        // Asignar categoría
        Categoria categoria = categoriaRepository.findById(requestDTO.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría con ID " + requestDTO.getIdCategoria() + " no encontrada"));
        entity.setCategoria(categoria);
    }

    // ✅ Nueva validación central
    private void validarCategoriaAptaParaInsumos(Categoria categoria) {
        if (categoria == null || categoria.getTipoCategoria() == null) {
            throw new IllegalArgumentException("La categoría seleccionada es inválida");
        }
        TipoCategoria tipo = categoria.getTipoCategoria();
        if (!(TipoCategoria.INGREDIENTES.equals(tipo) || TipoCategoria.BEBIDAS.equals(tipo))) {
            throw new IllegalArgumentException("La categoría debe ser de tipo INGREDIENTES o BEBIDAS");
        }
    }
}