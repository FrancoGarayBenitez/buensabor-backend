package com.elbuensabor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.repository.IUsuarioRepository;

@Component
public class DataSeeder implements CommandLineRunner {
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        crearAdminInicial();
    }

    private void crearAdminInicial() {
        // Verificar si el usuario admin ya existe
        if (usuarioRepository.findByEmail("admin@elbuensabor.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("Principal");
            admin.setEmail("admin@elbuensabor.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            usuarioRepository.save(admin);

            System.out.println("✅ ===================================");
            System.out.println("✅ USUARIO ADMIN CREADO EXITOSAMENTE");
            System.out.println("✅ Email: admin@elbuensabor.com");
            System.out.println("✅ Password: Admin123!");
            System.out.println("✅ ===================================");

        } else {
            System.out.println("El usuario admin ya existe.");
        }
    }
}
