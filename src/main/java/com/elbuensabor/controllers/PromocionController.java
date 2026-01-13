package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.services.IPromocionService;
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
@RequestMapping("/api/promociones")
public class PromocionController {

    private static final Logger logger = LoggerFactory.getLogger(PromocionController.class);
    private final IPromocionService service;

    @Autowired
    public PromocionController(IPromocionService service) {
        this.service = service;
    }

    // ==================== CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> getAll() {
        logger.debug("📥 GET /api/promociones - Obteniendo todas las promociones");
        List<PromocionResponseDTO> promociones = service.findAll();
        return ResponseEntity.ok(promociones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> getById(@PathVariable Long id) {
        logger.debug("📥 GET /api/promociones/{} - Obteniendo promoción por ID", id);
        PromocionResponseDTO promocion = service.findById(id);
        return ResponseEntity.ok(promocion);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PromocionResponseDTO> create(@Valid @RequestBody PromocionRequestDTO requestDTO) {
        logger.info("📝 POST /api/promociones - Creando promoción: {}", requestDTO.getDenominacion());
        PromocionResponseDTO created = service.create(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PromocionResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody PromocionRequestDTO requestDTO) {
        logger.info("📝 PUT /api/promociones/{} - Actualizando promoción", id);
        PromocionResponseDTO updated = service.update(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("🗑️ DELETE /api/promociones/{} - Dando de baja lógica a promoción", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ACTIVAR / DESACTIVAR / TOGGLE ====================

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        logger.info("⛔ PATCH /api/promociones/{}/deactivate - Desactivando promoción (soft delete)", id);
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        logger.info("♻️ PATCH /api/promociones/{}/activate - Activando promoción", id);
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-activo")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PromocionResponseDTO> toggleActivo(@PathVariable Long id) {
        logger.info("🔄 PATCH /api/promociones/{}/toggle-activo - Cambiando estado 'activo'", id);
        PromocionResponseDTO updated = service.toggleActivo(id);
        return ResponseEntity.ok(updated);
    }

    // ==================== BÚSQUEDAS POR FILTRO ====================

    @GetMapping("/buscar")
    public ResponseEntity<List<PromocionResponseDTO>> searchByDenominacion(@RequestParam String denominacion) {
        logger.debug("🔍 GET /api/promociones/buscar?denominacion={}", denominacion);
        List<PromocionResponseDTO> promociones = service.search(denominacion);
        return ResponseEntity.ok(promociones);
    }
}