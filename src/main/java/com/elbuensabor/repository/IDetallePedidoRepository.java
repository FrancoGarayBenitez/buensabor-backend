package com.elbuensabor.repository;

import com.elbuensabor.dto.response.RankingProductoDTO;
import com.elbuensabor.entities.DetallePedido; // <-- IMPORT NECESARIO
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; // <-- IMPORT NECESARIO
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // <-- IMPORT NECESARIO

import java.time.LocalDateTime;
import java.util.List;

@Repository // <-- 1. ANOTACIÓN FALTANTE
public interface IDetallePedidoRepository extends JpaRepository<DetallePedido, Long> { // <-- 2. EXTENSIÓN FALTANTE

    @Query("SELECT new com.elbuensabor.dto.response.RankingProductoDTO(d.articulo.denominacion, SUM(d.cantidad), SUM(d.subtotal)) " +
            "FROM DetallePedido d " +
            "WHERE d.pedido.fecha BETWEEN :fechaDesde AND :fechaHasta AND d.pedido.estado <> 'CANCELADO' " +
            "GROUP BY d.articulo.idArticulo, d.articulo.denominacion " + // Agregué d.articulo.denominacion al GROUP BY
            "ORDER BY SUM(d.cantidad) DESC")
    List<RankingProductoDTO> findRankingProductos(@Param("fechaDesde") LocalDateTime fechaDesde, @Param("fechaHasta") LocalDateTime fechaHasta, Pageable pageable);

}
