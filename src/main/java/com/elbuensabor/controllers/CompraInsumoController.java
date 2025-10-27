package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.services.CompraInsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.elbuensabor.entities.CompraInsumo;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras-insumo")
@RequiredArgsConstructor
public class CompraInsumoController {

    private final CompraInsumoService compraInsumoService;

    @PostMapping
    public ResponseEntity<Void> registrarCompra(@RequestBody CompraInsumoRequestDTO dto) {
        compraInsumoService.registrarCompra(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CompraInsumoResponseDTO>> getAllCompras() {
        List<CompraInsumo> compras = compraInsumoService.getAllCompras();
        List<CompraInsumoResponseDTO> dtos = compras.stream()
                .map(compraInsumoService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraInsumoResponseDTO> getCompraById(@PathVariable Long id) {
        CompraInsumo compra = compraInsumoService.getCompraById(id);
        return ResponseEntity.ok(compraInsumoService.toDto(compra));
    }

    @GetMapping("/insumo/{idInsumo}")
    public ResponseEntity<List<CompraInsumoResponseDTO>> getComprasByInsumoId(@PathVariable Long idInsumo) {
        List<CompraInsumo> compras = compraInsumoService.getComprasByInsumoId(idInsumo);
        List<CompraInsumoResponseDTO> dtos = compras.stream()
                .map(compraInsumoService::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

}
