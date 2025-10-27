package com.elbuensabor.config;

import com.elbuensabor.services.IAuthService;
import com.elbuensabor.services.impl.JwtService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.context.annotation.Lazy; // <-- NUEVA IMPORTACIÓN
import org.slf4j.Logger; // <-- IMPORTACIÓN AÑADIDA
import org.slf4j.LoggerFactory; // <-- IMPORTACIÓN AÑADIDA
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Definición del Logger para ser utilizado
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final IAuthService authService; // Inyectamos el servicio para cargar UserDetails

    // Aplicamos @Lazy al IAuthService para romper el ciclo de dependencia
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy IAuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verificar si el encabezado Authorization está presente y en formato Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el JWT (omitiendo "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 3. Extraer el email del token
            userEmail = jwtService.extractUsername(jwt);

            // 4. Si se extrajo el email y el usuario no está ya autenticado en el contexto
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Cargar UserDetails (usando IAuthService)
                // Usamos una implementación sencilla de UserDetails basada en tu entidad Usuario:
                UserDetails userDetails = authService.findByEmail(userEmail);

                // 6. Validar el token y la expiración
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 7. Crear el objeto de autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No usamos credenciales (contraseña) aquí
                            userDetails.getAuthorities() // Asignar roles/permisos
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 8. Establecer el usuario como autenticado en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Logear o manejar la excepción de un token inválido/expirado
            logger.warn("JWT inválido o expirado: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
