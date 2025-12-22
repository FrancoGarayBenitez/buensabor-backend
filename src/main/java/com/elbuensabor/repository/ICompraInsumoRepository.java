package com.elbuensabor.repository;

import com.elbuensabor.entities.CompraInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICompraInsumoRepository extends JpaRepository<CompraInsumo, Long> {
    List<CompraInsumo> findByArticuloInsumo_IdArticulo(Long idArticuloInsumo);

    // ✅ borrar compras del insumo (después de borrar históricos)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CompraInsumo c WHERE c.articuloInsumo.idArticulo = :idArticulo")
    void deleteByArticuloInsumoId(@Param("idArticulo") Long idArticulo);
}
