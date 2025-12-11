package com.elbuensabor.services;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.entities.Categoria;
import com.elbuensabor.entities.TipoCategoria;

import java.util.List;

public interface ICategoriaService extends IGenericService<Categoria, Long, CategoriaResponseDTO> {

    // Métodos específicos para categorías
    List<CategoriaResponseDTO> findCategoriasPrincipales();

    // Obtener categorías por tipo
    List<CategoriaResponseDTO> findCategoriasParaComidas();

    List<CategoriaResponseDTO> findCategoriasParaIngredientes();

    List<CategoriaResponseDTO> findCategoriasParaBebidas();

    List<CategoriaResponseDTO> findByTipo(TipoCategoria tipoCategoria);

    List<CategoriaResponseDTO> findSubcategoriasByPadre(Long idCategoriaPadre);

    // Obtener subcategorías por tipo
    List<CategoriaResponseDTO> findSubcategoriasByPadreAndTipo(Long idCategoriaPadre, TipoCategoria tipoCategoria);

    CategoriaResponseDTO createCategoria(CategoriaRequestDTO categoriaRequestDTO);

    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO categoriaRequestDTO);

    List<CategoriaResponseDTO> searchByDenominacion(String denominacion);

    // Buscar por denominación y tipo
    List<CategoriaResponseDTO> searchByDenominacionAndTipo(String denominacion, TipoCategoria tipoCategoria);

    // Validaciones de negocio
    boolean existsByDenominacion(String denominacion);

    boolean hasSubcategorias(Long idCategoria);

    boolean hasArticulos(Long idCategoria);
}