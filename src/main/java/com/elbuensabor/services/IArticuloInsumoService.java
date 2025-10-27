package com.elbuensabor.services;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;

import java.util.List;

public interface IArticuloInsumoService extends IGenericService<ArticuloInsumo, Long, ArticuloInsumoResponseDTO> {

    // Métodos específicos para insumos
    ArticuloInsumoResponseDTO createInsumo(ArticuloInsumoRequestDTO insumoRequestDTO);
    ArticuloInsumoResponseDTO updateInsumo(Long id, ArticuloInsumoRequestDTO insumoRequestDTO);

    // Búsquedas específicas
    List<ArticuloInsumoResponseDTO> findByCategoria(Long idCategoria);
    List<ArticuloInsumoResponseDTO> findByUnidadMedida(Long idUnidadMedida);
    List<ArticuloInsumoResponseDTO> findIngredientes(); // esParaElaborar = true
    List<ArticuloInsumoResponseDTO> findProductosNoManufacturados(); // esParaElaborar = false
    List<ArticuloInsumoResponseDTO> searchByDenominacion(String denominacion);

    // Control de stock
    List<ArticuloInsumoResponseDTO> findStockCritico();
    List<ArticuloInsumoResponseDTO> findStockBajo();
    List<ArticuloInsumoResponseDTO> findInsuficientStock(Integer cantidadRequerida);

    // Operaciones de stock
    ArticuloInsumoResponseDTO actualizarStock(Long id, Integer nuevoStock);
    ArticuloInsumoResponseDTO incrementarStock(Long id, Integer cantidad);
    ArticuloInsumoResponseDTO decrementarStock(Long id, Integer cantidad);

    // Validaciones
    boolean existsByDenominacion(String denominacion);
    boolean hasStockAvailable(Long idInsumo, Integer cantidad);
    boolean isUsedInProducts(Long idInsumo);

    // Información adicional
    Double calcularPorcentajeStock(Long idInsumo);
    String determinarEstadoStock(Long idInsumo);
}