package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
    // Buscar por número (para validación de unicidad)
    Pregunta findByNumero(Integer numero);

    // Listar ordenadas por número de pregunta
    List<Pregunta> findAllByOrderByNumeroAsc();
}