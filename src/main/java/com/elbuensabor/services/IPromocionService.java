package com.elbuensabor.services;

import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import java.util.List;

public interface IPromocionService {

    /**
     * Obtiene todas las promociones (activas y no activas).
     * 
     * @return Lista de DTOs de promociones.
     */
    List<PromocionResponseDTO> findAll();

    /**
     * Busca una promoción por su ID.
     * 
     * @param id El ID de la promoción.
     * @return El DTO de la promoción encontrada.
     */
    PromocionResponseDTO findById(Long id);

    /**
     * Crea una nueva promoción.
     * 
     * @param requestDTO DTO con los datos para la creación.
     * @return El DTO de la promoción creada.
     */
    PromocionResponseDTO create(PromocionRequestDTO requestDTO);

    /**
     * Actualiza una promoción existente.
     * 
     * @param id         El ID de la promoción a actualizar.
     * @param requestDTO DTO con los datos para la actualización.
     * @return El DTO de la promoción actualizada.
     */
    PromocionResponseDTO update(Long id, PromocionRequestDTO requestDTO);

    /**
     * Realiza una baja lógica de una promoción.
     * 
     * @param id El ID de la promoción a dar de baja.
     */
    void delete(Long id);

    /**
     * Desactiva una promoción (baja lógica).
     * 
     * @param id El ID de la promoción a desactivar.
     */
    void deactivate(Long id);

    /**
     * Reactiva una promoción que fue dada de baja lógicamente.
     * 
     * @param id El ID de la promoción a activar.
     */
    void activate(Long id);

    /**
     * Cambia el estado 'activo' de una promoción.
     * Esto permite habilitar o deshabilitar una promoción manualmente sin alterar
     * sus fechas.
     * 
     * @param id El ID de la promoción.
     * @return El DTO de la promoción con su nuevo estado.
     */
    PromocionResponseDTO toggleActivo(Long id);

    /**
     * Busca promociones por una denominación.
     * 
     * @param denominacion Término de búsqueda.
     * @return Lista de promociones que coinciden.
     */
    List<PromocionResponseDTO> search(String denominacion);
}
