package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.PedidoRequestDTO;
import com.elbuensabor.dto.request.PromocionAgrupadaDTO;
import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.dto.response.PedidoResponseDTO;
import com.elbuensabor.entities.*;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.*;
import com.elbuensabor.services.IPedidoService;
import com.elbuensabor.services.mapper.PedidoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.elbuensabor.services.IFacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements IPedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IDomicilioRepository domicilioRepository;

    @Autowired
    private IArticuloRepository articuloRepository;

    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private ISucursalEmpresaRepository sucursalRepository;

    @Autowired
    private IPromocionRepository promocionRepository;

    @Autowired
    private PromocionPedidoService promocionPedidoService;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    private PedidoResponseDTO enrichPedidoResponse(Pedido pedido) {
        PedidoResponseDTO response = pedidoMapper.toDTO(pedido);

        // Calcular stock (siempre true para pedidos ya creados)
        response.setStockSuficiente(true);

        // Calcular tiempo estimado desde los detalles
        response.setTiempoEstimadoTotal(calcularTiempoEstimadoDesdeDetalles(pedido.getDetalles()));

        return response;
    }
    // ==================== MÉTODO AUXILIAR PARA BUSCAR ARTÍCULOS ====================
    private Articulo buscarArticuloPorId(Long idArticulo) {
        // Primero intentar buscar en manufacturados
        Optional<Articulo> manufacturado = articuloRepository.findById(idArticulo);
        if (manufacturado.isPresent()) {
            return manufacturado.get();
        }

        // Si no se encuentra, buscar en insumos
        Optional<ArticuloInsumo> insumo = articuloInsumoRepository.findById(idArticulo);
        if (insumo.isPresent()) {
            return insumo.get();
        }

        throw new ResourceNotFoundException("Artículo con ID " + idArticulo + " no encontrado");
    }

    private Integer calcularTiempoEstimadoDesdeDetalles(List<DetallePedido> detalles) {
        int tiempoMaximo = 0;

        for (DetallePedido detalle : detalles) {
            if (detalle.getArticulo() instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) detalle.getArticulo();
                if (manufacturado.getTiempoEstimadoEnMinutos() != null) {
                    tiempoMaximo = Math.max(tiempoMaximo, manufacturado.getTiempoEstimadoEnMinutos());
                }
            }
        }

        return tiempoMaximo > 0 ? tiempoMaximo : null;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoRequest) {
        // 1. Validar cliente
        System.out.println("🚀 RECIBIENDO PEDIDO REQUEST:");
        System.out.println("📝 Observaciones recibidas: '" + pedidoRequest.getObservaciones() + "'");
        Cliente cliente = clienteRepository.findById(pedidoRequest.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        SucursalEmpresa sucursal = sucursalRepository.findById(pedidoRequest.getIdSucursal())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada"));

        // 2. Validar stock disponible
        if (!validarStockDisponible(pedidoRequest)) {
            throw new IllegalArgumentException("Stock insuficiente para algunos productos");
        }

        // 3. Crear entidad Pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setSucursal(sucursal);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(Estado.PENDIENTE);
        pedido.setTipoEnvio(TipoEnvio.valueOf(pedidoRequest.getTipoEnvio()));
        // Asignar observaciones generales
        pedido.setObservaciones(pedidoRequest.getObservaciones());
        System.out.println("💾 Observaciones asignadas a entidad: '" + pedido.getObservaciones() + "'");


        // 4. Asignar domicilio según tipo de envío

        if (pedidoRequest.getTipoEnvio().equals("DELIVERY")) {
            // ✅ DELIVERY: Dirección del cliente
            Domicilio domicilioCliente = null;

            if (pedidoRequest.getIdDomicilio() != null) {
                // Si se especifica domicilio en el request, usarlo
                domicilioCliente = domicilioRepository.findById(pedidoRequest.getIdDomicilio())
                        .orElseThrow(() -> new ResourceNotFoundException("Domicilio especificado no encontrado"));

                // Verificar que el domicilio pertenezca al cliente
                if (domicilioCliente.getCliente() == null ||
                        !domicilioCliente.getCliente().getIdCliente().equals(pedidoRequest.getIdCliente())) {
                    throw new IllegalArgumentException("El domicilio especificado no pertenece al cliente");
                }
            } else {
                // Si no se especifica, buscar el primer domicilio del cliente
                List<Domicilio> domiciliosCliente = domicilioRepository.findByClienteIdOrderByPrincipal(pedidoRequest.getIdCliente());

                if (domiciliosCliente.isEmpty()) {
                    throw new IllegalArgumentException("El cliente no tiene domicilios registrados para delivery. Debe registrar una dirección primero.");
                }

                // Usar el primer domicilio del cliente
                domicilioCliente = domiciliosCliente.get(0);
                logger.info("✅ DELIVERY: Usando domicilio automático ID: {} ({}) para cliente: {}",
                        domicilioCliente.getIdDomicilio(),
                        domicilioCliente.getCalle() + " " + domicilioCliente.getNumero(),
                        cliente.getUsuario().getNombre() + " " + cliente.getUsuario().getApellido());
            }

            pedido.setDomicilio(domicilioCliente);

        } else if (pedidoRequest.getTipoEnvio().equals("TAKE_AWAY")) {
            // ✅ TAKE_AWAY: Dirección de la sucursal
            if (sucursal.getDomicilio() != null) {
                pedido.setDomicilio(sucursal.getDomicilio());
                logger.info("✅ TAKE_AWAY: Usando dirección de sucursal ID: {} ({})",
                        sucursal.getDomicilio().getIdDomicilio(),
                        sucursal.getDomicilio().getCalle() + " " + sucursal.getDomicilio().getNumero());
            } else {
                logger.error("❌ ERROR: Sucursal ID: {} no tiene domicilio configurado", sucursal.getIdSucursalEmpresa());
                throw new IllegalStateException("La sucursal debe tener un domicilio configurado");
            }
        }

        // 5. Calcular totales
        Double total;
        // ✅ NUEVO: Verificar si aplicar descuento TAKE_AWAY
        if ("TAKE_AWAY".equals(pedidoRequest.getTipoEnvio())) {
            System.out.println("🏪 TAKE_AWAY detectado - Aplicando descuento...");
            total = calcularTotalConDescuentoTakeAway(pedidoRequest);
        } else {
            System.out.println("🚚 DELIVERY detectado - Sin descuento TAKE_AWAY...");
            total = calcularTotalConPromocionAgrupada(pedidoRequest);
        }

        System.out.println("💰 Total final calculado para pedido: $" + total);

        Double totalCosto = calcularTotalCosto(pedidoRequest);
        pedido.setTotal(total);
        pedido.setTotalCosto(totalCosto);

        // 6. Calcular tiempo estimado
        Integer tiempoEstimado = calcularTiempoEstimado(pedidoRequest);
        LocalTime horaEstimada = LocalTime.now().plusMinutes(tiempoEstimado);
        pedido.setHoraEstimadaFinalizacion(horaEstimada);

        // 7. Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        System.out.println("💾 Observaciones asignadas a entidad: '" + pedido.getObservaciones() + "'");


        // 8. Aplicar promociones antes de crear detalles del pedido
        System.out.println("🎯 Aplicando promociones al pedido...");
        PromocionPedidoService.PromocionesAplicadasDTO promocionesAplicadas =
                promocionPedidoService.aplicarPromocionesAPedidoConAgrupada(pedidoRequest);

        System.out.println("💰 Promociones procesadas: " + promocionesAplicadas.getResumenPromociones());

// 9. Crear detalles del pedido CON PROMOCIONES
        List<DetallePedido> detalles = promocionesAplicadas.getDetallesConPromociones().stream()
                .map(detalleConPromocion -> {
                    Articulo articulo = buscarArticuloPorId(detalleConPromocion.getIdArticulo());

                    DetallePedido detalle = new DetallePedido();
                    detalle.setPedido(pedidoGuardado);
                    detalle.setArticulo(articulo);
                    detalle.setCantidad(detalleConPromocion.getCantidad());

                    // ✅ NUEVO: Campos de promoción
                    detalle.setPrecioUnitarioOriginal(detalleConPromocion.getPrecioUnitarioOriginal());
                    detalle.setDescuentoPromocion(detalleConPromocion.getDescuentoAplicado());
                    detalle.setSubtotal(detalleConPromocion.getSubtotalFinal()); // Precio con descuento
                    detalle.setObservaciones(detalleConPromocion.getObservaciones());

                    // Asignar promoción si existe
                    if (detalleConPromocion.getTienePromocion() &&
                            detalleConPromocion.getPromocionAplicada() != null) {

                        try {
                            Promocion promocion = promocionRepository.findById(
                                    detalleConPromocion.getPromocionAplicada().getIdPromocion()
                            ).orElse(null);
                            detalle.setPromocionAplicada(promocion);
                        } catch (Exception e) {
                            logger.warn("⚠️ Error asignando promoción: {}", e.getMessage());
                        }
                    }

                    System.out.println("📦 Detalle creado: " + articulo.getDenominacion() +
                            " x " + detalleConPromocion.getCantidad() +
                            " = $" + detalle.getSubtotal() +
                            (detalleConPromocion.getTienePromocion() ?
                                    " (con promoción: -$" + detalleConPromocion.getDescuentoAplicado() + ")" : ""));

                    return detalle;
                })
                .collect(Collectors.toList());

        pedidoGuardado.setDetalles(detalles);

        // 9. Actualizar stock de ingredientes - NO ACTUALIZAR HASTA CONFIRMAR
        // actualizarStockIngredientes(pedidoRequest);

        // 10. Guardar con detalles
        Pedido pedidoFinal = pedidoRepository.save(pedidoGuardado);

        // 🆕 11. CREAR FACTURA AUTOMÁTICAMENTE
        try {
            facturaService.crearFacturaFromPedido(pedidoFinal);
            logger.info("✅ Factura creada automáticamente para pedido ID: {}", pedidoFinal.getIdPedido());
        } catch (Exception e) {
            logger.error("❌ Error creando factura para pedido ID: {}", pedidoFinal.getIdPedido(), e);
            // La factura se puede crear después manualmente, no falla el pedido
        }

        // 12. Mapear a DTO (código existente)
        PedidoResponseDTO response = pedidoMapper.toDTO(pedidoFinal);
        System.out.println("📤 Observaciones en response: '" + response.getObservaciones() + "'");

        // 13. Calcular campos faltantes (código existente)
        response.setStockSuficiente(validarStockDisponible(pedidoRequest));
        response.setTiempoEstimadoTotal(calcularTiempoEstimado(pedidoRequest));

        // Notificar creación de pedido via WebSocket
        webSocketNotificationService.notificarNuevoPedido(
                pedidoFinal.getIdPedido(),
                pedidoFinal.getCliente().getUsuario().getNombre() + " " + pedidoFinal.getCliente().getUsuario().getApellido()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public FacturaResponseDTO getFacturaPedido(Long pedidoId) {
        // Verificar que el pedido existe
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Buscar factura del pedido
        return facturaService.findByPedidoId(pedidoId);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO findById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        return enrichPedidoResponseConPromociones(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findAll() {
        return pedidoRepository.findAll().stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findByCliente(Long idCliente) {
        return pedidoRepository.findByClienteIdClienteOrderByFechaDesc(idCliente).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponseDTO confirmarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }

        // Al confirmar, cambiar a PREPARACION y actualizar stock
        pedido.setEstado(Estado.PREPARACION);
        actualizarStockDesdePedido(pedido);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        return enrichPedidoResponseConPromociones(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO marcarEnPreparacion(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden pasar a preparación pedidos pendientes");
        }

        pedido.setEstado(Estado.PREPARACION);
        actualizarStockDesdePedido(pedido);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        String clienteEmail = pedidoActualizado.getCliente().getUsuario().getEmail();

        // Notificar cambio de estado vía WebSocket
        webSocketNotificationService.notificarCambioEstado(
                id,
                "EN_PREPARACION",
                clienteEmail
        );

        return enrichPedidoResponseConPromociones(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO marcarListo(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (pedido.getEstado() != Estado.PREPARACION) {
            throw new IllegalStateException("El pedido debe estar en preparación para marcarlo como listo");
        }

        pedido.setEstado(Estado.LISTO);
        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // Obtener datos del cliente
        String clienteEmail = "";
        String clienteNombre = "Cliente";

        try {
            if (pedidoActualizado.getCliente() != null) {
                clienteNombre = pedidoActualizado.getCliente().getUsuario().getNombre() + " " +
                        pedidoActualizado.getCliente().getUsuario().getApellido();

                if (pedidoActualizado.getCliente().getUsuario() != null) {
                    clienteEmail = pedidoActualizado.getCliente().getUsuario().getEmail();
                }
            }
        } catch (Exception e) {
            logger.warn("⚠️ Error obteniendo datos del cliente para notificación: {}", e.getMessage());
        }

        // Notificar cambio de estado general
        webSocketNotificationService.notificarCambioEstado(
                id,
                "LISTO",
                clienteEmail
        );

        // ✅ NUEVO: Si es delivery, notificar específicamente a delivery
        if (pedidoActualizado.getTipoEnvio() == TipoEnvio.DELIVERY) {
            webSocketNotificationService.notificarPedidoListoParaDelivery(
                    id,
                    clienteNombre
            );
        }

        return enrichPedidoResponseConPromociones(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO marcarEntregado(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        // Verificar que esté listo o en preparación (para take away)
        if (pedido.getEstado() != Estado.LISTO &&
                !(pedido.getEstado() == Estado.PREPARACION && pedido.getTipoEnvio() == TipoEnvio.TAKE_AWAY)) {
            throw new IllegalStateException("El pedido debe estar listo para ser entregado");
        }

        pedido.setEstado(Estado.ENTREGADO);
        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // Notificar cambio de estado vía WebSocket
        webSocketNotificationService.notificarCambioEstado(
                id,
                "ENTREGADO",
                pedidoActualizado.getCliente().getUsuario().getEmail()
        );

        return enrichPedidoResponseConPromociones(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        if (pedido.getEstado() == Estado.ENTREGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido entregado");
        }

        // Restaurar stock solo si el pedido estaba en preparación o listo
        if (pedido.getEstado() == Estado.PREPARACION || pedido.getEstado() == Estado.LISTO) {
            restaurarStockIngredientes(pedido);
        }

        pedido.setEstado(Estado.CANCELADO);
        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // ✅ NUEVO: Usar notificación específica de cancelación
        String clienteAuthId = null;
        String clienteNombre = "Cliente";

        try {
            if (pedidoActualizado.getCliente() != null) {
                clienteNombre = pedidoActualizado.getCliente().getUsuario().getNombre() + " " +
                        pedidoActualizado.getCliente().getUsuario().getApellido();

                if (pedidoActualizado.getCliente().getUsuario() != null) {
                    clienteAuthId = pedidoActualizado.getCliente().getUsuario().getEmail();
                }
            }
        } catch (Exception e) {
            logger.warn("⚠️ Error obteniendo datos del cliente para notificación: {}", e.getMessage());
        }

        // Notificar cancelación específicamente
        webSocketNotificationService.notificarCancelacionPedido(
                id,
                clienteNombre,
                clienteAuthId
        );

        return enrichPedidoResponseConPromociones(pedidoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean validarStockDisponible(PedidoRequestDTO pedidoRequest) {
        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo());

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;

                // Verificar stock de cada ingrediente
                for (var ingrediente : manufacturado.getDetalles()) {
                    double cantidadNecesaria = ingrediente.getCantidad() * detalle.getCantidad();
                    if (ingrediente.getArticuloInsumo().getStockActual() < cantidadNecesaria) {
                        System.out.println("❌ Stock insuficiente - Ingrediente: " +
                                ingrediente.getArticuloInsumo().getDenominacion() +
                                ", Necesario: " + cantidadNecesaria +
                                ", Disponible: " + ingrediente.getArticuloInsumo().getStockActual());
                        return false;
                    }
                }
            } else if (articulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                if (insumo.getStockActual() < detalle.getCantidad()) {
                    System.out.println("❌ Stock insuficiente - Insumo: " +
                            insumo.getDenominacion() +
                            ", Necesario: " + detalle.getCantidad() +
                            ", Disponible: " + insumo.getStockActual());
                    return false;
                }
            }
        }
        System.out.println("✅ Stock disponible para todos los productos");
        return true;
    }

    // ==================== MÉTODO CALCULAR TOTAL CORREGIDO ====================
    @Override
    @Transactional(readOnly = true)
    public Double calcularTotal(PedidoRequestDTO pedidoRequest) {
        System.out.println("💰 Calculando total CON promociones...");

        // Aplicar promociones y obtener subtotal con descuentos
        PromocionPedidoService.PromocionesAplicadasDTO promocionesAplicadas =
                promocionPedidoService.aplicarPromocionesAPedido(pedidoRequest);

        double subtotal = promocionesAplicadas.getSubtotalFinal(); // Ya incluye descuentos

        System.out.println("💰 Subtotal con promociones: $" + subtotal);
        System.out.println("🎯 Descuento total aplicado: $" + promocionesAplicadas.getDescuentoTotal());

        // Agregar costo de envío si es delivery
        if ("DELIVERY".equals(pedidoRequest.getTipoEnvio())) {
            subtotal += 200; // Costo fijo de delivery
            System.out.println("🚚 Costo delivery: $200");
        }

        System.out.println("💰 Total final calculado: $" + subtotal);
        return subtotal;
    }
    // ==================== MÉTODO CALCULAR TIEMPO ESTIMADO CORREGIDO ====================
    @Override
    @Transactional(readOnly = true)
    public Integer calcularTiempoEstimado(PedidoRequestDTO pedidoRequest) {
        int tiempoMaximo = 0;

        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo());

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
                if (manufacturado.getTiempoEstimadoEnMinutos() != null) {
                    tiempoMaximo = Math.max(tiempoMaximo, manufacturado.getTiempoEstimadoEnMinutos());
                    System.out.println("⏱️ Producto manufacturado: " + manufacturado.getDenominacion() +
                            " - Tiempo: " + manufacturado.getTiempoEstimadoEnMinutos() + " min");
                }
            }
            // Los insumos no tienen tiempo de preparación, se entregan inmediatamente
        }

        // Agregar tiempo de delivery si corresponde
        if ("DELIVERY".equals(pedidoRequest.getTipoEnvio())) {
            tiempoMaximo += 15; // Tiempo estimado de entrega
            System.out.println("🚚 Tiempo delivery agregado: +15 min");
        }

        System.out.println("⏱️ Tiempo total estimado: " + tiempoMaximo + " min");
        return tiempoMaximo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findPedidosPendientes() {
        return pedidoRepository.findByEstadoOrderByFechaAsc(Estado.PENDIENTE).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findPedidosEnPreparacion() {
        return pedidoRepository.findByEstadoOrderByFechaAsc(Estado.PREPARACION).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findPedidosListos() {
        return pedidoRepository.findByEstadoOrderByFechaAsc(Estado.LISTO).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findPedidosListosParaEntrega() {
        // Pedidos listos para delivery
        return pedidoRepository.findByEstadoAndTipoEnvioOrderByFechaAsc(Estado.LISTO, TipoEnvio.DELIVERY).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findPedidosListosParaRetiro() {
        // Pedidos listos para take away
        return pedidoRepository.findByEstadoAndTipoEnvioOrderByFechaAsc(Estado.LISTO, TipoEnvio.TAKE_AWAY).stream()
                .map(this::enrichPedidoResponseConPromociones)
                .collect(Collectors.toList());
    }

    // Métodos auxiliares privados
    private Double calcularTotalCosto(PedidoRequestDTO pedidoRequest) {
        double totalCosto = 0;

        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo()); // ← Cambiar esta línea

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
                double costoUnitario = manufacturado.getDetalles().stream()
                        .mapToDouble(ing -> ing.getCantidad() * ing.getArticuloInsumo().getPrecioCompra())
                        .sum();
                totalCosto += costoUnitario * detalle.getCantidad();
            } else if (articulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                totalCosto += insumo.getPrecioCompra() * detalle.getCantidad();
            }
        }

        return totalCosto;
    }
    private void actualizarStockIngredientes(PedidoRequestDTO pedidoRequest) {
        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo()); // ← Cambiar esta línea

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;

                for (var ingrediente : manufacturado.getDetalles()) {
                    ArticuloInsumo insumo = ingrediente.getArticuloInsumo();
                    int cantidadARestar = (int) (ingrediente.getCantidad() * detalle.getCantidad());
                    int stockAnterior = insumo.getStockActual();
                    insumo.setStockActual(insumo.getStockActual() - cantidadARestar);
                    articuloInsumoRepository.save(insumo);

                    System.out.println("📉 Stock actualizado - " + insumo.getDenominacion() +
                            ": " + stockAnterior + " -> " + insumo.getStockActual() + " (-" + cantidadARestar + ")");
                }
            } else if (articulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                int stockAnterior = insumo.getStockActual();
                insumo.setStockActual(insumo.getStockActual() - detalle.getCantidad());
                articuloInsumoRepository.save(insumo);

                System.out.println("📉 Stock actualizado - " + insumo.getDenominacion() +
                        ": " + stockAnterior + " -> " + insumo.getStockActual() + " (-" + detalle.getCantidad() + ")");
            }
        }
    }
    private void actualizarStockDesdePedido(Pedido pedido) {
        for (var detalle : pedido.getDetalles()) {
            Articulo articulo = detalle.getArticulo();

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;

                for (var ingrediente : manufacturado.getDetalles()) {
                    ArticuloInsumo insumo = ingrediente.getArticuloInsumo();
                    int cantidadARestar = (int) (ingrediente.getCantidad() * detalle.getCantidad());
                    insumo.setStockActual(insumo.getStockActual() - cantidadARestar);
                    articuloInsumoRepository.save(insumo);
                }
            } else if (articulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                insumo.setStockActual(insumo.getStockActual() - detalle.getCantidad());
                articuloInsumoRepository.save(insumo);
            }
        }
    }

    private void restaurarStockIngredientes(Pedido pedido) {
        for (var detalle : pedido.getDetalles()) {
            Articulo articulo = detalle.getArticulo();

            if (articulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;

                for (var ingrediente : manufacturado.getDetalles()) {
                    ArticuloInsumo insumo = ingrediente.getArticuloInsumo();
                    int cantidadARestaurar = (int) (ingrediente.getCantidad() * detalle.getCantidad());
                    insumo.setStockActual(insumo.getStockActual() + cantidadARestaurar);
                    articuloInsumoRepository.save(insumo);
                }
            } else if (articulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                insumo.setStockActual(insumo.getStockActual() + detalle.getCantidad());
                articuloInsumoRepository.save(insumo);
            }
        }

    }

    private PedidoResponseDTO enrichPedidoResponseConPromociones(Pedido pedido) {
        PedidoResponseDTO response = pedidoMapper.toDTO(pedido);

        // Calcular stock (siempre true para pedidos ya creados)
        response.setStockSuficiente(true);

        // Calcular tiempo estimado desde los detalles
        response.setTiempoEstimadoTotal(calcularTiempoEstimadoDesdeDetalles(pedido.getDetalles()));

        // ✅ NUEVO: Calcular resumen de promociones
        PedidoResponseDTO.ResumenPromocionesDTO resumenPromociones = calcularResumenPromociones(pedido.getDetalles());
        response.setResumenPromociones(resumenPromociones);

        return response;
    }

    private PedidoResponseDTO.ResumenPromocionesDTO calcularResumenPromociones(List<DetallePedido> detalles) {
        PedidoResponseDTO.ResumenPromocionesDTO resumen = new PedidoResponseDTO.ResumenPromocionesDTO();

        double subtotalOriginal = 0.0;
        double totalDescuentos = 0.0;
        int cantidadPromociones = 0;
        List<String> nombresPromociones = new ArrayList<>();

        for (DetallePedido detalle : detalles) {
            subtotalOriginal += detalle.getPrecioUnitarioOriginal() * detalle.getCantidad();

            if (detalle.getDescuentoPromocion() != null && detalle.getDescuentoPromocion() > 0) {
                totalDescuentos += detalle.getDescuentoPromocion();
                cantidadPromociones++;

                if (detalle.getPromocionAplicada() != null) {
                    String nombrePromocion = detalle.getPromocionAplicada().getDenominacion();
                    if (!nombresPromociones.contains(nombrePromocion)) {
                        nombresPromociones.add(nombrePromocion);
                    }
                }
            }
        }

        resumen.setSubtotalOriginal(subtotalOriginal);
        resumen.setTotalDescuentos(totalDescuentos);
        resumen.setSubtotalConDescuentos(subtotalOriginal - totalDescuentos);
        resumen.setCantidadPromociones(cantidadPromociones);
        resumen.setNombresPromociones(nombresPromociones);

        if (cantidadPromociones > 0) {
            resumen.setResumenTexto(String.format("%d promoción(es) aplicada(s) - Ahorro: $%.2f",
                    cantidadPromociones, totalDescuentos));
        } else {
            resumen.setResumenTexto("Sin promociones aplicadas");
        }

        return resumen;
    }

    private Double calcularTotalConPromocionAgrupada(PedidoRequestDTO pedidoRequest) {
        System.out.println("💰 Calculando total CON promoción agrupada...");

        // Calcular subtotal original
        double subtotalOriginal = 0.0;
        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo());
            subtotalOriginal += articulo.getPrecioVenta() * detalle.getCantidad();
        }

        System.out.println("💰 Subtotal original: $" + subtotalOriginal);

        // Aplicar descuento de promoción agrupada si existe
        double descuentoPromocionAgrupada = 0.0;
        if (pedidoRequest.getPromocionAgrupada() != null) {
            PromocionAgrupadaDTO promocion = pedidoRequest.getPromocionAgrupada();

            if ("PORCENTUAL".equals(promocion.getTipoDescuento())) {
                descuentoPromocionAgrupada = (subtotalOriginal * promocion.getValorDescuento()) / 100;
            } else {
                descuentoPromocionAgrupada = Math.min(promocion.getValorDescuento(), subtotalOriginal);
            }

            System.out.println("🎁 Promoción agrupada aplicada: " + promocion.getDenominacion());
            System.out.println("🎁 Descuento: $" + descuentoPromocionAgrupada);
        }

        // Aplicar otras promociones individuales (usar servicio existente)
        PromocionPedidoService.PromocionesAplicadasDTO promocionesIndividuales =
                promocionPedidoService.aplicarPromocionesAPedido(pedidoRequest);

        double descuentoIndividual = promocionesIndividuales.getDescuentoTotal();
        System.out.println("🎯 Descuento promociones individuales: $" + descuentoIndividual);

        // Total con todos los descuentos
        double subtotalConDescuentos = subtotalOriginal - descuentoPromocionAgrupada - descuentoIndividual;

        // Agregar costo de envío si es delivery
        if ("DELIVERY".equals(pedidoRequest.getTipoEnvio())) {
            subtotalConDescuentos += 200; // Costo fijo de delivery
            System.out.println("🚚 Costo delivery: $200");
        }

        System.out.println("💰 Total final con promoción agrupada: $" + subtotalConDescuentos);
        return Math.max(0, subtotalConDescuentos); // No puede ser negativo
    }
    /**
     * ✅ MÉTODO CORREGIDO: Calcula el total CON descuento TAKE_AWAY
     * APLICA LA MISMA LÓGICA QUE EL FRONTEND
     */
    @Transactional(readOnly = true)
    public Double calcularTotalConDescuentoTakeAway(PedidoRequestDTO pedidoRequest) {
        System.out.println("🏪 === CALCULANDO TOTAL CON DESCUENTO TAKE_AWAY (LÓGICA CORREGIDA) ===");

        // 1. Calcular subtotal original SIN descuentos
        double subtotalOriginal = 0.0;
        for (var detalle : pedidoRequest.getDetalles()) {
            Articulo articulo = buscarArticuloPorId(detalle.getIdArticulo());
            subtotalOriginal += articulo.getPrecioVenta() * detalle.getCantidad();
        }
        System.out.println("💰 Subtotal original: $" + subtotalOriginal);

        // 2. Usar el servicio de promociones para obtener cálculo COMPLETO
        PromocionPedidoService.PromocionesAplicadasDTO promocionesAplicadas =
                promocionPedidoService.aplicarPromocionesAPedidoConAgrupada(pedidoRequest);

        double descuentoPromociones = promocionesAplicadas.getDescuentoTotal();
        System.out.println("🎯 Descuento promociones (completo): $" + descuentoPromociones);

        // 3. El servicio ya maneja productos con y sin promoción correctamente
        double subtotalConPromociones = promocionesAplicadas.getSubtotalFinal();
        System.out.println("💰 Subtotal con promociones (del servicio): $" + subtotalConPromociones);

        // 4. Verificar que coincida con nuestro cálculo manual
        double verificacion = subtotalOriginal - descuentoPromociones;
        if (Math.abs(subtotalConPromociones - verificacion) > 0.01) {
            System.out.println("⚠️ ADVERTENCIA: Diferencia en cálculo - Servicio: $" + subtotalConPromociones + " vs Manual: $" + verificacion);
        }
        System.out.println("💰 Subtotal CON promociones: $" + subtotalConPromociones);

        // 5. ✅ CORREGIDO: Aplicar descuento TAKE_AWAY sobre subtotal CON promociones
        double porcentajeDescuento = 10.0;
        double descuentoTakeAway = subtotalConPromociones * (porcentajeDescuento / 100);
        System.out.println("🏪 Descuento TAKE_AWAY (" + porcentajeDescuento + "% de $" + subtotalConPromociones + "): $" + descuentoTakeAway);

        // 6. Calcular total final
        double totalFinal = subtotalConPromociones - descuentoTakeAway;

        // 7. Agregar gastos de envío si es DELIVERY (normalmente no aplica para TAKE_AWAY)
        if ("DELIVERY".equals(pedidoRequest.getTipoEnvio())) {
            totalFinal += 200;
            System.out.println("🚚 Gastos envío DELIVERY: $200");
        }

        totalFinal = Math.max(0, totalFinal);

        System.out.println("💰 === RESUMEN FINAL (CORREGIDO) ===");
        System.out.println("💰 Subtotal original: $" + subtotalOriginal);
        System.out.println("🎯 Descuento promociones (total): -$" + descuentoPromociones);
        System.out.println("💰 Subtotal con promociones: $" + subtotalConPromociones);
        System.out.println("🏪 Descuento TAKE_AWAY (sobre subtotal con promociones): -$" + descuentoTakeAway);
        System.out.println("💰 TOTAL FINAL: $" + totalFinal);

        return totalFinal;
    }
}