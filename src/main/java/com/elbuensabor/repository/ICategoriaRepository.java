package com.elbuensabor.repository;

import com.elbuensabor.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {

    // Buscar por denominación
    Optional<Categoria> findByDenominacion(String denominacion);

    // Verificar si existe por denominación (para evitar duplicados)
    boolean existsByDenominacion(String denominacion);

    // Obtener todas las categorías principales (no subcategorías)
    List<Categoria> findByEsSubcategoriaFalse();

    // Obtener subcategorías de una categoría padre
    List<Categoria> findByCategoriaPadreIdCategoria(Long idCategoriaPadre);

    // Contar artículos por categoría
    @Query("SELECT COUNT(a) FROM Articulo a WHERE a.categoria.idCategoria = :idCategoria")
    Integer countArticulosByCategoria(@Param("idCategoria") Long idCategoria);

    // Verificar si una categoría tiene subcategorías
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.categoriaPadre.idCategoria = :idCategoria")
    boolean hasSubcategorias(@Param("idCategoria") Long idCategoria);

    // Buscar categorías por nombre (búsqueda parcial)
    @Query("SELECT c FROM Categoria c WHERE LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
    List<Categoria> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);
}