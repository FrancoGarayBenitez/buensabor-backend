package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IClienteService;
import com.elbuensabor.services.mapper.ClientePerfilMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones CRUD de clientes
 * El registro se maneja en AuthController
 *
 * Incluye funcionalidades específicas de perfil y cambio de contraseña
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);
    private final IClienteService clienteService;


    @Autowired
    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * GET /api/clientes/me
     * Endpoint específico para MercadoPago integration con mejor manejo de errores
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentClienteForMercadoPago(Authentication authentication) {
        try {
            // Obtener el email del Principal (el Subject del JWT local)
            String email = authentication.getName();
            logger.debug("🔍 /api/clientes/me called for email: {}", email);

            // Buscar cliente por email
            ClienteResponseDTO cliente = clienteService.findByEmail(email);

            // Formatear respuesta específicamente para MercadoPago
            Map<String, Object> response = Map.of(
                    "idCliente", cliente.getIdCliente(),
                    "idUsuario", cliente.getIdUsuario(),
                    "emailComprador", cliente.getEmail(),
                    "nombreComprador", cliente.getNombre(),
                    "apellidoComprador", cliente.getApellido(),
                    "email", email,
                    "rol", cliente.getRol() != null ? cliente.getRol() : "CLIENTE"
            );

            logger.info("✅ Cliente data retrieved successfully for user: {} - Cliente ID: {}",
                    email, cliente.getIdCliente());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            String email = authentication != null ? authentication.getName() : "unknown";
            logger.error("❌ Cliente not found for email: {} - Error: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Cliente no encontrado",
                            "details", "No se encontró un cliente asociado con este usuario",
                            "code", "CLIENTE_NOT_FOUND",
                            "email", email
                    ));

        } catch (Exception e) {
            String email = authentication != null ? authentication.getName() : "unknown";
            logger.error("❌ Error interno en /api/clientes/me para user {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error interno del servidor",
                            "details", e.getMessage(),
                            "code", "INTERNAL_ERROR",
                            "timestamp", java.time.Instant.now().toString()
                    ));
        }
    }
    // ==================== ENDPOINTS ADMINISTRATIVOS ====================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes() {
        logger.debug("Admin requesting all clientes");
        List<ClienteResponseDTO> clientes = clienteService.findAll();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteSecurityService.isOwner(authentication.name, #id)")
    public ResponseEntity<ClienteResponseDTO> getClienteById(@PathVariable Long id) {
        logger.debug("Getting cliente with ID: {}", id);
        ClienteResponseDTO cliente = clienteService.findById(id);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteSecurityService.isOwner(authentication.name, #id)")
    public ResponseEntity<ClienteResponseDTO> updateCliente(@PathVariable Long id,
                                                            @Valid @RequestBody ClienteResponseDTO clienteDTO) {
        logger.debug("Updating cliente with ID: {}", id);
        ClienteResponseDTO clienteActualizado = clienteService.update(id, clienteDTO);
        return ResponseEntity.ok(clienteActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        logger.info("Admin deleting cliente with ID: {}", id);
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}