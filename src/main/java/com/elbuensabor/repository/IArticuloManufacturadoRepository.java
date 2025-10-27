package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Long> {

    // Buscar por denominación
    Optional<ArticuloManufacturado> findByDenominacion(String denominacion);

    // Verificar si existe por denominación
    boolean existsByDenominacion(String denominacion);

    // Buscar por categoría
    List<ArticuloManufacturado> findByCategoriaIdCategoria(Long idCategoria);

    // Buscar por tiempo de preparación
    List<ArticuloManufacturado> findByTiempoEstimadoEnMinutosLessThanEqual(Integer tiempoMaximo);

    // Buscar productos que usan un ingrediente específico
    @Query("SELECT DISTINCT am FROM ArticuloManufacturado am JOIN am.detalles d WHERE d.articuloInsumo.idArticulo = :idInsumo")
    List<ArticuloManufacturado> findByIngrediente(@Param("idInsumo") Long idInsumo);

    // Buscar por rango de precios
    @Query("SELECT am FROM ArticuloManufacturado am WHERE am.precioVenta BETWEEN :precioMin AND :precioMax")
    List<ArticuloManufacturado> findByPrecioVentaBetween(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    // Productos más complejos (más ingredientes)
    @Query("SELECT am FROM ArticuloManufacturado am WHERE SIZE(am.detalles) >= :cantidadMinima")
    List<ArticuloManufacturado> findByMinimoIngredientes(@Param("cantidadMinima") Integer cantidadMinima);

    // Búsqueda por nombre (parcial)
    @Query("SELECT am FROM ArticuloManufacturado am WHERE LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<ArticuloManufacturado> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

    // Buscar productos preparables (con stock suficiente)
    @Query("""
        SELECT am FROM ArticuloManufacturado am 
        WHERE NOT EXISTS (
            SELECT d FROM ArticuloManufacturadoDetalle d 
            WHERE d.articuloManufacturado = am 
            AND d.articuloInsumo.stockActual < d.cantidad
        )
    """)
    List<ArticuloManufacturado> findPreparables();

    // Calcular cantidad máxima preparable de un producto
    @Query("""
        SELECT MIN(FLOOR(d.articuloInsumo.stockActual / d.cantidad))
        FROM ArticuloManufacturadoDetalle d 
        WHERE d.articuloManufacturado.idArticulo = :idProducto
    """)
    Integer calcularMaximoPreparable(@Param("idProducto") Long idProducto);
}