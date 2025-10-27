package com.elbuensabor.services;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.entities.Imagen;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IImagenService {

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    /**
     * Solo crea registro en BD con URL existente, diseñado para entidades como Cliente.
     * Requiere denominacion, ya que es NOT NULL en la entidad Imagen.
     */
    Imagen createFromExistingUrl(String denominacion, String url);

    /**
     * Solo crea registro en BD con URL existente, asociado a un Artículo.
     */
    Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo);

    Imagen findById(Long id);
    List<Imagen> findAll();
    void delete(Long id);

    // ==================== OPERACIONES CON ARCHIVOS Y BD ====================

    /**
     * Sube archivo Y crea registro en BD asociado a un artículo
     */
    Imagen uploadAndCreateForArticulo(MultipartFile file, String denominacion, Long idArticulo);

    /**
     * Actualiza imagen de un artículo (elimina la anterior y crea nueva)
     */
    Imagen updateImagenArticulo(Long idArticulo, MultipartFile newFile, String denominacion);

    /**
     * Elimina imagen tanto del filesystem como de BD
     */
    void deleteCompletely(Long idImagen);

    // ==================== BÚSQUEDAS Y CONSULTAS ====================

    List<Imagen> findByArticulo(Long idArticulo);
    List<Imagen> findImagenesHuerfanas();
    boolean existsByUrl(String url);

    // ==================== OPERACIONES DE MANTENIMIENTO ====================

    /**
     * Limpia archivos huérfanos (archivos sin registro en BD)
     */
    void limpiarArchivosHuerfanos();

    /**
     * Limpia registros huérfanos (registros sin archivo)
     */
    void limpiarRegistrosHuerfanos();

    /**
     * Validación de archivo
     */
    Map<String, Object> validateImageFile(MultipartFile file);
}
