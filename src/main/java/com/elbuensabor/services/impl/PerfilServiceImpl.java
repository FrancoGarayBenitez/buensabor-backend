package com.elbuensabor.services.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elbuensabor.dto.request.ClientePerfilDTO;
import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.dto.response.EmpleadoResponseDTO;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IUsuarioRepository;
import com.elbuensabor.services.IClienteService;
import com.elbuensabor.services.IPerfilService;
import com.elbuensabor.services.mapper.ClientePerfilMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements IPerfilService {

    private static final Logger logger = LoggerFactory.getLogger(PerfilServiceImpl.class);

    // Inyectamos los servicios específicos que contienen los datos
    private final IUsuarioRepository usuarioRepository;
    private final IClienteService clienteService;
    private final ClientePerfilMapper clientePerfilMapper; // Para mapear DTOs de edición

    @Override
    @Transactional(readOnly = true)
    public Object obtenerMiPerfil(String email, String rol) {
        logger.info("Obteniendo perfil para email: {} con rol: {}", email, rol);

        if ("CLIENTE".equals(rol)) {
            return clienteService.findByEmail(email);
        }

        // Si es EMPLEADO/ADMIN, se busca el perfil base usando lógica local.
        if (List.of("ADMIN", "CAJERO", "COCINERO", "DELIVERY").contains(rol)) {
            // Se usa un método privado para encapsular la lógica de mapeo
            return getEmpleadoProfile(email);
        }

        throw new ResourceNotFoundException("Rol de usuario inválido o no manejado para perfil: " + rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Object obtenerMiPerfilInfo(String email, String rol) {
        logger.info("Obteniendo info de perfil (edición) para email: {} con rol: {}", email, rol);

        if ("CLIENTE".equals(rol)) {
            ClienteResponseDTO clienteCompleto = clienteService.findByEmail(email);
            return clientePerfilMapper.responseToPerfilDTO(clienteCompleto);
        }

        // Si es EMPLEADO/ADMIN, se usa el mismo perfil base
        if (List.of("ADMIN", "CAJERO", "COCINERO", "DELIVERY").contains(rol)) {
            return getEmpleadoProfile(email);
        }

        throw new ResourceNotFoundException("Rol de usuario inválido o no manejado para info de perfil: " + rol);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizarMiInfo(String email, ClientePerfilDTO perfilDTO) {
        logger.info("Actualizando info de perfil de Cliente con email: {}", email);

        // 1. Buscar cliente actual para obtener el ID
        ClienteResponseDTO currentCliente = clienteService.findByEmail(email);

        // 2. Crear DTO para actualización manteniendo datos inmutables (email, ID,
        // etc.)
        ClienteResponseDTO clienteParaActualizar = new ClienteResponseDTO();
        clienteParaActualizar.setNombre(perfilDTO.getNombre());
        clienteParaActualizar.setApellido(perfilDTO.getApellido());
        clienteParaActualizar.setTelefono(perfilDTO.getTelefono());
        clienteParaActualizar.setFechaNacimiento(perfilDTO.getFechaNacimiento());
        clienteParaActualizar.setEmail(currentCliente.getEmail()); // Mantener email actual
        clienteParaActualizar.setImagen(perfilDTO.getImagen());

        // 3. Actualizar
        // El clienteService.update() se encarga de ignorar los domicilios si no están
        // en el DTO
        return clienteService.update(currentCliente.getIdCliente(), clienteParaActualizar);
    }

    @Override
    @Transactional
    public void eliminarMiCuenta(String email) {
        logger.warn("Solicitud de eliminación de cuenta para Cliente con email: {}", email);

        ClienteResponseDTO cliente = clienteService.findByEmail(email);
        clienteService.delete(cliente.getIdCliente());

        logger.info("✅ Cuenta de cliente eliminada exitosamente: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasPerfil(String email) {
        logger.debug("Obteniendo estadísticas de perfil para email: {}", email);

        ClienteResponseDTO cliente = clienteService.findByEmail(email);

        Map<String, Object> estadisticas = Map.of(
                "idCliente", cliente.getIdCliente(),
                "nombreCompleto", cliente.getNombre() + " " + cliente.getApellido(),
                "email", cliente.getEmail(),
                "cantidadDomicilios", cliente.getDomicilios() != null ? cliente.getDomicilios().size() : 0,
                "tieneImagen", cliente.getImagen() != null,
                "fechaNacimiento", cliente.getFechaNacimiento());

        return estadisticas;
    }

    /**
     * Solo se usa para roles no-Cliente.
     */
    private EmpleadoResponseDTO getEmpleadoProfile(String email) {
        logger.debug("Construyendo perfil de Empleado/Admin localmente para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Para Empleados, la Imagen es siempre NULL en el EmpleadoResponseDTO
        // Si en el futuro los empleados tienen imagen, la lógica de búsqueda de imagen
        // iría aquí

        return new EmpleadoResponseDTO(
                usuario.getIdUsuario(),
                usuario.getEmail(),
                usuario.getRol().toString(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.isActivo(),
                null // ImagenDTO (o null) para Empleado/Admin
        );
    }
}
