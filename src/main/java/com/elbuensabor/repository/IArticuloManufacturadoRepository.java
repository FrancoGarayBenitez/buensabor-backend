package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Long> {

    // ==================== BÚSQUEDAS POR DENOMINACIÓN (para validaciones y
    // búsqueda) ====================

    /**
     * Verifica si ya existe un producto con la misma denominación.
     * Usado para evitar duplicados al crear.
     * 
     * @param denominacion La denominación a verificar.
     * @return true si existe, false si no.
     */
    boolean existsByDenominacion(String denominacion);

    /**
     * Verifica si existe otro producto con la misma denominación, excluyendo el ID
     * actual.
     * Usado para evitar duplicados al actualizar.
     * 
     * @param denominacion La denominación a verificar.
     * @param id           El ID del producto que se está actualizando.
     * @return true si existe otro, false si no.
     */
    boolean existsByDenominacionAndIdArticuloNot(String denominacion, Long id);

    /**
     * Busca productos cuya denominación contenga el texto de búsqueda, ignorando
     * mayúsculas/minúsculas.
     * 
     * @param denominacion El término de búsqueda.
     * @return Lista de productos que coinciden.
     */
    @Query("SELECT am FROM ArticuloManufacturado am WHERE LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<ArticuloManufacturado> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

    // ==================== BÚSQUEDAS POR RELACIONES ====================

    /**
     * Busca todos los productos que pertenecen a una categoría específica.
     * 
     * @param idCategoria El ID de la categoría.
     * @return Lista de productos en esa categoría.
     */
    List<ArticuloManufacturado> findByCategoriaIdCategoria(Long idCategoria);

    // Contar cuántos productos manufacturados usan un insumo específico
    @Query("SELECT COUNT(am) FROM ArticuloManufacturado am JOIN am.detalles d WHERE d.articuloInsumo.id = :idInsumo")
    Integer countByInsumo(@Param("idInsumo") Long idInsumo);
}