package com.elbuensabor.repository;

import com.elbuensabor.entities.DetalleManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDetalleManufacturadoRepository extends JpaRepository<DetalleManufacturado, Long> {
}
