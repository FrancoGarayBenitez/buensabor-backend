package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.HistoricoPrecioDTO;
import com.elbuensabor.dto.response.HistoricoPrecioStats;
import com.elbuensabor.dto.response.PrecioVentaSugeridoDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IHistoricoPrecioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico-precios")
@CrossOrigin(origins = "*")
public class HistoricoPrecioController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoPrecioController.class);

    @Autowired
    private IHistoricoPrecioService historicoPrecioService;

    /**
     * ✅ GET /api/historico-precios/{idArticulo}
     * Obtiene el historial de precios de un artículo
     */
    @GetMapping("/{idArticulo}")
    public ResponseEntity<List<HistoricoPrecioDTO>> getHistorial(@PathVariable("idArticulo") Long idArticulo) {
        logger.info("📡 GET /historico-precios/{}", idArticulo);
        try {
            List<HistoricoPrecioDTO> historial = historicoPrecioService.getHistorialByArticulo(idArticulo);
            logger.info("✅ Historial obtenido: {} registros", historial.size());
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            logger.error("❌ Error al obtener historial: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/estadisticas
     * Obtiene estadísticas de precios (min, max, promedio, cantidad)
     */
    @GetMapping("/{idArticulo}/estadisticas")
    public ResponseEntity<HistoricoPrecioStats> getEstadisticas(@PathVariable("idArticulo") Long idArticulo) {
        logger.info("📡 GET /historico-precios/{}/estadisticas", idArticulo);
        try {
            HistoricoPrecioStats stats = historicoPrecioService.getEstadisticas(idArticulo);
            logger.info("✅ Estadísticas: {} registros, promedio: ${}",
                    stats.getTotalRegistros(), stats.getPrecioPromedio());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Error al obtener estadísticas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/ultimos
     * Obtiene los últimos N precios (por defecto 10)
     */
    @GetMapping("/{idArticulo}/ultimos")
    public ResponseEntity<List<HistoricoPrecioDTO>> getUltimosPrecios(
            @PathVariable("idArticulo") Long idArticulo,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        logger.info("📡 GET /historico-precios/{}/ultimos?limit={}", idArticulo, limit);
        try {
            List<HistoricoPrecioDTO> precios = historicoPrecioService.getLastNPrecios(idArticulo, limit);
            logger.info("✅ Últimos precios obtenidos: {} registros", precios.size());
            return ResponseEntity.ok(precios);
        } catch (ResourceNotFoundException e) {
            logger.warn("⚠️ Artículo {} no encontrado", idArticulo);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Parámetro inválido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("❌ Error al obtener últimos precios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/precio-venta-sugerido
     * Calcula el precio SUGERIDO DE VENTA basado en historial de compra
     * 💡 CORRECCIÓN: Agregado "value" explícito en @RequestParam
     */
    @GetMapping("/{idArticulo}/precio-venta-sugerido")
    public ResponseEntity<PrecioVentaSugeridoDTO> getPrecioVentaSugerido(
            @PathVariable("idArticulo") Long idArticulo,
            @RequestParam(value = "margenGanancia", defaultValue = "1.2") Double margenGanancia) {

        logger.info("📡 GET /historico-precios/{}/precio-venta-sugerido?margenGanancia={}",
                idArticulo, margenGanancia);

        try {
            PrecioVentaSugeridoDTO dto = historicoPrecioService.calcularPrecioVentaSugerido(
                    idArticulo,
                    margenGanancia);

            logger.info("✅ Precio venta sugerido calculado: ${} (ganancia: ${})",
                    dto.getPrecioVentaSugerido(), dto.getGananciaUnitaria());
            logger.info("📦 DTO completo: {}", dto);

            return ResponseEntity.ok(dto);

        } catch (ResourceNotFoundException e) {
            logger.warn("⚠️ Artículo {} no encontrado", idArticulo);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("❌ Error al calcular precio venta sugerido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
