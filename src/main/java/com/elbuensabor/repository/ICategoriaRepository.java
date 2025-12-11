package com.elbuensabor.repository;

import com.elbuensabor.entities.Categoria;
import com.elbuensabor.entities.TipoCategoria;
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

        // Obtener categorías para comidas (manufacturados)
        @Query("SELECT c FROM Categoria c WHERE c.tipoCategoria = com.elbuensabor.entities.TipoCategoria.COMIDAS AND c.esSubcategoria = false")
        List<Categoria> findCategoriasParaComidas();

        // Obtener categorías para ingredientes (insumos)
        @Query("SELECT c FROM Categoria c WHERE c.tipoCategoria = com.elbuensabor.entities.TipoCategoria.INGREDIENTES AND c.esSubcategoria = false")
        List<Categoria> findCategoriasParaIngredientes();

        // Obtener categorías para bebidas (nueva)
        @Query("SELECT c FROM Categoria c WHERE c.tipoCategoria = com.elbuensabor.entities.TipoCategoria.BEBIDAS AND c.esSubcategoria = false")
        List<Categoria> findCategoriasParaBebidas();

        // Obtener todas las categorías filtradas por tipo
        List<Categoria> findByTipoCategoria(TipoCategoria tipoCategoria);

        // Obtener subcategorías de un padre filtrando por tipo
        @Query("SELECT c FROM Categoria c WHERE c.categoriaPadre.idCategoria = :idCategoriaPadre AND c.tipoCategoria = :tipoCategoria")
        List<Categoria> findSubcategoriasByPadreAndTipo(
                        @Param("idCategoriaPadre") Long idCategoriaPadre,
                        @Param("tipoCategoria") TipoCategoria tipoCategoria);

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

        // Buscar categorías por nombre filtradas por tipo (enum)
        @Query("SELECT c FROM Categoria c WHERE LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%')) AND c.tipoCategoria = :tipoCategoria")
        List<Categoria> findByDenominacionAndTipo(
                        @Param("denominacion") String denominacion,
                        @Param("tipoCategoria") TipoCategoria tipoCategoria);
}