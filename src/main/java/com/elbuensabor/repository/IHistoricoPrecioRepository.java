package com.elbuensabor.repository;

import com.elbuensabor.entities.HistoricoPrecio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IHistoricoPrecioRepository extends JpaRepository<HistoricoPrecio, Long> {

    // ✅ Obtener todos los precios de un artículo ordenados por fecha DESC
    @Query("SELECT hp FROM HistoricoPrecio hp WHERE hp.articuloInsumo.idArticulo = :idArticulo ORDER BY hp.fecha DESC")
    List<HistoricoPrecio> findByArticuloOrderByFechaDesc(@Param("idArticulo") Long idArticulo);

    // ✅ Obtener últimos N precios (usando Pageable)
    @Query("SELECT hp FROM HistoricoPrecio hp WHERE hp.articuloInsumo.idArticulo = :idArticulo ORDER BY hp.fecha DESC")
    List<HistoricoPrecio> findLastNPrecios(@Param("idArticulo") Long idArticulo, Pageable pageable);
}
