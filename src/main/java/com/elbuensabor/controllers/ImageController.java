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
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private IImagenService imagenService;

    @Value("${app.upload.dir:src/main/resources/static/img/}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // ==================== UPLOAD - GENÉRICO ====================

    /**
     * ✅ POST /api/imagenes/upload/{entityType}
     * Sube imagen sin entidad asociada (ej: Cliente sin ID aún, Promoción sin ID)
     */
    @PostMapping("/upload/{entityType}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String entityType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen") String denominacion) {
        return handleUpload(entityType, null, file, denominacion);
    }

    /**
     * ✅ POST /api/imagenes/upload/{entityType}/{entityId}
     * Sube imagen y la asocia a una entidad (Insumo, Manufacturado)
     */
    @PostMapping("/upload/{entityType}/{entityId}")
    public ResponseEntity<?> uploadImageWithEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen") String denominacion) {
        return handleUpload(entityType, entityId, file, denominacion);
    }

    // ==================== UPDATE ====================

    /**
     * ✅ PUT /api/imagenes/{idImagen}
     * Actualiza imagen: elimina anterior + sube nueva
     */
    @PutMapping("/{idImagen}")
    public ResponseEntity<?> updateImagen(
            @PathVariable Long idImagen,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "denominacion", defaultValue = "Imagen") String denominacion) {
        try {
            Imagen imagenActual = imagenService.findById(idImagen);

            // Eliminar archivo anterior
            deletePhysicalFile(imagenActual.getUrl());

            // Guardar nuevo archivo
            String newUrl = savePhysicalFile(file);

            // Actualizar registro
            imagenActual.setUrl(newUrl);
            imagenActual.setDenominacion(denominacion);
            imagenService.save(imagenActual);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagenActual.getIdImagen(),
                    "denominacion", imagenActual.getDenominacion(),
                    "url", imagenActual.getUrl()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== DELETE ====================

    /**
     * ✅ DELETE /api/imagenes/{idImagen}
     * Elimina imagen completamente (archivo + BD)
     */
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<?> deleteImagen(@PathVariable Long idImagen) {
        try {
            imagenService.deleteCompletely(idImagen);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagen eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== GET ====================

    /**
     * ✅ GET /api/imagenes/{entityType}/{entityId}
     * Obtiene todas las imágenes de una entidad
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<?> getImagesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            List<Imagen> imagenes = imagenService.findByArticulo(entityId);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontraron imágenes"));
        }
    }

    /**
     * ✅ GET /api/imagenes/{idImagen}
     * Obtiene una imagen por ID
     */
    @GetMapping("/{idImagen}")
    public ResponseEntity<?> getImagenById(@PathVariable Long idImagen) {
        try {
            Imagen imagen = imagenService.findById(idImagen);
            return ResponseEntity.ok(imagen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Imagen no encontrada"));
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private ResponseEntity<?> handleUpload(String entityType, Long entityId, MultipartFile file, String denominacion) {
        try {
            Map<String, Object> validation = imagenService.validateImageFile(file);
            if (!(Boolean) validation.get("valid")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", validation.get("error")));
            }

            Imagen imagen;

            if (entityId != null && isArticuloType(entityType)) {
                // Sube y asocia a artículo
                imagen = imagenService.uploadAndCreateForArticulo(file, denominacion, entityId);
            } else {
                // Solo sube archivo
                String url = savePhysicalFile(file);
                imagen = imagenService.createFromExistingUrl(denominacion, url);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "idImagen", imagen.getIdImagen(),
                    "denominacion", imagen.getDenominacion(),
                    "url", imagen.getUrl()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private boolean isArticuloType(String entityType) {
        return entityType.equalsIgnoreCase("INSUMO") ||
                entityType.equalsIgnoreCase("MANUFACTURADO");
    }

    private String savePhysicalFile(MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFilename = generateUniqueFilename(fileExtension);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/img/" + uniqueFilename;
    }

    private boolean deletePhysicalFile(String imageUrl) {
        try {
            String filename = extractFilenameFromUrl(imageUrl);
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return !Files.exists(filePath);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error eliminando archivo: " + e.getMessage());
            return false;
        }
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String generateUniqueFilename(String extension) {
        return System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString().substring(0, 8) +
                extension;
    }
}
