package com.elbuensabor.repository;

import com.elbuensabor.entities.ArticuloManufacturadoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IManufacturadoDetalleRepository extends JpaRepository<ArticuloManufacturadoDetalle, Long> {

    @Modifying
    @Query("DELETE FROM ArticuloManufacturadoDetalle d WHERE d.articuloManufacturado.idArticulo = :idArticulo")
    void deleteByArticuloManufacturadoId(@Param("idArticulo") Long idArticulo);
    List<ArticuloManufacturadoDetalle> findByArticuloInsumo_IdArticulo(Long idArticuloInsumo);


}
