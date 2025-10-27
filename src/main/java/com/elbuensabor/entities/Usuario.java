package com.elbuensabor.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.CLIENTE;

    @OneToOne(mappedBy = "usuario")
    private Cliente cliente; // Relación solo para roles CLIENTE

    @Column(nullable = false)
    private boolean activo = true;

    // Token temporal para el restablecimiento de contraseña
    @Column(name = "reset_token")
    private String resetToken;

    // Tiempo de expiración del token de reseteo (en milisegundos)
    @Column(name = "token_expiration_time")
    private Long tokenExpirationTime;

    // --- IMPLEMENTACIÓN DE USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte el enum Rol a la autoridad que Spring Security entenderá
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getUsername() {
        // El email es el username para el login
        return email;
    }

    // Los siguientes métodos son para control de estado de cuenta
    @Override
    public boolean isAccountNonExpired() {
        return true; // Simple, no implementamos lógica de expiración de cuenta
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Simple, no implementamos lógica de bloqueo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Simple, no implementamos lógica de expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return activo; // Usamos tu campo 'activo'
    }

    // Ya tienes el método getPassword() gracias a Lombok @Data
}
