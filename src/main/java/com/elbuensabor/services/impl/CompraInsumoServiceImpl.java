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
     * ✅ Registrar compra SOLO por paquetes y calcular estado
     */
    @Override
    @Transactional
    public CompraInsumoResponseDTO registrarCompra(CompraInsumoRequestDTO dto) {
        logger.info("📦 Compra por paquetes -> insumo={}, paquetes={}, contenido/paq={} {}, precio/paq={}",
                dto.getIdArticuloInsumo(), dto.getPaquetes(),
                dto.getContenidoPorPaquete(), dto.getUnidadContenido(), dto.getPrecioPorPaquete());

        // Validaciones nuevas (flujo paquetes)
        if (dto.getPaquetes() == null || dto.getPaquetes() <= 0) {
            throw new IllegalArgumentException("Los paquetes deben ser > 0");
        }
        if (dto.getContenidoPorPaquete() == null || dto.getContenidoPorPaquete() <= 0) {
            throw new IllegalArgumentException("El contenido por paquete debe ser > 0");
        }
        if (dto.getPrecioPorPaquete() == null || dto.getPrecioPorPaquete() <= 0) {
            throw new IllegalArgumentException("El precio por paquete debe ser > 0");
        }

        // 1. Validar que el insumo existe
        ArticuloInsumo insumo = articuloInsumoRepository.findById(dto.getIdArticuloInsumo())
                .orElseThrow(() -> {
                    logger.error("❌ Insumo no encontrado: {}", dto.getIdArticuloInsumo());
                    return new ResourceNotFoundException(
                            "Insumo no encontrado con ID: " + dto.getIdArticuloInsumo());
                });

        // 2. Normalizar contenido de UN paquete a unidad técnica (g/ml/unidad)
        String unidadTecnica = norm(insumo.getUnidadMedida().getDenominacion()); // "g" | "ml" | "unidad"
        String unidadContenido = norm(dto.getUnidadContenido());
        if (unidadContenido.isEmpty())
            unidadContenido = unidadTecnica;

        double contenidoPorPaqueteTecnico = contenidoPorPaqueteAUnidadTecnica(
                unidadTecnica, dto.getContenidoPorPaquete(), unidadContenido);

        // 3. Calcular cantidad técnica total y precio unitario técnico
        double cantidadTecnica = dto.getPaquetes() * contenidoPorPaqueteTecnico;
        double precioUnitarioTecnico = dto.getPrecioPorPaquete() / contenidoPorPaqueteTecnico;

        // 4. Guardar compra (siempre en unidad técnica)
        CompraInsumo compra = new CompraInsumo();
        compra.setArticuloInsumo(insumo);
        compra.setCantidad(cantidadTecnica); // ✅ unidad técnica
        compra.setPrecioUnitario(redondear2(precioUnitarioTecnico)); // ✅ $ por unidad técnica
        compra.setFechaCompra(dto.getFechaCompra() != null ? dto.getFechaCompra() : LocalDate.now());

        CompraInsumo compraGuardada = compraInsumoRepository.save(compra);
        logger.info("✅ Compra guardada ID: {} ({} {} a ${}/{}) => cantidad técnica: {}, total: ${}",
                compraGuardada.getId(),
                dto.getPaquetes(), "paquetes", dto.getPrecioPorPaquete(), "paquete",
                cantidadTecnica, dto.getPaquetes() * dto.getPrecioPorPaquete());

        // 5. Actualizar precio promedio (ponderado) usando cantidad técnica
        Double nuevoPromedio = calcularPrecioPromedio(insumo, compra.getPrecioUnitario(), cantidadTecnica);
        insumo.setPrecioCompra(nuevoPromedio);

        // 6. Aumentar stock (unidad técnica)
        Double stockActual = insumo.getStockActual() != null ? insumo.getStockActual() : 0.0;
        insumo.setStockActual(stockActual + cantidadTecnica);

        // 7. Calcular estado del stock (enum)
        EstadoStock nuevoEstado = calcularEstadoStockEnum(insumo.getStockActual(), insumo.getStockMaximo());
        insumo.setEstadoStock(nuevoEstado.name());

        // 8. Guardar cambios en insumo
        articuloInsumoRepository.save(insumo);

        // 9. Registrar en historial de precios (cantidad técnica)
        try {
            HistoricoPrecio historico = new HistoricoPrecio();
            historico.setCompra(compraGuardada);
            historico.setArticuloInsumo(insumo);
            historico.setPrecioUnitario(compra.getPrecioUnitario());
            historico.setCantidad(cantidadTecnica); // ✅ unidad técnica
            historicoPrecioRepository.save(historico);
            logger.info("✅ Precio registrado en historial");
        } catch (Exception e) {
            logger.warn("⚠️ Error registrando en historial: {}", e.getMessage());
        }

        logger.info("✅ Compra procesada exitosamente (flujo paquetes)");
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
        dto.setCantidad(compra.getCantidad()); // normalizado (unidad técnica)
        dto.setPrecioUnitario(compra.getPrecioUnitario()); // $ por unidad técnica
        dto.setFechaCompra(compra.getFechaCompra());
        if (compra.getArticuloInsumo().getImagenes() != null &&
                !compra.getArticuloInsumo().getImagenes().isEmpty()) {
            dto.setImagenUrl(compra.getArticuloInsumo().getImagenes().get(0).getUrl());
        }
        // Nota: si luego persistes campos de paquete en CompraInsumo, mápealos aquí.
        return dto;
    }

    /**
     * 🔁 Conversión del contenido de UN paquete a la unidad técnica (g, ml o
     * unidad)
     */
    private double contenidoPorPaqueteAUnidadTecnica(String unidadTecnica, double contenido, String unidadContenido) {
        String base = unidadTecnica;
        String u = unidadContenido;

        // normalizar alias
        if (base.equals("gramo") || base.equals("gramos"))
            base = "g";
        if (base.equals("mililitro") || base.equals("mililitros"))
            base = "ml";

        if (u.equals("kilogramo") || u.equals("kilogramos"))
            u = "kg";
        if (u.equals("litro") || u.equals("litros"))
            u = "l";

        if (base.equals("g")) {
            if (u.equals("kg"))
                return contenido * 1000.0;
            return contenido; // g
        }
        if (base.equals("ml")) {
            if (u.equals("l"))
                return contenido * 1000.0;
            return contenido; // ml
        }
        // unidad
        return contenido;
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * Redondear a 2 decimales (String)
     */
    private Double redondear2(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
