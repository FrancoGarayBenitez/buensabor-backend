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
}