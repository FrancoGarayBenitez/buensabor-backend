package com.elbuensabor.services.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<T, DTO> {
    DTO toDTO(T entity);
    T toEntity(DTO dto);
    void updateEntityFromDTO(DTO dto, @MappingTarget T entity);
}
