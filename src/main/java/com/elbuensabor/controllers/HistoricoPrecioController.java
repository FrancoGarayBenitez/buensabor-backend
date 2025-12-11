package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.HistoricoPrecioDTO;
import com.elbuensabor.dto.response.HistoricoPrecioStats;
import com.elbuensabor.dto.response.PrecioVentaSugeridoDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IHistoricoPrecioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico-precios")
@CrossOrigin(origins = "*")
public class HistoricoPrecioController {

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
     * ✅ GET /api/historico-precios/{idArticulo}/ultimos
     * Obtiene los últimos N precios (por defecto 10)
     */
    @GetMapping("/{idArticulo}/ultimos")
    public ResponseEntity<List<HistoricoPrecioDTO>> getUltimosPrecios(
            @PathVariable Long idArticulo,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<HistoricoPrecioDTO> precios = historicoPrecioService.getLastNPrecios(idArticulo, limit);
            return ResponseEntity.ok(precios);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
}
