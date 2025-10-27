package com.elbuensabor.services;

import com.elbuensabor.dto.request.DomicilioRequestDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.entities.Usuario;

import java.util.List;

/**
 * Servicio para gestión de domicilios desde el perfil del usuario
 * Todas las operaciones están restringidas al usuario autenticado
 */
public interface IDomicilioPerfilService {

    /**
     * Obtiene todos los domicilios del usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @return Lista de domicilios del usuario
     */
    List<DomicilioResponseDTO> getMisDomicilios(Usuario usuarioAutenticado);

    /**
     * Obtiene el domicilio principal del usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @return Domicilio principal o null si no tiene
     */
    DomicilioResponseDTO getMiDomicilioPrincipal(Usuario usuarioAutenticado);

    /**
     * Crea un nuevo domicilio para el usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @param domicilioDTO Datos del nuevo domicilio
     * @return Domicilio creado
     */
    DomicilioResponseDTO crearMiDomicilio(Usuario usuarioAutenticado, DomicilioRequestDTO domicilioDTO);

    /**
     * Actualiza un domicilio del usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @param domicilioId ID del domicilio a actualizar
     * @param domicilioDTO Nuevos datos del domicilio
     * @return Domicilio actualizado
     */
    DomicilioResponseDTO actualizarMiDomicilio(Usuario usuarioAutenticado, Long domicilioId, DomicilioRequestDTO domicilioDTO);

    /**
     * Elimina un domicilio del usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @param domicilioId ID del domicilio a eliminar
     */
    void eliminarMiDomicilio(Usuario usuarioAutenticado, Long domicilioId);

    /**
     * Marca un domicilio específico como principal
     *
     * @param usuarioAutenticado Usuario autenticado
     * @param domicilioId ID del domicilio a marcar como principal
     * @return Domicilio marcado como principal
     */
    DomicilioResponseDTO marcarComoPrincipal(Usuario usuarioAutenticado, Long domicilioId);

    /**
     * Obtiene un domicilio específico del usuario autenticado
     *
     * @param usuarioAutenticado Usuario autenticado
     * @param domicilioId ID del domicilio a buscar
     * @return Domicilio encontrado
     */
    DomicilioResponseDTO getMiDomicilio(Usuario usuarioAutenticado, Long domicilioId);

    /**
     * Cuenta cuántos domicilios tiene el usuario
     *
     * @param usuarioAutenticado Usuario autenticado
     * @return Cantidad de domicilios
     */
    long contarMisDomicilios(Usuario usuarioAutenticado);
}