package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.EmpleadoResponseDTO;
import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IUsuarioRepository;
import com.elbuensabor.services.IUsuarioService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de gestión de usuarios
 * Enfocado ahora SOLO en funciones administrativas (CRUD para el Admin).
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> obtenerUsuariosParaGrilla() {
        logger.debug("Obteniendo todos los usuarios para grilla");

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<EmpleadoResponseDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            resultado.add(new EmpleadoResponseDTO(
                    usuario.getIdUsuario(),
                    usuario.getEmail(),
                    usuario.getRol().toString(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.isActivo(),
                    null));
        }

        logger.debug("Retornando {} usuarios", resultado.size());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO obtenerUsuarioPorId(@NonNull Long idUsuario) {
        logger.debug("Obteniendo usuario por ID: {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        return new EmpleadoResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                null);
    }

    // ==================== MÉTODOS DE GESTIÓN ADMINISTRATIVA ====================

    @Override
    @Transactional
    public EmpleadoResponseDTO cambiarRol(@NonNull Long idUsuario, String nuevoRol) {
        logger.info("Cambiando rol de usuario {} a {}", idUsuario, nuevoRol);

        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Validar nuevo rol
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(nuevoRol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + nuevoRol);
        }

        String rolAnterior = usuario.getRol().toString();

        // Actualizar rol en base de datos
        usuario.setRol(rolEnum);
        usuarioRepository.save(usuario);

        logger.info("✅ Rol actualizado en BD para usuario {}: {} -> {}", idUsuario, rolAnterior, nuevoRol);

        // Retornar usuario actualizado directamente
        return new EmpleadoResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                null);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO cambiarEstado(@NonNull Long idUsuario, boolean activo) {
        logger.info("{} usuario {}", activo ? "Activando" : "Desactivando", idUsuario);

        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Actualizar estado
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);

        logger.info("✅ Estado {} para usuario {} ({})",
                activo ? "ACTIVO" : "INACTIVO",
                idUsuario,
                usuario.getEmail());

        // Retornar usuario actualizado directamente
        return new EmpleadoResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                null);
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    @Override
    @Transactional(readOnly = true)
    public long contarAdministradoresActivos() {
        logger.debug("Contando administradores activos");

        long count = usuarioRepository.countByRol(Rol.ADMIN);

        logger.debug("Administradores activos encontrados: {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerIdUsuarioPorEmail(String email) {
        logger.debug("Buscando ID de usuario por email: {}", email);
        return usuarioRepository.findByEmail(email)
                .map(Usuario::getIdUsuario)
                .orElse(null);
    }
}