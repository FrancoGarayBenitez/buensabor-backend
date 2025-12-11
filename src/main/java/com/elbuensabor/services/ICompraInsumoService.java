package com.elbuensabor.services;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.entities.CompraInsumo;

import java.util.List;

public interface ICompraInsumoService {
    CompraInsumoResponseDTO registrarCompra(CompraInsumoRequestDTO dto);

    Long eliminarCompra(Long idCompra);

    List<CompraInsumo> getAllCompras();

    CompraInsumo getCompraById(Long id);

    List<CompraInsumo> getComprasByInsumoId(Long idInsumo);

    CompraInsumoResponseDTO toDto(CompraInsumo compra);
}
