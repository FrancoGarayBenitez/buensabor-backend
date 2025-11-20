package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.HistoricoPrecioDTO;
import com.elbuensabor.dto.response.HistoricoPrecioStats;
import com.elbuensabor.dto.response.PrecioVentaSugeridoDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IHistoricoPrecioService;
import com.elbuensabor.services.impl.HistoricoPrecioServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<HistoricoPrecioDTO>> getHistorial(@PathVariable Long idArticulo) {
        try {
            List<HistoricoPrecioDTO> historial = historicoPrecioService.getHistorialByArticulo(idArticulo);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/estadisticas
     * Obtiene estadísticas de precios (min, max, promedio, cantidad)
     */
    @GetMapping("/{idArticulo}/estadisticas")
    public ResponseEntity<HistoricoPrecioStats> getEstadisticas(@PathVariable Long idArticulo) {
        try {
            HistoricoPrecioStats stats = historicoPrecioService.getEstadisticas(idArticulo);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ POST /api/historico-precios
     * Registra un nuevo precio en el historial
     * Body: { "idArticulo": 1, "precioUnitario": 25.50, "cantidad": 100 }
     */
    @PostMapping
    public ResponseEntity<?> registrarPrecio(@RequestBody Map<String, Object> request) {
        try {
            Long idArticulo = ((Number) request.get("idArticulo")).longValue();
            Double precioUnitario = ((Number) request.get("precioUnitario")).doubleValue();
            Double cantidad = request.containsKey("cantidad")
                    ? ((Number) request.get("cantidad")).doubleValue()
                    : 0.0;

            HistoricoPrecioDTO result = historicoPrecioService.registrarPrecio(
                    idArticulo,
                    precioUnitario,
                    cantidad);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Datos inválidos: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al registrar precio: " + e.getMessage()));
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/ultimos
     * Obtiene los últimos N precios (por defecto 10)
     */
    @GetMapping("/{idArticulo}/ultimos")
    public ResponseEntity<List<HistoricoPrecioDTO>> getUltimosPrecios(
            @PathVariable Long idArticulo,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<HistoricoPrecioDTO> precios = ((HistoricoPrecioServiceImpl) historicoPrecioService)
                    .getLastNPrecios(idArticulo, limit);
            return ResponseEntity.ok(precios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ GET /api/historico-precios/{idArticulo}/precio-venta-sugerido
     * Calcula el precio SUGERIDO DE VENTA basado en historial de compra
     */
    @GetMapping("/{idArticulo}/precio-venta-sugerido")
    public ResponseEntity<PrecioVentaSugeridoDTO> getPrecioVentaSugerido(
            @PathVariable Long idArticulo,
            @RequestParam(defaultValue = "1.2") Double margenGanancia) {
        try {
            PrecioVentaSugeridoDTO dto = historicoPrecioService.calcularPrecioVentaSugerido(
                    idArticulo,
                    margenGanancia);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ NUEVO: DELETE /api/historico-precios/{id}
     * Elimina una compra individual del historial
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteHistoricoPrecio(@PathVariable Long id) {
        try {
            historicoPrecioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al eliminar la compra: " + e.getMessage()));
        }
    }
}
