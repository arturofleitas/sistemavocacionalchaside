package com.tesis.vocacional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.model.SesionInvitado;

public interface TestUsuarioRepository extends JpaRepository<TestUsuario, Integer>{

	// Busca el test EN_CURSO más reciente del usuario para reanudar el progreso
	Optional<TestUsuario> findTopByUsuarioAndEstadoOrderByIdDesc(Usuario usuario, String estado);

	// Busca el test EN_CURSO más reciente de un acceso de invitado
	Optional<TestUsuario> findTopBySesionInvitadoAndEstadoOrderByIdDesc(SesionInvitado sesionInvitado, String estado);

	// Busca el test más reciente de un acceso de invitado (independiente del estado)
	Optional<TestUsuario> findTopBySesionInvitadoOrderByIdDesc(SesionInvitado sesionInvitado);

	// Cantidad de tests (sesiones de test) que ha realizado un usuario
	long countByUsuarioId(Integer usuarioId);

}
