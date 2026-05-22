package com.tesis.vocacional.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.repository.TestUsuarioRepository;

@Service
public class TestUsuarioService {
	@Autowired
	private TestUsuarioRepository testUsuarioRepository;

	public TestUsuario guardar(TestUsuario testUsuario) {
		return testUsuarioRepository.save(testUsuario);
	}
}
