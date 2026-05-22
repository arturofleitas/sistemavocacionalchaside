package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TestUsuarioPreguntaRepository extends JpaRepository<TestUsuarioPregunta, Integer> {

    @Query("SELECT tup FROM TestUsuarioPregunta tup WHERE tup.testUsuario = :testUsuario AND tup.pregunta = :pregunta")
    Optional<TestUsuarioPregunta> buscarPorTestUsuarioYPregunta(@Param("testUsuario") TestUsuario testUsuario,
                                                                @Param("pregunta") Pregunta pregunta);

    @Query("SELECT tup FROM TestUsuarioPregunta tup JOIN FETCH tup.pregunta p WHERE tup.testUsuario.id = :testUsuarioId ORDER BY p.id ASC")
    List<TestUsuarioPregunta> findByTestUsuarioId(@Param("testUsuarioId") Integer testUsuarioId);
}