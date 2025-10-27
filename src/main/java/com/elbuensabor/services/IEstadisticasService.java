package com.elbuensabor.services;

import com.elbuensabor.dto.response.MovimientosMonetariosDTO;
import com.elbuensabor.dto.response.RankingProductoDTO;

import java.time.LocalDate;
import java.util.List;

public interface IEstadisticasService {
    MovimientosMonetariosDTO findMovimientosMonetarios(LocalDate fechaDesde, LocalDate fechaHasta);
    List<RankingProductoDTO> findRankingProductos(LocalDate fechaDesde, LocalDate fechaHasta, Integer limit);
}
