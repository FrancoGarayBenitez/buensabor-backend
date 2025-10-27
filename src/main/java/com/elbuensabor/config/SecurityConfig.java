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
        // Adaptador que usa IAuthService para cargar el Usuario por email
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
                // 1. Configuración de CORS: Usa el bean inyectado de CorsConfig.java
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 2. Deshabilitar CSRF para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Definir reglas de autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de acceso PÚBLICO (Anonimo)
                        .requestMatchers("/api/v1/auth/**", "/public/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/articulos/**",
                                "/api/v1/categorias/**",
                                "/api/v1/unidadmedida/**"
                        ).permitAll() // Permite GETs públicos para el catálogo

                        // Endpoints que requieren Roles específicos
                        .requestMatchers("/api/v1/clientes/**").hasAuthority("CLIENTE")
                        .requestMatchers("/api/v1/pedidos/**").hasAuthority("CLIENTE")
                        .requestMatchers("/api/v1/perfil/**").hasAnyAuthority("CLIENTE", "ADMIN") // El endpoint de perfil lo pueden ver ambos roles

                        // Ejemplo de endpoints para gestión interna (ADMIN, COCINERO, etc.)
                        .requestMatchers("/api/v1/estadisticas/**").hasAnyAuthority("ADMIN", "COCINERO", "DELIVERY")

                        // Todas las demás solicitudes requieren autenticación (token válido)
                        .anyRequest().authenticated()
                )

                // 4. Configurar la gestión de sesión como STATELESS (sin estado)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Configurar el proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // 6. Añadir el filtro JWT customizado ANTES del filtro de autenticación estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
