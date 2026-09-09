package com.tesis.vocacional.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.model.SesionInvitado;
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

	// Busca el test EN_CURSO de un acceso de invitado para reanudar el progreso
	public Optional<TestUsuario> buscarEnCursoPorSesionInvitado(SesionInvitado sesionInvitado) {
		return testUsuarioRepository.findTopBySesionInvitadoAndEstadoOrderByIdDesc(sesionInvitado, "EN_CURSO");
	}

	// Busca el test más reciente de un acceso de invitado (EN_CURSO o COMPLETADO)
	public Optional<TestUsuario> buscarPorSesionInvitado(SesionInvitado sesionInvitado) {
		return testUsuarioRepository.findTopBySesionInvitadoOrderByIdDesc(sesionInvitado);
	}
}
