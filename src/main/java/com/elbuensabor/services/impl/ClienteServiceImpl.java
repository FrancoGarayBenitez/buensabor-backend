package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.entities.Cliente;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IClienteRepository;
import com.elbuensabor.services.IClienteService;
import com.elbuensabor.services.mapper.ClienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de clientes
 */
@Service
public class ClienteServiceImpl extends GenericServiceImpl<Cliente, Long, ClienteResponseDTO, IClienteRepository, ClienteMapper>
        implements IClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);

    @Autowired
    public ClienteServiceImpl(IClienteRepository repository, ClienteMapper mapper) {
        super(repository, mapper, Cliente.class, ClienteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO findByEmail(String email) {
        logger.debug("Finding cliente by email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email no puede ser null o vacío");
        }

        Cliente cliente = repository.findByUsuarioEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Cliente not found for email: {}", email);
                    return new ResourceNotFoundException("Cliente no encontrado con email: " + email);
                });

        logger.debug("Found cliente with ID: {} for email: {}", cliente.getIdCliente(), email);
        return mapper.toDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO update(Long id, ClienteResponseDTO clienteDTO) {
        logger.debug("Updating cliente with ID: {}", id);

        // Buscar cliente existente
        Cliente existingCliente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        // Actualizar solo los campos permitidos (no tocar usuario/auth0)
        updateClienteFields(existingCliente, clienteDTO);

        // Guardar cambios
        Cliente updatedCliente = repository.save(existingCliente);

        logger.debug("Cliente updated successfully with ID: {}", id);
        return mapper.toDTO(updatedCliente);
    }

    /**
     * Actualiza los campos del cliente manteniendo la información de usuario intacta
     */
    private void updateClienteFields(Cliente existingCliente, ClienteResponseDTO clienteDTO) {
        // Actualizar datos básicos del cliente
        if (clienteDTO.getNombre() != null && !clienteDTO.getNombre().trim().isEmpty()) {
            existingCliente.getUsuario().setNombre(clienteDTO.getNombre().trim());
        }

        if (clienteDTO.getApellido() != null && !clienteDTO.getApellido().trim().isEmpty()) {
            existingCliente.getUsuario().setApellido(clienteDTO.getApellido().trim());
        }

        if (clienteDTO.getTelefono() != null) {
            existingCliente.setTelefono(clienteDTO.getTelefono().trim());
        }

        if (clienteDTO.getFechaNacimiento() != null) {
            existingCliente.setFechaNacimiento(clienteDTO.getFechaNacimiento());
        }

        // Actualizar email en Usuario si es diferente y válido
        if (clienteDTO.getEmail() != null &&
                !clienteDTO.getEmail().trim().isEmpty() &&
                !clienteDTO.getEmail().equals(existingCliente.getUsuario().getEmail())) {

            // Verificar que el nuevo email no esté en uso por otro cliente
            if (repository.existsByUsuarioEmailAndIdClienteNot(clienteDTO.getEmail(), existingCliente.getIdCliente())) {
                throw new IllegalArgumentException("El email ya está en uso por otro cliente");
            }

            existingCliente.getUsuario().setEmail(clienteDTO.getEmail().trim());
            logger.debug("Updated email for cliente ID: {}", existingCliente.getIdCliente());
        }

        // TODO: Actualizar domicilios e imagen si están presentes en el DTO
        // Por ahora se mantiene la lógica simple, se puede extender después

        logger.debug("Updated fields for cliente ID: {}", existingCliente.getIdCliente());
    }
}