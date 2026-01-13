package com.elbuensabor.services.mapper;

import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.entities.Imagen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImagenMapper {

    @Mapping(source = "idImagen", target = "idImagen")
    ImagenDTO toDTO(Imagen entity);

    // No se define un toEntity ya que la creación de imágenes
    // usualmente implica lógica de guardado de archivos que se maneja en el
    // servicio.
}
