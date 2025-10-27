package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.services.ICategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")

public class CategoriaController {

    private final ICategoriaService categoriaService;

    @Autowired
    public CategoriaController(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategorias() {
        List<CategoriaResponseDTO> categorias = categoriaService.findAll();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(@PathVariable Long id) {
        CategoriaResponseDTO categoria = categoriaService.findById(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> createCategoria(@Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO categoriaCreada = categoriaService.createCategoria(categoriaRequestDTO);
        return new ResponseEntity<>(categoriaCreada, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> updateCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO categoriaActualizada = categoriaService.updateCategoria(id, categoriaRequestDTO);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== OPERACIONES ESPECÍFICAS ====================

    @GetMapping("/principales")
    public ResponseEntity<List<CategoriaResponseDTO>> getCategoriasPrincipales() {
        List<CategoriaResponseDTO> categoriasPrincipales = categoriaService.findCategoriasPrincipales();
        return ResponseEntity.ok(categoriasPrincipales);
    }

    @GetMapping("/{idPadre}/subcategorias")
    public ResponseEntity<List<CategoriaResponseDTO>> getSubcategoriasByPadre(@PathVariable Long idPadre) {
        List<CategoriaResponseDTO> subcategorias = categoriaService.findSubcategoriasByPadre(idPadre);
        return ResponseEntity.ok(subcategorias);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponseDTO>> searchCategorias(@RequestParam String denominacion) {
        List<CategoriaResponseDTO> categorias = categoriaService.searchByDenominacion(denominacion);
        return ResponseEntity.ok(categorias);
    }

    // ==================== ENDPOINTS DE VALIDACIÓN ====================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByDenominacion(@RequestParam String denominacion) {
        boolean exists = categoriaService.existsByDenominacion(denominacion);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/has-subcategorias")
    public ResponseEntity<Boolean> hasSubcategorias(@PathVariable Long id) {
        boolean hasSubcategorias = categoriaService.hasSubcategorias(id);
        return ResponseEntity.ok(hasSubcategorias);
    }

    @GetMapping("/{id}/has-articulos")
    public ResponseEntity<Boolean> hasArticulos(@PathVariable Long id) {
        boolean hasArticulos = categoriaService.hasArticulos(id);
        return ResponseEntity.ok(hasArticulos);
    }
}