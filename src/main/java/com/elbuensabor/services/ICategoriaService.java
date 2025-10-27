package com.elbuensabor.services;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.entities.Categoria;

import java.util.List;

public interface ICategoriaService extends IGenericService<Categoria, Long, CategoriaResponseDTO> {

    // Métodos específicos para categorías
    List<CategoriaResponseDTO> findCategoriasPrincipales();
    List<CategoriaResponseDTO> findSubcategoriasByPadre(Long idCategoriaPadre);
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO categoriaRequestDTO);
    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO categoriaRequestDTO);
    List<CategoriaResponseDTO> searchByDenominacion(String denominacion);

    // Validaciones de negocio
    boolean existsByDenominacion(String denominacion);
    boolean hasSubcategorias(Long idCategoria);
    boolean hasArticulos(Long idCategoria);
}