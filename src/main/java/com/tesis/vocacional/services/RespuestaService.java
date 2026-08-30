package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.repository.RespuestaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;

    public RespuestaService(RespuestaRepository respuestaRepository) {
        this.respuestaRepository = respuestaRepository;
    }

    public Respuesta guardar(Respuesta respuesta) {
        return respuestaRepository.save(respuesta);
    }

    public List<Respuesta> obtenerPorTestUsuarioId(Integer testUsuarioId) {
        return respuestaRepository.findByTestUsuarioId(testUsuarioId);
    }

    // Elimina las respuestas previas de una pregunta (evita acumular duplicados)
    @Transactional
    public void eliminarPorTestUsuarioPregunta(TestUsuarioPregunta tup) {
        respuestaRepository.eliminarPorTestUsuarioPregunta(tup);
    }
}