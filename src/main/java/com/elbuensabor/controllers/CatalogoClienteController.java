package com.elbuensabor.controllers;

import com.elbuensabor.dto.response.cliente.CatalogoArticuloDTO;
import com.elbuensabor.dto.response.cliente.DetalleArticuloDTO;
import com.elbuensabor.dto.response.cliente.PromocionClienteDTO;
import com.elbuensabor.services.ICatalogoClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para el catálogo de productos visible para el cliente.
 * ✅ Endpoints públicos (sin autenticación requerida para navegar el catálogo)
 * ✅ Solo muestra información relevante para el cliente
 * ✅ Oculta información administrativa (costos, márgenes, etc.)
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoClienteController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoClienteController.class);
    private final ICatalogoClienteService catalogoService;

    @Autowired
    public CatalogoClienteController(ICatalogoClienteService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // ==================== ARTÍCULOS ====================

    /**
     * Obtiene todos los artículos manufacturados disponibles para el cliente.
     * Solo muestra artículos no eliminados y con stock suficiente.
     * 
     * GET /api/catalogo/articulos
     */
    @GetMapping("/articulos")
    public ResponseEntity<List<CatalogoArticuloDTO>> getArticulosDisponibles() {
        logger.debug("🛍️ GET /api/catalogo/articulos - Cliente consultando catálogo");
        List<CatalogoArticuloDTO> articulos = catalogoService.obtenerArticulosDisponibles();
        logger.info("✅ Devolviendo {} artículos disponibles", articulos.size());
        return ResponseEntity.ok(articulos);
    }

    /**
     * Obtiene el detalle completo de un artículo específico.
     * 
     * GET /api/catalogo/articulos/{idArticulo}
     */
    @GetMapping("/articulos/{idArticulo}")
    public ResponseEntity<DetalleArticuloDTO> getDetalleArticulo(@PathVariable("idArticulo") Long idArticulo) {
        logger.debug("🛍️ GET /api/catalogo/articulos/{} - Cliente viendo detalle", idArticulo);
        DetalleArticuloDTO detalle = catalogoService.obtenerDetalleArticulo(idArticulo);
        logger.info("✅ Detalle del artículo '{}' devuelto", detalle.getDenominacion());
        return ResponseEntity.ok(detalle);
    }

    /**
     * Filtra artículos por categoría.
     * 
     * GET /api/catalogo/articulos/categoria/{idCategoria}
     */
    @GetMapping("/articulos/categoria/{idCategoria}")
    public ResponseEntity<List<CatalogoArticuloDTO>> getArticulosPorCategoria(
            @PathVariable("idCategoria") Long idCategoria) {
        logger.debug("🔍 GET /api/catalogo/articulos/categoria/{}", idCategoria);
        List<CatalogoArticuloDTO> articulos = catalogoService.obtenerArticulosPorCategoria(idCategoria);
        logger.info("✅ Encontrados {} artículos en la categoría {}", articulos.size(), idCategoria);
        return ResponseEntity.ok(articulos);
    }

    /**
     * Busca artículos por denominación.
     * 
     * GET /api/catalogo/articulos/buscar?query=hamburguesa
     */
    @GetMapping("/articulos/buscar")
    public ResponseEntity<List<CatalogoArticuloDTO>> buscarArticulos(
            @RequestParam(name = "query", required = false, defaultValue = "") String query) {
        logger.debug("🔍 GET /api/catalogo/articulos/buscar?query={}", query);
        List<CatalogoArticuloDTO> articulos = catalogoService.buscarArticulos(query);
        logger.info("✅ Búsqueda '{}' devolvió {} resultados", query, articulos.size());
        return ResponseEntity.ok(articulos);
    }

    /**
     * Obtiene artículos en promoción.
     * Endpoint destacado para la sección "Ofertas" del cliente.
     * 
     * GET /api/catalogo/articulos/promociones
     */
    @GetMapping("/articulos/promociones")
    public ResponseEntity<List<CatalogoArticuloDTO>> getArticulosEnPromocion() {
        logger.debug("🎁 GET /api/catalogo/articulos/promociones - Artículos en oferta");
        List<CatalogoArticuloDTO> articulos = catalogoService.obtenerArticulosEnPromocion();
        logger.info("✅ Encontrados {} artículos en promoción", articulos.size());
        return ResponseEntity.ok(articulos);
    }

    // ==================== PROMOCIONES ====================

    /**
     * Obtiene todas las promociones vigentes.
     * Muestra solo promociones activas y dentro del periodo de validez.
     * 
     * GET /api/catalogo/promociones
     */
    @GetMapping("/promociones")
    public ResponseEntity<List<PromocionClienteDTO>> getPromocionesVigentes() {
        logger.debug("🎁 GET /api/catalogo/promociones - Cliente consultando promociones");
        List<PromocionClienteDTO> promociones = catalogoService.obtenerPromocionesVigentes();
        logger.info("✅ Devolviendo {} promociones vigentes", promociones.size());
        return ResponseEntity.ok(promociones);
    }

    /**
     * Obtiene el detalle de una promoción específica.
     * 
     * GET /api/catalogo/promociones/{idPromocion}
     */
    @GetMapping("/promociones/{idPromocion}")
    public ResponseEntity<PromocionClienteDTO> getDetallePromocion(@PathVariable("idPromocion") Long idPromocion) {
        logger.debug("🎁 GET /api/catalogo/promociones/{} - Cliente viendo detalle de promoción", idPromocion);
        PromocionClienteDTO promocion = catalogoService.obtenerDetallePromocion(idPromocion);
        logger.info("✅ Detalle de promoción '{}' devuelto", promocion.getNombre());
        return ResponseEntity.ok(promocion);
    }
}