package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.MovimientosMonetariosDTO;
import com.elbuensabor.dto.response.RankingProductoDTO;
import com.elbuensabor.services.IEstadisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    // --- CAMBIO 1: Declarar el servicio como un campo final ---
    private final IEstadisticasService estadisticasService;

    // --- CAMBIO 2: Crear un constructor para que Spring inyecte el servicio ---
    @Autowired
    public EstadisticasController(IEstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovimientosMonetariosDTO> getMovimientosMonetarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        MovimientosMonetariosDTO movimientos = estadisticasService.findMovimientosMonetarios(fechaDesde, fechaHasta);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/ranking-productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RankingProductoDTO>> getRankingProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<RankingProductoDTO> ranking = estadisticasService.findRankingProductos(fechaDesde, fechaHasta, limit);
        return ResponseEntity.ok(ranking);
    }
}

