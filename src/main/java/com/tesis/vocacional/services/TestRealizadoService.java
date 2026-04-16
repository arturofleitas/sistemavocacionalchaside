package com.tesis.vocacional.services;

import com.tesis.vocacional.model.TestRealizado;
import com.tesis.vocacional.repository.TestRealizadoRepository;
import org.springframework.stereotype.Service;

@Service
public class TestRealizadoService {

    private final TestRealizadoRepository repository;

    public TestRealizadoService(TestRealizadoRepository repository) {
        this.repository = repository;
    }

    public TestRealizado guardar(TestRealizado testRealizado) {
        return repository.save(testRealizado);
    }
}