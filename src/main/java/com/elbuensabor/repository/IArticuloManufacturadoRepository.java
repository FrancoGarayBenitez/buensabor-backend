package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Long> {

    // Verificar si existe por denominación
    boolean existsByDenominacion(String denominacion);

    // Buscar por denominación (búsqueda parcial)
    List<ArticuloManufacturado> findByDenominacionContainingIgnoreCase(String denominacion);

    // Buscar por categoría
    List<ArticuloManufacturado> findByCategoriaIdCategoria(Long idCategoria);

    // Contar cuántos productos manufacturados usan un insumo específico
    @Query("SELECT COUNT(am) FROM ArticuloManufacturado am JOIN am.detalles d WHERE d.articuloInsumo.id = :idInsumo")
    Integer countByInsumo(@Param("idInsumo") Long idInsumo);
}