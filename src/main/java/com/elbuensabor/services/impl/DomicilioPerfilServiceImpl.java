package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.DomicilioRequestDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.entities.Cliente;
import com.elbuensabor.entities.Domicilio;
import com.elbuensabor.entities.Rol;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IDomicilioRepository;
import com.elbuensabor.services.IDomicilioPerfilService;
import com.elbuensabor.services.mapper.DomicilioMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de domicilios para perfil de usuario
 * Maneja todas las operaciones de domicilios restringidas al usuario autenticado
 */
@Service
public class DomicilioPerfilServiceImpl implements IDomicilioPerfilService {

    private static final Logger logger = LoggerFactory.getLogger(DomicilioPerfilServiceImpl.class);
    private final IDomicilioRepository domicilioRepository;
    private final DomicilioMapper domicilioMapper;

    @Autowired
    public DomicilioPerfilServiceImpl(IDomicilioRepository domicilioRepository,
                                      DomicilioMapper domicilioMapper) {
        this.domicilioRepository = domicilioRepository;
        this.domicilioMapper = domicilioMapper;
    }

    // Helper para obtener Cliente
    private Cliente getClienteValidated(Usuario usuario) {
        if (usuario.getRol() != Rol.CLIENTE) {
            throw new SecurityException("Acceso denegado: El flujo de domicilios es solo para Clientes. Rol actual: " + usuario.getRol());
        }
        Cliente cliente = usuario.getCliente();
        if (cliente == null) {
            throw new ResourceNotFoundException("Error de consistencia: El Usuario con rol CLIENTE (ID: " + usuario.getIdUsuario() + ") no tiene un registro de Cliente asociado.");
        }
        return cliente;
    }


    // --- MÉTODOS DE LA INTERFAZ ---
    @Override
    @Transactional(readOnly = true)
    public List<DomicilioResponseDTO> getMisDomicilios(Usuario usuarioAutenticado) {
        logger.debug("Getting domicilios for user ID: {}", usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        List<Domicilio> domicilios = domicilioRepository.findByClienteIdOrderByPrincipal(clienteId);

        return domicilios.stream()
                .map(domicilioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DomicilioResponseDTO getMiDomicilioPrincipal(Usuario usuarioAutenticado) {
        logger.debug("Getting principal domicilio for user ID: {}", usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        return domicilioRepository.findPrincipalByClienteId(clienteId)
                .map(domicilioMapper::toResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public DomicilioResponseDTO crearMiDomicilio(Usuario usuarioAutenticado, DomicilioRequestDTO domicilioDTO) {
        logger.debug("Creating new domicilio for user ID: {}", usuarioAutenticado.getIdUsuario());

        Cliente cliente = getClienteValidated(usuarioAutenticado);
        Long clienteId = cliente.getIdCliente();

        // Si será principal, quitar principal de los demás
        if (Boolean.TRUE.equals(domicilioDTO.getEsPrincipal())) {
            domicilioRepository.clearPrincipalForCliente(cliente.getIdCliente());
            logger.debug("Cleared principal status for other domicilios of cliente: {}", cliente.getIdCliente());
        }
        // Si es el primer domicilio, marcarlo como principal automáticamente
        else if (domicilioRepository.countByClienteId(cliente.getIdCliente()) == 0) {
            domicilioDTO.setEsPrincipal(true);
            logger.debug("First domicilio for cliente {}, marking as principal", cliente.getIdCliente());
        }

        Domicilio nuevoDomicilio = domicilioMapper.toEntity(domicilioDTO);
        nuevoDomicilio.setCliente(cliente);

        Domicilio domicilioGuardado = domicilioRepository.save(nuevoDomicilio);
        return domicilioMapper.toResponseDTO(domicilioGuardado);
    }

    @Override
    @Transactional
    public DomicilioResponseDTO actualizarMiDomicilio(Usuario usuarioAutenticado, Long domicilioId, DomicilioRequestDTO domicilioDTO) {
        logger.debug("Updating domicilio {} for user ID: {}", domicilioId, usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        Domicilio domicilioExistente = domicilioRepository.findByIdAndClienteId(domicilioId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio no encontrado o no pertenece al usuario"));

        // Si será principal, quitar principal de los demás
        if (Boolean.TRUE.equals(domicilioDTO.getEsPrincipal()) && !domicilioExistente.getEsPrincipal()) {
            domicilioRepository.clearPrincipalForCliente(clienteId);
            logger.debug("Cleared principal status for other domicilios of cliente: {}", clienteId);
        }

        Domicilio domicilioActualizado = domicilioRepository.save(domicilioExistente);
        return domicilioMapper.toResponseDTO(domicilioActualizado);
    }

    @Override
    @Transactional
    public void eliminarMiDomicilio(Usuario usuarioAutenticado, Long domicilioId) {
        logger.debug("Deleting domicilio {} for user ID: {}", domicilioId, usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        // Validar que el domicilio pertenezca al usuario
        Domicilio domicilio = domicilioRepository.findByIdAndClienteId(domicilioId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio no encontrado o no pertenece al usuario"));

        // Verificar que no sea el último domicilio
        long cantidadDomicilios = domicilioRepository.countByClienteId(clienteId);
        if (cantidadDomicilios <= 1) {
            throw new IllegalStateException("No se puede eliminar el último domicilio. El cliente debe tener al menos uno.");
        }

        domicilioRepository.delete(domicilio);
    }

    @Override
    @Transactional
    public DomicilioResponseDTO marcarComoPrincipal(Usuario usuarioAutenticado, Long domicilioId) {
        logger.debug("Marking domicilio {} as principal for user ID: {}", domicilioId, usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        // Validar que el domicilio pertenezca al usuario
        Domicilio domicilio = domicilioRepository.findByIdAndClienteId(domicilioId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio no encontrado o no pertenece al usuario"));

        // Quitar principal de todos los domicilios del cliente
        domicilioRepository.clearPrincipalForCliente(clienteId);

        // Marcar este como principal
        domicilio.setEsPrincipal(true);

        Domicilio domicilioActualizado = domicilioRepository.save(domicilio);
        return domicilioMapper.toResponseDTO(domicilioActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public DomicilioResponseDTO getMiDomicilio(Usuario usuarioAutenticado, Long domicilioId) {
        logger.debug("Getting domicilio {} for user ID: {}", domicilioId, usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();

        Domicilio domicilio = domicilioRepository.findByIdAndClienteId(domicilioId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio no encontrado o no pertenece al usuario"));

        return domicilioMapper.toResponseDTO(domicilio);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarMisDomicilios(Usuario usuarioAutenticado) {
        logger.debug("Counting domicilios for user ID: {}", usuarioAutenticado.getIdUsuario());

        Long clienteId = getClienteValidated(usuarioAutenticado).getIdCliente();
        return domicilioRepository.countByClienteId(clienteId);
    }
}