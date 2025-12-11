package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.CompraInsumo;
import com.elbuensabor.entities.EstadoStock;
import com.elbuensabor.entities.HistoricoPrecio;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.ICompraInsumoRepository;
import com.elbuensabor.repository.IHistoricoPrecioRepository;
import com.elbuensabor.services.ICompraInsumoService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraInsumoServiceImpl implements ICompraInsumoService {

    private static final Logger logger = LoggerFactory.getLogger(CompraInsumoServiceImpl.class);

    private final ICompraInsumoRepository compraInsumoRepository;
    private final IArticuloInsumoRepository articuloInsumoRepository;
    private final IHistoricoPrecioRepository historicoPrecioRepository;

    /**
     * ✅ Registrar compra Y calcular estado
     */
    @Override
    @Transactional
    public CompraInsumoResponseDTO registrarCompra(CompraInsumoRequestDTO dto) {
        logger.info("📦 Registrando compra para insumo {}: {} unidades a ${}",
                dto.getIdArticuloInsumo(), dto.getCantidad(), dto.getPrecioUnitario());

        // ✅ VALIDACIONES PREVIAS
        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        if (dto.getPrecioUnitario() == null || dto.getPrecioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }

        // 1. Validar que el insumo existe
        ArticuloInsumo insumo = articuloInsumoRepository.findById(dto.getIdArticuloInsumo())
                .orElseThrow(() -> {
                    logger.error("❌ Insumo no encontrado: {}", dto.getIdArticuloInsumo());
                    return new ResourceNotFoundException(
                            "Insumo no encontrado con ID: " + dto.getIdArticuloInsumo());
                });

        // 2. Guardar compra
        CompraInsumo compra = new CompraInsumo();
        compra.setArticuloInsumo(insumo);
        compra.setCantidad(dto.getCantidad());
        compra.setPrecioUnitario(dto.getPrecioUnitario());
        compra.setFechaCompra(dto.getFechaCompra() != null ? dto.getFechaCompra() : LocalDate.now());

        CompraInsumo compraGuardada = compraInsumoRepository.save(compra);
        logger.info("✅ Compra guardada con ID: {}", compraGuardada.getId());

        // 3. Actualizar precio promedio (ponderado)
        Double nuevoPromedio = calcularPrecioPromedio(insumo, dto.getPrecioUnitario(), dto.getCantidad());
        insumo.setPrecioCompra(nuevoPromedio);

        // 4. Aumentar stock
        Double nuevoStock = insumo.getStockActual() + dto.getCantidad();
        insumo.setStockActual(nuevoStock);

        // 5. Calcular estado del stock basado en porcentaje (enum)
        EstadoStock nuevoEstado = calcularEstadoStockEnum(insumo.getStockActual(), insumo.getStockMaximo());
        insumo.setEstadoStock(nuevoEstado.name());

        // 6. Guardar cambios en insumo
        articuloInsumoRepository.save(insumo);

        // 7. Registrar en historial de precios
        try {
            HistoricoPrecio historico = new HistoricoPrecio();
            historico.setCompra(compraGuardada);
            historico.setArticuloInsumo(insumo);
            historico.setPrecioUnitario(dto.getPrecioUnitario());
            historico.setCantidad(dto.getCantidad());
            historicoPrecioRepository.save(historico);
            logger.info("✅ Precio registrado en historial");
        } catch (Exception e) {
            logger.warn("⚠️ Error registrando en historial: {}", e.getMessage());
        }

        logger.info("✅ Compra procesada exitosamente");
        return toDto(compraGuardada);
    }

    /**
     * ✅ Eliminar compra y recalcular stock + estado
     * Retorna solo el ID del insumo
     */
    @Override
    @Transactional
    public Long eliminarCompra(Long idCompra) {
        logger.info("🗑️ Eliminando compra ID: {}", idCompra);

        // 1. Obtener compra
        CompraInsumo compra = compraInsumoRepository.findById(idCompra)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compra no encontrada con ID: " + idCompra));

        ArticuloInsumo insumo = compra.getArticuloInsumo();
        Long idInsumo = insumo.getIdArticulo();

        logger.info("📦 Compra: {} unidades a ${}", compra.getCantidad(), compra.getPrecioUnitario());

        // 2. Restar stock
        Double nuevoStock = Math.max(insumo.getStockActual() - compra.getCantidad(), 0.0);
        insumo.setStockActual(nuevoStock);

        // 3. Recalcular precio promedio
        Double nuevoPrecio = recalcularPrecioPromedio(insumo, compra.getPrecioUnitario(), compra.getCantidad());
        insumo.setPrecioCompra(nuevoPrecio);

        // 4. Recalcular estado (enum)
        EstadoStock nuevoEstado = calcularEstadoStockEnum(insumo.getStockActual(), insumo.getStockMaximo());
        insumo.setEstadoStock(nuevoEstado.name());

        // 5. Guardar cambios
        articuloInsumoRepository.save(insumo);

        // 6. Eliminar compra (su HistoricoPrecio se elimina por cascade = REMOVE)
        compraInsumoRepository.delete(compra);
        logger.info("✅ Compra eliminada - Retornando ID insumo: {}", idInsumo);

        return idInsumo;
    }

    /**
     * ✅ Calcular precio promedio ponderado (al comprar)
     */
    private Double calcularPrecioPromedio(ArticuloInsumo insumo, Double nuevoPrecio, Double cantidadComprada) {
        Double stockActual = insumo.getStockActual();
        Double precioActual = insumo.getPrecioCompra();

        if (stockActual == null || stockActual <= 0 || precioActual == null) {
            return Math.round(nuevoPrecio * 100.0) / 100.0;
        }

        Double promedio = (precioActual * stockActual + nuevoPrecio * cantidadComprada)
                / (stockActual + cantidadComprada);
        return Math.round(promedio * 100.0) / 100.0;
    }

    /**
     * ✅ Recalcular precio promedio al eliminar compra (si no quedan compras, deja
     * 0)
     */
    private Double recalcularPrecioPromedio(ArticuloInsumo insumo, Double precioBorrado, Double cantidadBorrada) {
        // Recalcula a partir del historial real para evitar arrastre de errores
        List<HistoricoPrecio> historial = historicoPrecioRepository
                .findByArticuloOrderByFechaDesc(insumo.getIdArticulo());
        if (historial == null || historial.isEmpty()) {
            return 0.0; // sin compras => sin costo promedio
        }

        double totalInvertido = historial.stream()
                .mapToDouble(h -> h.getPrecioUnitario() * (h.getCantidad() != null ? h.getCantidad() : 1.0))
                .sum();
        double totalCantidad = historial.stream()
                .mapToDouble(h -> (h.getCantidad() != null ? h.getCantidad() : 0.0))
                .sum();

        double promedio = totalCantidad > 0 ? totalInvertido / totalCantidad : 0.0;
        return Math.round(promedio * 100.0) / 100.0;
    }

    /**
     * ✅ Calcular estado usando los umbrales del enum
     */
    private EstadoStock calcularEstadoStockEnum(Double stockActual, Double stockMaximo) {
        double sa = stockActual != null ? stockActual : 0.0;
        double sm = stockMaximo != null ? stockMaximo : 0.0;

        if (sm <= 0)
            return EstadoStock.CRITICO;

        double porcentaje = (sa / sm) * 100.0;

        // Umbrales exactos:
        // 0–25 => CRITICO, 26–50 => BAJO, 51–75 => NORMAL, 76–100 => ALTO
        if (porcentaje <= 25.0)
            return EstadoStock.CRITICO;
        if (porcentaje <= 50.0)
            return EstadoStock.BAJO;
        if (porcentaje <= 75.0)
            return EstadoStock.NORMAL;
        return EstadoStock.ALTO;
    }

    @Override
    public List<CompraInsumo> getAllCompras() {
        return compraInsumoRepository.findAll();
    }

    @Override
    public CompraInsumo getCompraById(Long id) {
        return compraInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
    }

    @Override
    public List<CompraInsumo> getComprasByInsumoId(Long idInsumo) {
        return compraInsumoRepository.findByArticuloInsumo_IdArticulo(idInsumo);
    }

    @Override
    public CompraInsumoResponseDTO toDto(CompraInsumo compra) {
        CompraInsumoResponseDTO dto = new CompraInsumoResponseDTO();
        dto.setId(compra.getId());
        dto.setIdArticuloInsumo(compra.getArticuloInsumo().getIdArticulo());
        dto.setDenominacionInsumo(compra.getArticuloInsumo().getDenominacion());
        dto.setCantidad(compra.getCantidad());
        dto.setPrecioUnitario(compra.getPrecioUnitario());
        dto.setFechaCompra(compra.getFechaCompra());
        if (compra.getArticuloInsumo().getImagenes() != null &&
                !compra.getArticuloInsumo().getImagenes().isEmpty()) {
            dto.setImagenUrl(compra.getArticuloInsumo().getImagenes().get(0).getUrl());
        }
        return dto;
    }
}
