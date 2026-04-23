package com.tesis.vocacional.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Usuario extends Persona {

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	private Boolean estado;

	private String rol;

	public Usuario() {
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}


}
