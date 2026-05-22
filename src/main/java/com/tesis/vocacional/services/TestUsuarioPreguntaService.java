package com.tesis.vocacional.services;

import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.repository.TestUsuarioPreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestUsuarioPreguntaService {

    private final TestUsuarioPreguntaRepository repository;

    public TestUsuarioPreguntaService(TestUsuarioPreguntaRepository repository) {
        this.repository = repository;
    }

    public TestUsuarioPregunta guardar(TestUsuarioPregunta tup) {
        return repository.save(tup);
    }

    public List<TestUsuarioPregunta> obtenerPorTestUsuarioId(Integer testUsuarioId) {
        return repository.findByTestUsuarioId(testUsuarioId);
    }

    // Este es el método que faltaba
    public Optional<TestUsuarioPregunta> buscarPorTestUsuarioYPregunta(TestUsuario testUsuario, Pregunta pregunta) {
        return repository.buscarPorTestUsuarioYPregunta(testUsuario, pregunta);
    }
}