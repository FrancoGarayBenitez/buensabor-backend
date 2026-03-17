package com.elbuensabor.config;

import com.elbuensabor.services.IAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final IAuthService authService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            @Lazy IAuthService authService,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authService = authService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    // --- BEANS DE AUTENTICACIÓN ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return authService::findByEmail;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // --- CADENA DE FILTROS DE SEGURIDAD ---

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuración de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 2. Deshabilitar CSRF para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Definir reglas de autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // ==================== ACCESO PÚBLICO ====================
                        // Recursos estáticos
                        .requestMatchers("/img/**", "/static/**").permitAll()

                        // Auth y documentación
                        .requestMatchers("/api/auth/**", "/public/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // ✅ CATÁLOGO PÚBLICO PARA CLIENTE (sin autenticación)
                        .requestMatchers(HttpMethod.GET, "/api/catalogo/**").permitAll()

                        // Metadatos públicos (categorías, unidades de medida)
                        .requestMatchers(HttpMethod.GET,
                                "/api/categorias/**",
                                "/api/unidades-medida/**")
                        .permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // ==================== GESTIÓN DE IMÁGENES ====================
                        .requestMatchers(HttpMethod.POST, "/api/imagenes/upload/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/imagenes/upload/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/imagenes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/imagenes/**").authenticated()

                        // ==================== PERFIL DE USUARIO ====================
                        .requestMatchers("/api/perfil").authenticated()

                        // ==================== GESTIÓN DE PEDIDOS ====================

                        // Cliente puede crear y ver sus pedidos
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").hasAuthority("CLIENTE")

                        // ✅ FIX: El endpoint es /cancelar sin /{id}, y también CAJERO/ADMIN pueden
                        // cancelar
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/cancelar")
                        .hasAnyAuthority("CLIENTE", "CAJERO", "ADMIN", "COCINERO", "DELIVERY")

                        // Cajero gestiona pedidos del día y confirmación de pago
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/del-dia").hasAnyAuthority("CAJERO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/confirmar-pago")
                        .hasAnyAuthority("CAJERO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/asignar-delivery")
                        .hasAnyAuthority("CAJERO", "ADMIN")

                        // Cocinero gestiona preparación
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/cocina").hasAnyAuthority("COCINERO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/cambiar-estado")
                        .hasAnyAuthority("ADMIN", "CAJERO", "COCINERO", "DELIVERY")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id}/iniciar-preparacion")
                        .hasAnyAuthority("COCINERO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id}/marcar-listo")
                        .hasAnyAuthority("COCINERO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/extender-tiempo")
                        .hasAnyAuthority("COCINERO", "ADMIN")

                        // Delivery gestiona entregas
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/delivery").hasAnyAuthority("DELIVERY", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id}/marcar-entregado")
                        .hasAnyAuthority("DELIVERY", "ADMIN")

                        // Admin tiene acceso total a pedidos
                        .requestMatchers("/api/pedidos/**").hasAuthority("ADMIN")

                        // ==================== GESTIÓN DE CLIENTES ====================
                        .requestMatchers("/api/clientes/**").hasAnyAuthority("CLIENTE", "ADMIN")

                        // Obtener usuarios por rol (ADMIN / CAJERO (Asigna deliveries en pedido))
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/por-rol/**")
                        .hasAnyAuthority("ADMIN", "CAJERO")

                        // ==================== GESTIÓN ADMINISTRATIVA (SOLO ADMIN) ====================
                        .requestMatchers("/api/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/empleados/**").hasAuthority("ADMIN")

                        // ✅ PROMOCIONES: Admin gestiona, Cliente solo lee
                        .requestMatchers(HttpMethod.GET, "/api/promociones/**").permitAll() // Público para catálogo
                        .requestMatchers(HttpMethod.POST, "/api/promociones/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/promociones/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promociones/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/promociones/**").hasAuthority("ADMIN")

                        // Estadísticas
                        .requestMatchers("/api/estadisticas/**").hasAnyAuthority("ADMIN", "COCINERO", "DELIVERY")

                        // ✅ ARTÍCULOS: Admin/Cocinero gestionan, Cliente solo lee vía /api/catalogo
                        .requestMatchers(HttpMethod.GET, "/api/articulos/**").permitAll() // Público
                        .requestMatchers(HttpMethod.POST, "/api/articulos/**").hasAnyAuthority("ADMIN", "COCINERO")
                        .requestMatchers(HttpMethod.PUT, "/api/articulos/**").hasAnyAuthority("ADMIN", "COCINERO")
                        .requestMatchers(HttpMethod.DELETE, "/api/articulos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/articulos/**").hasAuthority("ADMIN")

                        .requestMatchers("/api/articulos-insumo/**").hasAnyAuthority("ADMIN", "COCINERO")
                        .requestMatchers("/api/articulos-manufacturados/**").hasAnyAuthority("ADMIN", "COCINERO")

                        // ==================== FALLBACK ====================
                        // Todas las demás solicitudes requieren autenticación
                        .anyRequest().authenticated())

                // 4. Configurar la gestión de sesión como STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Configurar el proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // 6. Añadir el filtro JWT customizado
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
