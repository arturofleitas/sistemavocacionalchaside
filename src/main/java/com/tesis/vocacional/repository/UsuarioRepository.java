package com.tesis.vocacional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tesis.vocacional.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
	
	// Verifica si ya existe un username o email
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByCedula(int cedula);
    long countByEstadoTrue();
    
    Optional<Usuario> findByUsername(String username);
}
