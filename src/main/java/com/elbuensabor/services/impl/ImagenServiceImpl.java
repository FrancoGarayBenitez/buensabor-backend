package com.elbuensabor.services.impl;

import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Promocion;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IImagenRepository;
import com.elbuensabor.repository.IPromocionRepository;
import com.elbuensabor.services.IImagenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImagenServiceImpl implements IImagenService {

    private static final Logger logger = LoggerFactory.getLogger(ImagenServiceImpl.class);

    @Autowired
    private IImagenRepository imagenRepository;

    @Autowired
    private IArticuloRepository articuloRepository;

    @Autowired
    private IPromocionRepository promocionRepository;

    @Value("${app.upload.dir:src/main/resources/static/img/}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.public.img-path:/img/}")
    private String publicImgPath;

    // ==================== CRUD BÁSICAS ====================

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
    public Imagen save(Imagen imagen) {
        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        imagenRepository.deleteById(id);
    }

    // ==================== OPERACIONES CON ARCHIVOS ====================

    @Override
    @Transactional
    public String uploadPhysicalFileAndGetUrl(MultipartFile file) {
        Map<String, Object> validation = validateImageFile(file);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException(validation.get("error").toString());
        }

        try {
            String filename = uploadPhysicalFile(file);
            // Construye y devuelve la URL pública completa
            String url = baseUrl + publicImgPath + filename;
            logger.info("✅ Archivo subido: {} → {}", filename, url);
            return url;
        } catch (IOException e) {
            logger.error("❌ Error al subir archivo: {}", e.getMessage());
            throw new RuntimeException("Error al subir archivo: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVO: Elimina solo el archivo físico del disco.
     */
    @Override
    public void deletePhysicalFile(String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            // Medida de seguridad para evitar ataques de Path Traversal
            if (!filePath.startsWith(uploadPath)) {
                throw new SecurityException("Acceso a ruta de archivo no permitido: " + filename);
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("🗑️ Archivo físico eliminado: {}", filename);
            } else {
                logger.warn("⚠️ Se intentó eliminar un archivo físico que no existe: {}", filename);
            }
        } catch (IOException e) {
            logger.error("❌ No se pudo eliminar el archivo físico {}: {}", filename, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar el archivo físico: " + filename, e);
        }
    }

    @Override
    @Transactional
    public Imagen createFromExistingUrl(String denominacion, String url) {
        Imagen imagen = new Imagen();
        imagen.setUrl(url);
        imagen.setDenominacion(denominacion);
        Imagen saved = imagenRepository.save(imagen);
        logger.info("✅ Imagen creada sin asociación: {} (ID: {})", denominacion, saved.getIdImagen());
        return saved;
    }

    @Override
    @Transactional
    public Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo) {
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        Imagen saved = createAndSaveImage(denominacion, url, articulo, null);
        logger.info("✅ Imagen creada y asociada a Artículo {}: {} (ID: {})",
                idArticulo, denominacion, saved.getIdImagen());
        return saved;
    }

    /**
     * ✅ NUEVO: Crea imagen asociada a una Promoción
     */
    @Override
    @Transactional
    public Imagen createFromExistingUrlPromocion(String denominacion, String url, Long idPromocion) {
        Promocion promocion = promocionRepository.findById(idPromocion)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción con ID " + idPromocion + " no encontrada"));

        Imagen saved = createAndSaveImage(denominacion, url, null, promocion);
        logger.info("✅ Imagen creada y asociada a Promoción {}: {} (ID: {})",
                idPromocion, denominacion, saved.getIdImagen());
        return saved;
    }

    @Override
    @Transactional
    public void deleteCompletely(Long id) {
        // 1. Buscar la entidad de la imagen en la base de datos.
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la imagen con el ID: " + id));

        // 2. Obtener la URL de la imagen.
        String imageUrl = imagen.getUrl();

        // 3. Extraer solo el nombre del archivo de la URL.
        String filename = null;
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                // Usar la clase URI para parsear la URL y obtener la parte de la ruta.
                // Luego, usar Paths para obtener el nombre del archivo final de esa ruta.
                filename = Paths.get(new URI(imageUrl).getPath()).getFileName().toString();
            } catch (Exception e) {
                logger.error("No se pudo parsear la URL para extraer el nombre del archivo: {}", imageUrl, e);

            }
        }

        // 4. Eliminar el registro de la base de datos.
        imagenRepository.delete(imagen);
        logger.info("🗑️ Registro de imagen eliminado de la BD (ID: {})", id);

        // 5. Si se obtuvo un nombre de archivo, intentar eliminar el archivo físico.
        if (filename != null) {
            deletePhysicalFile(filename);
        }
    }

    // ==================== BÚSQUEDAS ====================

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findByArticulo(Long idArticulo) {
        return imagenRepository.findByArticuloIdArticulo(idArticulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findByPromocion(Long idPromocion) {
        return imagenRepository.findByPromocionIdPromocion(idPromocion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return imagenRepository.existsByUrl(url);
    }

    // ==================== VALIDACIÓN ====================

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
            result.put("error", "Tipo de archivo no válido. Solo: JPG, PNG, GIF, WEBP");
            return result;
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            result.put("valid", false);
            result.put("error", "El archivo es demasiado grande. Máximo 5MB");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String uploadPhysicalFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);

        Path uploadPath = Paths.get(uploadDir).normalize().toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("Ruta de archivo inválida");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    /**
     * MÉTODO AUXILIAR PRIVADO: Unifica la creación de la entidad Imagen.
     */
    private Imagen createAndSaveImage(String denominacion, String url, Articulo articulo, Promocion promocion) {
        Imagen imagen = new Imagen();
        imagen.setDenominacion(denominacion);
        imagen.setUrl(url);
        if (articulo != null) {
            imagen.setArticulo(articulo);
        }
        if (promocion != null) {
            imagen.setPromocion(promocion);
        }
        return imagenRepository.save(imagen);
    }

    private boolean isValidImageType(String contentType) {
        if (contentType == null)
            return false;
        return contentType.toLowerCase().startsWith("image/");
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

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}