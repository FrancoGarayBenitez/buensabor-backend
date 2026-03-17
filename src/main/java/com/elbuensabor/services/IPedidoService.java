package com.elbuensabor.services;

import com.elbuensabor.dto.request.pedido.*;
import com.elbuensabor.dto.response.pedido.*;
import com.elbuensabor.entities.Usuario;

import java.time.LocalDate;
import java.util.List;

public interface IPedidoService {

    // ==================== CREACIÓN DE PEDIDOS ====================

    /**
     * Crea un nuevo pedido (Usuario CLIENTE autenticado)
     */
    PedidoClienteResponse crearPedido(CrearPedidoRequest request, Usuario usuarioAutenticado);

    // ==================== CONSULTAS POR ROL ====================

    /**
     * Obtiene todos los pedidos (ADMIN)
     */
    List<PedidoResponse> listarTodosPedidos();

    /**
     * Obtiene pedidos filtrados por estado (ADMIN/CAJERO)
     */
    List<PedidoResponse> listarPedidosPorEstado(String estado);

    /**
     * Obtiene pedidos del día actual (CAJERO)
     */
    List<PedidoCajeroResponse> listarPedidosDelDia();

    /**
     * Obtiene pedidos de una fecha específica (ADMIN/CAJERO)
     */
    List<PedidoResponse> listarPedidosPorFecha(LocalDate fecha);

    /**
     * Obtiene pedidos en preparación o pendientes (COCINERO)
     */
    List<PedidoCocineroResponse> listarPedidosCocina();

    /**
     * Obtiene pedidos listos para entregar (DELIVERY)
     */
    List<PedidoDeliveryResponse> listarPedidosDelivery(Usuario usuario);

    /**
     * Obtiene pedidos de un cliente específico (CLIENTE)
     */
    List<PedidoClienteResponse> listarPedidosCliente(Long idCliente);

    /**
     * Obtiene un pedido por ID según el rol del usuario
     */
    Object obtenerPedidoPorId(Long id, Usuario usuarioAutenticado);

    // ==================== GESTIÓN DE ESTADOS ====================

    /**
     * Confirma el pago en efectivo (CAJERO/ADMIN)
     */
    PedidoResponse confirmarPago(ConfirmarPagoRequest request, Usuario usuarioAutenticado);

    /**
     * Cambia el estado del pedido (según permisos del rol)
     */
    Object cambiarEstado(CambiarEstadoPedidoRequest request, Usuario usuarioAutenticado);

    /**
     * Cancela un pedido (CLIENTE/CAJERO/ADMIN)
     */
    Object cancelarPedido(CancelarPedidoRequest request, Usuario usuarioAutenticado);

    /**
     * Inicia la preparación del pedido (COCINERO - automático al recibir pedido
     * confirmado)
     */
    PedidoCocineroResponse iniciarPreparacion(Long idPedido);

    /**
     * Marca el pedido como listo (COCINERO)
     */
    PedidoCocineroResponse marcarListo(Long idPedido);

    /**
     * Marca el pedido como entregado (DELIVERY)
     */
    PedidoDeliveryResponse marcarEntregado(Long idPedido, Usuario usuarioDelivery);

    // ==================== GESTIÓN DE TIEMPOS ====================

    /**
     * Extiende el tiempo de preparación (COCINERO)
     */
    PedidoCocineroResponse extenderTiempo(ExtenderTiempoRequest request);

    // ==================== ASIGNACIÓN DE DELIVERY ====================

    /**
     * Asigna un delivery al pedido (CAJERO/ADMIN)
     */
    PedidoResponse asignarDelivery(AsignarDeliveryRequest request, Usuario usuarioAutenticado);

    // ==================== VALIDACIONES ====================

    /**
     * Verifica si un pedido pertenece a un cliente
     */
    boolean pedidoPerteneceACliente(Long idPedido, Long idCliente);
}
