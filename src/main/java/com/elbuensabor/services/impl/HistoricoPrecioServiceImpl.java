package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.HistoricoPrecioDTO;
import com.elbuensabor.dto.response.HistoricoPrecioStats;
import com.elbuensabor.dto.response.PrecioVentaSugeridoDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.HistoricoPrecio;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.IHistoricoPrecioRepository;
import com.elbuensabor.services.IHistoricoPrecioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoPrecioServiceImpl implements IHistoricoPrecioService {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoPrecioServiceImpl.class);

    @Autowired
    private IHistoricoPrecioRepository historicoPrecioRepository;

    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistoricoPrecioDTO> getHistorialByArticulo(Long idArticulo) {
        return historicoPrecioRepository.findByArticuloOrderByFechaDesc(idArticulo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistoricoPrecioDTO> getLastNPrecios(Long idArticulo, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return historicoPrecioRepository.findLastNPrecios(idArticulo, pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HistoricoPrecioDTO registrarPrecio(Long idArticulo, Double precioUnitario, Double cantidad) {
        if (idArticulo == null) {
            throw new IllegalArgumentException("idArticulo no puede ser null");
        }
        ArticuloInsumo articuloInsumo = articuloInsumoRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        HistoricoPrecio historico = new HistoricoPrecio();
        historico.setArticuloInsumo(articuloInsumo);
        historico.setPrecioUnitario(precioUnitario);
        historico.setCantidad(cantidad);

        HistoricoPrecio saved = historicoPrecioRepository.save(historico);
        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoPrecioStats getEstadisticas(Long idArticulo) {
        List<HistoricoPrecio> historicos = historicoPrecioRepository.findByArticuloOrderByFechaDesc(idArticulo);

        if (historicos.isEmpty()) {
            return new HistoricoPrecioStats(0, 0.0, 0.0, 0.0);
        }

        double precioPromedio = historicos.stream()
                .mapToDouble(HistoricoPrecio::getPrecioUnitario)
                .average()
                .orElse(0.0);

        double precioMin = historicos.stream()
                .mapToDouble(HistoricoPrecio::getPrecioUnitario)
                .min()
                .orElse(0.0);

        double precioMax = historicos.stream()
                .mapToDouble(HistoricoPrecio::getPrecioUnitario)
                .max()
                .orElse(0.0);

        return new HistoricoPrecioStats(
                historicos.size(),
                precioPromedio,
                precioMin,
                precioMax);
    }

    private HistoricoPrecioDTO toDTO(HistoricoPrecio historico) {
        return new HistoricoPrecioDTO(
                historico.getIdHistoricoPrecio(),
                historico.getArticuloInsumo().getIdArticulo(),
                historico.getPrecioUnitario(),
                historico.getFecha(),
                historico.getCantidad());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!historicoPrecioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compra con ID " + id + " no encontrada");
        }

        historicoPrecioRepository.deleteById(id);
        logger.info("Compra con ID {} eliminada exitosamente.", id);
    }

    /**
     * Calcular precio sugerido de VENTA
     * Formula: precioPromedio × margenGanancia
     */
    @Override
    @Transactional(readOnly = true)
    public PrecioVentaSugeridoDTO calcularPrecioVentaSugerido(
            Long idArticulo,
            Double margenGanancia) {

        logger.info("💰 Calculando precio de venta sugerido para artículo {}", idArticulo);

        // Obtener estadísticas del historial
        HistoricoPrecioStats stats = this.getEstadisticas(idArticulo);

        // ✅ Si no hay historial, retornar DTO con valores 0
        if (stats == null || stats.getTotalRegistros() == 0) {
            logger.warn("⚠️ Sin historial de compras para artículo {}", idArticulo);
            return new PrecioVentaSugeridoDTO(
                    0.0,
                    0.0,
                    margenGanancia,
                    0.0,
                    0,
                    0.0,
                    0.0,
                    "Sin historial de compras");
        }

        // ✅ Calcular precio de venta
        Double precioCompraPromedio = stats.getPrecioPromedio();
        Double precioVentaSugerido = Math.round(
                precioCompraPromedio * margenGanancia * 100.0) / 100.0;
        Double gananciaUnitaria = Math.round(
                (precioVentaSugerido - precioCompraPromedio) * 100.0) / 100.0;

        logger.info("✅ Precio sugerido: ${} (ganancia: ${})",
                precioVentaSugerido, gananciaUnitaria);

        return new PrecioVentaSugeridoDTO(
                Math.round(precioCompraPromedio * 100.0) / 100.0,
                precioVentaSugerido,
                margenGanancia,
                gananciaUnitaria,
                stats.getTotalRegistros(),
                Math.round(stats.getPrecioMinimo() * 100.0) / 100.0,
                Math.round(stats.getPrecioMaximo() * 100.0) / 100.0,
                "Precio sugerido para vender y obtener " +
                        String.format("%.0f", ((margenGanancia - 1) * 100)) +
                        "% de ganancia");
    }
}
