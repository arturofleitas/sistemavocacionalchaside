package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.repository.ResultadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultadoService {

    private final ResultadoRepository resultadoRepository;

    public ResultadoService(ResultadoRepository resultadoRepository) {
        this.resultadoRepository = resultadoRepository;
    }

    // NUEVO: Guardar un resultado (para cuando se finaliza un test)
    public Resultado guardar(Resultado resultado) {
        return resultadoRepository.save(resultado);
    }

    public List<Resultado> obtenerTodosConUsuarioYTest() {
        return resultadoRepository.findAllWithUsuarioAndTest();
    }

    public List<Resultado> obtenerPorUsuarioId(Integer usuarioId) {
        return resultadoRepository.findByUsuarioId(usuarioId);
    }

    public long countTotalTests() {
        return resultadoRepository.count();
    }

    public long countUsuariosConTests() {
        return resultadoRepository.countUsuariosConTests();
    }

    public Resultado buscarPorId(Integer id) {
        return resultadoRepository.findById(id).orElse(null);
    }
}