package com.elbuensabor.repository;

import com.elbuensabor.entities.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface IPromocionRepository extends JpaRepository<Promocion, Long> {

    // ✅ BUSCAR PROMOCIONES ACTIVAS Y VIGENTES (CORREGIDO PARA CRUZAR MEDIANOCHE)
    @Query("""
    SELECT p FROM Promocion p 
    WHERE p.activo = true 
    AND :fechaActual BETWEEN p.fechaDesde AND p.fechaHasta
    AND (
        (p.horaDesde <= p.horaHasta AND :horaActual BETWEEN p.horaDesde AND p.horaHasta) OR
        (p.horaDesde > p.horaHasta AND (:horaActual >= p.horaDesde OR :horaActual <= p.horaHasta))
    )
    ORDER BY p.denominacion
    """)
    List<Promocion> findPromocionesVigentes(
            @Param("fechaActual") LocalDateTime fechaActual,
            @Param("horaActual") LocalTime horaActual
    );

    // ✅ BUSCAR PROMOCIONES PARA UN ARTÍCULO ESPECÍFICO (TAMBIÉN CORREGIDO)
    @Query("""
    SELECT p FROM Promocion p 
    JOIN p.articulos a 
    WHERE a.idArticulo = :idArticulo 
    AND p.activo = true 
    AND :fechaActual BETWEEN p.fechaDesde AND p.fechaHasta
    AND (
        (p.horaDesde <= p.horaHasta AND :horaActual BETWEEN p.horaDesde AND p.horaHasta) OR
        (p.horaDesde > p.horaHasta AND (:horaActual >= p.horaDesde OR :horaActual <= p.horaHasta))
    )
    ORDER BY p.valorDescuento DESC
    """)
    List<Promocion> findPromocionesVigentesPorArticulo(
            @Param("idArticulo") Long idArticulo,
            @Param("fechaActual") LocalDateTime fechaActual,
            @Param("horaActual") LocalTime horaActual
    );

    // ✅ BUSCAR PROMOCIONES PARA UNA SUCURSAL (TAMBIÉN CORREGIDO)
    @Query("""
    SELECT p FROM Promocion p 
    JOIN p.sucursales s 
    WHERE s.idSucursalEmpresa = :idSucursal 
    AND p.activo = true 
    AND :fechaActual BETWEEN p.fechaDesde AND p.fechaHasta
    AND (
        (p.horaDesde <= p.horaHasta AND :horaActual BETWEEN p.horaDesde AND p.horaHasta) OR
        (p.horaDesde > p.horaHasta AND (:horaActual >= p.horaDesde OR :horaActual <= p.horaHasta))
    )
    ORDER BY p.denominacion
    """)
    List<Promocion> findPromocionesVigentesPorSucursal(
            @Param("idSucursal") Long idSucursal,
            @Param("fechaActual") LocalDateTime fechaActual,
            @Param("horaActual") LocalTime horaActual
    );

    // ✅ BUSCAR PROMOCIONES APLICABLES (TAMBIÉN CORREGIDO)
    @Query("""
    SELECT DISTINCT p FROM Promocion p 
    JOIN p.articulos a 
    JOIN p.sucursales s 
    WHERE a.idArticulo = :idArticulo 
    AND s.idSucursalEmpresa = :idSucursal 
    AND p.activo = true 
    AND :fechaActual BETWEEN p.fechaDesde AND p.fechaHasta
    AND (
        (p.horaDesde <= p.horaHasta AND :horaActual BETWEEN p.horaDesde AND p.horaHasta) OR
        (p.horaDesde > p.horaHasta AND (:horaActual >= p.horaDesde OR :horaActual <= p.horaHasta))
    )
    ORDER BY p.valorDescuento DESC
    """)
    List<Promocion> findPromocionesAplicables(
            @Param("idArticulo") Long idArticulo,
            @Param("idSucursal") Long idSucursal,
            @Param("fechaActual") LocalDateTime fechaActual,
            @Param("horaActual") LocalTime horaActual
    );

    // ✅ BUSCAR POR ESTADO
    List<Promocion> findByActivoOrderByDenominacion(Boolean activo);

    // ✅ BUSCAR POR DENOMINACIÓN (para admin)
    List<Promocion> findByDenominacionContainingIgnoreCaseOrderByDenominacion(String denominacion);
}