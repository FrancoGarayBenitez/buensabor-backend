package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IArticuloInsumoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ArticuloInsumoController.class);
    private final IArticuloInsumoService service;

    @Autowired
    public ArticuloInsumoController(IArticuloInsumoService service) {
        this.service = service;
    }

    // ==================== CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getAll() {
        logger.debug("📥 GET /api/articulos-insumo - Obteniendo todos los insumos");
        List<ArticuloInsumoResponseDTO> articulos = service.findAll();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloInsumoResponseDTO> getById(@PathVariable("id") Long id) {
        logger.debug("📥 GET /api/articulos-insumo/{} - Obteniendo insumo por ID", id);
        ArticuloInsumoResponseDTO articulo = service.findById(id);
        return ResponseEntity.ok(articulo);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloInsumoResponseDTO> create(
            @Valid @RequestBody ArticuloInsumoRequestDTO requestDTO) {
        logger.info("📝 POST /api/articulos-insumo - Creando insumo: {}", requestDTO.getDenominacion());
        ArticuloInsumoResponseDTO created = service.create(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COCINERO')")
    public ResponseEntity<ArticuloInsumoResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ArticuloInsumoRequestDTO requestDTO) {
        logger.info("📝 PUT /api/articulos-insumo/{} - Actualizando insumo", id);
        ArticuloInsumoResponseDTO updated = service.update(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        logger.info("🗑️ DELETE /api/articulos-insumo/{} - Eliminando insumo", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== BÚSQUEDAS POR FILTRO ====================

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByCategoria(
            @PathVariable("idCategoria") Long idCategoria) {
        logger.debug("🔍 GET /api/articulos-insumo/categoria/{}", idCategoria);
        List<ArticuloInsumoResponseDTO> articulos = service.findByCategoria(idCategoria);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/unidad-medida/{idUnidadMedida}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByUnidadMedida(
            @PathVariable("idUnidadMedida") Long idUnidadMedida) {
        logger.debug("🔍 GET /api/articulos-insumo/unidad-medida/{}", idUnidadMedida);
        List<ArticuloInsumoResponseDTO> articulos = service.findByUnidadMedida(idUnidadMedida);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> searchByDenominacion(
            @RequestParam String denominacion) {
        logger.debug("🔍 GET /api/articulos-insumo/buscar?denominacion={}", denominacion);
        List<ArticuloInsumoResponseDTO> articulos = service.findByDenominacion(denominacion);
        return ResponseEntity.ok(articulos);
    }

    // ==================== BÚSQUEDAS POR TIPO ====================

    @GetMapping("/tipo/para-elaborar")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getParaElaborar() {
        logger.debug("🔍 GET /api/articulos-insumo/tipo/para-elaborar");
        List<ArticuloInsumoResponseDTO> insumos = service.findParaElaborar();
        return ResponseEntity.ok(insumos);
    }

    @GetMapping("/tipo/no-para-elaborar")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getNoParaElaborar() {
        logger.debug("🔍 GET /api/articulos-insumo/tipo/no-para-elaborar");
        List<ArticuloInsumoResponseDTO> productos = service.findNoParaElaborar();
        return ResponseEntity.ok(productos);
    }

    // ==================== BÚSQUEDAS POR ESTADO DE STOCK ====================

    @GetMapping("/stock/critico")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByCriticoStock() {
        logger.debug("🔍 GET /api/articulos-insumo/stock/critico");
        List<ArticuloInsumoResponseDTO> articulos = service.findByCriticoStock();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/stock/bajo")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByBajoStock() {
        logger.debug("🔍 GET /api/articulos-insumo/stock/bajo");
        List<ArticuloInsumoResponseDTO> articulos = service.findByBajoStock();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/stock/alto")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByAltoStock() {
        logger.debug("🔍 GET /api/articulos-insumo/stock/alto");
        List<ArticuloInsumoResponseDTO> articulos = service.findByAltoStock();
        return ResponseEntity.ok(articulos);
    }

    // ==================== BÚSQUEDAS POR PRECIO ====================

    @GetMapping("/precio")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getByPrecioRange(
            @RequestParam Double precioMin,
            @RequestParam Double precioMax) {
        logger.debug("🔍 GET /api/articulos-insumo/precio?precioMin={}&precioMax={}", precioMin, precioMax);
        List<ArticuloInsumoResponseDTO> articulos = service.findByPrecioCompraBetween(precioMin, precioMax);
        return ResponseEntity.ok(articulos);
    }

    // ==================== VALIDACIONES ====================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByDenominacion(@RequestParam String denominacion) {
        logger.debug("✓ GET /api/articulos-insumo/exists?denominacion={}", denominacion);
        boolean exists = service.existsByDenominacion(denominacion);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/en-uso")
    public ResponseEntity<Boolean> estaEnUso(@PathVariable("id") Long id) {
        logger.debug("✓ GET /api/articulos-insumo/{}/en-uso", id);
        boolean enUso = service.estaEnUso(id);
        return ResponseEntity.ok(enUso);
    }

    @GetMapping("/{id}/productos-que-lo-usan")
    public ResponseEntity<Integer> countProductosQueLoUsan(@PathVariable("id") Long id) {
        logger.debug("✓ GET /api/articulos-insumo/{}/productos-que-lo-usan", id);
        Integer count = service.countProductosQueLoUsan(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/stock-disponible")
    public ResponseEntity<Boolean> tieneStockDisponible(
            @PathVariable("id") Long id,
            @RequestParam Double cantidad) {
        logger.debug("✓ GET /api/articulos-insumo/{}/stock-disponible?cantidad={}", id, cantidad);
        boolean disponible = service.tieneStockDisponible(id, cantidad);
        return ResponseEntity.ok(disponible);
    }

    // ==================== INFORMACIÓN CALCULADA ====================

    @GetMapping("/{id}/informacion-stock")
    public ResponseEntity<Map<String, Object>> getInformacionStock(@PathVariable("id") Long id) {
        logger.debug("ℹ️ GET /api/articulos-insumo/{}/informacion-stock", id);
        ArticuloInsumoResponseDTO insumo = service.findById(id);

        Map<String, Object> info = Map.of(
                "idArticulo", insumo.getIdArticulo(),
                "denominacion", insumo.getDenominacion(),
                "stockActual", insumo.getStockActual(),
                "stockMaximo", insumo.getStockMaximo(),
                "porcentajeStock", insumo.getPorcentajeStock(),
                "estadoStock", insumo.getEstadoStock(),
                "costoTotalInventario", insumo.getCostoTotalInventario(),
                "margenGanancia", insumo.getMargenGanancia(),
                "cantidadProductosQueLoUsan", insumo.getCantidadProductosQueLoUsan());

        return ResponseEntity.ok(info);
    }
}