package com.elbuensabor.services;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface IGenericService<T, ID, DTO> {
    DTO create(DTO dto);
    DTO findById(ID id);
    List<DTO> findAll();
    DTO update(ID id, DTO dto);
    void delete(ID id);
}

