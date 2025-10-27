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

    // Buscar por denominación
    Optional<ArticuloInsumo> findByDenominacion(String denominacion);

    // Verificar si existe por denominación
    boolean existsByDenominacion(String denominacion);

    // Buscar por categoría
    List<ArticuloInsumo> findByCategoriaIdCategoria(Long idCategoria);

    // Buscar solo los que son para elaborar (ingredientes)
    List<ArticuloInsumo> findByEsParaElaborarTrue();

    // Buscar solo los que NO son para elaborar (productos no manufacturados: gaseosas, etc.)
    List<ArticuloInsumo> findByEsParaElaborarFalse();

    // Buscar por unidad de medida
    List<ArticuloInsumo> findByUnidadMedidaIdUnidadMedida(Long idUnidadMedida);

    // Stock crítico (menos del 25% del stock máximo)
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE (ai.stockActual * 100.0 / ai.stockMaximo) < 25")
    List<ArticuloInsumo> findStockCritico();

    // Stock bajo (entre 25% y 50% del stock máximo)
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE (ai.stockActual * 100.0 / ai.stockMaximo) BETWEEN 25 AND 50")
    List<ArticuloInsumo> findStockBajo();

    // Stock insuficiente para una cantidad específica
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE ai.stockActual < :cantidadRequerida")
    List<ArticuloInsumo> findInsuficientStock(@Param("cantidadRequerida") Integer cantidadRequerida);

    // Buscar por rango de precios
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE ai.precioCompra BETWEEN :precioMin AND :precioMax")
    List<ArticuloInsumo> findByPrecioCompraBetween(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    // Contar cuántos productos manufacturados usan este insumo
    @Query("SELECT COUNT(DISTINCT amd.articuloManufacturado) FROM ArticuloManufacturadoDetalle amd WHERE amd.articuloInsumo.idArticulo = :idInsumo")
    Integer countProductosQueUsan(@Param("idInsumo") Long idInsumo);

    // Búsqueda por nombre (parcial)
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE LOWER(ai.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<ArticuloInsumo> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

    // Verificar disponibilidad de stock para una cantidad
    @Query("SELECT CASE WHEN ai.stockActual >= :cantidad THEN true ELSE false END FROM ArticuloInsumo ai WHERE ai.idArticulo = :idInsumo")
    Boolean hasStockAvailable(@Param("idInsumo") Long idInsumo, @Param("cantidad") Integer cantidad);
}