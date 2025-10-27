package com.elbuensabor.services;

import com.elbuensabor.dto.request.ClienteRegisterDTO;
import com.elbuensabor.dto.request.LoginRequestDTO;
import com.elbuensabor.dto.request.PasswordResetRequest;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.dto.response.LoginResponseDTO;
import com.elbuensabor.entities.Usuario;

public interface IAuthService {
    /**
     * Registra un nuevo cliente/usuario.
     * @param request Datos del cliente y credenciales.
     * @return Usuario creado.
     * @throws com.elbuensabor.exceptions.DuplicateResourceException si el email ya existe.
     */
    Usuario register(ClienteRegisterDTO request);

    /**
     * Procesa el login local.
     * @param request Credenciales del usuario (email y password).
     * @return Un DTO que contiene el JWT.
     * @throws org.springframework.security.core.AuthenticationException si las credenciales son inválidas.
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * Método auxiliar para cargar un usuario por email (útil para Spring Security).
     * @param email Email del usuario.
     * @return Usuario encontrado.
     */
    Usuario findByEmail(String email);

    // Inicia el proceso de restablecimiento de contraseña.
    void requestPasswordReset(Usuario usuarioAutenticado);

    // Cambia la contraseña usando un token de reseteo. (Endpoint público)
    void resetPassword(PasswordResetRequest request);

    // Elimina la cuenta del usuario autenticado
    void deleteAccount(Usuario usuarioAutenticado);
}
