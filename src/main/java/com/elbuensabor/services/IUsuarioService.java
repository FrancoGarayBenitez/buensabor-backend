package com.elbuensabor.services;

import com.elbuensabor.dto.response.EmpleadoResponseDTO;

import java.util.List;

/**
 * Interfaz para servicios de gestión de usuarios
 */
public interface IUsuarioService {

    /**
     * Obtiene todos los usuarios para mostrar en la grilla administrativa
     */
    List<EmpleadoResponseDTO> obtenerUsuariosParaGrilla();

    /**
     * Obtiene un usuario específico por ID
     */
    EmpleadoResponseDTO obtenerUsuarioPorId(Long idUsuario);

    /**
     * Cambia el rol de un usuario
     */
    EmpleadoResponseDTO cambiarRol(Long idUsuario, String nuevoRol);

    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    EmpleadoResponseDTO cambiarEstado(Long idUsuario, boolean activo);

    /**
     * Cuenta la cantidad de administradores activos en el sistema
     */
    long contarAdministradoresActivos();

    /**
     * Obtiene el ID de un usuario por su email
     * 
     * @param email Email del usuario a buscar
     * @return ID del usuario o null si no se encuentra
     */
    Long obtenerIdUsuarioPorEmail(String email);

}