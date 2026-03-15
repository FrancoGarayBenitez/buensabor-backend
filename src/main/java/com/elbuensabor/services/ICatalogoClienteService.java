package com.elbuensabor.services;

import com.elbuensabor.dto.response.cliente.CatalogoArticuloDTO;
import com.elbuensabor.dto.response.cliente.DetalleArticuloDTO;
import com.elbuensabor.dto.response.cliente.PromocionClienteDTO;

import java.util.List;

/**
 * Servicio para gestionar el catálogo de productos visible para el cliente.
 * Solo muestra artículos disponibles y oculta información administrativa.
 */
public interface ICatalogoClienteService {

    /**
     * Obtiene todos los artículos manufacturados disponibles para el cliente.
     * Solo incluye artículos no eliminados y con stock suficiente.
     * 
     * @return Lista de artículos en formato catálogo.
     */
    List<CatalogoArticuloDTO> obtenerArticulosDisponibles();

    /**
     * Obtiene el detalle completo de un artículo específico.
     * 
     * @param idArticulo ID del artículo.
     * @return Detalle completo del artículo.
     */
    DetalleArticuloDTO obtenerDetalleArticulo(Long idArticulo);

    /**
     * Filtra artículos por categoría.
     * 
     * @param idCategoria ID de la categoría.
     * @return Lista de artículos de la categoría.
     */
    List<CatalogoArticuloDTO> obtenerArticulosPorCategoria(Long idCategoria);

    /**
     * Busca artículos por denominación.
     * 
     * @param query Término de búsqueda.
     * @return Lista de artículos que coinciden.
     */
    List<CatalogoArticuloDTO> buscarArticulos(String query);

    /**
     * Obtiene todos los artículos que tienen promoción vigente.
     * 
     * @return Lista de artículos en promoción.
     */
    List<CatalogoArticuloDTO> obtenerArticulosEnPromocion();

    /**
     * Obtiene todas las promociones vigentes para el cliente.
     * 
     * @return Lista de promociones activas.
     */
    List<PromocionClienteDTO> obtenerPromocionesVigentes();

    /**
     * Obtiene el detalle de una promoción específica.
     * 
     * @param idPromocion ID de la promoción.
     * @return Detalle de la promoción.
     */
    PromocionClienteDTO obtenerDetallePromocion(Long idPromocion);
}