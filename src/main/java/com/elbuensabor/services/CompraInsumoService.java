package com.elbuensabor.services;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.entities.CompraInsumo;

import java.util.List;

public interface CompraInsumoService {
    void registrarCompra(CompraInsumoRequestDTO dto);
    List<CompraInsumo> getAllCompras();
    CompraInsumo getCompraById(Long id);
    List<CompraInsumo> getComprasByInsumoId(Long idInsumo);

    // 🔥 AGREGÁ ESTA LÍNEA 🔥
    CompraInsumoResponseDTO toDto(CompraInsumo compra);
}
