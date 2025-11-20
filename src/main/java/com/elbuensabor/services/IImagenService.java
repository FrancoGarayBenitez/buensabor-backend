package com.elbuensabor.services;

import com.elbuensabor.entities.Imagen;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IImagenService {

    // ==================== CRUD BÁSICAS ====================

    Imagen findById(Long id);

    List<Imagen> findAll();

    Imagen save(Imagen imagen);

    void delete(Long id);

    // ==================== OPERACIONES CON ARCHIVOS ====================

    /**
     * Sube archivo Y crea registro en BD asociado a un artículo
     */
    Imagen uploadAndCreateForArticulo(MultipartFile file, String denominacion, Long idArticulo);

    /**
     * Crea registro en BD con URL existente (para uploads sin artículo asociado)
     */
    Imagen createFromExistingUrl(String denominacion, String url);

    /**
     * Crea registro en BD con URL existente y asociado a artículo
     */
    Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo);

    /**
     * Elimina imagen completamente (archivo + registro BD)
     */
    void deleteCompletely(Long idImagen);

    // ==================== BÚSQUEDAS ====================

    List<Imagen> findByArticulo(Long idArticulo);

    boolean existsByUrl(String url);

    // ==================== VALIDACIÓN ====================

    Map<String, Object> validateImageFile(MultipartFile file);
}
