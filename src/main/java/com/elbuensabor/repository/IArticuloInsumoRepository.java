package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IArticuloInsumoRepository extends JpaRepository<ArticuloInsumo, Long> {

    // ==================== BÚSQUEDAS POR DENOMINACIÓN ====================

    Optional<ArticuloInsumo> findByDenominacion(String denominacion);

    boolean existsByDenominacion(String denominacion);

    @Query("SELECT ai FROM ArticuloInsumo ai WHERE LOWER(ai.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<ArticuloInsumo> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

    // ==================== BÚSQUEDAS POR RELACIONES ====================

    List<ArticuloInsumo> findByCategoriaIdCategoria(Long idCategoria);

    List<ArticuloInsumo> findByUnidadMedidaIdUnidadMedida(Long idUnidadMedida);

    // ==================== BÚSQUEDAS POR TIPO ====================

    List<ArticuloInsumo> findByEsParaElaborarTrue();

    List<ArticuloInsumo> findByEsParaElaborarFalse();

    // ==================== BÚSQUEDAS POR PRECIO ====================

    @Query("SELECT ai FROM ArticuloInsumo ai WHERE ai.precioCompra BETWEEN :precioMin AND :precioMax")
    List<ArticuloInsumo> findByPrecioCompraBetween(
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax);

    // ==================== INFORMACIÓN Y CONTEOS ====================

    /**
     * ✅ CLAVE: Contar cuántos productos manufacturados usan este insumo
     * Usado para validar eliminación y auditoría
     */
    @Query("SELECT COUNT(DISTINCT dm.articuloManufacturado) FROM DetalleManufacturado dm WHERE dm.articuloInsumo.idArticulo = :idInsumo")
    Integer countProductosQueUsan(@Param("idInsumo") Long idInsumo);

    // ==================== VALIDACIONES DE STOCK ====================

    /**
     * ✅ Verificar si hay stock disponible para una cantidad específica
     * Parameterizado: idInsumo, cantidad (como Double)
     */
    @Query("SELECT CASE WHEN ai.stockActual >= :cantidad THEN true ELSE false END FROM ArticuloInsumo ai WHERE ai.idArticulo = :idInsumo")
    Boolean hasStockAvailable(
            @Param("idInsumo") Long idInsumo,
            @Param("cantidad") Double cantidad); // ✅ CAMBIO: Double en lugar de Integer
}