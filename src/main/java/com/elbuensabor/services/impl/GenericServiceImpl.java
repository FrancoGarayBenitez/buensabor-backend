package com.elbuensabor.services.impl;

import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IGenericService;
import com.elbuensabor.services.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;


public abstract class GenericServiceImpl<T, ID, DTO, R extends JpaRepository<T, ID>, M extends BaseMapper<T, DTO>>
        implements IGenericService<T, ID, DTO> {

    protected final R repository;
    protected final M mapper;  // Mapper de MapStruct
    private final Class<T> entityClass;
    private final Class<DTO> dtoClass;

    protected GenericServiceImpl(R repository,
                                 M mapper,
                                 Class<T> entityClass,
                                 Class<DTO> dtoClass) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    @Override
    @Transactional
    public DTO create(DTO dto) {
        T entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DTO findById(ID id) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso con ID " + id + " no encontrado"));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DTO update(ID id, DTO dto) {
        T existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso con ID " + id + " no encontrado"));

        mapper.updateEntityFromDTO(dto, existingEntity);
        T updatedEntity = repository.save(existingEntity);
        return mapper.toDTO(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso con ID " + id + " no encontrado");
        }
        repository.deleteById(id);
    }
}

