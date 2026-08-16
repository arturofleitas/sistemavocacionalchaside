package com.tesis.vocacional.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class TestUsuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private LocalDate fecha;

	private String estado;

	@ManyToOne
	private Usuario usuario;

	@ManyToOne
	private Test test;
	
	@OneToMany(mappedBy = "test",
	           cascade = CascadeType.ALL,
	           orphanRemoval = true)
	private List<Resultado> resultados = new ArrayList<>();
	
	@OneToMany(mappedBy = "testUsuario",
	           cascade = CascadeType.ALL,
	           orphanRemoval = true)
	private List<TestUsuarioPregunta> testUsuarioPreguntas = new ArrayList<>();


	public TestUsuario() {
		this.fecha = LocalDate.now();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public List<Resultado> getResultados() {
		return resultados;
	}

	public void setResultados(List<Resultado> resultados) {
		this.resultados = resultados;
	}

	public List<TestUsuarioPregunta> getTestUsuarioPreguntas() {
		return testUsuarioPreguntas;
	}

	public void setTestUsuarioPreguntas(List<TestUsuarioPregunta> testUsuarioPreguntas) {
		this.testUsuarioPreguntas = testUsuarioPreguntas;
	}
	
	
}
