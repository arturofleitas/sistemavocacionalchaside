package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {

    // Obtener todas las respuestas de un resultado específico
    // Navegamos: Resultado -> TestUsuario -> TestUsuarioPregunta -> Respuesta
    @Query("SELECT r FROM Respuesta r " +
           "JOIN FETCH r.pregunta tup " +
           "JOIN FETCH tup.pregunta p " +
           "WHERE tup.testUsuario.id = :testUsuarioId " +
           "ORDER BY p.id ASC")
    List<Respuesta> findByTestUsuarioId(@Param("testUsuarioId") Integer testUsuarioId);

    // Elimina las respuestas previas de una pregunta (evita duplicados al re-responder)
    @Modifying
    @Query("DELETE FROM Respuesta r WHERE r.pregunta = :tup")
    void eliminarPorTestUsuarioPregunta(@Param("tup") TestUsuarioPregunta tup);

    // Cantidad de respuestas de un usuario
    @Query("SELECT COUNT(r) FROM Respuesta r WHERE r.pregunta.testUsuario.usuario.id = :usuarioId")
    long countByUsuarioId(@Param("usuarioId") Integer usuarioId);
}