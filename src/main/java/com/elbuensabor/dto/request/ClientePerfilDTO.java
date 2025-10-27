package com.elbuensabor.dto.request;

import com.elbuensabor.dto.request.ImagenDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO específico para editar información personal del perfil
 * No incluye domicilios (se manejan por separado)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePerfilDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    // Email se mantiene en Usuario, no se edita desde aquí
    // Los domicilios se manejan con endpoints separados

    private ImagenDTO imagen; // Opcional
}