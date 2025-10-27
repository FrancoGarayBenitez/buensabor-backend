package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.dto.request.ImagenDTO;
import com.elbuensabor.dto.response.UsuarioBaseResponseDTO;
import com.elbuensabor.entities.Cliente;
import com.elbuensabor.entities.Imagen;
import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IClienteRepository;
import com.elbuensabor.repository.IUsuarioRepository;
import com.elbuensabor.services.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final IUsuarioRepository usuarioRepository;
    private final IClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioBaseResponseDTO> obtenerUsuariosParaGrilla() {
        logger.debug("Obteniendo todos los usuarios para grilla");

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioBaseResponseDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            resultado.add(new UsuarioBaseResponseDTO(
                    usuario.getIdUsuario(),
                    usuario.getEmail(),
                    usuario.getRol().toString(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.isActivo(),
                    null
            ));
        }

        logger.debug("Retornando {} usuarios", resultado.size());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioBaseResponseDTO obtenerUsuarioPorId(Long idUsuario) {
        logger.debug("Obteniendo usuario por ID: {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        return new UsuarioBaseResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                null
        );
    }

    @Override
    @Transactional
    public UsuarioBaseResponseDTO cambiarRol(Long idUsuario, String nuevoRol) {
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

        // Retornar usuario actualizado
        return obtenerUsuarioPorId(idUsuario);
    }

    @Override
    @Transactional
    public UsuarioBaseResponseDTO cambiarEstado(Long idUsuario, boolean activo) {
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

        // Retornar usuario actualizado
        return obtenerUsuarioPorId(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarAdministradoresActivos() {
        logger.debug("Contando administradores activos");

        // TODO: Si tienes campo 'activo', usar: usuarioRepository.countByRolAndActivo(Rol.ADMIN, true)
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

    @Override
    @Transactional
    public Usuario createEmployee(EmpleadoRequestDTO request){
        if(usuarioRepository.findByEmail(request.getEmail()).isPresent()){
            throw new DuplicateResourceException("El email ya está en uso: " + request.getEmail());
        }

        // Validar y asignar rol
        Rol rolAsignado;
        try {
            rolAsignado = Rol.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Rol inválido: " + request.getRol());
        }

        // Prohibir la creación de usuarios con rol CLIENTE
        if(rolAsignado.equals(Rol.CLIENTE)){
            throw new IllegalArgumentException("No se puede crear un usuario con rol CLIENTE a través de este método.");
        }

        // Crear usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setRol(rolAsignado);
        nuevoUsuario.setActivo(true);

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioBaseResponseDTO findBaseProfileByEmail(String email) {
        logger.debug("Buscando perfil base por email: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        ImagenDTO imagenDto = null; // Cambiado a ImagenDTO

        // 1. Si el usuario es un Cliente, buscar su imagen de perfil
        if (usuario.getRol().equals(Rol.CLIENTE)) {
            Optional<Cliente> clienteOptional = clienteRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());

            if (clienteOptional.isPresent()) {
                Cliente cliente = clienteOptional.get();
                Imagen imagen = cliente.getImagen();

                if (imagen != null) {
                    String url = imagen.getUrl();

                    // 2. Mapeamos la entidad Imagen a su DTO solo si la URL es válida
                    if (url != null && !url.isEmpty()) {
                        imagenDto = new ImagenDTO(
                                imagen.getIdImagen(),
                                imagen.getDenominacion(),
                                url
                        );
                    }
                }
            }
        }

        // Mapeamos a DTO
        return new UsuarioBaseResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                imagenDto // Ahora pasa el ImagenDTO (o null)
        );
    }
}