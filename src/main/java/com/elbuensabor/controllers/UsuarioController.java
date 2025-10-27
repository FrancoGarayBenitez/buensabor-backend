package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.dto.response.UsuarioBaseResponseDTO;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.services.IClienteService;
import com.elbuensabor.services.IUsuarioService;
import com.elbuensabor.services.mapper.ClientePerfilMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de usuarios por parte del administrador
 * Incluye operaciones de cambio de rol, registro de empleados, activación/desactivación, etc.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private final ClientePerfilMapper clientePerfilMapper;

    // REGISTRO DE EMPLEADOS
    @PostMapping("/empleado")
    @PreAuthorize("hasAuthority('ADMIN')") // ¡SOLO EL ADMINISTRADOR PUEDE CREAR EMPLEADOS!
    public ResponseEntity<Usuario> createEmployee(@Valid @RequestBody EmpleadoRequestDTO request) {
        Usuario nuevoEmpleado = usuarioService.createEmployee(request);
        // Aquí podrías devolver un DTO de respuesta simplificado
        return new ResponseEntity<>(nuevoEmpleado, HttpStatus.CREATED);
    }

    // ==================== ENDPOINTS DE LISTADO ====================

    /**
     * GET /api/usuarios/grilla
     * Obtiene todos los usuarios para mostrar en la grilla administrativa
     */
    @GetMapping("/grilla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioBaseResponseDTO>> obtenerUsuariosParaGrilla() {
        logger.info("🎯 Admin solicitando grilla de usuarios");
        List<UsuarioBaseResponseDTO> usuarios = usuarioService.obtenerUsuariosParaGrilla();
        logger.debug("✅ Retornando {} usuarios", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    /**
     * GET /api/usuarios/{id}
     * Obtiene detalles específicos de un usuario
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioBaseResponseDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        logger.debug("Admin solicitando detalles del usuario ID: {}", id);
        UsuarioBaseResponseDTO usuario = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    // ==================== ENDPOINTS DE GESTIÓN DE ROLES ====================

    /**
     * PUT /api/usuarios/rol
     * Cambia el rol de un usuario
     */
    @PutMapping("/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cambiarRolUsuario(
            @Valid @RequestBody CambiarRolRequest request,
            Authentication authentication) {

        String adminEmail = authentication.getName();

        logger.info("🔄 Admin {} cambiando rol de usuario {} a {}",
                adminEmail, request.getIdUsuario(), request.getNuevoRol());

        try {
            // Validaciones de seguridad
            validarCambioRol(request, authentication);

            // Ejecutar cambio
            UsuarioBaseResponseDTO usuarioActualizado = usuarioService.cambiarRol(
                    request.getIdUsuario(),
                    request.getNuevoRol()
            );

            logger.info("✅ Rol cambiado exitosamente para usuario {}: {} -> {}",
                    request.getIdUsuario(),
                    request.getRolAnterior() != null ? request.getRolAnterior() : "N/A",
                    request.getNuevoRol());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rol actualizado correctamente",
                    "data", usuarioActualizado,
                    "cambio", Map.of(
                            "rolAnterior", request.getRolAnterior(),
                            "rolNuevo", request.getNuevoRol(),
                            "usuario", usuarioActualizado.getEmail()
                    )
            ));

        } catch (SecurityException e) {
            logger.warn("❌ Violación de seguridad en cambio de rol: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "code", "SECURITY_VIOLATION"
            ));

        } catch (Exception e) {
            logger.error("❌ Error cambiando rol de usuario {}: {}", request.getIdUsuario(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno al cambiar rol: " + e.getMessage(),
                    "code", "INTERNAL_ERROR"
            ));
        }
    }

    // ==================== ENDPOINTS DE GESTIÓN DE ESTADO ====================

    /**
     * PUT /api/usuarios/estado
     * Activa o desactiva un usuario
     */
    @PutMapping("/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cambiarEstadoUsuario(
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication authentication) {

        String adminEmail = authentication.getName();

        logger.info("🔄 Admin {} {} usuario {}",
                adminEmail,
                request.isActivo() ? "activando" : "desactivando",
                request.getIdUsuario());

        try {
            // Validaciones de seguridad
            validarCambioEstado(request, authentication);

            // Ejecutar cambio
            UsuarioBaseResponseDTO usuarioActualizado = usuarioService.cambiarEstado(
                    request.getIdUsuario(),
                    request.isActivo()
            );

            logger.info("✅ Estado cambiado exitosamente para usuario {}: {}",
                    request.getIdUsuario(), request.isActivo() ? "ACTIVO" : "INACTIVO");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Estado actualizado correctamente",
                    "data", usuarioActualizado,
                    "estado", request.isActivo() ? "ACTIVO" : "INACTIVO"
            ));

        } catch (SecurityException e) {
            logger.warn("❌ Violación de seguridad en cambio de estado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "code", "SECURITY_VIOLATION"
            ));

        } catch (Exception e) {
            logger.error("❌ Error cambiando estado de usuario {}: {}", request.getIdUsuario(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno al cambiar estado: " + e.getMessage(),
                    "code", "INTERNAL_ERROR"
            ));
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida si el cambio de rol es seguro y permitido
     */
    private void validarCambioRol(CambiarRolRequest request, Authentication authentication) {

        String adminEmail = authentication.getName();

        // Obtener ID del usuario actual del JWT
        Long currentUserId = usuarioService.obtenerIdUsuarioPorEmail(adminEmail);

        // No permitir que el admin se quite su propio rol ADMIN
        if (currentUserId.equals(request.getIdUsuario()) &&
                "ADMIN".equals(request.getRolAnterior()) &&
                !"ADMIN".equals(request.getNuevoRol())) {
            throw new SecurityException("No puedes cambiar tu propio rol de administrador");
        }

        // Validar que el nuevo rol sea válido
        if (!esRolValido(request.getNuevoRol())) {
            throw new IllegalArgumentException("Rol inválido: " + request.getNuevoRol());
        }

        // Advertencia especial para asignación de ADMIN
        if ("ADMIN".equals(request.getNuevoRol()) && !"ADMIN".equals(request.getRolAnterior())) {
            logger.warn("⚠️ Admin {} está otorgando permisos de ADMIN a usuario {}",
                    adminEmail, request.getIdUsuario());
        }
    }

    /**
     * Valida si el cambio de estado es seguro y permitido
     */
    private void validarCambioEstado(CambiarEstadoRequest request, Authentication authentication) {

        String adminEmail = authentication.getName();

        // Obtener ID del usuario actual del JWT
        Long currentUserId = usuarioService.obtenerIdUsuarioPorEmail(adminEmail);

        // No permitir que el admin se desactive a sí mismo
        if (currentUserId.equals(request.getIdUsuario()) && !request.isActivo()) {
            throw new SecurityException("No puedes desactivar tu propia cuenta");
        }

        // Verificar que no sea el último admin (si se va a desactivar un admin)
        if (!request.isActivo()) {
            UsuarioBaseResponseDTO usuario = usuarioService.obtenerUsuarioPorId(request.getIdUsuario());
            if ("ADMIN".equals(usuario.getRol())) {
                long adminCount = usuarioService.contarAdministradoresActivos();
                if (adminCount <= 1) {
                    throw new SecurityException("No se puede desactivar el último administrador del sistema");
                }
            }
        }
    }

    /**
     * Verifica si un rol es válido
     */
    private boolean esRolValido(String rol) {
        return List.of("CLIENTE", "CAJERO", "COCINERO", "DELIVERY", "ADMIN").contains(rol);
    }

    // ==================== ENDPOINTS DE PERFIL ====================

    /**
     * GET /api/clientes/perfil
     * Obtiene el perfil del usuario autenticado.
     */
    @GetMapping("/perfil")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        logger.debug("Getting complete profile for user email: {}", email);

        String rol = authentication.getAuthorities().stream()
                // Lógica para extraer el rol del token (limpiando ROLE_)
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .filter(r -> r.equals("CLIENTE") || r.equals("ADMIN") || r.equals("CAJERO") || r.equals("COCINERO") || r.equals("DELIVERY"))
                .findFirst()
                .orElse(null);

        if ("CLIENTE".equals(rol)) {
            // Llama al servicio de Cliente (DTO Extendido)
            ClienteResponseDTO cliente = clienteService.findByEmail(email);
            logger.info("✅ Retornando ClienteResponseDTO para: {}", email);
            return ResponseEntity.ok(cliente);
        }

        if (rol != null) {
            // Llama al servicio de Usuario (DTO Base)
            UsuarioBaseResponseDTO empleado = usuarioService.findBaseProfileByEmail(email);
            logger.info("✅ Retornando UsuarioBaseResponseDTO para: {}", email);
            return ResponseEntity.ok(empleado);
        }

        logger.warn("❌ Acceso denegado: Rol no reconocido para email: {}", email);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Rol de usuario no autorizado."));
    }

    /**
     * GET /api/clientes/perfil/info
     * Obtiene solo la información personal del usuario (sin domicilios)
     * Útil para formularios de edición de perfil
     */
    @GetMapping("/perfil/info")
    public ResponseEntity<?> getMyProfileInfo(Authentication authentication) {
        String email = authentication.getName();
        String rol = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("");

        // Si es CLIENTE, devuelve el DTO específico de edición
        if ("CLIENTE".equals(rol)) {
            ClienteResponseDTO clienteCompleto = clienteService.findByEmail(email);
            ClientePerfilDTO perfilInfo = clientePerfilMapper.responseToPerfilDTO(clienteCompleto);
            return ResponseEntity.ok(perfilInfo);
        }
        // Si es EMPLEADO/ADMIN, solo devolvemos los campos básicos
        if (rol.equals("ADMIN") || rol.equals("CAJERO") || rol.equals("COCINERO") || rol.equals("DELIVERY")) {
            UsuarioBaseResponseDTO empleado = usuarioService.findBaseProfileByEmail(email);
            return ResponseEntity.ok(empleado);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado."));
    }

    /**
     * PUT /api/clientes/perfil/info
     * Actualiza solo la información personal del usuario (sin domicilios)
     * Los domicilios se manejan con el DomicilioPerfilController
     */
    @PutMapping("/perfil/info")
    public ResponseEntity<ClienteResponseDTO> updateMyProfileInfo(@Valid @RequestBody ClientePerfilDTO clientePerfilDTO,
                                                                  Authentication authentication) {
        String email = authentication.getName();
        logger.debug("Updating profile info for user email: {}", email);

        // Buscar cliente actual
        ClienteResponseDTO currentCliente = clienteService.findByEmail(email);

        // Crear DTO para actualización manteniendo email y domicilios
        ClienteResponseDTO clienteParaActualizar = new ClienteResponseDTO();
        clienteParaActualizar.setNombre(clientePerfilDTO.getNombre());
        clienteParaActualizar.setApellido(clientePerfilDTO.getApellido());
        clienteParaActualizar.setTelefono(clientePerfilDTO.getTelefono());
        clienteParaActualizar.setFechaNacimiento(clientePerfilDTO.getFechaNacimiento());
        clienteParaActualizar.setEmail(currentCliente.getEmail()); // Mantener email actual
        clienteParaActualizar.setImagen(clientePerfilDTO.getImagen());
        // Los domicilios se ignoran en el servicio

        // Actualizar usando el ID local
        ClienteResponseDTO clienteActualizado = clienteService.update(currentCliente.getIdCliente(), clienteParaActualizar);

        logger.info("Profile info updated successfully for user: {}", email);
        return ResponseEntity.ok(clienteActualizado);
    }

    /**
     * DELETE /api/clientes/perfil
     * Elimina la cuenta del usuario autenticado
     */
    @DeleteMapping("/perfil")
    public ResponseEntity<Void> deleteMyProfile(Authentication authentication) {
        String email = authentication.getName();
        logger.info("User {} requesting account deletion", email);

        ClienteResponseDTO cliente = clienteService.findByEmail(email);
        clienteService.delete(cliente.getIdCliente());

        logger.info("Cliente account deleted successfully for user: {}", email);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/clientes/perfil/estadisticas
     * Obtiene estadísticas del perfil del usuario
     */
    @GetMapping("/perfil/estadisticas")
    public ResponseEntity<Map<String, Object>> getMyProfileStats(Authentication authentication) {
        String email = authentication.getName();
        logger.debug("Getting profile statistics for user email: {}", email);

        ClienteResponseDTO cliente = clienteService.findByEmail(email);

        Map<String, Object> estadisticas = Map.of(
                "idCliente", cliente.getIdCliente(),
                "nombreCompleto", cliente.getNombre() + " " + cliente.getApellido(),
                "email", cliente.getEmail(),
                "cantidadDomicilios", cliente.getDomicilios() != null ? cliente.getDomicilios().size() : 0,
                "tieneImagen", cliente.getImagen() != null,
                "fechaNacimiento", cliente.getFechaNacimiento()
        );

        return ResponseEntity.ok(estadisticas);
    }

    // ==================== CLASES DE REQUEST ====================

    /**
     * DTO para cambio de rol
     */
    public static class CambiarRolRequest {
        private Long idUsuario;
        private String nuevoRol;
        private String rolAnterior; // Para logging y validación

        // Constructors, getters y setters
        public CambiarRolRequest() {}

        public CambiarRolRequest(Long idUsuario, String nuevoRol, String rolAnterior) {
            this.idUsuario = idUsuario;
            this.nuevoRol = nuevoRol;
            this.rolAnterior = rolAnterior;
        }

        public Long getIdUsuario() { return idUsuario; }
        public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

        public String getNuevoRol() { return nuevoRol; }
        public void setNuevoRol(String nuevoRol) { this.nuevoRol = nuevoRol; }

        public String getRolAnterior() { return rolAnterior; }
        public void setRolAnterior(String rolAnterior) { this.rolAnterior = rolAnterior; }
    }

    /**
     * DTO para cambio de estado
     */
    public static class CambiarEstadoRequest {
        private Long idUsuario;
        private boolean activo;

        // Constructors, getters y setters
        public CambiarEstadoRequest() {}

        public CambiarEstadoRequest(Long idUsuario, boolean activo) {
            this.idUsuario = idUsuario;
            this.activo = activo;
        }

        public Long getIdUsuario() { return idUsuario; }
        public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
    }
}