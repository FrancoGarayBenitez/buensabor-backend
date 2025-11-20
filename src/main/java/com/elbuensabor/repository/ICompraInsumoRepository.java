package com.elbuensabor.repository;

import com.elbuensabor.entities.CompraInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICompraInsumoRepository extends JpaRepository<CompraInsumo, Long> {
    List<CompraInsumo> findByArticuloInsumo_IdArticulo(Long idArticuloInsumo);

}
