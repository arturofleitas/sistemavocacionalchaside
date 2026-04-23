package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public List<Test> listarTodos() {
        return testRepository.findAll();
    }

    public Test guardar(Test test) {
        return testRepository.save(test);
    }

    public Test buscarPorId(int id) {
        return testRepository.findById(id).orElse(null);
    }

    public void eliminar(int id) {
        testRepository.deleteById(id);
    }
}