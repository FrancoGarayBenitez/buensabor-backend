package com.elbuensabor.services;

import com.elbuensabor.dto.request.PromocionAplicacionDTO;
import com.elbuensabor.dto.request.PromocionRequestDTO;
import com.elbuensabor.dto.response.PromocionCalculoDTO;
import com.elbuensabor.dto.response.PromocionCompletaDTO;
import com.elbuensabor.dto.response.PromocionResponseDTO;
import com.elbuensabor.entities.Promocion;

import java.util.List;

public interface IPromocionService extends IGenericService<Promocion, Long, PromocionResponseDTO> {

    // ✅ MÉTODOS PARA CLIENTES
    List<PromocionResponseDTO> findPromocionesVigentes();
    List<PromocionResponseDTO> findPromocionesParaArticulo(Long idArticulo);
    List<PromocionResponseDTO> findPromocionesAplicables(Long idArticulo, Long idSucursal);

    // ✅ MÉTODOS PARA ADMINISTRACIÓN
    PromocionResponseDTO crearPromocion(PromocionRequestDTO request);
    PromocionResponseDTO actualizarPromocion(Long id, PromocionRequestDTO request);
    void activarPromocion(Long id);
    void desactivarPromocion(Long id);

    // ✅ MÉTODO CLAVE: CALCULAR DESCUENTOS PARA UN PEDIDO
    PromocionCalculoDTO calcularDescuentosParaPedido(Long idSucursal, List<PromocionAplicacionDTO> aplicaciones);

    List<PromocionCompletaDTO> findPromocionesVigentesCompletas();
}
