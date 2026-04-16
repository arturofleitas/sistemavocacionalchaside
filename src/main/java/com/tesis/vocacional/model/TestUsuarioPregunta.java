package com.tesis.vocacional.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class TestUsuarioPregunta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String categoria;

	private String texto;

	@ManyToOne
	private TestUsuario testUsuario;

	@ManyToOne
	private Pregunta pregunta;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public TestUsuario getTestUsuario() {
		return testUsuario;
	}

	public void setTestUsuario(TestUsuario testUsuario) {
		this.testUsuario = testUsuario;
	}

	public Pregunta getPregunta() {
		return pregunta;
	}

	public void setPregunta(Pregunta pregunta) {
		this.pregunta = pregunta;
	}

}
