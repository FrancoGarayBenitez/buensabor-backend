package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.services.IArticuloInsumoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- 1. IMPORTA LA ANOTACIÓN
import org.springframework.web.bind.annotation.*;

import com.elbuensabor.entities.Imagen;
import com.elbuensabor.services.IImagenService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;


import java.util.List;

@RestController
@RequestMapping("/api/articulos-insumo")
public class ArticuloInsumoController {

    private final IArticuloInsumoService articuloInsumoService;

    @Autowired
    public ArticuloInsumoController(IArticuloInsumoService articuloInsumoService) {
        this.articuloInsumoService = articuloInsumoService;
    }

    @Autowired
    private IImagenService imagenService;


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
    @PreAuthorize("hasAnyRole('ADMIN', 'COCINERO')") // <-- 2. REGLA AÑADIDA
    public ResponseEntity<ArticuloInsumoResponseDTO> createArticuloInsumo(@Valid @RequestBody ArticuloInsumoRequestDTO articuloRequestDTO) {
        ArticuloInsumoResponseDTO articuloCreado = articuloInsumoService.createInsumo(articuloRequestDTO);
        return new ResponseEntity<>(articuloCreado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COCINERO')") // <-- 3. REGLA AÑADIDA
    public ResponseEntity<ArticuloInsumoResponseDTO> updateArticuloInsumo(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloInsumoRequestDTO articuloRequestDTO) {
        ArticuloInsumoResponseDTO articuloActualizado = articuloInsumoService.updateInsumo(id, articuloRequestDTO);
        return ResponseEntity.ok(articuloActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- 4. REGLA AÑADIDA
    public ResponseEntity<Void> deleteArticuloInsumo(@PathVariable Long id) {
        articuloInsumoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getArticulosByCategoria(@PathVariable Long idCategoria) {
        List<ArticuloInsumoResponseDTO> articulos = articuloInsumoService.findByCategoria(idCategoria);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/unidad-medida/{idUnidadMedida}")
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getArticulosByUnidadMedida(@PathVariable Long idUnidadMedida) {
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

    // ==================== OPERACIONES DE STOCK ====================

    @PutMapping("/{id}/stock")
    public ResponseEntity<ArticuloInsumoResponseDTO> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer nuevoStock) {
        ArticuloInsumoResponseDTO articuloActualizado = articuloInsumoService.actualizarStock(id, nuevoStock);
        return ResponseEntity.ok(articuloActualizado);
    }

    @PutMapping("/{id}/stock/incrementar")
    public ResponseEntity<ArticuloInsumoResponseDTO> incrementarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        ArticuloInsumoResponseDTO articuloActualizado = articuloInsumoService.incrementarStock(id, cantidad);
        return ResponseEntity.ok(articuloActualizado);
    }

    @PutMapping("/{id}/stock/decrementar")
    public ResponseEntity<ArticuloInsumoResponseDTO> decrementarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        ArticuloInsumoResponseDTO articuloActualizado = articuloInsumoService.decrementarStock(id, cantidad);
        return ResponseEntity.ok(articuloActualizado);
    }

    // ==================== ENDPOINTS DE VALIDACIÓN E INFORMACIÓN ====================

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

    // ==================== ENDPOINTS PARA MANEJO DE IMÁGENES ====================

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> uploadImagenInsumo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen del producto") String denominacion) {
        try {
            Imagen imagen = imagenService.uploadAndCreateForArticulo(file, denominacion, id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagen.getIdImagen(),
                    "url", imagen.getUrl(),
                    "denominacion", imagen.getDenominacion()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir imagen: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/imagen")
    public ResponseEntity<?> updateImagenInsumo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen del producto") String denominacion) {
        try {
            Imagen imagen = imagenService.updateImagenArticulo(id, file, denominacion);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagen.getIdImagen(),
                    "url", imagen.getUrl(),
                    "denominacion", imagen.getDenominacion(),
                    "message", "Imagen actualizada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar imagen: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/imagenes")
    public ResponseEntity<?> deleteImagenesInsumo(@PathVariable Long id) {
        try {
            System.out.println("🗑️ Request para eliminar imágenes del artículo: " + id);

            // 1. Buscar todas las imágenes del artículo
            List<Imagen> imagenes = imagenService.findByArticulo(id);
            System.out.println("🔍 Encontradas " + imagenes.size() + " imágenes para eliminar");

            // 2. Eliminar cada imagen COMPLETAMENTE (archivo + BD)
            for (Imagen imagen : imagenes) {
                System.out.println("🔥 Eliminando imagen ID: " + imagen.getIdImagen() + " URL: " + imagen.getUrl());
                imagenService.deleteCompletely(imagen.getIdImagen());
            }

            System.out.println("✅ Todas las imágenes eliminadas exitosamente");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Todas las imágenes eliminadas correctamente",
                    "imagenesEliminadas", imagenes.size()
            ));
        } catch (Exception e) {
            System.err.println("❌ Error eliminando imágenes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar imágenes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/imagenes")
    public ResponseEntity<List<Imagen>> getImagenesInsumo(@PathVariable Long id) {
        List<Imagen> imagenes = imagenService.findByArticulo(id);
        return ResponseEntity.ok(imagenes);
    }
}