package com.elbuensabor.services;

import com.elbuensabor.dto.request.PedidoRequestDTO;
import com.elbuensabor.dto.response.PedidoResponseDTO;
import com.elbuensabor.dto.response.FacturaResponseDTO;
import java.util.List;

public interface IPedidoService {

    // CRUD básico
    PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoRequest);
    PedidoResponseDTO findById(Long id);
    List<PedidoResponseDTO> findAll();
    List<PedidoResponseDTO> findByCliente(Long idCliente);

    // Cambios de estado
    PedidoResponseDTO confirmarPedido(Long id);
    PedidoResponseDTO marcarEnPreparacion(Long id);
    PedidoResponseDTO marcarListo(Long id);
    PedidoResponseDTO marcarEntregado(Long id);
    PedidoResponseDTO cancelarPedido(Long id);

    // Validaciones y cálculos
    Boolean validarStockDisponible(PedidoRequestDTO pedidoRequest);
    Double calcularTotal(PedidoRequestDTO pedidoRequest);
    Integer calcularTiempoEstimado(PedidoRequestDTO pedidoRequest);

    // Filtros por estado
    List<PedidoResponseDTO> findPedidosPendientes();
    List<PedidoResponseDTO> findPedidosEnPreparacion();
    List<PedidoResponseDTO> findPedidosListosParaEntrega();

    List<PedidoResponseDTO> findPedidosListos();
    List<PedidoResponseDTO> findPedidosListosParaRetiro();


    // Nuevo método para obtener factura del pedido
    FacturaResponseDTO getFacturaPedido(Long pedidoId);

}