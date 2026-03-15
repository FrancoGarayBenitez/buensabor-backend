// package com.elbuensabor.services.impl;

// import com.elbuensabor.dto.response.DetallePedidoResponseDTO;
// import com.elbuensabor.dto.response.DomicilioResponseDTO;
// import com.elbuensabor.dto.response.FacturaResponseDTO;
// import com.elbuensabor.dto.response.PagoSummaryDTO;
// import com.elbuensabor.dto.response.pedido.DetallePedidoResponse;
// import com.elbuensabor.entities.*;
// import com.elbuensabor.exceptions.DuplicateResourceException;
// import com.elbuensabor.exceptions.ResourceNotFoundException;
// import com.elbuensabor.repository.IFacturaRepository;
// import com.elbuensabor.repository.IPagoRepository;
// import com.elbuensabor.services.IFacturaService;
// import com.elbuensabor.services.mapper.FacturaMapper;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class FacturaServiceImpl
// extends GenericServiceImpl<Factura, Long, FacturaResponseDTO,
// IFacturaRepository, FacturaMapper>
// implements IFacturaService {
// private static final Logger logger =
// LoggerFactory.getLogger(FacturaServiceImpl.class);

// @Autowired
// private IPagoRepository pagoRepository;

// @Autowired
// public FacturaServiceImpl(IFacturaRepository repository, FacturaMapper
// mapper) {
// super(repository, mapper, Factura.class, FacturaResponseDTO.class);
// }

// @Override
// @Transactional
// public FacturaResponseDTO crearFacturaFromPedido(Pedido pedido) {
// // Validar que el pedido no tenga ya una factura
// if (repository.existsByPedidoIdPedido(pedido.getIdPedido())) {
// throw new DuplicateResourceException(
// "El pedido con ID " + pedido.getIdPedido() + " ya tiene una factura
// asociada");
// }

// // Crear nueva factura
// Factura factura = new Factura();
// factura.setPedido(pedido);
// factura.setFechaFactura(LocalDate.now());
// factura.setNroComprobante(generarNumeroComprobante());

// // Calcular totales
// calcularTotalesFactura(factura, pedido);

// Factura facturaGuardada = repository.save(factura);
// return mapearFacturaSimple(facturaGuardada);
// }

// @Override
// @Transactional(readOnly = true)
// public FacturaResponseDTO findByPedidoId(Long pedidoId) {
// Factura factura = repository.findByPedidoIdPedido(pedidoId)
// .orElseThrow(() -> new ResourceNotFoundException(
// "No se encontró factura para el pedido con ID: " + pedidoId));
// return mapearFacturaSimple(factura);
// }

// @Override
// @Transactional(readOnly = true)
// public List<FacturaResponseDTO> findByClienteId(Long clienteId) {
// List<Factura> facturas = repository.findByClienteId(clienteId);
// return facturas.stream()
// .map(this::mapearFacturaSimple)
// .collect(Collectors.toList());
// }

// @Override
// @Transactional(readOnly = true)
// public List<FacturaResponseDTO> findByFechaRange(LocalDate fechaInicio,
// LocalDate fechaFin) {
// List<Factura> facturas = repository.findByFechaFacturaBetween(fechaInicio,
// fechaFin);
// return facturas.stream()
// .map(this::mapearFacturaSimple)
// .collect(Collectors.toList());
// }

// @Override
// @Transactional(readOnly = true)
// public List<FacturaResponseDTO> findFacturasPendientesPago() {
// List<Factura> facturas = repository.findFacturasPendientesPago();
// return facturas.stream()
// .map(this::mapearFacturaSimple)
// .collect(Collectors.toList());
// }

// @Override
// @Transactional(readOnly = true)
// public boolean existeFacturaParaPedido(Long pedidoId) {
// return repository.existsByPedidoIdPedido(pedidoId);
// }

// @Override
// public String generarNumeroComprobante() {
// String fecha =
// LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
// String prefijo = "FAC-" + fecha + "-";

// // Generar número secuencial simple
// long timestamp = System.currentTimeMillis() % 10000; // Últimos 4 dígitos del
// timestamp
// return prefijo + String.format("%04d", timestamp);
// }

// @Override
// @Transactional(readOnly = true)
// public FacturaResponseDTO calcularTotales(Pedido pedido) {
// Factura facturaTemp = new Factura();
// facturaTemp.setPedido(pedido);
// calcularTotalesFactura(facturaTemp, pedido);
// return mapearFacturaSimple(facturaTemp);
// }

// @Override
// @Transactional(readOnly = true)
// public FacturaResponseDTO findById(Long id) {
// Factura factura = repository.findById(id)
// .orElseThrow(() -> new ResourceNotFoundException("Factura con ID " + id + "
// no encontrada"));
// return mapearFacturaSimple(factura);
// }

// // ==================== MÉTODOS AUXILIARES PRIVADOS ====================
// // ✅ CORRECCIÓN COMPLETA en FacturaServiceImpl.java - método
// // calcularTotalesFactura

// private void calcularTotalesFactura(Factura factura, Pedido pedido) {
// logger.info("💰 === CALCULANDO TOTALES DE FACTURA ===");
// logger.info("💰 Total del pedido recibido: ${}", pedido.getTotal());
// logger.info("💰 Tipo de envío: {}", pedido.getTipoEnvio());

// Double totalPedido = pedido.getTotal(); // Ya viene procesado desde
// PedidoService
// Double subtotal;
// Double gastosEnvio = 0.0;
// Double descuento = 0.0;

// // 🚚 DELIVERY: El total YA incluye gastos de envío, separamos para mostrar
// if (pedido.getTipoEnvio() == TipoEnvio.DELIVERY) {
// gastosEnvio = 200.0;
// subtotal = totalPedido - gastosEnvio;

// logger.info("🚚 DELIVERY: Total ya incluye gastos de envío");
// logger.info(" Total pedido: ${}", totalPedido);
// logger.info(" Gastos envío incluidos: ${}", gastosEnvio);
// logger.info(" Subtotal productos: ${}", subtotal);
// logger.info(" ✅ NO se agregan gastos adicionales - ya están incluidos");
// }

// // 🏪 TAKE_AWAY: El total YA tiene descuento aplicado, calculamos para
// mostrar
// else if (pedido.getTipoEnvio() == TipoEnvio.TAKE_AWAY) {
// // El total viene con descuento del 10% ya aplicado
// // Calculamos el subtotal original para mostrar el desglose
// subtotal = totalPedido / 0.9; // Reverso del 10% de descuento
// descuento = subtotal * 0.10; // 10% para mostrar en factura

// logger.info("🏪 TAKE_AWAY: Total ya viene con descuento aplicado");
// logger.info(" Total final (con descuento): ${}", totalPedido);
// logger.info(" Subtotal original calculado: ${}", subtotal);
// logger.info(" Descuento para mostrar (10%): ${}", descuento);
// logger.info(" ✅ NO se aplica descuento adicional - solo se muestra");
// }

// // ⚠️ OTROS TIPOS (fallback)
// else {
// subtotal = totalPedido;
// logger.info("⚠️ Tipo de envío no reconocido, usando total como subtotal");
// }

// // ✅ IMPORTANTE: totalVenta SIEMPRE es el total del pedido (ya procesado
// // correctamente)
// Double totalVenta = totalPedido;

// // Asignar valores a la factura
// factura.setSubTotal(subtotal);
// factura.setDescuento(descuento);
// factura.setGastosEnvio(gastosEnvio);
// factura.setTotalVenta(totalVenta);

// logger.info("💰 === TOTALES FINALES DE FACTURA ===");
// logger.info(" Subtotal: ${}", subtotal);
// logger.info(" Descuento: ${}", descuento);
// logger.info(" Gastos Envío: ${}", gastosEnvio);
// logger.info(" TOTAL VENTA: ${}", totalVenta);

// // ✅ Verificación matemática
// double verificacion = subtotal + gastosEnvio - descuento;
// logger.info(" ✅ Verificación: ${} + ${} - ${} = ${}",
// subtotal, gastosEnvio, descuento, verificacion);

// if (Math.abs(verificacion - totalVenta) > 0.01) {
// logger.warn("⚠️ ADVERTENCIA: La verificación matemática no coincide!");
// logger.warn(" Calculado: ${}, Esperado: ${}", verificacion, totalVenta);
// } else {
// logger.info(" ✅ Verificación matemática correcta");
// }
// }

// // ✅ MÉTODO ACTUALIZADO para mapear datos completos del pedido
// private FacturaResponseDTO mapearFacturaSimple(Factura factura) {
// FacturaResponseDTO dto = new FacturaResponseDTO();

// // Mapeo básico de factura
// dto.setIdFactura(factura.getIdFactura());
// dto.setFechaFactura(factura.getFechaFactura());
// dto.setNroComprobante(factura.getNroComprobante());
// dto.setSubTotal(factura.getSubTotal());
// dto.setDescuento(factura.getDescuento());
// dto.setGastosEnvio(factura.getGastosEnvio());
// dto.setTotalVenta(factura.getTotalVenta());

// // Información del pedido
// if (factura.getPedido() != null) {
// Pedido pedido = factura.getPedido();
// dto.setPedidoId(pedido.getIdPedido());
// dto.setEstadoPedido(pedido.getEstado() != null ?
// pedido.getEstado().toString() : null);
// dto.setTipoEnvio(pedido.getTipoEnvio() != null ?
// pedido.getTipoEnvio().toString() : null);
// dto.setObservacionesPedido(pedido.getObservaciones());

// // 🆕 INFORMACIÓN COMPLETA DEL CLIENTE
// if (pedido.getCliente() != null) {
// Cliente cliente = pedido.getCliente();
// dto.setClienteId(cliente.getIdCliente());
// dto.setNombreCliente(cliente.getUsuario().getNombre());
// dto.setApellidoCliente(cliente.getUsuario().getApellido());
// dto.setTelefonoCliente(cliente.getTelefono());
// }

// // 🆕 DOMICILIO DE ENTREGA (si es DELIVERY)
// if (pedido.getDomicilio() != null && "DELIVERY".equals(dto.getTipoEnvio())) {
// Domicilio domicilio = pedido.getDomicilio();
// DomicilioResponseDTO domicilioDTO = new DomicilioResponseDTO();
// domicilioDTO.setIdDomicilio(domicilio.getIdDomicilio());
// domicilioDTO.setCalle(domicilio.getCalle());
// domicilioDTO.setNumero(domicilio.getNumero());
// domicilioDTO.setLocalidad(domicilio.getLocalidad());
// domicilioDTO.setCp(domicilio.getCp());
// dto.setDomicilioEntrega(domicilioDTO);
// }

// // 🆕 DETALLES REALES DEL PEDIDO CON PROMOCIONES COMPLETAS
// if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
// logger.info("🔍 DEBUG: Cargando {} detalles reales del pedido {}",
// pedido.getDetalles().size(), pedido.getIdPedido());

// List<DetallePedidoResponse> detallesDTO = new ArrayList<>();

// for (DetallePedido detalle : pedido.getDetalles()) {
// logger.info("🔍 DEBUG PROMOCIONES: Detalle - {} x{} = ${} (orig: ${}, desc:
// ${})",
// detalle.getArticulo().getDenominacion(),
// detalle.getCantidad(),
// detalle.getSubtotal(),
// detalle.getPrecioUnitarioOriginal(),
// detalle.getDescuentoPromocion());

// DetallePedidoResponse detalleDTO = new DetallePedidoResponse();
// detalleDTO.setIdDetallePedido(detalle.getIdDetallePedido());
// detalleDTO.setCantidad(detalle.getCantidad());
// detalleDTO.setSubtotal(detalle.getSubtotal());
// detalleDTO.setObservaciones(detalle.getObservaciones());

// // Información del artículo
// if (detalle.getArticulo() != null) {
// Articulo articulo = detalle.getArticulo();
// detalleDTO.setIdArticulo(articulo.getIdArticulo());
// detalleDTO.setDenominacionArticulo(articulo.getDenominacion());

// if (articulo.getUnidadMedida() != null) {
// detalleDTO.setUnidadMedida(articulo.getUnidadMedida().getDenominacion());
// }
// }

// // ✅ NUEVOS: Campos de promociones COMPLETOS
// if (detalle.getPrecioUnitarioOriginal() != null) {
// // Tiene información de promoción guardada
// detalleDTO.setPrecioUnitarioOriginal(detalle.getPrecioUnitarioOriginal());
// detalleDTO.setDescuentoPromocion(
// detalle.getDescuentoPromocion() != null ? detalle.getDescuentoPromocion() :
// 0.0);

// // Calcular precio unitario final
// double descuentoPorUnidad = detalleDTO.getDescuentoPromocion() /
// detalle.getCantidad();
// double precioUnitarioFinal = detalle.getPrecioUnitarioOriginal() -
// descuentoPorUnidad;
// detalleDTO.setPrecioUnitarioFinal(precioUnitarioFinal);
// detalleDTO.setPrecioUnitario(precioUnitarioFinal); // Para compatibilidad

// // Marcar que tiene promoción
// detalleDTO.setTienePromocion(
// detalle.getDescuentoPromocion() != null && detalle.getDescuentoPromocion() >
// 0);

// logger.info("✅ PROMOCIÓN DETECTADA: {} - Original: ${}, Final: ${},
// Descuento: ${}",
// detalle.getArticulo().getDenominacion(),
// detalle.getPrecioUnitarioOriginal(),
// precioUnitarioFinal,
// detalleDTO.getDescuentoPromocion());
// } else {
// // Sin promoción - usar precio del artículo
// if (detalle.getArticulo() != null) {
// detalleDTO.setPrecioUnitarioOriginal(detalle.getArticulo().getPrecioVenta());
// detalleDTO.setPrecioUnitario(detalle.getArticulo().getPrecioVenta());
// detalleDTO.setPrecioUnitarioFinal(detalle.getArticulo().getPrecioVenta());
// detalleDTO.setDescuentoPromocion(0.0);
// detalleDTO.setTienePromocion(false);

// logger.info("✅ SIN PROMOCIÓN: {} - Precio: ${}",
// detalle.getArticulo().getDenominacion(),
// detalle.getArticulo().getPrecioVenta());
// }
// }

// // ✅ INFORMACIÓN DE PROMOCIÓN APLICADA (si existe)
// if (detalle.getPromocionAplicada() != null) {
// DetallePedidoResponseDTO.PromocionAplicadaDTO promocionDTO = new
// DetallePedidoResponseDTO.PromocionAplicadaDTO();
// promocionDTO.setIdPromocion(detalle.getPromocionAplicada().getIdPromocion());
// promocionDTO.setDenominacion(detalle.getPromocionAplicada().getDenominacion());
// promocionDTO.setDescripcion(detalle.getPromocionAplicada().getDescripcionDescuento());
// promocionDTO.setTipoDescuento(detalle.getPromocionAplicada().getTipoDescuento().toString());
// promocionDTO.setValorDescuento(detalle.getPromocionAplicada().getValorDescuento());
// promocionDTO.setResumenDescuento(
// String.format("%s - Ahorro: $%.2f",
// detalle.getPromocionAplicada().getDenominacion(),
// detalleDTO.getDescuentoPromocion()));
// detalleDTO.setPromocionAplicada(promocionDTO);

// logger.info("✅ INFO PROMOCIÓN: {}", promocionDTO.getDenominacion());
// }

// detallesDTO.add(detalleDTO);
// }

// dto.setDetallesPedido(detallesDTO);
// logger.info("📋 {} detalles con información completa de promociones
// cargados", detallesDTO.size());
// }
// }

// // ✅ CALCULAR PAGOS REALES desde la BD (código existente sin cambios)
// try {
// List<Pago> pagosFactura =
// pagoRepository.findByFacturaIdFactura(factura.getIdFactura());

// List<PagoSummaryDTO> pagosSummary = new ArrayList<>();
// double totalPagado = 0.0;

// for (Pago pago : pagosFactura) {
// if (EstadoPago.APROBADO.equals(pago.getEstado())) {
// totalPagado += pago.getMonto();

// PagoSummaryDTO summary = new PagoSummaryDTO();
// summary.setIdPago(pago.getIdPago());
// summary.setFormaPago(pago.getFormaPago().name());
// summary.setEstado(pago.getEstado().name());
// summary.setMonto(pago.getMonto());
// summary.setFechaCreacion(pago.getFechaCreacion().format(
// DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

// pagosSummary.add(summary);
// }
// }

// dto.setPagos(pagosSummary);
// dto.setTotalPagado(totalPagado);
// dto.setSaldoPendiente(factura.getTotalVenta() - totalPagado);
// dto.setCompletamentePagada(totalPagado >= factura.getTotalVenta());

// logger.info("Factura {}: {} pagos encontrados, total pagado: ${}",
// factura.getIdFactura(), pagosFactura.size(), totalPagado);

// } catch (Exception e) {
// logger.error("Error calculando pagos: {}", e.getMessage());
// // Fallback a valores por defecto
// dto.setPagos(new ArrayList<>());
// dto.setTotalPagado(0.0);
// dto.setSaldoPendiente(factura.getTotalVenta());
// dto.setCompletamentePagada(false);
// }

// return dto;
// }
// }