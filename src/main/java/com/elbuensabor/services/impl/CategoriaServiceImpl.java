package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.dto.response.CategoriaSimpleDTO;
import com.elbuensabor.entities.Categoria;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.ICategoriaRepository;
import com.elbuensabor.services.ICategoriaService;
import com.elbuensabor.services.mapper.CategoriaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl extends GenericServiceImpl<Categoria, Long, CategoriaResponseDTO, ICategoriaRepository, CategoriaMapper>
        implements ICategoriaService {

    @Autowired
    public CategoriaServiceImpl(ICategoriaRepository repository, CategoriaMapper mapper) {
        super(repository, mapper, Categoria.class, CategoriaResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapearCategoriaCompleta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO findById(Long id) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada"));
        return mapearCategoriaCompleta(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findCategoriasPrincipales() {
        List<Categoria> categoriasPrincipales = repository.findByEsSubcategoriaFalse();
        return categoriasPrincipales.stream()
                .map(this::mapearCategoriaCompleta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findSubcategoriasByPadre(Long idCategoriaPadre) {
        if (!repository.existsById(idCategoriaPadre)) {
            throw new ResourceNotFoundException("Categoría padre con ID " + idCategoriaPadre + " no encontrada");
        }

        List<Categoria> subcategorias = repository.findByCategoriaPadreIdCategoria(idCategoriaPadre);
        return subcategorias.stream()
                .map(this::mapearCategoriaCompleta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaResponseDTO createCategoria(CategoriaRequestDTO categoriaRequestDTO) {
        // Validar que no exista una categoría con el mismo nombre
        if (repository.existsByDenominacion(categoriaRequestDTO.getDenominacion())) {
            throw new DuplicateResourceException("Ya existe una categoría con la denominación: " + categoriaRequestDTO.getDenominacion());
        }

        // Mapear DTO a Entity
        Categoria categoria = mapper.toEntity(categoriaRequestDTO);

        // Si es subcategoría, validar y asignar categoría padre
        if (categoriaRequestDTO.getEsSubcategoria()) {
            if (categoriaRequestDTO.getIdCategoriaPadre() == null) {
                throw new IllegalArgumentException("Las subcategorías deben tener una categoría padre");
            }

            Categoria categoriaPadre = repository.findById(categoriaRequestDTO.getIdCategoriaPadre())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría padre con ID " + categoriaRequestDTO.getIdCategoriaPadre() + " no encontrada"));

            // Validar que la categoría padre no sea también una subcategoría
            if (categoriaPadre.isEsSubcategoria()) {
                throw new IllegalArgumentException("Una subcategoría no puede tener como padre a otra subcategoría");
            }

            categoria.setCategoriaPadre(categoriaPadre);
        } else {
            // Si no es subcategoría, asegurar que no tenga padre
            categoria.setCategoriaPadre(null);
        }

        Categoria savedCategoria = repository.save(categoria);
        return mapearCategoriaCompleta(savedCategoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO categoriaRequestDTO) {
        Categoria existingCategoria = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada"));

        // Validar que no exista otra categoría con el mismo nombre (excluyendo la actual)
        if (repository.existsByDenominacion(categoriaRequestDTO.getDenominacion()) &&
                !existingCategoria.getDenominacion().equals(categoriaRequestDTO.getDenominacion())) {
            throw new DuplicateResourceException("Ya existe otra categoría con la denominación: " + categoriaRequestDTO.getDenominacion());
        }

        // Actualizar campos básicos
        mapper.updateEntityFromDTO(categoriaRequestDTO, existingCategoria);

        // Manejar lógica de categoría padre
        if (categoriaRequestDTO.getEsSubcategoria()) {
            if (categoriaRequestDTO.getIdCategoriaPadre() == null) {
                throw new IllegalArgumentException("Las subcategorías deben tener una categoría padre");
            }

            // Validar que no se esté asignando como padre a si misma
            if (categoriaRequestDTO.getIdCategoriaPadre().equals(id)) {
                throw new IllegalArgumentException("Una categoría no puede ser padre de sí misma");
            }

            Categoria categoriaPadre = repository.findById(categoriaRequestDTO.getIdCategoriaPadre())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría padre con ID " + categoriaRequestDTO.getIdCategoriaPadre() + " no encontrada"));

            if (categoriaPadre.isEsSubcategoria()) {
                throw new IllegalArgumentException("Una subcategoría no puede tener como padre a otra subcategoría");
            }

            existingCategoria.setCategoriaPadre(categoriaPadre);
        } else {
            existingCategoria.setCategoriaPadre(null);
        }

        Categoria updatedCategoria = repository.save(existingCategoria);
        return mapearCategoriaCompleta(updatedCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> searchByDenominacion(String denominacion) {
        List<Categoria> categorias = repository.findByDenominacionContainingIgnoreCase(denominacion);
        return categorias.stream()
                .map(this::mapearCategoriaCompleta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDenominacion(String denominacion) {
        return repository.existsByDenominacion(denominacion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSubcategorias(Long idCategoria) {
        return repository.hasSubcategorias(idCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasArticulos(Long idCategoria) {
        Integer count = repository.countArticulosByCategoria(idCategoria);
        return count > 0;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada"));

        // Validar que no tenga subcategorías
        if (hasSubcategorias(id)) {
            throw new IllegalArgumentException("No se puede eliminar una categoría que tiene subcategorías");
        }

        // Validar que no tenga artículos asociados
        if (hasArticulos(id)) {
            throw new IllegalArgumentException("No se puede eliminar una categoría que tiene artículos asociados");
        }

        repository.deleteById(id);
    }

    // Método auxiliar para mapear categoría con información completa
    private CategoriaResponseDTO mapearCategoriaCompleta(Categoria categoria) {
        CategoriaResponseDTO dto = mapper.toDTO(categoria);

        // Obtener cantidad de artículos
        dto.setCantidadArticulos(repository.countArticulosByCategoria(categoria.getIdCategoria()));

        // Si no es subcategoría, obtener sus subcategorías
        if (!categoria.isEsSubcategoria()) {
            List<Categoria> subcategorias = repository.findByCategoriaPadreIdCategoria(categoria.getIdCategoria());
            List<CategoriaSimpleDTO> subcategoriasDTO = subcategorias.stream()
                    .map(sub -> new CategoriaSimpleDTO(
                            sub.getIdCategoria(),
                            sub.getDenominacion(),
                            repository.countArticulosByCategoria(sub.getIdCategoria())
                    ))
                    .collect(Collectors.toList());
            dto.setSubcategorias(subcategoriasDTO);
        }

        return dto;
    }
}