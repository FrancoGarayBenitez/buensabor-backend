package com.elbuensabor.services;

import com.elbuensabor.dto.request.ArticuloInsumoRequestDTO;
import com.elbuensabor.dto.response.ArticuloInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;

import java.util.List;

public interface IArticuloInsumoService extends IGenericService<ArticuloInsumo, Long, ArticuloInsumoResponseDTO> {

    // ==================== CRUD ESPECÍFICOS ====================
    ArticuloInsumoResponseDTO create(ArticuloInsumoRequestDTO requestDTO);

    ArticuloInsumoResponseDTO update(Long id, ArticuloInsumoRequestDTO requestDTO);

    // ==================== BÚSQUEDAS POR FILTRO ====================
    List<ArticuloInsumoResponseDTO> findByCategoria(Long idCategoria);

    List<ArticuloInsumoResponseDTO> findByUnidadMedida(Long idUnidadMedida);

    List<ArticuloInsumoResponseDTO> findByDenominacion(String denominacion);

    // ==================== BÚSQUEDAS POR TIPO ====================
    List<ArticuloInsumoResponseDTO> findParaElaborar();

    List<ArticuloInsumoResponseDTO> findNoParaElaborar();

    // ==================== BÚSQUEDAS POR ESTADO DE STOCK ====================
    List<ArticuloInsumoResponseDTO> findByCriticoStock();

    List<ArticuloInsumoResponseDTO> findByBajoStock();

    List<ArticuloInsumoResponseDTO> findByAltoStock();

    // ==================== BÚSQUEDAS POR PRECIO ====================
    // Buscar insumos por rango de precio de compra
    List<ArticuloInsumoResponseDTO> findByPrecioCompraBetween(Double precioMin, Double precioMax);

    // ==================== VALIDACIONES ====================
    boolean existsByDenominacion(String denominacion);

    boolean tieneStockDisponible(Long idInsumo, Double cantidad);

    boolean estaEnUso(Long idInsumo);

    // ==================== INFORMACIÓN ====================
    Integer countProductosQueLoUsan(Long idInsumo);
}