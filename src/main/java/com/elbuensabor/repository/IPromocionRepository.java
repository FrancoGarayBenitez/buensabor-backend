package com.elbuensabor.repository;

import com.elbuensabor.entities.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPromocionRepository extends JpaRepository<Promocion, Long> {

        // ==================== MÉTODOS EXISTENTES ====================

        List<Promocion> findByEliminadoFalse();

        // ==================== QUERIES DIVIDIDAS (evita MultipleBagFetchException)
        // ====================

        /**
         * ✅ Query 1: Carga promociones con sus detalles y artículos
         * NO incluye imágenes para evitar MultipleBagFetchException
         */
        @Query("SELECT DISTINCT p FROM Promocion p " +
                        "LEFT JOIN FETCH p.detalles d " +
                        "LEFT JOIN FETCH d.articulo a " +
                        "LEFT JOIN FETCH a.categoria " +
                        "WHERE p.eliminado = false AND p.activo = true")
        List<Promocion> findAllVigentesConDetalles();

        /**
         * ✅ Query 2: Carga las imágenes de las promociones
         * Se ejecuta después de la primera query
         */
        @Query("SELECT DISTINCT p FROM Promocion p " +
                        "LEFT JOIN FETCH p.imagenes " +
                        "WHERE p IN :promociones")
        List<Promocion> fetchImagenesPromocion(@Param("promociones") List<Promocion> promociones);

        /**
         * ✅ Query 3: Carga las imágenes de los artículos
         */
        @Query("SELECT DISTINCT a FROM Articulo a " +
                        "LEFT JOIN FETCH a.imagenes " +
                        "WHERE a IN (SELECT d.articulo FROM PromocionDetalle d WHERE d.promocion IN :promociones)")
        List<com.elbuensabor.entities.Articulo> fetchImagenesArticulos(
                        @Param("promociones") List<Promocion> promociones);

        /**
         * ✅ Obtiene una promoción específica con detalles (sin imágenes)
         */
        @Query("SELECT p FROM Promocion p " +
                        "LEFT JOIN FETCH p.detalles d " +
                        "LEFT JOIN FETCH d.articulo a " +
                        "LEFT JOIN FETCH a.categoria " +
                        "WHERE p.idPromocion = :id AND p.eliminado = false")
        Optional<Promocion> findByIdConDetalles(@Param("id") Long id);

        /**
         * ✅ Carga imágenes de una promoción específica
         */
        @Query("SELECT p FROM Promocion p " +
                        "LEFT JOIN FETCH p.imagenes " +
                        "WHERE p.idPromocion = :id")
        Optional<Promocion> fetchImagenesPorId(@Param("id") Long id);

        // ==================== OTROS MÉTODOS EXISTENTES ====================

        @Query("SELECT p FROM Promocion p WHERE lower(p.denominacion) = lower(:denominacion)")
        Optional<Promocion> findByDenominacionIgnoreCaseIncludingEliminado(@Param("denominacion") String denominacion);

        @Query("SELECT p FROM Promocion p WHERE lower(p.denominacion) = lower(:denominacion) AND p.idPromocion <> :id")
        Optional<Promocion> findByDenominacionIgnoreCaseAndIdNotIncludingEliminado(
                        @Param("denominacion") String denominacion, @Param("id") Long id);

        List<Promocion> findByDenominacionContainingIgnoreCaseAndEliminadoFalse(String denominacion);

        @Override
        @Query("SELECT p FROM Promocion p WHERE p.eliminado = false")
        List<Promocion> findAll();

        List<Promocion> findByEliminadoFalseAndActivoTrue();

        @Query("SELECT p FROM Promocion p WHERE p.eliminado = false " +
                        "AND p.activo = true " +
                        "AND CURRENT_TIMESTAMP BETWEEN p.fechaDesde AND p.fechaHasta")
        List<Promocion> findPromocionesVigentes();

        @Query("SELECT COUNT(p) > 0 FROM Promocion p " +
                        "JOIN p.detalles pd " +
                        "WHERE pd.articulo.idArticulo = :idArticulo " +
                        "AND p.eliminado = false " +
                        "AND p.activo = true " +
                        "AND CURRENT_TIMESTAMP BETWEEN p.fechaDesde AND p.fechaHasta")
        boolean articuloTienePromocionVigente(@Param("idArticulo") Long idArticulo);
}