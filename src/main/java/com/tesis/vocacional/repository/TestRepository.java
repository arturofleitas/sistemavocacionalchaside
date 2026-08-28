package com.tesis.vocacional.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tesis.vocacional.model.Test;

public interface TestRepository extends JpaRepository<Test, Integer>{
	Optional<Test> findByNombre(String nombre);
	 long countByEstadoTrue();
	List<Test> findByEstadoTrue();
}
