package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Integer> {

    @Query("SELECT r FROM Resultado r JOIN FETCH r.test tu JOIN FETCH tu.usuario u ORDER BY r.fechaRealizacion DESC")
    List<Resultado> findAllWithUsuarioAndTest();

    @Query("SELECT r FROM Resultado r JOIN FETCH r.test tu JOIN FETCH tu.usuario u WHERE u.id = :usuarioId ORDER BY r.fechaRealizacion DESC")
    List<Resultado> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(DISTINCT r.test.usuario.id) FROM Resultado r")
    long countUsuariosConTests();   // ← ESTE FALTABA
}