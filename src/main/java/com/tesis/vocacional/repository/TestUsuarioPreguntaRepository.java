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

    // Fix: se ordenaba por p.id (orden de inserción en la tabla "pregunta"),
    // no por p.numero (el número real de la pregunta del CHASIDE). Si alguna
    // pregunta fue corregida borrando e insertando una fila nueva en vez de
    // solo actualizar el texto, su id queda más alto aunque su numero no
    // cambie, y la tabla de detalle mostraba el texto de otra pregunta en la
    // fila equivocada. Se ordena por numero para que la fila N sea siempre la
    // pregunta N, sin importar el orden real de inserción en la BD.
    @Query("SELECT tup FROM TestUsuarioPregunta tup JOIN FETCH tup.pregunta p WHERE tup.testUsuario.id = :testUsuarioId ORDER BY p.numero ASC")
    List<TestUsuarioPregunta> findByTestUsuarioId(@Param("testUsuarioId") Integer testUsuarioId);
}