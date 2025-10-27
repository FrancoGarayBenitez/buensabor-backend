package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.CategoriaRequestDTO;
import com.elbuensabor.dto.response.CategoriaResponseDTO;
import com.elbuensabor.entities.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper extends BaseMapper<Categoria, CategoriaResponseDTO> {

    // ENTITY → RESPONSE DTO
    @Override
    @Mapping(source = "categoriaPadre.idCategoria", target = "idCategoriaPadre")
    @Mapping(source = "categoriaPadre.denominacion", target = "denominacionCategoriaPadre")
    @Mapping(target = "subcategorias", ignore = true) // Se mapea manualmente en el service
    @Mapping(target = "cantidadArticulos", ignore = true) // Se calcula en el service
    CategoriaResponseDTO toDTO(Categoria entity);

    // REQUEST DTO → ENTITY (para crear)
    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true) // Se asigna manualmente en el service
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);

    // RESPONSE DTO → ENTITY (para operaciones genéricas)
    @Override
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Categoria toEntity(CategoriaResponseDTO dto);

    // UPDATE desde REQUEST DTO
    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true) // Se maneja en el service
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO dto, @MappingTarget Categoria entity);

    // UPDATE desde RESPONSE DTO (para métodos genéricos)
    @Override
    @Mapping(target = "idCategoria", ignore = true)
    @Mapping(target = "articulos", ignore = true)
    @Mapping(target = "categoriaPadre", ignore = true)
    @Mapping(target = "subcategorias", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    void updateEntityFromDTO(CategoriaResponseDTO dto, @MappingTarget Categoria entity);
}