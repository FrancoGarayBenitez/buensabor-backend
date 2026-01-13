// package com.elbuensabor.services.impl;

// import com.elbuensabor.dto.request.DetallePedidoRequestDTO;
// import com.elbuensabor.dto.request.PedidoRequestDTO;
// import com.elbuensabor.dto.request.PromocionAgrupadaDTO;
// import com.elbuensabor.dto.response.PedidoResponseDTO;
// import com.elbuensabor.entities.Articulo;
// import com.elbuensabor.entities.DetallePedido;
// import com.elbuensabor.entities.Promocion;
// import com.elbuensabor.exceptions.ResourceNotFoundException;
// import com.elbuensabor.repository.IArticuloRepository;
// import com.elbuensabor.repository.IPromocionRepository;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// @Service
// public class PromocionPedidoService {

// private static final Logger logger =
// LoggerFactory.getLogger(PromocionPedidoService.class);

// @Autowired
// private IPromocionRepository promocionRepository;

// @Autowired
// private IArticuloRepository articuloRepository;

// // ==================== MÉTODO PRINCIPAL: APLICAR PROMOCIONES A PEDIDO
// // ====================

// public PromocionesAplicadasDTO aplicarPromocionesAPedido(PedidoRequestDTO
// pedidoRequest) {
// logger.info("🎯 Aplicando promociones a pedido con {} detalles",
// pedidoRequest.getDetalles().size());

// PromocionesAplicadasDTO resultado = new PromocionesAplicadasDTO();
// resultado.setDetallesConPromociones(new ArrayList<>());
// resultado.setDescuentoTotal(0.0);
// resultado.setSubtotalOriginal(0.0);

// for (DetallePedidoRequestDTO detalleRequest : pedidoRequest.getDetalles()) {
// DetalleConPromocionDTO detalleConPromocion = procesarDetalleConPromocion(
// detalleRequest,
// pedidoRequest.getIdSucursal());

// resultado.getDetallesConPromociones().add(detalleConPromocion);
// resultado.setSubtotalOriginal(resultado.getSubtotalOriginal() +
// detalleConPromocion.getSubtotalOriginal());
// resultado.setDescuentoTotal(resultado.getDescuentoTotal() +
// detalleConPromocion.getDescuentoAplicado());
// }

// resultado.setSubtotalFinal(resultado.getSubtotalOriginal() -
// resultado.getDescuentoTotal());
// resultado.generarResumen();

// logger.info("🎉 Promociones aplicadas: Subtotal original: ${}, Descuento:
// ${}, Final: ${}",
// resultado.getSubtotalOriginal(), resultado.getDescuentoTotal(),
// resultado.getSubtotalFinal());

// return resultado;
// }

// // ==================== PROCESAR DETALLE INDIVIDUAL ====================

// private DetalleConPromocionDTO
// procesarDetalleConPromocion(DetallePedidoRequestDTO detalleRequest,
// Long idSucursal) {
// DetalleConPromocionDTO detalle = new DetalleConPromocionDTO();

// // Obtener artículo
// Articulo articulo =
// articuloRepository.findById(detalleRequest.getIdArticulo())
// .orElseThrow(() -> new ResourceNotFoundException(
// "Artículo no encontrado: " + detalleRequest.getIdArticulo()));

// // Datos básicos
// detalle.setIdArticulo(articulo.getIdArticulo());
// detalle.setDenominacionArticulo(articulo.getDenominacion());
// detalle.setCantidad(detalleRequest.getCantidad());
// detalle.setPrecioUnitarioOriginal(articulo.getPrecioVenta());
// detalle.setSubtotalOriginal(articulo.getPrecioVenta() *
// detalleRequest.getCantidad());
// detalle.setObservaciones(detalleRequest.getObservaciones());

// // Aplicar promoción si fue seleccionada
// if (detalleRequest.getIdPromocionSeleccionada() != null) {
// aplicarPromocionADetalle(detalle,
// detalleRequest.getIdPromocionSeleccionada(), idSucursal);
// } else {
// // Sin promoción
// detalle.setDescuentoAplicado(0.0);
// detalle.setPrecioUnitarioFinal(articulo.getPrecioVenta());
// detalle.setSubtotalFinal(detalle.getSubtotalOriginal());
// detalle.setTienePromocion(false);
// }

// return detalle;
// }

// // ==================== APLICAR PROMOCIÓN A DETALLE ====================

// private void aplicarPromocionADetalle(DetalleConPromocionDTO detalle, Long
// idPromocion, Long idSucursal) {
// try {
// Optional<Promocion> promocionOpt = promocionRepository.findById(idPromocion);

// if (promocionOpt.isEmpty()) {
// logger.warn("⚠️ Promoción {} no encontrada, se omite", idPromocion);
// sinPromocion(detalle);
// return;
// }

// Promocion promocion = promocionOpt.get();

// // Validar que la promoción esté vigente
// if (!promocion.estaVigente()) {
// logger.warn("⚠️ Promoción '{}' no está vigente, se omite",
// promocion.getDenominacion());
// sinPromocion(detalle);
// return;
// }

// // Validar que aplique para el artículo
// if (!promocion.aplicaParaArticulo(detalle.getIdArticulo())) {
// logger.warn("⚠️ Promoción '{}' no aplica para artículo {}, se omite",
// promocion.getDenominacion(), detalle.getIdArticulo());
// sinPromocion(detalle);
// return;
// }

// // Validar cantidad mínima
// if (detalle.getCantidad() < promocion.getCantidadMinima()) {
// logger.warn("⚠️ Promoción '{}' requiere cantidad mínima {}, actual: {}, se
// omite",
// promocion.getDenominacion(), promocion.getCantidadMinima(),
// detalle.getCantidad());
// sinPromocion(detalle);
// return;
// }

// // ✅ APLICAR PROMOCIÓN
// Double descuento =
// promocion.calcularDescuento(detalle.getPrecioUnitarioOriginal(),
// detalle.getCantidad());

// detalle.setDescuentoAplicado(descuento);
// detalle.setPrecioUnitarioFinal(detalle.getPrecioUnitarioOriginal() -
// (descuento / detalle.getCantidad()));
// detalle.setSubtotalFinal(detalle.getSubtotalOriginal() - descuento);
// detalle.setTienePromocion(true);

// // Información de la promoción
// detalle.setPromocionAplicada(new DetalleConPromocionDTO.PromocionInfoDTO());
// detalle.getPromocionAplicada().setIdPromocion(promocion.getIdPromocion());
// detalle.getPromocionAplicada().setDenominacion(promocion.getDenominacion());
// detalle.getPromocionAplicada().setDescripcion(promocion.getDescripcionDescuento());
// detalle.getPromocionAplicada().setTipoDescuento(promocion.getTipoDescuento().toString());
// detalle.getPromocionAplicada().setValorDescuento(promocion.getValorDescuento());
// detalle.getPromocionAplicada().setResumenDescuento(
// String.format("%s - Ahorro: $%.2f", promocion.getDenominacion(), descuento));

// logger.info("✅ Promoción '{}' aplicada a {}: descuento ${}",
// promocion.getDenominacion(), detalle.getDenominacionArticulo(), descuento);

// } catch (Exception e) {
// logger.error("❌ Error aplicando promoción {}: {}", idPromocion,
// e.getMessage());
// sinPromocion(detalle);
// }
// }

// private void sinPromocion(DetalleConPromocionDTO detalle) {
// detalle.setDescuentoAplicado(0.0);
// detalle.setPrecioUnitarioFinal(detalle.getPrecioUnitarioOriginal());
// detalle.setSubtotalFinal(detalle.getSubtotalOriginal());
// detalle.setTienePromocion(false);
// detalle.setPromocionAplicada(null);
// }

// // ==================== DTOs AUXILIARES ====================

// public static class PromocionesAplicadasDTO {
// private List<DetalleConPromocionDTO> detallesConPromociones;
// private Double subtotalOriginal;
// private Double descuentoTotal;
// private Double subtotalFinal;
// private String resumenPromociones;

// public void generarResumen() {
// long promocionesAplicadas = detallesConPromociones.stream()
// .mapToLong(d -> d.getTienePromocion() ? 1 : 0)
// .sum();

// if (promocionesAplicadas == 0) {
// resumenPromociones = "Sin promociones aplicadas";
// } else {
// resumenPromociones = String.format("%d promoción(es) aplicada(s) - Ahorro
// total: $%.2f",
// promocionesAplicadas, descuentoTotal);
// }
// }

// // Getters y setters
// public List<DetalleConPromocionDTO> getDetallesConPromociones() {
// return detallesConPromociones;
// }

// public void setDetallesConPromociones(List<DetalleConPromocionDTO>
// detallesConPromociones) {
// this.detallesConPromociones = detallesConPromociones;
// }

// public Double getSubtotalOriginal() {
// return subtotalOriginal;
// }

// public void setSubtotalOriginal(Double subtotalOriginal) {
// this.subtotalOriginal = subtotalOriginal;
// }

// public Double getDescuentoTotal() {
// return descuentoTotal;
// }

// public void setDescuentoTotal(Double descuentoTotal) {
// this.descuentoTotal = descuentoTotal;
// }

// public Double getSubtotalFinal() {
// return subtotalFinal;
// }

// public void setSubtotalFinal(Double subtotalFinal) {
// this.subtotalFinal = subtotalFinal;
// }

// public String getResumenPromociones() {
// return resumenPromociones;
// }

// public void setResumenPromociones(String resumenPromociones) {
// this.resumenPromociones = resumenPromociones;
// }
// }

// public static class DetalleConPromocionDTO {
// private Long idArticulo;
// private String denominacionArticulo;
// private Integer cantidad;
// private Double precioUnitarioOriginal;
// private Double precioUnitarioFinal;
// private Double subtotalOriginal;
// private Double subtotalFinal;
// private Double descuentoAplicado;
// private Boolean tienePromocion;
// private String observaciones;
// private PromocionInfoDTO promocionAplicada;

// public static class PromocionInfoDTO {
// private Long idPromocion;
// private String denominacion;
// private String descripcion;
// private String tipoDescuento;
// private Double valorDescuento;
// private String resumenDescuento;

// // Getters y setters
// public Long getIdPromocion() {
// return idPromocion;
// }

// public void setIdPromocion(Long idPromocion) {
// this.idPromocion = idPromocion;
// }

// public String getDenominacion() {
// return denominacion;
// }

// public void setDenominacion(String denominacion) {
// this.denominacion = denominacion;
// }

// public String getDescripcion() {
// return descripcion;
// }

// public void setDescripcion(String descripcion) {
// this.descripcion = descripcion;
// }

// public String getTipoDescuento() {
// return tipoDescuento;
// }

// public void setTipoDescuento(String tipoDescuento) {
// this.tipoDescuento = tipoDescuento;
// }

// public Double getValorDescuento() {
// return valorDescuento;
// }

// public void setValorDescuento(Double valorDescuento) {
// this.valorDescuento = valorDescuento;
// }

// public String getResumenDescuento() {
// return resumenDescuento;
// }

// public void setResumenDescuento(String resumenDescuento) {
// this.resumenDescuento = resumenDescuento;
// }
// }

// // Getters y setters completos
// public Long getIdArticulo() {
// return idArticulo;
// }

// public void setIdArticulo(Long idArticulo) {
// this.idArticulo = idArticulo;
// }

// public String getDenominacionArticulo() {
// return denominacionArticulo;
// }

// public void setDenominacionArticulo(String denominacionArticulo) {
// this.denominacionArticulo = denominacionArticulo;
// }

// public Integer getCantidad() {
// return cantidad;
// }

// public void setCantidad(Integer cantidad) {
// this.cantidad = cantidad;
// }

// public Double getPrecioUnitarioOriginal() {
// return precioUnitarioOriginal;
// }

// public void setPrecioUnitarioOriginal(Double precioUnitarioOriginal) {
// this.precioUnitarioOriginal = precioUnitarioOriginal;
// }

// public Double getPrecioUnitarioFinal() {
// return precioUnitarioFinal;
// }

// public void setPrecioUnitarioFinal(Double precioUnitarioFinal) {
// this.precioUnitarioFinal = precioUnitarioFinal;
// }

// public Double getSubtotalOriginal() {
// return subtotalOriginal;
// }

// public void setSubtotalOriginal(Double subtotalOriginal) {
// this.subtotalOriginal = subtotalOriginal;
// }

// public Double getSubtotalFinal() {
// return subtotalFinal;
// }

// public void setSubtotalFinal(Double subtotalFinal) {
// this.subtotalFinal = subtotalFinal;
// }

// public Double getDescuentoAplicado() {
// return descuentoAplicado;
// }

// public void setDescuentoAplicado(Double descuentoAplicado) {
// this.descuentoAplicado = descuentoAplicado;
// }

// public Boolean getTienePromocion() {
// return tienePromocion;
// }

// public void setTienePromocion(Boolean tienePromocion) {
// this.tienePromocion = tienePromocion;
// }

// public String getObservaciones() {
// return observaciones;
// }

// public void setObservaciones(String observaciones) {
// this.observaciones = observaciones;
// }

// public PromocionInfoDTO getPromocionAplicada() {
// return promocionAplicada;
// }

// public void setPromocionAplicada(PromocionInfoDTO promocionAplicada) {
// this.promocionAplicada = promocionAplicada;
// }
// }

// public PromocionesAplicadasDTO
// aplicarPromocionesAPedidoConAgrupada(PedidoRequestDTO pedidoRequest) {
// System.out.println("🎁 === APLICANDO PROMOCIONES CON PROMOCIÓN AGRUPADA
// ===");

// // 1. Aplicar promociones individuales normalmente
// PromocionesAplicadasDTO promocionesIndividuales =
// aplicarPromocionesAPedido(pedidoRequest);
// System.out.println(
// "🎯 Promociones individuales procesadas. Descuento: $" +
// promocionesIndividuales.getDescuentoTotal());

// // 2. Si hay promoción agrupada, aplicarla a los detalles
// if (pedidoRequest.getPromocionAgrupada() != null) {
// PromocionAgrupadaDTO promocionAgrupada =
// pedidoRequest.getPromocionAgrupada();
// System.out.println("🎁 Aplicando promoción agrupada: " +
// promocionAgrupada.getDenominacion());

// // ✅ NUEVO: Aplicar promoción agrupada a cada detalle
// aplicarPromocionAgrupadaADetalles(promocionesIndividuales, promocionAgrupada,
// pedidoRequest);
// }

// System.out.println("💰 === RESUMEN FINAL ===");
// System.out.println("💰 Subtotal original: $" +
// promocionesIndividuales.getSubtotalOriginal());
// System.out.println("🎯 Descuento total: $" +
// promocionesIndividuales.getDescuentoTotal());
// System.out.println("💰 Subtotal final: $" +
// promocionesIndividuales.getSubtotalFinal());

// return promocionesIndividuales;
// }

// // ✅ CORREGIDO: Método para aplicar promoción agrupada SOLO a productos
// // incluidos
// private void aplicarPromocionAgrupadaADetalles(
// PromocionesAplicadasDTO promocionesAplicadas,
// PromocionAgrupadaDTO promocionAgrupada,
// PedidoRequestDTO pedidoRequest) {

// System.out.println("🔄 Aplicando promoción agrupada a detalles
// individuales...");

// // Obtener la promoción desde la base de datos
// Optional<Promocion> promocionEntityOpt =
// promocionRepository.findById(promocionAgrupada.getIdPromocion());

// if (promocionEntityOpt.isEmpty()) {
// System.out.println("⚠️ Promoción agrupada no encontrada en BD, se omite
// aplicación a detalles");
// return;
// }

// Promocion promocionEntity = promocionEntityOpt.get();
// System.out.println("✅ Promoción encontrada: " +
// promocionEntity.getDenominacion());

// // ✅ PASO 1: Identificar qué productos están incluidos en la promoción
// List<Long> productosEnPromocion = new ArrayList<>();
// double subtotalProductosEnPromocion = 0.0;

// for (DetalleConPromocionDTO detalle :
// promocionesAplicadas.getDetallesConPromociones()) {
// // ✅ VERIFICAR si este artículo está incluido en la promoción agrupada
// if (promocionEntity.aplicaParaArticulo(detalle.getIdArticulo())) {
// productosEnPromocion.add(detalle.getIdArticulo());
// subtotalProductosEnPromocion += detalle.getSubtotalOriginal();
// System.out.println("✅ Producto INCLUIDO en promoción: " +
// detalle.getDenominacionArticulo() +
// " (subtotal: $" + detalle.getSubtotalOriginal() + ")");
// } else {
// System.out.println("❌ Producto NO incluido en promoción: " +
// detalle.getDenominacionArticulo());
// }
// }

// System.out.println("📊 Total productos en promoción: " +
// productosEnPromocion.size());
// System.out.println("💰 Subtotal SOLO productos en promoción: $" +
// subtotalProductosEnPromocion);

// if (productosEnPromocion.isEmpty()) {
// System.out.println("⚠️ Ningún producto está incluido en la promoción, no se
// aplica descuento");
// return;
// }

// // ✅ PASO 2: Calcular descuento solo sobre productos incluidos
// double descuentoTotalAgrupada = promocionAgrupada.getDescuentoAplicado();
// System.out.println("💰 Descuento total a aplicar: $" +
// descuentoTotalAgrupada);

// double descuentoTotalAplicado = 0.0;

// // ✅ PASO 3: Aplicar descuento SOLO a productos incluidos
// for (DetalleConPromocionDTO detalle :
// promocionesAplicadas.getDetallesConPromociones()) {

// // ✅ VERIFICAR si este producto está incluido en la promoción
// if (!productosEnPromocion.contains(detalle.getIdArticulo())) {
// System.out.println("⏭️ Saltando producto NO incluido: " +
// detalle.getDenominacionArticulo());
// continue; // ← SALTAR productos que no están en la promoción
// }

// // ✅ APLICAR DESCUENTO solo a productos incluidos
// // Calcular proporción de este detalle respecto al subtotal de productos EN
// // promoción
// double proporcion = detalle.getSubtotalOriginal() /
// subtotalProductosEnPromocion;
// double descuentoParaEsteDetalle = descuentoTotalAgrupada * proporcion;

// System.out.println("📦 Aplicando descuento a: " +
// detalle.getDenominacionArticulo());
// System.out.println(" 💰 Subtotal original: $" +
// detalle.getSubtotalOriginal());
// System.out.println(" 📊 Proporción: " + String.format("%.2f", proporcion *
// 100) + "%");
// System.out.println(" 🎁 Descuento asignado: $" + String.format("%.2f",
// descuentoParaEsteDetalle));

// // ✅ ACTUALIZAR CAMPOS DEL DETALLE CON PROMOCIÓN AGRUPADA
// double descuentoAnterior = detalle.getDescuentoAplicado();
// double descuentoTotal = descuentoAnterior + descuentoParaEsteDetalle;

// detalle.setDescuentoAplicado(descuentoTotal);
// detalle.setTienePromocion(true);

// // Calcular nuevo precio unitario final
// double precioUnitarioFinal = detalle.getPrecioUnitarioOriginal() -
// (descuentoTotal / detalle.getCantidad());
// detalle.setPrecioUnitarioFinal(precioUnitarioFinal);

// // Calcular nuevo subtotal final
// double subtotalFinal = detalle.getSubtotalOriginal() - descuentoTotal;
// detalle.setSubtotalFinal(subtotalFinal);

// // ✅ ACTUALIZAR INFORMACIÓN DE PROMOCIÓN APLICADA
// if (detalle.getPromocionAplicada() == null) {
// // Si no tenía promoción individual, crear info de promoción agrupada
// detalle.setPromocionAplicada(new DetalleConPromocionDTO.PromocionInfoDTO());
// detalle.getPromocionAplicada().setIdPromocion(promocionEntity.getIdPromocion());
// detalle.getPromocionAplicada().setDenominacion(promocionEntity.getDenominacion());
// detalle.getPromocionAplicada().setDescripcion("Promoción agrupada aplicada");
// detalle.getPromocionAplicada().setTipoDescuento(promocionEntity.getTipoDescuento().toString());
// detalle.getPromocionAplicada().setValorDescuento(promocionEntity.getValorDescuento());
// detalle.getPromocionAplicada().setResumenDescuento(
// String.format("%s - Ahorro: $%.2f", promocionEntity.getDenominacion(),
// descuentoTotal));
// } else {
// // Si ya tenía promoción individual, actualizar el resumen
// String resumenAnterior =
// detalle.getPromocionAplicada().getResumenDescuento();
// detalle.getPromocionAplicada().setResumenDescuento(
// resumenAnterior + " + " + promocionEntity.getDenominacion() + ": $"
// + String.format("%.2f", descuentoParaEsteDetalle));
// }

// descuentoTotalAplicado += descuentoParaEsteDetalle;

// System.out.println(
// " ✅ Actualizado - Precio final: $" + String.format("%.2f",
// precioUnitarioFinal) + " c/u");
// System.out.println(" ✅ Actualizado - Subtotal final: $" +
// String.format("%.2f", subtotalFinal));
// }

// // ✅ ACTUALIZAR TOTALES GLOBALES
// double descuentoTotalAnterior = promocionesAplicadas.getDescuentoTotal();
// promocionesAplicadas.setDescuentoTotal(descuentoTotalAnterior +
// descuentoTotalAplicado);
// promocionesAplicadas.setSubtotalFinal(
// promocionesAplicadas.getSubtotalOriginal() -
// promocionesAplicadas.getDescuentoTotal());

// // Actualizar resumen
// String resumenOriginal = promocionesAplicadas.getResumenPromociones();
// String resumenConAgrupada = promocionAgrupada.getDenominacion() +
// " (" + promocionAgrupada.getValorDescuento() + "% OFF)" +
// (resumenOriginal.contains("Sin promociones") ? "" : " + " + resumenOriginal);

// promocionesAplicadas.setResumenPromociones(resumenConAgrupada);

// System.out.println("🎉 Promoción agrupada aplicada a " +
// productosEnPromocion.size() + " productos (de " +
// promocionesAplicadas.getDetallesConPromociones().size() + " totales)");
// System.out.println("🎁 Descuento total aplicado: $" + String.format("%.2f",
// descuentoTotalAplicado));
// }
// }