package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.PromocionAplicacionDTO;
import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionCalculoDTO;
import com.elbuensabor.dto.response.PromocionCompletaDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.services.IPromocionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")

public class PromocionController {

    private static final Logger logger = LoggerFactory.getLogger(PromocionController.class);

    @Autowired
    private IPromocionService promocionService;

    // ==================== ENDPOINTS PARA CLIENTES ====================

    /**
     * GET /api/promociones/vigentes
     * Obtener todas las promociones vigentes (para mostrar en home/catálogo)
     */
    @GetMapping("/vigentes")
    public ResponseEntity<List<PromocionResponseDTO>> getPromocionesVigentes() {
        logger.info("📋 Consultando promociones vigentes");
        try {
            List<PromocionResponseDTO> promociones = promocionService.findPromocionesVigentes();
            logger.info("✅ Encontradas {} promociones vigentes", promociones.size());
            return ResponseEntity.ok(promociones);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo promociones vigentes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/promociones/articulo/{idArticulo}
     * Obtener promociones disponibles para un artículo específico
     */
    @GetMapping("/articulo/{idArticulo}")
    public ResponseEntity<List<PromocionResponseDTO>> getPromocionesParaArticulo(@PathVariable Long idArticulo) {
        logger.info("🎯 Consultando promociones para artículo ID: {}", idArticulo);
        try {
            List<PromocionResponseDTO> promociones = promocionService.findPromocionesParaArticulo(idArticulo);
            logger.info("✅ Encontradas {} promociones para artículo {}", promociones.size(), idArticulo);
            return ResponseEntity.ok(promociones);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo promociones para artículo {}: {}", idArticulo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/promociones/aplicables?idArticulo={id}&idSucursal={id}
     * Obtener promociones aplicables para un artículo en una sucursal específica
     */
    @GetMapping("/aplicables")
    public ResponseEntity<List<PromocionResponseDTO>> getPromocionesAplicables(
            @RequestParam Long idArticulo,
            @RequestParam(defaultValue = "1") Long idSucursal) {

        logger.info("🎯 Consultando promociones aplicables para artículo {} en sucursal {}", idArticulo, idSucursal);
        try {
            List<PromocionResponseDTO> promociones = promocionService.findPromocionesAplicables(idArticulo, idSucursal);
            logger.info("✅ Encontradas {} promociones aplicables", promociones.size());
            return ResponseEntity.ok(promociones);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo promociones aplicables: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/promociones/calcular-descuentos
     * Calcular descuentos para un conjunto de promociones (preview del carrito)
     */
    @PostMapping("/calcular-descuentos")
    public ResponseEntity<PromocionCalculoDTO> calcularDescuentos(
            @RequestParam(defaultValue = "1") Long idSucursal,
            @Valid @RequestBody List<PromocionAplicacionDTO> aplicaciones) {

        logger.info("💰 Calculando descuentos para {} aplicaciones en sucursal {}", aplicaciones.size(), idSucursal);
        try {
            PromocionCalculoDTO calculo = promocionService.calcularDescuentosParaPedido(idSucursal, aplicaciones);
            logger.info("✅ Descuentos calculados: total ${}", calculo.getDescuentoTotal());
            return ResponseEntity.ok(calculo);
        } catch (Exception e) {
            logger.error("❌ Error calculando descuentos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS PARA ADMINISTRACIÓN ====================

    /**
     * GET /api/promociones
     * Obtener todas las promociones (admin)
     */
    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> getAllPromociones() {
        logger.info("📋 Admin: Consultando todas las promociones");
        try {
            List<PromocionResponseDTO> promociones = promocionService.findAll();
            return ResponseEntity.ok(promociones);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo todas las promociones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/promociones/{id}
     * Obtener una promoción específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> getPromocionById(@PathVariable Long id) {
        logger.info("🔍 Consultando promoción ID: {}", id);
        try {
            PromocionResponseDTO promocion = promocionService.findById(id);
            return ResponseEntity.ok(promocion);
        } catch (Exception e) {
            logger.error("❌ Error obteniendo promoción {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * POST /api/promociones
     * Crear nueva promoción (admin)
     */
    @PostMapping
    public ResponseEntity<PromocionResponseDTO> crearPromocion(@Valid @RequestBody PromocionRequestDTO request) {
        logger.info("➕ Admin: Creando nueva promoción: {}", request.getDenominacion());
        try {
            PromocionResponseDTO promocionCreada = promocionService.crearPromocion(request);
            logger.info("✅ Promoción creada con ID: {}", promocionCreada.getIdPromocion());
            return ResponseEntity.status(HttpStatus.CREATED).body(promocionCreada);
        } catch (Exception e) {
            logger.error("❌ Error creando promoción: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/promociones/{id}
     * Actualizar promoción existente (admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> actualizarPromocion(
            @PathVariable Long id,
            @Valid @RequestBody PromocionRequestDTO request) {

        logger.info("✏️ Admin: Actualizando promoción ID: {}", id);
        try {
            PromocionResponseDTO promocionActualizada = promocionService.actualizarPromocion(id, request);
            logger.info("✅ Promoción {} actualizada exitosamente", id);
            return ResponseEntity.ok(promocionActualizada);
        } catch (Exception e) {
            logger.error("❌ Error actualizando promoción {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PATCH /api/promociones/{id}/activar
     * Activar promoción (admin)
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarPromocion(@PathVariable Long id) {
        logger.info("🟢 Admin: Activando promoción ID: {}", id);
        try {
            promocionService.activarPromocion(id);
            logger.info("✅ Promoción {} activada", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error activando promoción {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PATCH /api/promociones/{id}/desactivar
     * Desactivar promoción (admin)
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarPromocion(@PathVariable Long id) {
        logger.info("🔴 Admin: Desactivando promoción ID: {}", id);
        try {
            promocionService.desactivarPromocion(id);
            logger.info("✅ Promoción {} desactivada", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error desactivando promoción {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * DELETE /api/promociones/{id}
     * Eliminar promoción (admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPromocion(@PathVariable Long id) {
        logger.info("🗑️ Admin: Eliminando promoción ID: {}", id);
        try {
            promocionService.delete(id);
            logger.info("✅ Promoción {} eliminada", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error eliminando promoción {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/vigentes-completas")
    public ResponseEntity<List<PromocionCompletaDTO>> obtenerPromocionesVigentesCompletas() {
        logger.info("📋 Consultando promociones vigentes completas con artículos");

        List<PromocionCompletaDTO> promociones = promocionService.findPromocionesVigentesCompletas();

        logger.info("✅ Encontradas {} promociones vigentes completas", promociones.size());

        return ResponseEntity.ok(promociones);
    }
}