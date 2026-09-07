package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Integer> {

    @Query("SELECT r FROM Resultado r JOIN FETCH r.test tu JOIN FETCH tu.usuario u JOIN FETCH tu.test t ORDER BY r.fechaRealizacion DESC")
    List<Resultado> findAllWithUsuarioAndTest();

    @Query("SELECT r FROM Resultado r JOIN FETCH r.test tu JOIN FETCH tu.usuario u JOIN FETCH tu.test t WHERE u.id = :usuarioId ORDER BY r.fechaRealizacion DESC")
    List<Resultado> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(DISTINCT r.test.usuario.id) FROM Resultado r")
    long countUsuariosConTests();  

    // Cantidad de resultados (evaluaciones completadas) de un usuario
    @Query("SELECT COUNT(r) FROM Resultado r WHERE r.test.usuario.id = :usuarioId")
    long countByUsuarioId(@Param("usuarioId") Integer usuarioId);
}