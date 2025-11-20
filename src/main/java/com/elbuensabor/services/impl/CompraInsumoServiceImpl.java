package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.CompraInsumo;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.ICompraInsumoRepository;
import com.elbuensabor.services.ICompraInsumoService;
import com.elbuensabor.services.IHistoricoPrecioService;

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
    private final IHistoricoPrecioService historicoPrecioService;

    /**
     * Registrar compra con todas las validaciones y actualizaciones
     */
    @Override
    @Transactional
    public CompraInsumoResponseDTO registrarCompra(CompraInsumoRequestDTO dto) {
        logger.info("📦 Registrando compra para insumo {}: {} unidades a ${}",
                dto.getInsumoId(), dto.getCantidad(), dto.getPrecioUnitario());

        // 1. Validar que el insumo existe
        ArticuloInsumo insumo = articuloInsumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> {
                    logger.error("❌ Insumo no encontrado: {}", dto.getInsumoId());
                    return new ResourceNotFoundException(
                            "Insumo no encontrado con ID: " + dto.getInsumoId());
                });

        // 2. Guardar compra original
        CompraInsumo compra = new CompraInsumo();
        compra.setArticuloInsumo(insumo);
        compra.setCantidad(dto.getCantidad());
        compra.setPrecioUnitario(dto.getPrecioUnitario());
        compra.setFechaCompra(dto.getFechaCompra() != null ? dto.getFechaCompra() : LocalDate.now());

        CompraInsumo compraGuardada = compraInsumoRepository.save(compra);
        logger.info("✅ Compra guardada con ID: {}", compraGuardada.getId());

        // 3. Actualizar precio promedio (ponderado)
        Double nuevoPromedio = calcularPrecioPromedio(insumo, dto.getPrecioUnitario());
        insumo.setPrecioCompra(nuevoPromedio);
        logger.info("💰 Nuevo precio promedio: ${}", nuevoPromedio);

        // 4. Aumentar stock
        Double nuevoStock = insumo.getStockActual() + dto.getCantidad();
        insumo.setStockActual(nuevoStock);
        logger.info("📈 Nuevo stock: {} (anterior: {})", nuevoStock, insumo.getStockActual() - dto.getCantidad());

        // 5. ✅ NUEVO: Recalcular estado del stock
        String nuevoEstado = calcularEstadoStock(insumo);
        insumo.setEstadoStock(nuevoEstado);
        logger.info("🎯 Nuevo estado: {}", nuevoEstado);

        // 6. Guardar cambios en insumo
        ArticuloInsumo insumoActualizado = articuloInsumoRepository.save(insumo);

        // 7. ✅ NUEVO: Registrar en historial de precios
        try {
            historicoPrecioService.registrarPrecio(
                    dto.getInsumoId(),
                    dto.getPrecioUnitario(),
                    dto.getCantidad());
            logger.info("✅ Precio registrado en historial");
        } catch (Exception e) {
            logger.warn("⚠️ Error registrando en historial: {}", e.getMessage());
        }

        logger.info("✅ Compra procesada exitosamente");
        return toDto(compraGuardada);
    }

    /**
     * Calcular precio promedio ponderado
     */
    private Double calcularPrecioPromedio(ArticuloInsumo insumo, Double nuevoPrecio) {
        Double stockActual = insumo.getStockActual();
        Double precioActual = insumo.getPrecioCompra();

        if (stockActual <= 0) {
            // Si no hay stock, usar el nuevo precio
            return nuevoPrecio;
        }

        // Promedio ponderado: (precioActual × stockActual + nuevoPrecio × cantidad) /
        // (stockActual + cantidad)
        Double promedio = (precioActual * stockActual + nuevoPrecio * stockActual) / (stockActual + stockActual);
        return Math.round(promedio * 100.0) / 100.0;
    }

    /**
     * Calcular estado del stock
     */
    private String calcularEstadoStock(ArticuloInsumo insumo) {
        Double porcentaje = (insumo.getStockActual() / insumo.getStockMaximo()) * 100;

        if (porcentaje <= 20) {
            return "CRITICO";
        } else if (porcentaje <= 50) {
            return "BAJO";
        } else if (porcentaje <= 100) {
            return "NORMAL";
        } else {
            return "ALTO";
        }
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
