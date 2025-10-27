package com.elbuensabor.controllers;

import com.elbuensabor.entities.Imagen;
import com.elbuensabor.services.IImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
public class ImageController {

    @Autowired
    private IImagenService imagenService;

    @Value("${app.upload.dir:src/main/resources/static/img/}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // ==================== OPERACIONES INTEGRADAS (ARCHIVO + BD) ====================

    /**
     * Sube imagen y la asocia directamente a un artículo
     */
    @PostMapping("/upload-for-articulo/{idArticulo}")
    public ResponseEntity<?> uploadImageForArticulo(
            @PathVariable Long idArticulo,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen del producto") String denominacion) {
        try {
            Imagen imagen = imagenService.uploadAndCreateForArticulo(file, denominacion, idArticulo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagen.getIdImagen(),
                    "denominacion", imagen.getDenominacion(),
                    "url", imagen.getUrl(),
                    "message", "Imagen subida y asociada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir imagen: " + e.getMessage()));
        }
    }

    /**
     * Actualiza la imagen de un artículo (elimina la anterior y sube nueva)
     */
    @PutMapping("/update-articulo/{idArticulo}")
    public ResponseEntity<?> updateImagenArticulo(
            @PathVariable Long idArticulo,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen del producto") String denominacion) {
        try {
            Imagen imagen = imagenService.updateImagenArticulo(idArticulo, file, denominacion);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagen.getIdImagen(),
                    "denominacion", imagen.getDenominacion(),
                    "url", imagen.getUrl(),
                    "message", "Imagen actualizada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar imagen: " + e.getMessage()));
        }
    }

    /**
     * Elimina imagen completamente (archivo + registro BD)
     */
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<?> deleteImagenCompletely(@PathVariable Long idImagen) {
        try {
            imagenService.deleteCompletely(idImagen);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagen eliminada completamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar imagen: " + e.getMessage()));
        }
    }

    // ==================== OPERACIONES SOLO DE ARCHIVOS (LEGACY) ====================

    /**
     * Sube archivo sin crear registro en BD (para casos especiales)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validar archivo
            Map<String, Object> validation = imagenService.validateImageFile(file);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of("error", validation.get("error")));
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(fileExtension);

            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Guardar archivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generar URL
            String imageUrl = baseUrl + "/img/" + uniqueFilename;

            // Crear respuesta con información de la imagen
            Map<String, Object> response = Map.of(
                    "success", true,
                    "filename", uniqueFilename,
                    "url", imageUrl,
                    "originalName", originalFilename,
                    "size", file.getSize(),
                    "contentType", file.getContentType(),
                    "warning", "Archivo subido sin registro en BD. Use /upload-for-articulo para asociar a artículo"
            );

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar el archivo: " + e.getMessage()));
        }
    }

    /**
     * Elimina solo archivo físico (sin tocar BD)
     */
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteImageFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Archivo eliminado correctamente",
                        "warning", "Solo se eliminó el archivo. Registro en BD no afectado"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar el archivo: " + e.getMessage()));
        }
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtener todas las imágenes de un artículo
     */
    @GetMapping("/articulo/{idArticulo}")
    public ResponseEntity<List<Imagen>> getImagenesByArticulo(@PathVariable Long idArticulo) {
        List<Imagen> imagenes = imagenService.findByArticulo(idArticulo);
        return ResponseEntity.ok(imagenes);
    }

    /**
     * Obtener imagen por ID
     */
    @GetMapping("/{idImagen}")
    public ResponseEntity<Imagen> getImagenById(@PathVariable Long idImagen) {
        Imagen imagen = imagenService.findById(idImagen);
        return ResponseEntity.ok(imagen);
    }

    /**
     * Validar si un archivo existe
     */
    @GetMapping("/validate/{filename}")
    public ResponseEntity<?> validateImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                String url = baseUrl + "/img/" + filename;
                boolean existsInDB = imagenService.existsByUrl(url);

                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "url", url,
                        "filename", filename,
                        "hasDBRecord", existsInDB
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al validar la imagen: " + e.getMessage()));
        }
    }

    // ==================== OPERACIONES DE MANTENIMIENTO ====================

    /**
     * Limpiar archivos huérfanos (archivos sin registro en BD)
     */
    @PostMapping("/maintenance/clean-orphan-files")
    public ResponseEntity<?> cleanOrphanFiles() {
        try {
            imagenService.limpiarArchivosHuerfanos();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Archivos huérfanos eliminados"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al limpiar archivos: " + e.getMessage()));
        }
    }

    /**
     * Limpiar registros huérfanos (registros sin archivo)
     */
    @PostMapping("/maintenance/clean-orphan-records")
    public ResponseEntity<?> cleanOrphanRecords() {
        try {
            imagenService.limpiarRegistrosHuerfanos();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registros huérfanos eliminados"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al limpiar registros: " + e.getMessage()));
        }
    }

    /**
     * Obtener imágenes huérfanas
     */
    @GetMapping("/orphans")
    public ResponseEntity<List<Imagen>> getOrphanImages() {
        List<Imagen> orphans = imagenService.findImagenesHuerfanas();
        return ResponseEntity.ok(orphans);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String generateUniqueFilename(String extension) {
        return System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 8) +
                extension;
    }
}
