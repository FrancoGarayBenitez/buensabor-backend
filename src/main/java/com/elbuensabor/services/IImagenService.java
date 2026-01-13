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
     * ✅ ÚNICO MÉTODO: Sube archivo físicamente y devuelve su URL pública.
     * NO crea registro en BD. La asociación ocurre al guardar el formulario
     * completo.
     * 
     * Funciona para:
     * - Creación de ArticuloInsumo (venta directa)
     * - Creación de ArticuloManufacturado
     * - Creación de Promocion
     * - Edición de cualquiera de las anteriores
     */
    String uploadPhysicalFileAndGetUrl(MultipartFile file);

    /**
     * ✅ NUEVO: Elimina solo el archivo físico del disco.
     * Se usa para limpiar archivos subidos que no se asociaron a ninguna entidad.
     */
    void deletePhysicalFile(String filename);

    /**
     * ✅ NUEVO: Crea registro en BD con URL existente (sin asociación a
     * artículo/promoción).
     * Se usa cuando el DTO ya tiene la URL y necesita crear el registro.
     */
    Imagen createFromExistingUrl(String denominacion, String url);

    /**
     * ✅ NUEVO: Crea registro en BD con URL existente y lo asocia a un artículo.
     * Se usa en el método manejarImagenes() de los servicios.
     */
    Imagen createFromExistingUrl(String denominacion, String url, Long idArticulo);

    /**
     * ✅ NUEVO: Crea registro en BD con URL existente y lo asocia a una promoción.
     * Se usa en el método manejarImagenes() de PromocionServiceImpl.
     */
    Imagen createFromExistingUrlPromocion(String denominacion, String url, Long idPromocion);

    /**
     * Elimina imagen completamente (archivo + registro BD)
     */
    void deleteCompletely(Long idImagen);

    // ==================== BÚSQUEDAS ====================

    List<Imagen> findByArticulo(Long idArticulo);

    List<Imagen> findByPromocion(Long idPromocion);

    boolean existsByUrl(String url);

    // ==================== VALIDACIÓN ====================

    Map<String, Object> validateImageFile(MultipartFile file);
}
