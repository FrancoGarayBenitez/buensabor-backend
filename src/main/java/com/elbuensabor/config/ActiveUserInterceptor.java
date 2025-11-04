package com.elbuensabor.config;

import com.elbuensabor.entities.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para validar que solo usuarios activos puedan acceder
 */
@Component
public class ActiveUserInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ActiveUserInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 1. Verificar si hay un principal autenticado (debería ser nuestra entidad
            // Usuario)
            if (authentication != null && authentication.getPrincipal() instanceof Usuario) {

                // ✅ Obtener el objeto Usuario directamente del contexto
                Usuario usuario = (Usuario) authentication.getPrincipal();
                String email = usuario.getEmail(); // El identificador que usaremos

                // 2. Comprobar el estado activo directamente desde el objeto cargado
                if (!usuario.isActivo()) {
                    logger.warn("❌ Usuario desactivado intentando acceder: {}", email);

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                            {
                                "error": "Usuario desactivado",
                                "message": "Tu cuenta ha sido desactivada. Contacta al administrador.",
                                "code": "USER_DEACTIVATED",
                                "timestamp": "%s"
                            }
                            """.formatted(java.time.Instant.now().toString()));

                    return false; // Bloquear acceso
                }

                logger.debug("✅ Usuario activo validado: {}", email);

            } else if (authentication != null && authentication.getPrincipal().equals("anonymousUser")) {
                // Si es un endpoint protegido pero se permite acceso anónimo (no deberia pasar
                // con SecurityConfig),
                // o si el filtro JWT falló, puedes optar por devolver false o permitir.
                // Ya la capa de Spring Security debería haber bloqueado esto.
                logger.debug(
                        "Solicitud con principal no-Usuario (ej. anónimo), permitiendo si no está bloqueado por SecurityConfig.");
            }

        } catch (Exception e) {
            logger.error("Error validando usuario activo: {}", e.getMessage());
            // En caso de error, permitir acceso para no bloquear la aplicación
        }

        return true; // Permitir acceso (si no fue bloqueado explícitamente)
    }
}