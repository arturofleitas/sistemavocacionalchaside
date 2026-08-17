package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.repository.PreguntaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreguntaService {

    private static final Logger log = LoggerFactory.getLogger(PreguntaService.class);
    private final PreguntaRepository preguntaRepository;

    public PreguntaService(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Guarda una pregunta validando que el número no sea nulo y no esté duplicado.
     * En edición, permite mantener el mismo número (no se considera duplicado).
     */
    public Pregunta guardarPregunta(Pregunta pregunta) {
        // Validar número no nulo
        if (pregunta.getNumero() == null) {
            throw new RuntimeException("El número de pregunta es obligatorio.");
        }

        // Validar unicidad (excepto si es la misma pregunta en edición)
        Pregunta existente = preguntaRepository.findByNumero(pregunta.getNumero());
        if (existente != null && existente.getId() != pregunta.getId()) {
            throw new RuntimeException("Ya existe una pregunta con el número " + pregunta.getNumero());
        }

        log.info("Guardando pregunta: número={}, texto={}", pregunta.getNumero(), pregunta.getTexto());
        return preguntaRepository.save(pregunta);
    }

    public List<Pregunta> listarTodas() {
        return preguntaRepository.findAllByOrderByNumeroAsc();
    }

    public Pregunta buscarPorId(Integer id) {
        return preguntaRepository.findById(id).orElse(null);
    }

    public void eliminarPregunta(Integer id) {
        preguntaRepository.deleteById(id);
    }
}