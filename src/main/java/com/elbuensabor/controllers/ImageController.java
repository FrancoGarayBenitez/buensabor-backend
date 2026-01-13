package com.elbuensabor.controllers;

import com.elbuensabor.services.IImagenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private IImagenService imagenService;

    /**
     * ✅ ÚNICO ENDPOINT PARA SUBIR IMÁGENES (sin asociación inmediata)
     * POST /api/imagenes/upload/{entityType}
     * 
     * Funciona para CREACIÓN y EDICIÓN de:
     * - ArticuloInsumo (venta directa)
     * - ArticuloManufacturado
     * - Promocion
     * 
     * La asociación se hace al guardar el formulario completo.
     */
    @PostMapping("/upload/{entityType}")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("denominacion") String denominacion,
            @PathVariable String entityType) {
        try {
            String imageUrl = imagenService.uploadPhysicalFileAndGetUrl(file);

            // ✅ CORRECCIÓN: Usar HashMap en lugar de Map.of() para permitir valores nulos
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("idImagen", null); // HashMap sí permite null
            response.put("url", imageUrl);
            response.put("denominacion", denominacion);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Loguear el error para tener más detalles en el backend
            logger.error("❌ Error en uploadImage para tipo {}: {}", entityType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Error interno del servidor"));
        }
    }

    /**
     * ✅ NUEVO: Endpoint para eliminar un archivo físico que fue subido pero no
     * asociado.
     * Se usa para limpiar archivos huérfanos si el usuario cancela la operación.
     * DELETE /api/imagenes/upload
     */
    @DeleteMapping("/upload")
    public ResponseEntity<?> deletePhysicalImage(@RequestBody Map<String, String> payload) {
        String filename = payload.get("filename");
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "El nombre del archivo es requerido."));
        }

        try {
            imagenService.deletePhysicalFile(filename);
            logger.info("🧹 Archivo físico eliminado por solicitud de limpieza: {}", filename);
            return ResponseEntity.ok(Map.of("success", true, "message", "Archivo físico eliminado correctamente."));
        } catch (Exception e) {
            logger.error("❌ Error al intentar eliminar el archivo físico {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar una imagen (registro en BD + archivo físico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        try {
            // CORRECCIÓN: Usar deleteCompletely para borrar el archivo físico y el registro
            // en BD
            imagenService.deleteCompletely(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Imagen eliminada completamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
