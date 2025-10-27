package com.elbuensabor.services;

import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.dto.response.UsuarioBaseResponseDTO;
import com.elbuensabor.entities.Usuario;

import java.util.List;

/**
 * Interfaz para servicios de gestión de usuarios
 */
public interface IUsuarioService {

    /**
     * Obtiene todos los usuarios para mostrar en la grilla administrativa
     */
    List<UsuarioBaseResponseDTO> obtenerUsuariosParaGrilla();

    /**
     * Obtiene un usuario específico por ID
     */
    UsuarioBaseResponseDTO obtenerUsuarioPorId(Long idUsuario);

    /**
     * Cambia el rol de un usuario
     */
    UsuarioBaseResponseDTO cambiarRol(Long idUsuario, String nuevoRol);

    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    UsuarioBaseResponseDTO cambiarEstado(Long idUsuario, boolean activo);

    /**
     * Cuenta la cantidad de administradores activos en el sistema
     */
    long contarAdministradoresActivos();

    /**
     * Obtiene el ID de un usuario por su email
     * @param email Email del usuario a buscar
     * @return ID del usuario o null si no se encuentra
     */
    Long obtenerIdUsuarioPorEmail(String email);

    Usuario createEmployee(EmpleadoRequestDTO request);

    /**
     * Obtiene los datos básicos del perfil (nombre, apellido, email, rol, imagen)
     * Utilizado para Empleados/Admin en el endpoint /perfil
     */
    UsuarioBaseResponseDTO findBaseProfileByEmail(String email);
}