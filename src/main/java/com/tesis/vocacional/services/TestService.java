package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.repository.TestRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

	private final TestRepository testRepository;

	public TestService(TestRepository testRepository) {
		this.testRepository = testRepository;
	}

	public Test buscarPorNombre(String nombre) {
		return testRepository.findByNombre(nombre).orElse(null);
	}

	public List<Test> listarTodos() {
		return testRepository.findAll();
	}

	public List<Test> listarActivos() {
		return testRepository.findByEstadoTrue();
	}

	public Test guardar(Test test) {
		return testRepository.save(test);
	}

	public Test buscarPorId(int id) {
		return testRepository.findById(id).orElse(null);
	}

	@Transactional
	public void eliminar(int id) {
		Test test = testRepository.findById(id).orElse(null);
		if (test != null) {
			testRepository.delete(test); // Al borrar el objeto completo, JPA ejecuta las cascadas hacia abajo
		}
	}
	
	public long countActivos() {
	    return testRepository.countByEstadoTrue();
	}
}