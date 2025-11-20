package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IArticuloInsumoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articulos-insumo")
public class ArticuloInsumoController {

    private final IArticuloInsumoService articuloInsumoService;

    @Autowired
    public ArticuloInsumoController(IArticuloInsumoService articuloInsumoService) {
        this.articuloInsumoService = articuloInsumoService;
    }

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getAllArticulosInsumo() {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findAll();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloInsumoResponseDTO> getArticuloInsumoById(@PathVariable Long id) {
        ArticuloInsumoResponseDTO articulo = articuloInsumoService.findById(id);
        return ResponseEntity.ok(articulo);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloInsumoResponseDTO> createArticuloInsumo(
            @Valid @RequestBody ArticuloInsumoRequestDTO articuloRequestDTO) {
        ArticuloInsumoResponseDTO articuloCreado = articuloInsumoService.createInsumo(articuloRequestDTO);
        return new ResponseEntity<>(articuloCreado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloInsumoResponseDTO> updateArticuloInsumo(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloInsumoRequestDTO articuloRequestDTO) {
        ArticuloInsumoResponseDTO articuloActualizado = articuloInsumoService.updateInsumo(id, articuloRequestDTO);
        return ResponseEntity.ok(articuloActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteArticuloInsumo(@PathVariable Long id) {
        try {
            articuloInsumoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // ✅ Retornar 409 Conflict cuando hay compras asociadas
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", e.getMessage(),
                    "tipo", "COMPRAS_ASOCIADAS"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Error al eliminar el insumo: " + e.getMessage()));
        }
    }

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getArticulosByCategoria(@PathVariable Long idCategoria) {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findByCategoria(idCategoria);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/unidad-medida/{idUnidadMedida}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getArticulosByUnidadMedida(
            @PathVariable Long idUnidadMedida) {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findByUnidadMedida(idUnidadMedida);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/ingredientes")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getIngredientes() {
        List<ArticuloInsumoResponseDTO> ingredientes = articuloInsumoService.findIngredientes();
        return ResponseEntity.ok(ingredientes);
    }

    @GetMapping("/productos-no-manufacturados")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getProductosNoManufacturados() {
        List<ArticuloInsumoResponseDTO> productos = articuloInsumoService.findProductosNoManufacturados();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> searchArticulos(@RequestParam String denominacion) {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.searchByDenominacion(denominacion);
        return ResponseEntity.ok(articulos);
    }

    // ==================== CONTROL DE STOCK ====================

    @GetMapping("/stock/critico")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getStockCritico() {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findStockCritico();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/stock/bajo")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getStockBajo() {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findStockBajo();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/stock/insuficiente")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getStockInsuficiente(@RequestParam Integer cantidad) {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findInsuficientStock(cantidad);
        return ResponseEntity.ok(articulos);
    }

    // ==================== ENDPOINTS DE VALIDACIÓN E INFORMACIÓN
    // ====================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByDenominacion(@RequestParam String denominacion) {
        boolean exists = articuloInsumoService.existsByDenominacion(denominacion);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/stock-available")
    public ResponseEntity<Boolean> hasStockAvailable(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        boolean hasStock = articuloInsumoService.hasStockAvailable(id, cantidad);
        return ResponseEntity.ok(hasStock);
    }

    @GetMapping("/{id}/used-in-products")
    public ResponseEntity<Boolean> isUsedInProducts(@PathVariable Long id) {
        boolean isUsed = articuloInsumoService.isUsedInProducts(id);
        return ResponseEntity.ok(isUsed);
    }

    @GetMapping("/{id}/porcentaje-stock")
    public ResponseEntity<Double> getPorcentajeStock(@PathVariable Long id) {
        Double porcentaje = articuloInsumoService.calcularPorcentajeStock(id);
        return ResponseEntity.ok(porcentaje);
    }

    @GetMapping("/{id}/estado-stock")
    public ResponseEntity<String> getEstadoStock(@PathVariable Long id) {
        String estado = articuloInsumoService.determinarEstadoStock(id);
        return ResponseEntity.ok(estado);
    }
}