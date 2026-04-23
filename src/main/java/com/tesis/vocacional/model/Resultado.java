package com.tesis.vocacional.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Resultado {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String perfil;
	
	private int puntaje;

	@ManyToOne
	private TestUsuario test;
	
	public int getId() {
		return id;
	}

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}

	public int getPuntaje() {
		return puntaje;
	}

	public void setPuntaje(int puntaje) {
		this.puntaje = puntaje;
	}

	public TestUsuario getTest() {
		return test;
	}

	public void setTest(TestUsuario test) {
		this.test = test;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	

}
