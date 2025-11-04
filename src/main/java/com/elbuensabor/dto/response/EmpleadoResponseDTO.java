package com.elbuensabor.dto.response;

import com.elbuensabor.dto.request.ImagenDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmpleadoResponseDTO {
    private Long idUsuario;
    private String email;
    private String rol;
    private String nombre;
    private String apellido;
    private boolean activo;
    private ImagenDTO imagen;
}