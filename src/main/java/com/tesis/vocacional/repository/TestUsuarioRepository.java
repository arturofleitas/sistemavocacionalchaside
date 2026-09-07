package com.tesis.vocacional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;

public interface TestUsuarioRepository extends JpaRepository<TestUsuario, Integer>{

	// Busca el test EN_CURSO más reciente del usuario para reanudar el progreso
	Optional<TestUsuario> findTopByUsuarioAndEstadoOrderByIdDesc(Usuario usuario, String estado);

	// Cantidad de tests (sesiones de test) que ha realizado un usuario
	long countByUsuarioId(Integer usuarioId);

}
