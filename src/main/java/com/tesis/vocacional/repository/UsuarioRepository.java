package com.tesis.vocacional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tesis.vocacional.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
	
    boolean existsByUsername(String username);
    long countByEstadoTrue();
    
    Optional<Usuario> findByUsername(String username);
}
