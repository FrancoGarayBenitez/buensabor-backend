package com.elbuensabor.repository;

import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Cuenta usuarios por rol
     */
    long countByRol(Rol rol);

    /**
     * Cuenta usuarios activos por rol (si tienes campo activo)
     */
    // long countByRolAndActivo(Rol rol, boolean activo);

    /**
     * Buscar usuario por token de reseteo
     */
    Optional<Usuario> findByResetToken(String resetToken);
}