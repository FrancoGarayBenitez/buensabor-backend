package com.elbuensabor.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.services.IPerfilService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private static final Logger logger = LoggerFactory.getLogger(PerfilController.class);

    private final IPerfilService perfilService;

    // --- Método utilitario para extracción de Rol ---

    private String getRolFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .orElse(null);
    }

    // ==================== ENDPOINTS DE PERFIL (Acceso general)

    /**
     * GET /api/perfil
     * Obtiene el perfil completo del usuario autenticado (Cliente o Empleado).
     */
    @GetMapping
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        String rol = getRolFromAuthentication(authentication);

        if (rol == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Rol de usuario no encontrado."));
        }

        try {
            // 🎯 Llama al servicio centralizado
            Object perfil = perfilService.obtenerMiPerfil(email, rol);
            return ResponseEntity.ok(perfil);

        } catch (Exception e) {
            logger.error("❌ Error obteniendo perfil de {}: {}", email, e.getMessage());
            // Lanza la excepción (ej. ResourceNotFoundException) para el Handler global
            throw e;
        }
    }

    /**
     * GET /api/perfil/info
     * Obtiene solo la información personal del usuario (DTO para formulario de
     * edición).
     */
    @GetMapping("/info")
    public ResponseEntity<?> getMyProfileInfo(Authentication authentication) {
        String email = authentication.getName();
        String rol = getRolFromAuthentication(authentication);

        // No necesitamos try-catch si el servicio lanza excepciones controladas
        Object perfilInfo = perfilService.obtenerMiPerfilInfo(email, rol);
        return ResponseEntity.ok(perfilInfo);
    }

    /**
     * PUT /api/perfil/info
     * Actualiza solo la información personal del usuario (Solo para CLIENTE).
     */
    @PutMapping("/info")
    public ResponseEntity<ClienteResponseDTO> updateMyProfileInfo(
            @Valid @RequestBody ClientePerfilDTO clientePerfilDTO,
            Authentication authentication) {

        String email = authentication.getName();
        String rol = getRolFromAuthentication(authentication);

        // Validamos la autorización en el controlador (o podríamos hacerlo en el
        // servicio)
        if (!"CLIENTE".equals(rol)) {
            logger.warn("❌ Intento de PUT /perfil/info por rol no permitido: {}", rol);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 🎯 Llama al servicio centralizado de actualización
        ClienteResponseDTO clienteActualizado = perfilService.actualizarMiInfo(email, clientePerfilDTO);

        logger.info("✅ Profile info updated successfully for user: {}", email);
        return ResponseEntity.ok(clienteActualizado);
    }

    /**
     * DELETE /api/perfil
     * Elimina la cuenta del usuario autenticado (Solo para CLIENTE).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMyProfile(Authentication authentication) {
        String email = authentication.getName();
        String rol = getRolFromAuthentication(authentication);

        if (!"CLIENTE".equals(rol)) {
            logger.warn("❌ Intento de DELETE /perfil por rol no permitido: {}", rol);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 🎯 Llama al servicio centralizado de eliminación
        perfilService.eliminarMiCuenta(email);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/perfil/estadisticas
     * Obtiene estadísticas del perfil del usuario
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getMyProfileStats(Authentication authentication) {
        String email = authentication.getName();
        logger.debug("Getting profile statistics for user email: {}", email);

        Map<String, Object> estadisticas = perfilService.obtenerEstadisticasPerfil(email);

        return ResponseEntity.ok(estadisticas);
    }
}
