package com.elbuensabor.repository;

import com.elbuensabor.entities.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IDomicilioRepository extends JpaRepository<Domicilio, Long> {

    /**
     * Busca todos los domicilios de un cliente específico
     * Ordenados por principal primero, luego por ID
     */
    @Query("SELECT d FROM Domicilio d WHERE d.cliente.idCliente = :clienteId ORDER BY d.esPrincipal DESC, d.idDomicilio ASC")
    List<Domicilio> findByClienteIdOrderByPrincipal(@Param("clienteId") Long clienteId);

    /**
     * Busca el domicilio principal de un cliente
     */
    @Query("SELECT d FROM Domicilio d WHERE d.cliente.idCliente = :clienteId AND d.esPrincipal = true")
    Optional<Domicilio> findPrincipalByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Busca un domicilio específico que pertenezca a un cliente
     * Útil para validar que el usuario pueda editar/eliminar solo sus domicilios
     */
    @Query("SELECT d FROM Domicilio d WHERE d.idDomicilio = :domicilioId AND d.cliente.idCliente = :clienteId")
    Optional<Domicilio> findByIdAndClienteId(@Param("domicilioId") Long domicilioId, @Param("clienteId") Long clienteId);

    /**
     * Cuenta cuántos domicilios tiene un cliente
     */
    @Query("SELECT COUNT(d) FROM Domicilio d WHERE d.cliente.idCliente = :clienteId")
    long countByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Verifica si un cliente tiene un domicilio principal
     */
    @Query("SELECT COUNT(d) > 0 FROM Domicilio d WHERE d.cliente.idCliente = :clienteId AND d.esPrincipal = true")
    boolean existsPrincipalByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Quita el estado principal de todos los domicilios de un cliente
     * Se usa antes de marcar uno nuevo como principal
     */
    @Modifying
    @Query("UPDATE Domicilio d SET d.esPrincipal = false WHERE d.cliente.idCliente = :clienteId")
    void clearPrincipalForCliente(@Param("clienteId") Long clienteId);

    /**
     * Marca un domicilio específico como principal
     */
    @Modifying
    @Query("UPDATE Domicilio d SET d.esPrincipal = true WHERE d.idDomicilio = :domicilioId AND d.cliente.idCliente = :clienteId")
    int setPrincipal(@Param("domicilioId") Long domicilioId, @Param("clienteId") Long clienteId);

    /**
     * Verifica si existe otro domicilio principal para el cliente (excluyendo uno específico)
     * Útil para validaciones antes de actualizar
     */
    @Query("SELECT COUNT(d) > 0 FROM Domicilio d WHERE d.cliente.idCliente = :clienteId AND d.esPrincipal = true AND d.idDomicilio != :excludeId")
    boolean existsOtherPrincipalForCliente(@Param("clienteId") Long clienteId, @Param("excludeId") Long excludeId);

    // ✅ OPCIONAL: Buscar domicilio de sucursal (donde id_cliente es NULL)
    List<Domicilio> findByClienteIsNull();
}


