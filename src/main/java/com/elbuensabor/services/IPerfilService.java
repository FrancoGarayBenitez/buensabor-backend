package com.elbuensabor.services;

import java.util.Map;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;

public interface IPerfilService {
    /**
     * Obtiene el DTO de perfil específico basado en el email y el rol.
     * Centraliza la lógica de buscar Cliente o Empleado.
     * 
     * @param email Email del usuario autenticado.
     * @param rol   Rol principal del usuario (ADMIN, CLIENTE, etc.).
     * @return ClienteResponseDTO (si es CLIENTE) o EmpleadoResponseDTO (si es otro
     *         rol).
     */
    Object obtenerMiPerfil(String email, String rol);

    /**
     * Obtiene solo la información básica (DTO de edición).
     */
    Object obtenerMiPerfilInfo(String email, String rol);

    /**
     * Actualiza los campos personales del cliente.
     */
    ClienteResponseDTO actualizarMiInfo(String email, ClientePerfilDTO perfilDTO);

    /**
     * Elimina la cuenta del cliente.
     */
    void eliminarMiCuenta(String email);

    /**
     * Obtiene estadísticas del perfil del usuario.
     * 
     * @param email Email del usuario autenticado.
     * @return Map con las estadísticas del perfil.
     */
    Map<String, Object> obtenerEstadisticasPerfil(String email);
}
