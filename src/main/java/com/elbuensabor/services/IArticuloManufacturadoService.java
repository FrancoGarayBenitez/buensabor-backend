package com.elbuensabor.services;

import com.elbuensabor.dto.request.ArticuloManufacturadoRequestDTO;
import com.elbuensabor.dto.response.ArticuloManufacturadoResponseDTO;
import java.util.List;

public interface IArticuloManufacturadoService {

    /**
     * Obtiene todos los productos manufacturados.
     * 
     * @return Lista de DTOs de productos.
     */
    List<ArticuloManufacturadoResponseDTO> findAll();

    /**
     * Busca un producto manufacturado por su ID.
     * 
     * @param id El ID del producto.
     * @return El DTO del producto encontrado.
     */
    ArticuloManufacturadoResponseDTO findById(Long id);

    /**
     * Crea un nuevo producto manufacturado.
     * 
     * @param requestDTO DTO con los datos para la creación.
     * @return El DTO del producto creado.
     */
    ArticuloManufacturadoResponseDTO create(ArticuloManufacturadoRequestDTO requestDTO);

    /**
     * Actualiza un producto manufacturado existente.
     * 
     * @param id         El ID del producto a actualizar.
     * @param requestDTO DTO con los datos para la actualización.
     * @return El DTO del producto actualizado.
     */
    ArticuloManufacturadoResponseDTO update(Long id, ArticuloManufacturadoRequestDTO requestDTO);

    /**
     * Realiza una baja lógica de un producto manufacturado.
     * 
     * @param id El ID del producto a dar de baja.
     */
    void delete(Long id);

    // Toggle explícito
    void activate(Long id);

    void deactivate(Long id);

    /**
     * Busca productos por una denominación.
     * 
     * @param denominacion Término de búsqueda.
     * @return Lista de productos que coinciden.
     */
    List<ArticuloManufacturadoResponseDTO> search(String denominacion);

    /**
     * Busca productos por el ID de su categoría.
     * 
     * @param idCategoria ID de la categoría.
     * @return Lista de productos en esa categoría.
     */
    List<ArticuloManufacturadoResponseDTO> findByCategoria(Long idCategoria);
}