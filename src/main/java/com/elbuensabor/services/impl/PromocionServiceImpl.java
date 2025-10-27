package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.PromocionAplicacionDTO;
import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.ArticuloBasicoDTO;
import com.elbuensabor.dto.response.PromocionCalculoDTO;
import com.elbuensabor.dto.response.PromocionCompletaDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.entities.SucursalEmpresa;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IPromocionRepository;
import com.elbuensabor.repository.ISucursalEmpresaRepository;
import com.elbuensabor.services.IPromocionService;
import com.elbuensabor.services.mapper.PromocionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionServiceImpl extends GenericServiceImpl<Promocion, Long, PromocionResponseDTO, IPromocionRepository, PromocionMapper>
        implements IPromocionService {

    private static final Logger logger = LoggerFactory.getLogger(PromocionServiceImpl.class);

    @Autowired
    private IArticuloRepository articuloRepository;

    @Autowired
    private ISucursalEmpresaRepository sucursalRepository;

    // ✅ CONSTRUCTOR REQUERIDO POR TU GENERIC SERVICE
    public PromocionServiceImpl(IPromocionRepository repository, PromocionMapper mapper) {
        super(repository, mapper, Promocion.class, PromocionResponseDTO.class);
        this.articuloRepository = articuloRepository;
        this.sucursalRepository = sucursalRepository;
    }

    // ==================== MÉTODOS PARA CLIENTES ====================

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findPromocionesVigentes() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        List<Promocion> promociones = repository.findPromocionesVigentes(ahora, horaActual);

        logger.info("✅ Encontradas {} promociones vigentes", promociones.size());

        return promociones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findPromocionesParaArticulo(Long idArticulo) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        List<Promocion> promociones = repository.findPromocionesVigentesPorArticulo(
                idArticulo, ahora, horaActual
        );

        logger.info("✅ Encontradas {} promociones para artículo ID: {}", promociones.size(), idArticulo);

        return promociones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findPromocionesAplicables(Long idArticulo, Long idSucursal) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        List<Promocion> promociones = repository.findPromocionesAplicables(
                idArticulo, idSucursal, ahora, horaActual
        );

        logger.info("✅ Encontradas {} promociones aplicables para artículo {} en sucursal {}",
                promociones.size(), idArticulo, idSucursal);

        return promociones.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS PARA ADMINISTRACIÓN ====================

    @Override
    @Transactional
    public PromocionResponseDTO crearPromocion(PromocionRequestDTO request) {
        logger.info("📝 Creando nueva promoción: {}", request.getDenominacion());

        Promocion promocion = mapper.toEntity(request);

        // Asignar artículos
        if (request.getIdsArticulos() != null && !request.getIdsArticulos().isEmpty()) {
            List<Articulo> articulos = articuloRepository.findAllById(request.getIdsArticulos());
            promocion.setArticulos(articulos);
            logger.info("✅ Asignados {} artículos a la promoción", articulos.size());
        }

        // Asignar sucursales (por ahora siempre sucursal 1)
        SucursalEmpresa sucursal = sucursalRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal principal no encontrada"));
        promocion.setSucursales(List.of(sucursal));

        Promocion promocionGuardada = repository.save(promocion);
        logger.info("✅ Promoción creada con ID: {}", promocionGuardada.getIdPromocion());

        return mapper.toDTO(promocionGuardada);
    }

    @Override
    @Transactional
    public PromocionResponseDTO actualizarPromocion(Long id, PromocionRequestDTO request) {
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        // Actualizar campos básicos
        mapper.updateEntityFromDTO(request, promocion);

        // Actualizar artículos si se especifican
        if (request.getIdsArticulos() != null) {
            List<Articulo> articulos = articuloRepository.findAllById(request.getIdsArticulos());
            promocion.setArticulos(articulos);
        }

        Promocion promocionActualizada = repository.save(promocion);
        logger.info("✅ Promoción actualizada: ID {}", id);

        return mapper.toDTO(promocionActualizada);
    }

    @Override
    @Transactional
    public void activarPromocion(Long id) {
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        promocion.setActivo(true);
        repository.save(promocion);
        logger.info("✅ Promoción activada: ID {}", id);
    }

    @Override
    @Transactional
    public void desactivarPromocion(Long id) {
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        promocion.setActivo(false);
        repository.save(promocion);
        logger.info("✅ Promoción desactivada: ID {}", id);
    }

    // ==================== MÉTODO CLAVE: CALCULAR DESCUENTOS ====================

    @Override
    @Transactional(readOnly = true)
    public PromocionCalculoDTO calcularDescuentosParaPedido(Long idSucursal, List<PromocionAplicacionDTO> aplicaciones) {
        logger.info("💰 Calculando descuentos para {} aplicaciones de promociones", aplicaciones.size());

        PromocionCalculoDTO calculo = new PromocionCalculoDTO();
        calculo.setDescuentoTotal(0.0);
        calculo.setDetallesDescuentos(new ArrayList<>());

        for (PromocionAplicacionDTO aplicacion : aplicaciones) {
            try {
                Promocion promocion = repository.findById(aplicacion.getIdPromocion())
                        .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada: " + aplicacion.getIdPromocion()));

                // Validar que la promoción esté vigente
                if (!promocion.estaVigente()) {
                    logger.warn("⚠️ Promoción {} no está vigente, se omite", promocion.getDenominacion());
                    continue;
                }

                // Validar que aplique para el artículo
                if (!promocion.aplicaParaArticulo(aplicacion.getIdArticulo())) {
                    logger.warn("⚠️ Promoción {} no aplica para artículo {}", promocion.getDenominacion(), aplicacion.getIdArticulo());
                    continue;
                }

                // Calcular descuento
                Double descuento = promocion.calcularDescuento(aplicacion.getPrecioUnitario(), aplicacion.getCantidad());

                if (descuento > 0) {
                    calculo.setDescuentoTotal(calculo.getDescuentoTotal() + descuento);

                    PromocionCalculoDTO.DetalleDescuentoDTO detalle = new PromocionCalculoDTO.DetalleDescuentoDTO();
                    detalle.setIdPromocion(promocion.getIdPromocion());
                    detalle.setDenominacionPromocion(promocion.getDenominacion());
                    detalle.setIdArticulo(aplicacion.getIdArticulo());
                    detalle.setMontoDescuento(descuento);
                    detalle.setTipoDescuento(promocion.getTipoDescuento());
                    detalle.setValorDescuento(promocion.getValorDescuento());

                    calculo.getDetallesDescuentos().add(detalle);

                    logger.info("✅ Descuento aplicado: {} - ${}", promocion.getDenominacion(), descuento);
                }

            } catch (Exception e) {
                logger.error("❌ Error procesando promoción {}: {}", aplicacion.getIdPromocion(), e.getMessage());
            }
        }

        logger.info("💰 Descuento total calculado: ${}", calculo.getDescuentoTotal());
        return calculo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionCompletaDTO> findPromocionesVigentesCompletas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = LocalTime.now();

        List<Promocion> promociones = repository.findPromocionesVigentes(ahora, horaActual);

        logger.info("✅ Encontradas {} promociones vigentes completas", promociones.size());

        return promociones.stream()
                .map(this::convertirAPromocionCompleta)
                .collect(Collectors.toList());
    }

    private PromocionCompletaDTO convertirAPromocionCompleta(Promocion promocion) {
        PromocionCompletaDTO dto = new PromocionCompletaDTO();
        dto.setIdPromocion(promocion.getIdPromocion());
        dto.setDenominacion(promocion.getDenominacion());
        dto.setDescripcionDescuento(promocion.getDescripcionDescuento());
        dto.setFechaDesde(promocion.getFechaDesde());
        dto.setFechaHasta(promocion.getFechaHasta());
        dto.setHoraDesde(promocion.getHoraDesde());
        dto.setHoraHasta(promocion.getHoraHasta());
        dto.setTipoDescuento(String.valueOf(promocion.getTipoDescuento()));
        dto.setValorDescuento(promocion.getValorDescuento());
        dto.setActivo(promocion.getActivo());

        // Convertir artículos a ArticuloBasicoDTO
        List<ArticuloBasicoDTO> articulosDTO = promocion.getArticulos().stream()
                .map(this::convertirAArticuloBasico)
                .collect(Collectors.toList());
        dto.setArticulos(articulosDTO);

        return dto;
    }

    private ArticuloBasicoDTO convertirAArticuloBasico(Articulo articulo) {
        ArticuloBasicoDTO dto = new ArticuloBasicoDTO();
        dto.setIdArticulo(articulo.getIdArticulo());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setPrecioVenta(articulo.getPrecioVenta());
        // Agregar imagen principal si existe
        if (articulo.getImagenes() != null && !articulo.getImagenes().isEmpty()) {
            dto.setImagenUrl(articulo.getImagenes().get(0).getUrl());
        }
        return dto;
    }
}