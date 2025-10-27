package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.services.IArticuloManufacturadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.elbuensabor.entities.Imagen;
import com.elbuensabor.services.IImagenService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/articulos-manufacturados")

public class ArticuloManufacturadoController {

    private final IArticuloManufacturadoService articuloManufacturadoService;

    @Autowired
    public ArticuloManufacturadoController(IArticuloManufacturadoService articuloManufacturadoService) {
        this.articuloManufacturadoService = articuloManufacturadoService;
    }

    @Autowired
    private IImagenService imagenService;


    // ==================== OPERACIONES CRUD BÁSICAS ====================

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getAllArticulosManufacturados() {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findAll();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> getArticuloManufacturadoById(@PathVariable Long id) {
        ArticuloManufacturadoResponseDTO articulo = articuloManufacturadoService.findById(id);
        return ResponseEntity.ok(articulo);
    }

    @PostMapping
    public ResponseEntity<ArticuloManufacturadoResponseDTO> createArticuloManufacturado(@Valid @RequestBody ArticuloManufacturadoRequestDTO articuloRequestDTO) {
        ArticuloManufacturadoResponseDTO articuloCreado = articuloManufacturadoService.createManufacturado(articuloRequestDTO);
        return new ResponseEntity<>(articuloCreado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> updateArticuloManufacturado(
            @PathVariable Long id,
            @Valid @RequestBody ArticuloManufacturadoRequestDTO articuloRequestDTO) {
        ArticuloManufacturadoResponseDTO articuloActualizado = articuloManufacturadoService.updateManufacturado(id, articuloRequestDTO);
        return ResponseEntity.ok(articuloActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticuloManufacturado(@PathVariable Long id) {
        articuloManufacturadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}/desactivar")
    public ResponseEntity<Void> bajaLogica(@PathVariable Long id) {
        articuloManufacturadoService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> altaLogica(@PathVariable Long id) {
        articuloManufacturadoService.altaLogica(id);
        return ResponseEntity.noContent().build();
    }
    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosByCategoria(@PathVariable Long idCategoria) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findByCategoria(idCategoria);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/tiempo-maximo/{tiempoMaximo}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosByTiempoMaximo(@PathVariable Integer tiempoMaximo) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findByTiempoMaximo(tiempoMaximo);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/ingrediente/{idInsumo}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosByIngrediente(@PathVariable Long idInsumo) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findByIngrediente(idInsumo);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/precio-rango")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosByPrecioRango(
            @RequestParam Double precioMin,
            @RequestParam Double precioMax) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findByPrecioRango(precioMin, precioMax);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/minimo-ingredientes/{cantidadMinima}")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosByMinimoIngredientes(@PathVariable Integer cantidadMinima) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findByMinimoIngredientes(cantidadMinima);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> searchArticulos(@RequestParam String denominacion) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.searchByDenominacion(denominacion);
        return ResponseEntity.ok(articulos);
    }

    // ==================== CONTROL DE PREPARABILIDAD Y STOCK ====================

    @GetMapping("/preparables")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosPreparables() {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findPreparables();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/no-preparables")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getArticulosNoPreparables() {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.findNoPreparables();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}/maximo-preparable")
    public ResponseEntity<Integer> getMaximoPreparable(@PathVariable Long id) {
        Integer maximo = articuloManufacturadoService.calcularMaximoPreparable(id);
        return ResponseEntity.ok(maximo);
    }

    @GetMapping("/{id}/puede-prepararse")
    public ResponseEntity<Boolean> puedePrepararse(@PathVariable Long id, @RequestParam Integer cantidad) {
        Boolean puede = articuloManufacturadoService.puedePrepararse(id, cantidad);
        return ResponseEntity.ok(puede);
    }

    // ==================== CÁLCULOS DE COSTOS Y PRECIOS ====================

    @GetMapping("/{id}/costo-total")
    public ResponseEntity<Double> getCostoTotal(@PathVariable Long id) {
        Double costo = articuloManufacturadoService.calcularCostoTotal(id);
        return ResponseEntity.ok(costo);
    }

    @GetMapping("/{id}/margen-ganancia")
    public ResponseEntity<Double> getMargenGanancia(@PathVariable Long id) {
        Double margen = articuloManufacturadoService.calcularMargenGanancia(id);
        return ResponseEntity.ok(margen);
    }

    @GetMapping("/{id}/precio-sugerido")
    public ResponseEntity<Double> getPrecioSugerido(@PathVariable Long id, @RequestParam Double margen) {
        Double precioSugerido = articuloManufacturadoService.calcularPrecioSugerido(id, margen);
        return ResponseEntity.ok(precioSugerido);
    }

    // ==================== GESTIÓN DE RECETAS (DETALLES) ====================

    @PostMapping("/{id}/ingredientes")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> agregarIngrediente(
            @PathVariable Long id,
            @RequestParam Long idInsumo,
            @RequestParam Double cantidad) {
        ArticuloManufacturadoResponseDTO articuloActualizado = articuloManufacturadoService.agregarIngrediente(id, idInsumo, cantidad);
        return ResponseEntity.ok(articuloActualizado);
    }

    @PutMapping("/{id}/ingredientes/{idDetalle}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> actualizarIngrediente(
            @PathVariable Long id,
            @PathVariable Long idDetalle,
            @RequestParam Double nuevaCantidad) {
        ArticuloManufacturadoResponseDTO articuloActualizado = articuloManufacturadoService.actualizarIngrediente(id, idDetalle, nuevaCantidad);
        return ResponseEntity.ok(articuloActualizado);
    }

    @DeleteMapping("/{id}/ingredientes/{idDetalle}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> eliminarIngrediente(
            @PathVariable Long id,
            @PathVariable Long idDetalle) {
        ArticuloManufacturadoResponseDTO articuloActualizado = articuloManufacturadoService.eliminarIngrediente(id, idDetalle);
        return ResponseEntity.ok(articuloActualizado);
    }

    // ==================== SIMULACIONES PARA PRODUCCIÓN ====================

    @GetMapping("/simulacion-produccion")
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> simularProduccion(@RequestParam Integer cantidadAProducir) {
        List<ArticuloManufacturadoResponseDTO> articulos = articuloManufacturadoService.simularProduccion(cantidadAProducir);
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}/verificar-stock-produccion")
    public ResponseEntity<Boolean> verificarStockParaProduccion(@PathVariable Long id, @RequestParam Integer cantidadAProducir) {
        Boolean stockSuficiente = articuloManufacturadoService.verificarStockParaProduccion(id, cantidadAProducir);
        return ResponseEntity.ok(stockSuficiente);
    }

    // ==================== ENDPOINTS DE VALIDACIÓN E INFORMACIÓN ====================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByDenominacion(@RequestParam String denominacion) {
        boolean exists = articuloManufacturadoService.existsByDenominacion(denominacion);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/tiene-ingredientes")
    public ResponseEntity<Boolean> tieneIngredientes(@PathVariable Long id) {
        boolean tieneIngredientes = articuloManufacturadoService.tieneIngredientes(id);
        return ResponseEntity.ok(tieneIngredientes);
    }

    @GetMapping("/{id}/usado-en-pedidos")
    public ResponseEntity<Boolean> seUsaEnPedidos(@PathVariable Long id) {
        boolean seUsa = articuloManufacturadoService.seUsaEnPedidos(id);
        return ResponseEntity.ok(seUsa);
    }

    // ==================== ENDPOINTS PARA MANEJO DE IMÁGENES ====================

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> uploadImagenManufacturado(
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
    public ResponseEntity<?> updateImagenManufacturado(
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
    public ResponseEntity<?> deleteImagenesManufacturado(@PathVariable Long id) {
        try {
            List<Imagen> imagenes = imagenService.findByArticulo(id);
            for (Imagen imagen : imagenes) {
                imagenService.deleteCompletely(imagen.getIdImagen());
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Todas las imágenes eliminadas correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar imágenes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/imagenes")
    public ResponseEntity<List<Imagen>> getImagenesManufacturado(@PathVariable Long id) {
        List<Imagen> imagenes = imagenService.findByArticulo(id);
        return ResponseEntity.ok(imagenes);
    }
}