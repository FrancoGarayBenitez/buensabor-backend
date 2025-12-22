package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.services.IArticuloManufacturadoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos-manufacturados")
public class ArticuloManufacturadoController {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloManufacturadoController.class);
    private final IArticuloManufacturadoService service;

    @Autowired
    public ArticuloManufacturadoController(IArticuloManufacturadoService service) {
        this.service = service;
    }

    // ==================== CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getAll() {
        logger.debug("📥 GET /api/articulos-manufacturados - Obteniendo todos los productos");
        List<ArticuloManufacturadoResponseDTO> productos = service.findAll();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> getById(@PathVariable Long id) {
        logger.debug("📥 GET /api/articulos-manufacturados/{} - Obteniendo producto por ID", id);
        ArticuloManufacturadoResponseDTO producto = service.findById(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> create(
            @Valid @RequestBody ArticuloManufacturadoRequestDTO requestDTO) {
        logger.info("📝 POST /api/articulos-manufacturados - Creando producto: {}", requestDTO.getDenominacion());
        ArticuloManufacturadoResponseDTO created = service.create(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloManufacturadoRequestDTO requestDTO) {
        logger.info("📝 PUT /api/articulos-manufacturados/{} - Actualizando producto", id);
        ArticuloManufacturadoResponseDTO updated = service.update(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("🗑️ DELETE /api/articulos-manufacturados/{} - Dando de baja lógica a producto", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ACTIVAR / DESACTIVAR ====================

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        logger.info("⛔ PATCH /api/articulos-manufacturados/{}/deactivate - Desactivando producto", id);
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        logger.info("♻️ PATCH /api/articulos-manufacturados/{}/activate - Activando producto", id);
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== BÚSQUEDAS POR FILTRO ====================

    @GetMapping("/buscar")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> searchByDenominacion(
            @RequestParam String denominacion) {
        logger.debug("🔍 GET /api/articulos-manufacturados/buscar?denominacion={}", denominacion);
        List<ArticuloManufacturadoResponseDTO> productos = service.search(denominacion);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getByCategoria(
            @PathVariable Long idCategoria) {
        logger.debug("🔍 GET /api/articulos-manufacturados/categoria/{}", idCategoria);
        List<ArticuloManufacturadoResponseDTO> productos = service.findByCategoria(idCategoria);
        return ResponseEntity.ok(productos);
    }
}