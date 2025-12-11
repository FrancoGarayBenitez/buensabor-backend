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
import com.elbuensabor.exceptions.ResourceNotFoundException;

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
            @RequestBody @jakarta.validation.Valid CompraInsumoRequestDTO dto) {
        try {
            logger.info("📦 Registrando compra...");
            compraInsumoService.registrarCompra(dto);
            ArticuloInsumoResponseDTO insumoActualizado = articuloInsumoService.findById(dto.getIdArticuloInsumo());
            return ResponseEntity.ok(insumoActualizado);
        } catch (ResourceNotFoundException e) {
            logger.warn("⚠️ Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Datos inválidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("❌ Error registrando compra: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/compras-insumo
     * Obtiene todas las compras
     */
    @GetMapping
    public ResponseEntity<List<CompraInsumoResponseDTO>> getAllCompras() {
        try {
            logger.info("📋 Obteniendo todas las compras");
            List<CompraInsumo> compras = compraInsumoService.getAllCompras();
            List<CompraInsumoResponseDTO> dtos = compras.stream()
                    .map(compraInsumoService::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo compras: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/compras-insumo/{id}
     * Obtiene una compra por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompraInsumoResponseDTO> getCompraById(@PathVariable Long id) {
        try {
            logger.info("🔍 Buscando compra ID: {}", id);
            CompraInsumo compra = compraInsumoService.getCompraById(id);
            return ResponseEntity.ok(compraInsumoService.toDto(compra));
        } catch (Exception e) {
            logger.error("❌ Error obteniendo compra: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/compras-insumo/insumo/{idInsumo}
     * Obtiene todas las compras de un insumo
     */
    @GetMapping("/insumo/{idInsumo}")
    public ResponseEntity<List<CompraInsumoResponseDTO>> getComprasByInsumoId(@PathVariable Long idInsumo) {
        try {
            logger.info("🔍 Buscando compras del insumo ID: {}", idInsumo);
            List<CompraInsumo> compras = compraInsumoService.getComprasByInsumoId(idInsumo);
            List<CompraInsumoResponseDTO> dtos = compras.stream()
                    .map(compraInsumoService::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo compras del insumo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/compras-insumo/{id}
     * Elimina una compra
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ArticuloInsumoResponseDTO> eliminarCompra(@PathVariable Long id) {
        try {
            logger.info("🗑️ DELETE /api/compras-insumo/{}", id);
            Long idInsumo = compraInsumoService.eliminarCompra(id);
            ArticuloInsumoResponseDTO insumoActualizado = articuloInsumoService.findById(idInsumo);
            return ResponseEntity.ok(insumoActualizado);
        } catch (ResourceNotFoundException e) {
            logger.warn("⚠️ Compra no encontrada: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("❌ Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
