package com.tesis.vocacional.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Respuesta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String valor;

	private LocalDate fechaRespuesta;

	@ManyToOne
	private TestUsuarioPregunta pregunta;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public LocalDate getFechaRespuesta() {
		return fechaRespuesta;
	}

	public void setFechaRespuesta(LocalDate fechaRespuesta) {
		this.fechaRespuesta = fechaRespuesta;
	}

	public TestUsuarioPregunta getPregunta() {
		return pregunta;
	}

	public void setPregunta(TestUsuarioPregunta pregunta) {
		this.pregunta = pregunta;
	}

}
