package com.elbuensabor.services.impl;

import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IImagenRepository;
import com.elbuensabor.services.IImagenService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImagenServiceImpl implements IImagenService {

    @Autowired
    private IImagenRepository imagenRepository;

    @Autowired
    private IArticuloRepository articuloRepository;

    @Value("${app.upload.dir:src/main/resources/static/img/}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    @Override
    @Transactional
    public Imagen createFromExistingUrl(String denominacion, String url) {
        Imagen imagen = new Imagen();
        imagen.setUrl(url);
        imagen.setDenominacion(denominacion);
        // Sin artículo ni promoción asociados
        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo) {
        // Verificar que el artículo existe
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        Imagen imagen = new Imagen();
        imagen.setDenominacion(denominacion);
        imagen.setUrl(url);
        imagen.setArticulo(articulo);

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen findById(Long id) {
        return imagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen con ID " + id + " no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findAll() {
        return imagenRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Imagen imagen = findById(id);

        // Eliminar archivo físico
        deletePhysicalFile(imagen.getUrl());

        // Eliminar registro de BD
        imagenRepository.deleteById(id);
    }

    // ==================== OPERACIONES CON ARCHIVOS Y BD ====================

    @Override
    @Transactional
    public Imagen uploadAndCreateForArticulo(MultipartFile file, String denominacion, Long idArticulo) {
        // Validar archivo
        Map<String, Object> validation = validateImageFile(file);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException(validation.get("error").toString());
        }

        // Verificar que el artículo existe
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        try {
            // 1. Subir archivo físico
            String filename = uploadPhysicalFile(file);
            String url = baseUrl + "/img/" + filename;

            // 2. Crear registro en BD
            Imagen imagen = new Imagen();
            imagen.setDenominacion(denominacion);
            imagen.setUrl(url);
            imagen.setArticulo(articulo);

            return imagenRepository.save(imagen);

        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Imagen updateImagenArticulo(Long idArticulo, MultipartFile newFile, String denominacion) {
        // Verificar que el artículo existe
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        // Eliminar imágenes anteriores del artículo
        List<Imagen> imagenesAnteriores = imagenRepository.findByArticuloIdArticulo(idArticulo);
        for (Imagen imagenAnterior : imagenesAnteriores) {
            deletePhysicalFile(imagenAnterior.getUrl());
        }
        imagenRepository.deleteByArticuloIdArticulo(idArticulo);

        // Crear nueva imagen
        return uploadAndCreateForArticulo(newFile, denominacion, idArticulo);
    }

    @Override
    @Transactional
    public void deleteCompletely(Long idImagen) {
        System.out.println("🔥 ===== ELIMINACIÓN COMPLETA - ID: " + idImagen + " =====");

        // 1. Buscar la imagen en BD
        Imagen imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen con ID " + idImagen + " no encontrada"));

        System.out.println("🔍 Imagen encontrada: URL = " + imagen.getUrl());

        // 2. Eliminar archivo físico
        boolean archivoEliminado = deletePhysicalFile(imagen.getUrl());

        if (!archivoEliminado) {
            System.err.println("⚠️ Advertencia: No se pudo eliminar archivo físico, pero continuando con BD");
        }

        // 3. Eliminar registro de BD
        imagenRepository.deleteById(idImagen);
        System.out.println("✅ Registro eliminado de BD - ID: " + idImagen);

        System.out.println("🔥 ===== FIN ELIMINACIÓN COMPLETA =====");
    }

    private boolean deletePhysicalFile(String imageUrl) {
        try {
            // 1. Extraer nombre del archivo de la URL
            String filename = extractFilenameFromUrl(imageUrl);

            // 2. Construir ruta ABSOLUTA del archivo (usando la config de Spring)
            Path filePath = Paths.get(uploadDir, filename);

            // 3. Intento de ruta alternativa (solo si la principal falla)
            if (!Files.exists(filePath)) {
                // Si la aplicación corre desde el JAR, la ruta relativa puede ser diferente
                String projectRoot = System.getProperty("user.dir");
                Path alternativePath = Paths.get(projectRoot, "src", "main", "resources", "static", "img", filename);

                if (Files.exists(alternativePath)) {
                    filePath = alternativePath;
                } else {
                    // Si no existe, no hay nada que eliminar
                    return true;
                }
            }

            // 4. Verificar que el archivo existe
            if (!Files.exists(filePath)) {
                return true; // Ya no existe, consideramos éxito
            }

            // 5. Eliminar el archivo
            Files.delete(filePath);

            return !Files.exists(filePath); // Retorna true si ya no existe

        } catch (IOException e) {
            System.err.println("❌ Error IOException eliminando archivo: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error general eliminando archivo: " + e.getMessage());
            return false;
        }
    }

    @PostConstruct
    public void verificarConfiguracionUploadDir() {
        System.out.println("🔧 ===== VERIFICACIÓN CONFIGURACIÓN UPLOAD DIR =====");
        System.out.println("📂 uploadDir configurado: " + uploadDir);

        try {
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("📂 Ruta absoluta uploadDir: " + uploadPath.toAbsolutePath());
            System.out.println("📂 uploadDir existe: " + Files.exists(uploadPath));
            System.out.println("📂 uploadDir es directorio: " + Files.isDirectory(uploadPath));
            System.out.println("📂 uploadDir es escribible: " + Files.isWritable(uploadPath));

            // Listar archivos existentes
            if (Files.exists(uploadPath) && Files.isDirectory(uploadPath)) {
                try (Stream<Path> files = Files.list(uploadPath)) {
                    List<String> fileNames = files
                            .filter(Files::isRegularFile)
                            .map(path -> path.getFileName().toString())
                            .collect(Collectors.toList());
                    System.out.println("📄 Archivos en uploadDir (" + fileNames.size() + "): " + fileNames);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error verificando uploadDir: " + e.getMessage());
        }

        System.out.println("🔧 ===== FIN VERIFICACIÓN =====");
    }

    // ==================== BÚSQUEDAS Y CONSULTAS ====================

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findByArticulo(Long idArticulo) {
        return imagenRepository.findByArticuloIdArticulo(idArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findImagenesHuerfanas() {
        return imagenRepository.findImagenesHuerfanas();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return imagenRepository.existsByUrl(url);
    }

    // ==================== OPERACIONES DE MANTENIMIENTO ====================

    @Override
    @Transactional
    public void limpiarArchivosHuerfanos() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) return;

            // Obtener todas las URLs de imágenes en BD
            List<String> urlsEnBD = imagenRepository.findAll().stream()
                    .map(Imagen::getUrl)
                    .map(this::extractFilenameFromUrl)
                    .toList();

            // Revisar archivos en el directorio
            try (Stream<Path> files = Files.list(uploadPath)) {
                files.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String filename = file.getFileName().toString();
                            if (!urlsEnBD.contains(filename)) {
                                try {
                                    Files.delete(file);
                                    System.out.println("Archivo huérfano eliminado: " + filename);
                                } catch (IOException e) {
                                    System.err.println("Error al eliminar archivo huérfano: " + filename);
                                }
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al limpiar archivos huérfanos: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void limpiarRegistrosHuerfanos() {
        List<Imagen> todasLasImagenes = imagenRepository.findAll();

        for (Imagen imagen : todasLasImagenes) {
            String filename = extractFilenameFromUrl(imagen.getUrl());
            Path filePath = Paths.get(uploadDir, filename);

            if (!Files.exists(filePath)) {
                imagenRepository.delete(imagen);
                System.out.println("Registro huérfano eliminado: " + imagen.getDenominacion());
            }
        }
    }

    @Override
    public Map<String, Object> validateImageFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("valid", false);
            result.put("error", "El archivo está vacío");
            return result;
        }

        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            result.put("valid", false);
            result.put("error", "Tipo de archivo no válido. Solo se permiten: JPG, PNG, GIF, WEBP");
            return result;
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            result.put("valid", false);
            result.put("error", "El archivo es demasiado grande. Máximo 5MB");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    private String uploadPhysicalFile(MultipartFile file) throws IOException {
        // Generar nombre único
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

        return uniqueFilename;
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );
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