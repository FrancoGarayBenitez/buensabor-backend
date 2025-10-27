package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.DomicilioRequestDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
// 🔑 Importar la entidad Usuario
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.services.IDomicilioPerfilService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de domicilios desde el perfil del usuario
 * Todas las operaciones están restringidas al usuario autenticado (Cliente)
 */
@RestController
@RequestMapping("/api/perfil/domicilios")
public class DomicilioPerfilController {

    private static final Logger logger = LoggerFactory.getLogger(DomicilioPerfilController.class);
    private final IDomicilioPerfilService domicilioPerfilService;

    @Autowired
    public DomicilioPerfilController(IDomicilioPerfilService domicilioPerfilService) {
        this.domicilioPerfilService = domicilioPerfilService;
    }

    /**
     * GET /api/perfil/domicilios
     * Obtiene todos los domicilios del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<DomicilioResponseDTO>> getMisDomicilios(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} ({}) requesting their domicilios",
                usuarioAutenticado.getUsername(), usuarioAutenticado.getIdUsuario());

        List<DomicilioResponseDTO> domicilios = domicilioPerfilService.getMisDomicilios(usuarioAutenticado);

        logger.debug("Returning {} domicilios for user {}", domicilios.size(), usuarioAutenticado.getUsername());
        return ResponseEntity.ok(domicilios);
    }

    /**
     * GET /api/perfil/domicilios/principal
     * Obtiene el domicilio principal del usuario autenticado
     */
    @GetMapping("/principal")
    public ResponseEntity<DomicilioResponseDTO> getMiDomicilioPrincipal(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} requesting their principal domicilio", usuarioAutenticado.getUsername());

        DomicilioResponseDTO domicilioPrincipal = domicilioPerfilService.getMiDomicilioPrincipal(usuarioAutenticado);

        if (domicilioPrincipal != null) {
            return ResponseEntity.ok(domicilioPrincipal);
        } else {
            logger.debug("User {} has no principal domicilio", usuarioAutenticado.getUsername());
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * GET /api/perfil/domicilios/{id}
     * Obtiene un domicilio específico del usuario autenticado
     */
    @GetMapping("/{id}")
    public ResponseEntity<DomicilioResponseDTO> getMiDomicilio(@PathVariable Long id,
                                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} requesting domicilio {}", usuarioAutenticado.getUsername(), id);

        DomicilioResponseDTO domicilio = domicilioPerfilService.getMiDomicilio(usuarioAutenticado, id);
        return ResponseEntity.ok(domicilio);
    }

    /**
     * POST /api/perfil/domicilios
     * Crea un nuevo domicilio para el usuario autenticado
     */
    @PostMapping
    public ResponseEntity<DomicilioResponseDTO> crearMiDomicilio(@Valid @RequestBody DomicilioRequestDTO domicilioDTO,
                                                                 @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} creating new domicilio", usuarioAutenticado.getUsername());

        DomicilioResponseDTO nuevoDomicilio = domicilioPerfilService.crearMiDomicilio(usuarioAutenticado, domicilioDTO);

        logger.debug("Created domicilio {} for user {}", nuevoDomicilio.getIdDomicilio(), usuarioAutenticado.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDomicilio);
    }

    /**
     * PUT /api/perfil/domicilios/{id}
     * Actualiza un domicilio del usuario autenticado
     */
    @PutMapping("/{id}")
    public ResponseEntity<DomicilioResponseDTO> actualizarMiDomicilio(@PathVariable Long id,
                                                                      @Valid @RequestBody DomicilioRequestDTO domicilioDTO,
                                                                      @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} updating domicilio {}", usuarioAutenticado.getUsername(), id);

        DomicilioResponseDTO domicilioActualizado = domicilioPerfilService.actualizarMiDomicilio(
                usuarioAutenticado, id, domicilioDTO);

        logger.debug("Updated domicilio {} for user {}", id, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(domicilioActualizado);
    }

    /**
     * DELETE /api/perfil/domicilios/{id}
     * Elimina un domicilio del usuario autenticado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMiDomicilio(@PathVariable Long id,
                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} deleting domicilio {}", usuarioAutenticado.getUsername(), id);

        domicilioPerfilService.eliminarMiDomicilio(usuarioAutenticado, id);

        logger.debug("Deleted domicilio {} for user {}", id, usuarioAutenticado.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/perfil/domicilios/{id}/principal
     * Marca un domicilio específico como principal
     */
    @PatchMapping("/{id}/principal")
    public ResponseEntity<DomicilioResponseDTO> marcarComoPrincipal(@PathVariable Long id,
                                                                    @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} marking domicilio {} as principal", usuarioAutenticado.getUsername(), id);

        DomicilioResponseDTO domicilioPrincipal = domicilioPerfilService.marcarComoPrincipal(usuarioAutenticado, id);

        logger.debug("Marked domicilio {} as principal for user {}", id, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(domicilioPrincipal);
    }

    /**
     * GET /api/perfil/domicilios/estadisticas
     * Obtiene estadísticas de domicilios del usuario (cantidad, etc.)
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticasDomicilios(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        logger.debug("User {} requesting domicilios statistics", usuarioAutenticado.getUsername());

        long cantidad = domicilioPerfilService.contarMisDomicilios(usuarioAutenticado);
        DomicilioResponseDTO principal = domicilioPerfilService.getMiDomicilioPrincipal(usuarioAutenticado);

        Map<String, Object> estadisticas = Map.of(
                "cantidadTotal", cantidad,
                "tienePrincipal", principal != null,
                "domicilioPrincipal", principal != null ? principal : Map.of()
        );

        return ResponseEntity.ok(estadisticas);
    }
}
