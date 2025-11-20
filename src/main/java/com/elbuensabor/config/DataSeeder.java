package com.elbuensabor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.UnidadMedida;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.repository.IUnidadMedidaRepository;
import com.elbuensabor.repository.IUsuarioRepository;

@Component
public class DataSeeder implements CommandLineRunner {
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUnidadMedidaRepository unidadMedidaRepository;

    public DataSeeder(IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            IUnidadMedidaRepository unidadMedidaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        crearAdminInicial();
        crearUnidadesDeMedida();
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

    private void crearUnidadesDeMedida() {
        String[] unidades = { "kg", "g", "l", "ml", "unidad", "paquete", "docena" };
        for (String denom : unidades) {
            if (!unidadMedidaRepository.existsByDenominacionIgnoreCase(denom)) {
                UnidadMedida um = new UnidadMedida();
                um.setDenominacion(denom);
                try {
                    unidadMedidaRepository.save(um);
                    System.out.println("✅ Unidad de medida creada: " + denom);
                } catch (DataIntegrityViolationException ex) {
                    System.out.println("⚠️ Ya existe (concurrency) la unidad: " + denom);
                }
            }
        }
    }
}
