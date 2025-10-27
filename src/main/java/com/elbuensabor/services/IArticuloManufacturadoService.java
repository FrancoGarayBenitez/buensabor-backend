package com.elbuensabor.services;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import com.elbuensabor.entities.ArticuloManufacturado;

import java.util.List;

public interface IArticuloManufacturadoService extends IGenericService<ArticuloManufacturado, Long, ArticuloManufacturadoResponseDTO> {

    // Métodos específicos para manufacturados
    ArticuloManufacturadoResponseDTO createManufacturado(ArticuloManufacturadoRequestDTO manufacturadoRequestDTO);
    ArticuloManufacturadoResponseDTO updateManufacturado(Long id, ArticuloManufacturadoRequestDTO manufacturadoRequestDTO);

    // Búsquedas específicas
    List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria);
    List<ArticuloManufacturadoResponseDTO> findByTiempoMaximo(Integer tiempoMaximo);
    List<ArticuloManufacturadoResponseDTO> findByIngrediente(Long idInsumo);
    List<ArticuloManufacturadoResponseDTO> findByPrecioRango(Double precioMin, Double precioMax);
    List<ArticuloManufacturadoResponseDTO> findByMinimoIngredientes(Integer cantidadMinima);
    List<ArticuloManufacturadoResponseDTO> searchByDenominacion(String denominacion);

    // Control de preparabilidad y stock
    List<ArticuloManufacturadoResponseDTO> findPreparables();
    List<ArticuloManufacturadoResponseDTO> findNoPreparables();
    Integer calcularMaximoPreparable(Long idProducto);
    Boolean puedePrepararse(Long idProducto, Integer cantidad);

    // Cálculos de costos y precios
    Double calcularCostoTotal(Long idProducto);
    Double calcularMargenGanancia(Long idProducto);
    Double calcularPrecioSugerido(Long idProducto, Double margen);

    // Gestión de recetas (detalles)
    ArticuloManufacturadoResponseDTO agregarIngrediente(Long idProducto, Long idInsumo, Double cantidad);
    ArticuloManufacturadoResponseDTO actualizarIngrediente(Long idProducto, Long idDetalle, Double nuevaCantidad);
    ArticuloManufacturadoResponseDTO eliminarIngrediente(Long idProducto, Long idDetalle);

    // Simulaciones para producción
    List<ArticuloManufacturadoResponseDTO> simularProduccion(Integer cantidadAProducir);
    Boolean verificarStockParaProduccion(Long idProducto, Integer cantidadAProducir);

    // Validaciones
    boolean existsByDenominacion(String denominacion);
    boolean tieneIngredientes(Long idProducto);
    boolean seUsaEnPedidos(Long idProducto);

    //Bajas y Altas logicas
    void bajaLogica(Long id);
    void altaLogica(Long id);

}