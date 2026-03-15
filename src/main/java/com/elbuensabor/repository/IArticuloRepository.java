package com.elbuensabor.repository;

import com.elbuensabor.entities.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IArticuloRepository extends JpaRepository<Articulo, Long> {

    // Buscar por denominación
    Optional<Articulo> findByDenominacion(String denominacion);

    // Verificar si existe por denominación
    boolean existsByDenominacion(String denominacion);

    // Buscar por categoría
    List<Articulo> findByCategoriaIdCategoria(Long idCategoria);

    // Búsqueda por nombre (parcial)
    @Query("SELECT a FROM Articulo a WHERE LOWER(a.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<Articulo> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

    // Buscar por rango de precios
    @Query("SELECT a FROM Articulo a WHERE a.precioVenta BETWEEN :precioMin AND :precioMax")
    List<Articulo> findByPrecioVentaBetween(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    // Obtener todos los artículos disponibles (tanto insumos como manufacturados)
    @Query("SELECT a FROM Articulo a ORDER BY a.denominacion")
    List<Articulo> findAllArticulos();

    // Incluye activos y desactivados (sin filtrar por eliminado)
    @Query("SELECT a FROM Articulo a WHERE LOWER(a.denominacion) = LOWER(:denominacion)")
    Optional<Articulo> findByDenominacionIgnoreCaseIncludingEliminado(@Param("denominacion") String denominacion);

    @Query("SELECT a FROM Articulo a WHERE LOWER(a.denominacion) = LOWER(:denominacion) AND a.idArticulo <> :excludeId")
    Optional<Articulo> findByDenominacionIgnoreCaseAndIdNotIncludingEliminado(
            @Param("denominacion") String denominacion,
            @Param("excludeId") Long excludeId);

    /**
     * ✅ Todos los artículos disponibles para el catálogo:
     * - ArticuloManufacturado: activos y no eliminados
     * - ArticuloInsumo: esParaElaborar = false (venta directa), no eliminados
     */
    @Query("SELECT a FROM Articulo a " +
            "WHERE a.eliminado = false " +
            "AND (" +
            "  TYPE(a) = ArticuloManufacturado " +
            "  OR (TYPE(a) = ArticuloInsumo AND a.esParaElaborar = false)" +
            ")")
    List<Articulo> findDisponiblesParaCatalogo();

    /**
     * ✅ Filtra por categoría incluyendo ambos tipos.
     */
    @Query("SELECT a FROM Articulo a " +
            "WHERE a.eliminado = false " +
            "AND a.categoria.idCategoria = :idCategoria " +
            "AND (" +
            "  TYPE(a) = ArticuloManufacturado " +
            "  OR (TYPE(a) = ArticuloInsumo AND a.esParaElaborar = false)" +
            ")")
    List<Articulo> findDisponiblesParaCatalogoPorCategoria(
            @Param("idCategoria") Long idCategoria);

    /**
     * ✅ Búsqueda por nombre incluyendo ambos tipos.
     */
    @Query("SELECT a FROM Articulo a " +
            "WHERE a.eliminado = false " +
            "AND LOWER(a.denominacion) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND (" +
            "  TYPE(a) = ArticuloManufacturado " +
            "  OR (TYPE(a) = ArticuloInsumo AND a.esParaElaborar = false)" +
            ")")
    List<Articulo> findDisponiblesParaCatalogoPorNombre(
            @Param("query") String query);
}