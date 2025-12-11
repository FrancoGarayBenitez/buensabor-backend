package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.services.IArticuloManufacturadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos-manufacturados")
public class ArticuloManufacturadoController {

    private final IArticuloManufacturadoService service;

    @Autowired
    public ArticuloManufacturadoController(IArticuloManufacturadoService service) {
        this.service = service;
    }

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getAll() {
        List<ArticuloManufacturadoResponseDTO> manufacturados = service.findAll();
        return ResponseEntity.ok(manufacturados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> getById(@PathVariable Long id) {
        ArticuloManufacturadoResponseDTO manufacturado = service.findById(id);
        return ResponseEntity.ok(manufacturado);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> create(
            @Valid @RequestBody ArticuloManufacturadoRequestDTO requestDTO) {
        ArticuloManufacturadoResponseDTO manufacturadoCreado = service.createManufacturado(requestDTO);
        return new ResponseEntity<>(manufacturadoCreado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloManufacturadoRequestDTO requestDTO) {
        ArticuloManufacturadoResponseDTO manufacturadoActualizado = service.updateManufacturado(id, requestDTO);
        return ResponseEntity.ok(manufacturadoActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getByCategoria(@PathVariable Long idCategoria) {
        List<ArticuloManufacturadoResponseDTO> manufacturados = service.findByCategoria(idCategoria);
        return ResponseEntity.ok(manufacturados);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> searchByDenominacion(
            @RequestParam String denominacion) {
        List<ArticuloManufacturadoResponseDTO> manufacturados = service.searchByDenominacion(denominacion);
        return ResponseEntity.ok(manufacturados);
    }
}