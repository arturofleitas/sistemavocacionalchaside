package com.tesis.vocacional.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TestRealizado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	private Usuario usuario;

	private LocalDateTime fechaRealizacion = LocalDateTime.now();

	// Puntajes finales por categoría
	private int puntajeC;
	private int puntajeH;
	private int puntajeA;
	private int puntajeS;
	private int puntajeI;
	private int puntajeD;
	private int puntajeE;

	public TestRealizado() {
	}

	// Getters y Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getFechaRealizacion() {
		return fechaRealizacion;
	}

	public void setFechaRealizacion(LocalDateTime fechaRealizacion) {
		this.fechaRealizacion = fechaRealizacion;
	}

	public int getPuntajeC() {
		return puntajeC;
	}

	public void setPuntajeC(int puntajeC) {
		this.puntajeC = puntajeC;
	}

	public int getPuntajeH() {
		return puntajeH;
	}

	public void setPuntajeH(int puntajeH) {
		this.puntajeH = puntajeH;
	}

	public int getPuntajeA() {
		return puntajeA;
	}

	public void setPuntajeA(int puntajeA) {
		this.puntajeA = puntajeA;
	}

	public int getPuntajeS() {
		return puntajeS;
	}

	public void setPuntajeS(int puntajeS) {
		this.puntajeS = puntajeS;
	}

	public int getPuntajeI() {
		return puntajeI;
	}

	public void setPuntajeI(int puntajeI) {
		this.puntajeI = puntajeI;
	}

	public int getPuntajeD() {
		return puntajeD;
	}

	public void setPuntajeD(int puntajeD) {
		this.puntajeD = puntajeD;
	}

	public int getPuntajeE() {
		return puntajeE;
	}

	public void setPuntajeE(int puntajeE) {
		this.puntajeE = puntajeE;
	}
}