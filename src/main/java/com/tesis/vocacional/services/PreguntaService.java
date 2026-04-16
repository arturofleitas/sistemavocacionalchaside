package com.tesis.vocacional.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.repository.PreguntaRepository;

@Service
public class PreguntaService {
	@Autowired
    private PreguntaRepository preguntaRepository;

    public Pregunta guardarPregunta(Pregunta pregunta) {
        return preguntaRepository.save(pregunta);
    }

    public List<Pregunta> listarTodas() {
        return preguntaRepository.findAll();
    }

    public Pregunta buscarPorId(int id) {
        return preguntaRepository.findById(id).orElse(null);
    }

    public void eliminarPregunta(int id) {
        preguntaRepository.deleteById(id);
    }
}
