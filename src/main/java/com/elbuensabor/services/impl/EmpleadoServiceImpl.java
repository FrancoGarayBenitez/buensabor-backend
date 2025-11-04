package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.exceptions.DuplicateResourceException;
import com.elbuensabor.repository.IUsuarioRepository;
import com.elbuensabor.services.IEmpleadoService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Roles válidos para empleados (excluimos CLIENTE)
    private static final List<Rol> ROLES_EMPLEADOS = Arrays.asList(
            Rol.ADMIN, Rol.COCINERO, Rol.CAJERO, Rol.DELIVERY);

    public EmpleadoServiceImpl(IUsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario registrarEmpleado(EmpleadoRequestDTO request, Usuario adminCreador) {
        // Validar que quien crea sea ADMIN
        if (adminCreador.getRol() != Rol.ADMIN) {
            throw new AccessDeniedException("Solo los administradores pueden crear empleados");
        }
        // Validar que el rol sea válido para empleados
        if (!ROLES_EMPLEADOS.contains(request.getRol())) {
            throw new IllegalArgumentException("Rol no válido para empleados: " + request.getRol());
        }

        // Verificar email único
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("El email ya está registrado");
        }

        // Crear nuevo empleado (solo Usuario, sin Cliente)
        Usuario empleado = new Usuario();
        empleado.setNombre(request.getNombre());
        empleado.setApellido(request.getApellido());
        empleado.setEmail(request.getEmail());
        empleado.setPassword(passwordEncoder.encode(request.getPassword()));
        empleado.setRol(request.getRol());
        empleado.setActivo(true);

        return usuarioRepository.save(empleado);
    }
}