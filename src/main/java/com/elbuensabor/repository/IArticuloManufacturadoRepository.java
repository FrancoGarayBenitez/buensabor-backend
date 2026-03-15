package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Long> {

        // ==================== BÚSQUEDAS POR DENOMINACIÓN ====================

        boolean existsByDenominacion(String denominacion);

        boolean existsByDenominacionAndIdArticuloNot(String denominacion, Long id);

        @Query("SELECT am FROM ArticuloManufacturado am WHERE LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%'))")
        List<ArticuloManufacturado> findByDenominacionContainingIgnoreCase(@Param("denominacion") String denominacion);

        // ==================== BÚSQUEDAS POR RELACIONES ====================

        List<ArticuloManufacturado> findByCategoriaIdCategoria(Long idCategoria);

        @Query("SELECT COUNT(am) FROM ArticuloManufacturado am JOIN am.detalles d WHERE d.articuloInsumo.id = :idInsumo")
        Integer countByInsumo(@Param("idInsumo") Long idInsumo);

        // ==================== ✅ MÉTODOS PARA CLIENTE ====================

        /**
         * Obtiene todos los artículos manufacturados no eliminados.
         */
        List<ArticuloManufacturado> findByEliminadoFalse();

        /**
         * Filtra artículos manufacturados por categoría (solo activos).
         */
        List<ArticuloManufacturado> findByCategoriaIdCategoriaAndEliminadoFalse(Long idCategoria);

        /**
         * Busca artículos por denominación (búsqueda del cliente).
         * Solo devuelve artículos no eliminados.
         */
        @Query("SELECT am FROM ArticuloManufacturado am WHERE LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :denominacion, '%')) AND am.eliminado = false")
        List<ArticuloManufacturado> findByDenominacionContainingIgnoreCaseAndEliminadoFalse(
                        @Param("denominacion") String denominacion);

        /**
         * Obtiene artículos que tienen promociones vigentes.
         * Útil para sección "Ofertas" del cliente.
         */
        @Query("SELECT DISTINCT am FROM ArticuloManufacturado am " +
                        "JOIN am.detallesPromocion pd " +
                        "JOIN pd.promocion p " +
                        "WHERE am.eliminado = false " +
                        "AND p.eliminado = false " +
                        "AND p.activo = true " +
                        "AND CURRENT_TIMESTAMP BETWEEN p.fechaDesde AND p.fechaHasta")
        List<ArticuloManufacturado> findArticulosConPromocionVigente();
}