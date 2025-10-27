package com.elbuensabor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para login.
 * Contiene el token JWT, el email y el rol del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token; // Token JWT
    private String email; // Email del usuario
    private String rol;   // Rol del usuario
}