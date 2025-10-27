package com.elbuensabor.repository;

import com.elbuensabor.entities.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IImagenRepository extends JpaRepository<Imagen, Long> {

    // Buscar imágenes por artículo
    List<Imagen> findByArticuloIdArticulo(Long idArticulo);

    // Buscar por URL (para verificar duplicados)
    Optional<Imagen> findByUrl(String url);

    // Verificar si existe una imagen con esa URL
    boolean existsByUrl(String url);

    // Buscar imágenes huérfanas (sin artículo, promoción o cliente)
    @Query("SELECT i FROM Imagen i WHERE i.articulo IS NULL AND i.promocion IS NULL AND i.cliente IS NULL")
    List<Imagen> findImagenesHuerfanas();

    // Buscar imágenes por denominación
    List<Imagen> findByDenominacionContainingIgnoreCase(String denominacion);

    // Eliminar todas las imágenes de un artículo
    void deleteByArticuloIdArticulo(Long idArticulo);

    // Buscar imágenes por promoción
    List<Imagen> findByPromocionIdPromocion(Long idPromocion);

    // Contar imágenes por artículo
    @Query("SELECT COUNT(i) FROM Imagen i WHERE i.articulo.idArticulo = :idArticulo")
    Integer countByArticuloId(@Param("idArticulo") Long idArticulo);
}