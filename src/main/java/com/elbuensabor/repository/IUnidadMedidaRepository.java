package com.elbuensabor.repository;

import com.elbuensabor.entities.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    boolean existsByDenominacionIgnoreCase(String denominacion);
}
