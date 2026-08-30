package com.tesis.vocacional.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.repository.TestUsuarioRepository;

@Service
public class TestUsuarioService {
	@Autowired
	private TestUsuarioRepository testUsuarioRepository;

	public TestUsuario guardar(TestUsuario testUsuario) {
		return testUsuarioRepository.save(testUsuario);
	}

	// Busca un test EN_CURSO del usuario para poder reanudar el progreso
	public Optional<TestUsuario> buscarEnCurso(Usuario usuario) {
		return testUsuarioRepository.findTopByUsuarioAndEstadoOrderByIdDesc(usuario, "EN_CURSO");
	}
}
