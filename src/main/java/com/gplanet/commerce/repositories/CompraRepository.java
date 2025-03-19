package com.gplanet.commerce.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gplanet.commerce.entities.Compra;
import com.gplanet.commerce.entities.Usuario;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuario(Usuario usuario);
    List<Compra> findByFechaBetween(LocalDateTime yesterday, LocalDateTime tomorrow);

    @Query("SELECT c FROM Compra c LEFT JOIN FETCH c.productos WHERE c.id = :id")
    Optional<Compra> findByIdWithProductos(@Param("id") Long id);
    
    @Query("SELECT c FROM Compra c LEFT JOIN FETCH c.productos WHERE c.usuario.id = :usuarioId")
    List<Compra> findAllByUsuarioIdWithProductos(@Param("usuarioId") Long usuarioId);
}
