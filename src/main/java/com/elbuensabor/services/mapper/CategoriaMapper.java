package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.entities.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper extends BaseMapper<Categoria, CategoriaResponseDTO> {

    @Override
    @Mapping(source = "categoriaPadre.idCategoria", target = "idCategoriaPadre")
    @Mapping(source = "categoriaPadre.denominacion", target = "denominacionCategoriaPadre")
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "cantidadArticulos", ignore = true)
    CategoriaResponseDTO toDTO(Categoria entity);

    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);

    @Override
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Categoria toEntity(CategoriaResponseDTO dto);

    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO dto, @MappingTarget Categoria entity);

    @Override
    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    void updateEntityFromDTO(CategoriaResponseDTO dto, @MappingTarget Categoria entity);
}