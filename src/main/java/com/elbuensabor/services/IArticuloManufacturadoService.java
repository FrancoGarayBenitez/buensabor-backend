package com.elbuensabor.services;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.ArticuloManufacturado;

import java.util.List;

public interface IArticuloManufacturadoService
        extends IGenericService<ArticuloManufacturado, Long, ArticuloManufacturadoResponseDTO> {

    // Métodos específicos para manufacturados
    ArticuloManufacturadoResponseDTO createManufacturado(ArticuloManufacturadoRequestDTO dto);

    ArticuloManufacturadoResponseDTO updateManufacturado(Long id, ArticuloManufacturadoRequestDTO dto);

    void bajaLogica(Long id);

    // Búsquedas específicas
    List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria);

    List<ArticuloManufacturadoResponseDTO> searchByDenominacion(String denominacion);

    // Lógica de negocio
    Double calcularCostoProduccion(ArticuloManufacturado manufacturado);

    Boolean verificarStockSuficiente(ArticuloManufacturado manufacturado);

    Integer calcularCantidadMaximaProduccion(ArticuloManufacturado manufacturado);
}