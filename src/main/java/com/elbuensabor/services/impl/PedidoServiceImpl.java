package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.pedido.*;
import com.elbuensabor.dto.response.pedido.*;
import com.elbuensabor.entities.*;
import com.elbuensabor.repository.*;
import com.elbuensabor.services.IPedidoService;
import com.elbuensabor.services.mapper.PedidoMapper;
import com.elbuensabor.services.mapper.DetallePedidoMapper;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements IPedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IDomicilioRepository domicilioRepository;

    @Autowired
    private IArticuloRepository articuloRepository;

    @Autowired
    private IArticuloManufacturadoRepository articuloManufacturadoRepository;

    @Autowired
    private IArticuloInsumoRepository articuloInsumoRepository;

    @Autowired
    private IPromocionRepository promocionRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private DetallePedidoMapper detallePedidoMapper;

    // ==================== CREACIÓN DE PEDIDOS ====================

    @Override
    @Transactional
    public PedidoClienteResponse crearPedido(CrearPedidoRequest request, Usuario usuarioAutenticado) {
        log.info("Creando pedido para usuario: {}", usuarioAutenticado.getEmail());

        // Validar que el usuario sea CLIENTE
        if (!usuarioAutenticado.getRol().equals(Rol.CLIENTE)) {
            log.error("Usuario {} no es cliente", usuarioAutenticado.getEmail());
            throw new IllegalArgumentException("Solo los clientes pueden crear pedidos");
        }

        // Obtener el cliente asociado al usuario
        Cliente cliente = clienteRepository.findByUsuarioIdUsuario(usuarioAutenticado.getIdUsuario())
                .orElseThrow(() -> {
                    log.error("No se encontró cliente para usuario: {}", usuarioAutenticado.getIdUsuario());
                    return new IllegalArgumentException("Cliente no encontrado");
                });

        // Crear la entidad Pedido
        Pedido pedido = pedidoMapper.toEntity(request);
        pedido.setCliente(cliente);

        // Asignar domicilio si es delivery
        if (request.getTipoEnvio() == TipoEnvio.DELIVERY) {
            if (request.getIdDomicilio() == null) {
                log.error("Pedido delivery sin domicilio especificado");
                throw new IllegalArgumentException("Debe especificar un domicilio para delivery");
            }

            Domicilio domicilio = domicilioRepository.findById(request.getIdDomicilio())
                    .orElseThrow(() -> new IllegalArgumentException("Domicilio no encontrado"));

            // Verificar que el domicilio pertenezca al cliente
            if (!domicilio.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                log.error("Domicilio {} no pertenece al cliente {}", request.getIdDomicilio(), cliente.getIdCliente());
                throw new IllegalArgumentException("El domicilio no pertenece al cliente");
            }

            pedido.setDomicilio(domicilio);
        }

        // Procesar detalles del pedido
        procesarDetallesPedido(pedido, request.getDetalles());

        // Calcular totales
        calcularTotales(pedido);

        // Calcular hora estimada de finalización (ejemplo: 30 min base + 5 min por
        // producto)
        int minutosEstimados = 30 + (pedido.getDetalles().size() * 5);
        pedido.setHoraEstimadaFinalizacion(LocalTime.now().plusMinutes(minutosEstimados));

        // Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido {} creado exitosamente", pedidoGuardado.getIdPedido());

        return pedidoMapper.toClienteResponse(pedidoGuardado);
    }

    // ==================== CONSULTAS POR ROL ====================

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodosPedidos() {
        log.info("Listando todos los pedidos (ADMIN)");
        return pedidoRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(pedidoMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorEstado(String estado) {
        log.info("Listando pedidos por estado: {}", estado);
        Estado estadoEnum = Estado.valueOf(estado.toUpperCase());
        return pedidoRepository.findByEstadoOrderByFechaDesc(estadoEnum)
                .stream()
                .map(pedidoMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCajeroResponse> listarPedidosDelDia() {
        log.info("Listando pedidos del día actual (CAJERO)");
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);

        return pedidoRepository.findByFechaBetweenOrderByFechaDesc(inicioDia, finDia)
                .stream()
                .map(pedidoMapper::toCajeroResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorFecha(LocalDate fecha) {
        log.info("Listando pedidos para la fecha: {}", fecha);
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        return pedidoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .stream()
                .map(pedidoMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCocineroResponse> listarPedidosCocina() {
        log.info("Listando pedidos para cocina (solo manufacturados)");

        return pedidoRepository.findPedidosParaCocina()
                .stream()
                .map(pedido -> {
                    PedidoCocineroResponse response = pedidoMapper.toCocineroResponse(pedido);

                    // Sobreescribir detalles filtrando solo artículos manufacturados
                    List<DetallePedidoResponse> soloManufacturados = pedido.getDetalles().stream()
                            .filter(d -> d.getArticulo() instanceof ArticuloManufacturado)
                            .map(detallePedidoMapper::toDTO)
                            .collect(Collectors.toList());
                    response.setDetalles(soloManufacturados);

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDeliveryResponse> listarPedidosDelivery(Usuario usuario) {
        log.info("Listando pedidos asignados al delivery {}", usuario.getEmail());
        return pedidoRepository.findByUsuarioDelivery_IdUsuarioOrderByFechaDesc(usuario.getIdUsuario())
                .stream()
                .map(pedidoMapper::toDeliveryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoClienteResponse> listarPedidosCliente(Long idCliente) {
        log.info("Listando pedidos del cliente: {}", idCliente);
        return pedidoRepository.findByCliente_IdClienteOrderByFechaDesc(idCliente)
                .stream()
                .map(pedidoMapper::toClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Object obtenerPedidoPorId(Long id, Usuario usuarioAutenticado) {
        log.info("Obteniendo pedido {} para usuario {}", id, usuarioAutenticado.getEmail());

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Retornar según el rol
        return switch (usuarioAutenticado.getRol()) {
            case ADMIN -> pedidoMapper.toAdminResponse(pedido);
            case CAJERO -> pedidoMapper.toCajeroResponse(pedido);
            case COCINERO -> pedidoMapper.toCocineroResponse(pedido);
            case DELIVERY -> pedidoMapper.toDeliveryResponse(pedido);
            case CLIENTE -> {
                // Validar que el pedido pertenezca al cliente
                if (!pedido.getCliente().getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario())) {
                    log.error("Cliente {} intentó acceder a pedido {} que no le pertenece",
                            usuarioAutenticado.getIdUsuario(), id);
                    throw new IllegalArgumentException("No tiene permisos para ver este pedido");
                }
                yield pedidoMapper.toClienteResponse(pedido);
            }
        };
    }

    // ==================== GESTIÓN DE ESTADOS ====================

    @Override
    @Transactional
    public PedidoResponse confirmarPago(ConfirmarPagoRequest request, Usuario usuarioAutenticado) {
        log.info("Confirmando pago del pedido {} por usuario {}", request.getIdPedido(), usuarioAutenticado.getEmail());

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Validar forma de pago
        if (pedido.getFormaPago() != FormaPago.EFECTIVO) {
            log.error("Intento de confirmar pago de pedido con forma de pago: {}", pedido.getFormaPago());
            throw new IllegalArgumentException("Solo se pueden confirmar pagos en efectivo");
        }

        if (pedido.getPagoConfirmado()) {
            log.warn("Pedido {} ya tiene el pago confirmado", request.getIdPedido());
            throw new IllegalArgumentException("El pago ya fue confirmado");
        }

        // Confirmar pago
        pedido.setPagoConfirmado(true);
        pedido.setFechaConfirmacionPago(LocalDateTime.now());
        pedido.setUsuarioConfirmaPago(usuarioAutenticado);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pago confirmado para pedido {}", pedidoActualizado.getIdPedido());

        return pedidoMapper.toAdminResponse(pedidoActualizado);
    }

    @Override
    @Transactional
    public Object cambiarEstado(CambiarEstadoPedidoRequest request, Usuario usuarioAutenticado) {
        log.info("Cambiando estado del pedido {} a {} por usuario {}",
                request.getIdPedido(), request.getNuevoEstado(), usuarioAutenticado.getEmail());

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Validar transición de estado
        validarTransicionEstado(pedido, request.getNuevoEstado(), usuarioAutenticado.getRol());

        // Actualizar estado y timestamps
        actualizarEstado(pedido, request.getNuevoEstado());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Estado del pedido {} actualizado a {}", pedidoActualizado.getIdPedido(), request.getNuevoEstado());

        // Retornar según el rol
        return switch (usuarioAutenticado.getRol()) {
            case ADMIN, CAJERO -> pedidoMapper.toAdminResponse(pedidoActualizado);
            case COCINERO -> pedidoMapper.toCocineroResponse(pedidoActualizado);
            case DELIVERY -> pedidoMapper.toDeliveryResponse(pedidoActualizado);
            default -> throw new IllegalArgumentException("Rol no autorizado para cambiar estados");
        };
    }

    @Override
    @Transactional
    public Object cancelarPedido(CancelarPedidoRequest request, Usuario usuarioAutenticado) {
        log.info("Cancelando pedido {} por usuario {}", request.getIdPedido(), usuarioAutenticado.getEmail());

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Validar que se pueda cancelar
        if (!pedido.puedeSerCancelado()) {
            log.error("Pedido {} no puede ser cancelado, estado actual: {}", request.getIdPedido(), pedido.getEstado());
            throw new IllegalArgumentException("El pedido no puede ser cancelado");
        }

        // Si es cliente, validar que sea su pedido
        if (usuarioAutenticado.getRol() == Rol.CLIENTE) {
            if (!pedido.getCliente().getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario())) {
                log.error("Cliente {} intentó cancelar pedido {} que no le pertenece",
                        usuarioAutenticado.getIdUsuario(), request.getIdPedido());
                throw new IllegalArgumentException("No puede cancelar este pedido");
            }
        }

        // Cancelar pedido
        pedido.setEstado(Estado.CANCELADO);
        pedido.setFechaCancelado(LocalDateTime.now());
        pedido.setMotivoCancelacion(request.getMotivo());
        pedido.setUsuarioCancela(usuarioAutenticado);

        Pedido pedidoCancelado = pedidoRepository.save(pedido);
        log.info("Pedido {} cancelado exitosamente", pedidoCancelado.getIdPedido());

        // Retornar según el rol
        return switch (usuarioAutenticado.getRol()) {
            case ADMIN, CAJERO -> pedidoMapper.toAdminResponse(pedidoCancelado);
            case COCINERO -> pedidoMapper.toCocineroResponse(pedidoCancelado);
            case CLIENTE -> pedidoMapper.toClienteResponse(pedidoCancelado);
            case DELIVERY -> pedidoMapper.toDeliveryResponse(pedidoCancelado);
            default -> throw new IllegalArgumentException("Rol no autorizado para cancelar pedidos");
        };
    }

    @Override
    @Transactional
    public PedidoCocineroResponse iniciarPreparacion(Long idPedido) {
        log.info("Iniciando preparación del pedido {}", idPedido);

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!pedido.puedeIniciarPreparacion()) {
            log.error("Pedido {} no puede iniciar preparación. Estado: {}, Pago confirmado: {}",
                    idPedido, pedido.getEstado(), pedido.getPagoConfirmado());
            throw new IllegalArgumentException("El pedido no puede iniciar preparación");
        }

        pedido.setEstado(Estado.PREPARACION);
        pedido.setFechaInicioPreparacion(LocalDateTime.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido {} en preparación", pedidoActualizado.getIdPedido());

        return pedidoMapper.toCocineroResponse(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoCocineroResponse marcarListo(Long idPedido) {
        log.info("Marcando pedido {} como listo", idPedido);

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!pedido.puedeMarcarListo()) {
            log.error("Pedido {} no puede marcarse como listo. Estado actual: {}", idPedido, pedido.getEstado());
            throw new IllegalArgumentException("El pedido no está en preparación");
        }

        pedido.setEstado(Estado.LISTO);
        pedido.setFechaListo(LocalDateTime.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido {} marcado como listo", pedidoActualizado.getIdPedido());

        return pedidoMapper.toCocineroResponse(pedidoActualizado);
    }

    @Override
    @Transactional
    public PedidoDeliveryResponse marcarEntregado(Long idPedido, Usuario usuarioDelivery) {
        log.info("Marcando pedido {} como entregado por delivery {}", idPedido, usuarioDelivery.getEmail());

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!pedido.puedeEntregarse()) {
            log.error("Pedido {} no puede marcarse como entregado. Estado actual: {}", idPedido, pedido.getEstado());
            throw new IllegalArgumentException("El pedido no está listo para entrega");
        }

        // Validar que el delivery asignado sea quien entrega
        if (pedido.getUsuarioDelivery() != null &&
                !pedido.getUsuarioDelivery().getIdUsuario().equals(usuarioDelivery.getIdUsuario())) {
            log.error("Delivery {} intentó entregar pedido {} asignado a otro delivery",
                    usuarioDelivery.getIdUsuario(), idPedido);
            throw new IllegalArgumentException("Este pedido está asignado a otro delivery");
        }

        pedido.setEstado(Estado.ENTREGADO);
        pedido.setFechaEntregado(LocalDateTime.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido {} entregado exitosamente", pedidoActualizado.getIdPedido());

        return pedidoMapper.toDeliveryResponse(pedidoActualizado);
    }

    // ==================== GESTIÓN DE TIEMPOS ====================

    @Override
    @Transactional
    public PedidoCocineroResponse extenderTiempo(ExtenderTiempoRequest request) {
        log.info("Extendiendo tiempo del pedido {} en {} minutos", request.getIdPedido(),
                request.getMinutosExtension());

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() != Estado.PREPARACION) {
            log.error("Solo se puede extender tiempo de pedidos en preparación. Estado actual: {}", pedido.getEstado());
            throw new IllegalArgumentException("Solo se puede extender tiempo de pedidos en preparación");
        }

        // Actualizar tiempo de extensión y hora estimada
        int extensionActual = pedido.getTiempoExtensionMinutos() != null ? pedido.getTiempoExtensionMinutos() : 0;
        pedido.setTiempoExtensionMinutos(extensionActual + request.getMinutosExtension());

        LocalTime nuevaHoraEstimada = pedido.getHoraEstimadaFinalizacion()
                .plusMinutes(request.getMinutosExtension());
        pedido.setHoraEstimadaFinalizacion(nuevaHoraEstimada);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Tiempo del pedido {} extendido. Nueva hora estimada: {}",
                pedidoActualizado.getIdPedido(), nuevaHoraEstimada);

        return pedidoMapper.toCocineroResponse(pedidoActualizado);
    }

    // ==================== ASIGNACIÓN DE DELIVERY ====================

    @Override
    @Transactional
    public PedidoResponse asignarDelivery(AsignarDeliveryRequest request, Usuario usuarioAutenticado) {
        log.info("Asignando delivery {} al pedido {} por usuario {}",
                request.getIdUsuarioDelivery(), request.getIdPedido(), usuarioAutenticado.getEmail());

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Validar que el pedido sea delivery
        if (pedido.getTipoEnvio() != TipoEnvio.DELIVERY) {
            log.error("Pedido {} no es para delivery", request.getIdPedido());
            throw new IllegalArgumentException("El pedido no es para delivery");
        }

        // Obtener y validar usuario delivery
        Usuario delivery = usuarioRepository.findById(request.getIdUsuarioDelivery())
                .orElseThrow(() -> new IllegalArgumentException("Usuario delivery no encontrado"));

        if (delivery.getRol() != Rol.DELIVERY) {
            log.error("Usuario {} no tiene rol DELIVERY", request.getIdUsuarioDelivery());
            throw new IllegalArgumentException("El usuario no es un delivery");
        }

        pedido.setUsuarioDelivery(delivery);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Delivery {} asignado al pedido {}", delivery.getEmail(), pedidoActualizado.getIdPedido());

        return pedidoMapper.toAdminResponse(pedidoActualizado);
    }

    // ==================== VALIDACIONES ====================

    @Override
    @Transactional(readOnly = true)
    public boolean pedidoPerteneceACliente(Long idPedido, Long idCliente) {
        return pedidoRepository.existsByIdPedidoAndCliente_IdCliente(idPedido, idCliente);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void procesarDetallesPedido(Pedido pedido, List<DetallePedidoRequest> detallesRequest) {
        log.debug("Procesando {} detalles del pedido", detallesRequest.size());

        for (DetallePedidoRequest detalleRequest : detallesRequest) {

            DetallePedido detalle = detallePedidoMapper.toEntity(detalleRequest);
            detalle.setPedido(pedido);

            if (detalleRequest.getIdPromocion() != null) {
                Promocion promocion = promocionRepository.findById(detalleRequest.getIdPromocion())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Promoción no encontrada: " + detalleRequest.getIdPromocion()));

                if (!promocion.estaVigente()) {
                    throw new IllegalArgumentException("La promoción no está vigente");
                }

                if (promocion.getDetalles() == null || promocion.getDetalles().isEmpty()) {
                    throw new IllegalArgumentException(
                            "La promoción no tiene artículos: " + detalleRequest.getIdPromocion());
                }

                // ✅ Precio original = suma de TODOS los artículos del combo
                // Usamos cargarArticuloReal() para obtener la subclase concreta
                double precioOriginalCombo = 0.0;
                Articulo articuloPrincipal = null;

                for (PromocionDetalle pd : promocion.getDetalles()) {
                    Articulo art = cargarArticuloReal(pd.getArticulo().getIdArticulo());

                    double precioArticulo = art.getPrecioVenta() * pd.getCantidad();
                    precioOriginalCombo += precioArticulo;

                    log.debug("   📦 '{}' ({}): precioVenta=${} x{} = ${}",
                            art.getDenominacion(),
                            art.getClass().getSimpleName(),
                            art.getPrecioVenta(),
                            pd.getCantidad(),
                            precioArticulo);

                    if (articuloPrincipal == null) {
                        articuloPrincipal = art;
                    }
                }

                // ✅ Precio final según tipo de descuento (alineado con PromocionClienteMapper)
                double precioFinalCombo = switch (promocion.getTipoDescuento()) {
                    case PORCENTUAL -> precioOriginalCombo * (1 - promocion.getValorDescuento() / 100.0);
                    case MONTO_FIJO -> Math.max(0, precioOriginalCombo - promocion.getValorDescuento());
                };

                double descuentoTotal = precioOriginalCombo - precioFinalCombo;

                detalle.setArticulo(articuloPrincipal);
                detalle.setPrecioUnitarioOriginal(precioOriginalCombo);
                detalle.setDescuentoPromocion(descuentoTotal * detalleRequest.getCantidad());
                detalle.setPromocionAplicada(promocion);

                double subtotal = precioFinalCombo * detalleRequest.getCantidad();
                detalle.setSubtotal(subtotal);

                log.info("✅ Promo '{}' | Original: ${} | Descuento: ${} ({}) | Final: ${} | Subtotal(x{}): ${}",
                        promocion.getDenominacion(),
                        precioOriginalCombo,
                        descuentoTotal,
                        promocion.getTipoDescuento() == TipoDescuento.PORCENTUAL
                                ? promocion.getValorDescuento().intValue() + "%"
                                : "$" + promocion.getValorDescuento().intValue(),
                        precioFinalCombo,
                        detalleRequest.getCantidad(),
                        subtotal);

            } else {
                if (detalleRequest.getIdArticulo() == null) {
                    throw new IllegalArgumentException("idArticulo es requerido para artículos individuales");
                }

                // ✅ Cargar artículo real (subclase concreta)
                Articulo articulo = cargarArticuloReal(detalleRequest.getIdArticulo());

                detalle.setArticulo(articulo);
                detalle.setPrecioUnitarioOriginal(articulo.getPrecioVenta());
                detalle.setDescuentoPromocion(0.0);

                double subtotal = articulo.getPrecioVenta() * detalleRequest.getCantidad();
                detalle.setSubtotal(subtotal);

                log.debug("✅ Artículo '{}' | Precio: ${} | Subtotal(x{}): ${}",
                        articulo.getDenominacion(),
                        articulo.getPrecioVenta(),
                        detalleRequest.getCantidad(),
                        subtotal);
            }

            pedido.getDetalles().add(detalle);
        }
    }

    private void calcularTotales(Pedido pedido) {
        Double total = pedido.getDetalles().stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        Double totalCosto = pedido.getDetalles().stream()
                .mapToDouble(detalle -> {
                    if (detalle.getPromocionAplicada() != null
                            && detalle.getPromocionAplicada().getDetalles() != null) {

                        double costoCombo = detalle.getPromocionAplicada().getDetalles().stream()
                                .mapToDouble(pd -> {
                                    Articulo art = cargarArticuloReal(pd.getArticulo().getIdArticulo());
                                    double costo = getCostoArticulo(art);

                                    // ✅ LOG DETALLADO para comparar con admin
                                    log.info("   💰 Artículo: '{}' ({})",
                                            art.getDenominacion(),
                                            art.getClass().getSimpleName());
                                    log.info(
                                            "      precioVenta=${}  |  costo(costoProduccion/precioCompra)=${}  |  cantidad={}",
                                            art.getPrecioVenta(), costo, pd.getCantidad());
                                    log.info("      subtotalCosto=${}", costo * pd.getCantidad());

                                    return costo * pd.getCantidad();
                                })
                                .sum();

                        double costoTotal = costoCombo * detalle.getCantidad();
                        log.info("   ✅ Costo total combo x{}: ${}", detalle.getCantidad(), costoTotal);
                        return costoTotal;
                    }

                    Articulo art = cargarArticuloReal(detalle.getArticulo().getIdArticulo());
                    double costo = getCostoArticulo(art) * detalle.getCantidad();

                    log.info("   💰 Artículo individual: '{}' ({}) | precioVenta=${} | costo=${} | x{}",
                            art.getDenominacion(),
                            art.getClass().getSimpleName(),
                            art.getPrecioVenta(),
                            getCostoArticulo(art),
                            detalle.getCantidad());

                    return costo;
                })
                .sum();

        pedido.setTotal(total);
        pedido.setTotalCosto(totalCosto);

        log.info("📊 ═══════════════════════════════════════");
        log.info("📊 Total venta:    ${}", String.format("%.2f", total));
        log.info("📊 Total costo:    ${}", String.format("%.2f", totalCosto));
        log.info("📊 Ganancia neta:  ${}", String.format("%.2f", total - totalCosto));
        log.info("📊 ═══════════════════════════════════════");
    }

    // ✅ Helper para obtener costo — recibe artículo ya cargado como subclase
    // concreta
    private double getCostoArticulo(Articulo articulo) {
        if (articulo == null)
            return 0.0;

        if (articulo instanceof ArticuloManufacturado am) {
            Double costo = am.getCostoProduccion();
            if (costo == null || costo == 0.0) {
                log.warn("⚠️ ArticuloManufacturado '{}' (id={}) costoProduccion={}",
                        am.getDenominacion(), am.getIdArticulo(), costo);
            }
            return costo != null ? costo : 0.0;
        }

        if (articulo instanceof ArticuloInsumo ai) {
            Double costo = ai.getPrecioCompra();
            if (costo == null || costo == 0.0) {
                log.warn("⚠️ ArticuloInsumo '{}' (id={}) precioCompra={}",
                        ai.getDenominacion(), ai.getIdArticulo(), costo);
            }
            return costo != null ? costo : 0.0;
        }

        log.warn("⚠️ Tipo desconocido: {} ({})",
                articulo.getDenominacion(), articulo.getClass().getSimpleName());
        return 0.0;
    }

    private void validarTransicionEstado(Pedido pedido, Estado nuevoEstado, Rol rolUsuario) {
        Estado estadoActual = pedido.getEstado();

        // Validar según el rol
        switch (rolUsuario) {
            case COCINERO:
                if (nuevoEstado != Estado.PREPARACION && nuevoEstado != Estado.LISTO) {
                    throw new IllegalArgumentException("Cocinero solo puede cambiar a PREPARACION o LISTO");
                }
                break;
            case DELIVERY:
                if (nuevoEstado != Estado.ENTREGADO) {
                    throw new IllegalArgumentException("Delivery solo puede marcar como ENTREGADO");
                }
                break;
            case CAJERO:
            case ADMIN:
                // Pueden hacer cualquier cambio (con validaciones lógicas)
                break;
            default:
                throw new IllegalArgumentException("Rol no autorizado para cambiar estados");
        }

        // Validaciones lógicas de transición
        if (estadoActual == Estado.ENTREGADO || estadoActual == Estado.CANCELADO) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un pedido " + estadoActual);
        }
    }

    private void actualizarEstado(Pedido pedido, Estado nuevoEstado) {
        pedido.setEstado(nuevoEstado);

        switch (nuevoEstado) {
            case PREPARACION:
                if (pedido.getFechaInicioPreparacion() == null) {
                    pedido.setFechaInicioPreparacion(LocalDateTime.now());
                }
                break;
            case LISTO:
                if (pedido.getFechaListo() == null) {
                    pedido.setFechaListo(LocalDateTime.now());
                }
                break;
            case ENTREGADO:
                if (pedido.getFechaEntregado() == null) {
                    pedido.setFechaEntregado(LocalDateTime.now());
                }
                break;
            case CANCELADO:
                if (pedido.getFechaCancelado() == null) {
                    pedido.setFechaCancelado(LocalDateTime.now());
                }
                break;
        }
    }

    /**
     * ✅ Carga el artículo real (subclase concreta) desde el repository específico.
     * Esto garantiza que los campos propios de la subclase (costoProduccion,
     * precioCompra)
     * estén correctamente inicializados — igual que en
     * PromocionClienteMapper/CatalogoService.
     */
    private Articulo cargarArticuloReal(Long idArticulo) {
        // Intentar primero como ArticuloManufacturado
        var manufacturado = articuloManufacturadoRepository.findById(idArticulo);
        if (manufacturado.isPresent()) {
            ArticuloManufacturado am = manufacturado.get();
            log.debug("   📦 Cargado como ArticuloManufacturado: '{}' | precioVenta=${} | costoProduccion=${}",
                    am.getDenominacion(), am.getPrecioVenta(), am.getCostoProduccion());
            return am;
        }

        // Intentar como ArticuloInsumo
        var insumo = articuloInsumoRepository.findById(idArticulo);
        if (insumo.isPresent()) {
            ArticuloInsumo ai = insumo.get();
            log.debug("   📦 Cargado como ArticuloInsumo: '{}' | precioVenta=${} | precioCompra=${}",
                    ai.getDenominacion(), ai.getPrecioVenta(), ai.getPrecioCompra());
            return ai;
        }

        // Fallback: cargar como Articulo base
        return articuloRepository.findById(idArticulo)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + idArticulo));
    }
}