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

    // ==================== BÚSQUEDAS POR DENOMINACIÓN (para validaciones y
    // búsqueda) ====================

    /**
     * Busca una promoción por su denominación, ignorando mayúsculas/minúsculas,
     * incluyendo eliminadas.
     * Usado para validar duplicados al crear.
     * 
     * @param denominacion La denominación a buscar.
     * @return Un Optional que contiene la promoción si se encuentra.
     */
    @Query("SELECT p FROM Promocion p WHERE lower(p.denominacion) = lower(:denominacion)")
    Optional<Promocion> findByDenominacionIgnoreCaseIncludingEliminado(@Param("denominacion") String denominacion);

    /**
     * Busca una promoción por su denominación, ignorando mayúsculas/minúsculas y
     * excluyendo un ID, incluyendo eliminadas.
     * Usado para validar duplicados al actualizar.
     * 
     * @param denominacion La denominación a buscar.
     * @param id           El ID de la promoción a excluir.
     * @return Un Optional que contiene la promoción si se encuentra.
     */
    @Query("SELECT p FROM Promocion p WHERE lower(p.denominacion) = lower(:denominacion) AND p.idPromocion <> :id")
    Optional<Promocion> findByDenominacionIgnoreCaseAndIdNotIncludingEliminado(
            @Param("denominacion") String denominacion, @Param("id") Long id);

    /**
     * Busca promociones cuya denominación contenga el texto de búsqueda, ignorando
     * mayúsculas/minúsculas.
     * Solo devuelve promociones no eliminadas.
     * 
     * @param denominacion El término de búsqueda.
     * @return Lista de promociones que coinciden.
     */
    List<Promocion> findByDenominacionContainingIgnoreCaseAndEliminadoFalse(String denominacion);

    // ==================== SOBREESCRITURA DE MÉTODOS JPA PARA FILTRAR ELIMINADOS
    // ====================

    /**
     * Busca todas las promociones que no han sido eliminadas lógicamente.
     * 
     * @return Lista de promociones activas.
     */
    @Override
    @Query("SELECT p FROM Promocion p WHERE p.eliminado = false")
    List<Promocion> findAll();
}