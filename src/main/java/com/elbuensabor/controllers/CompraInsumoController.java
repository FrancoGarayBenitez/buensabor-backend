package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.services.ICompraInsumoService;
import com.elbuensabor.services.IArticuloInsumoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.elbuensabor.entities.CompraInsumo;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras-insumo")
@RequiredArgsConstructor
public class CompraInsumoController {

    private static final Logger logger = LoggerFactory.getLogger(CompraInsumoController.class);

    private final ICompraInsumoService compraInsumoService;
    private final IArticuloInsumoService articuloInsumoService;

    /**
     * POST /api/compras-insumo
     * Registra compra y retorna el insumo actualizado
     */
    @PostMapping
    public ResponseEntity<ArticuloInsumoResponseDTO> registrarCompra(
            @RequestBody CompraInsumoRequestDTO dto) {
        try {
            logger.info("📦 Registrando compra...");
            compraInsumoService.registrarCompra(dto);

            // ✅ Retornar insumo actualizado
            ArticuloInsumoResponseDTO insumoActualizado = articuloInsumoService.findById(dto.getInsumoId());

            logger.info("✅ Compra registrada, retornando insumo actualizado");
            return ResponseEntity.ok(insumoActualizado);
        } catch (Exception e) {
            logger.error("❌ Error registrando compra: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
