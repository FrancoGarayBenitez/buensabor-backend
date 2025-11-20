package com.elbuensabor.services.impl;

import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.repository.IImagenRepository;
import com.elbuensabor.services.IImagenService;
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
    public Imagen uploadAndCreateForArticulo(MultipartFile file, String denominacion, Long idArticulo) {
        Map<String, Object> validation = validateImageFile(file);
        if (!(Boolean) validation.get("valid")) {
            throw new IllegalArgumentException(validation.get("error").toString());
        }

        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        try {
            String filename = uploadPhysicalFile(file);
            String url = baseUrl + "/img/" + filename;

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
    public Imagen createFromExistingUrl(String denominacion, String url) {
        Imagen imagen = new Imagen();
        imagen.setUrl(url);
        imagen.setDenominacion(denominacion);
        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo) {
        Articulo articulo = articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado"));

        Imagen imagen = new Imagen();
        imagen.setDenominacion(denominacion);
        imagen.setUrl(url);
        imagen.setArticulo(articulo);

        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public void deleteCompletely(Long idImagen) {
        Imagen imagen = findById(idImagen);
        deletePhysicalFile(imagen.getUrl());
        imagenRepository.deleteById(idImagen);
    }

    // ==================== BÚSQUEDAS ====================

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> findByArticulo(Long idArticulo) {
        return imagenRepository.findByArticuloIdArticulo(idArticulo);
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

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    private void deletePhysicalFile(String imageUrl) {
        try {
            String filename = extractFilenameFromUrl(imageUrl);
            Path filePath = Paths.get(uploadDir, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Error eliminando archivo: " + e.getMessage());
        }
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp"));
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