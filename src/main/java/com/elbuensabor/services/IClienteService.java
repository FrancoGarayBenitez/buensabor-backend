package com.elbuensabor.services;

import com.elbuensabor.dto.response.ClienteResponseDTO;
import com.elbuensabor.entities.Cliente;

/**
 * Servicio para manejo de clientes
 * Extends IGenericService para operaciones CRUD básicas
 */
public interface IClienteService extends IGenericService<Cliente, Long, ClienteResponseDTO> {
    /**
     * Busca un cliente por email
     * Útil para migraciones y validaciones
     *
     * @param email Email del cliente
     * @return ClienteResponseDTO del cliente encontrado
     * @throws ResourceNotFoundException si no se encuentra el cliente
     */
    ClienteResponseDTO findByEmail(String email);
}