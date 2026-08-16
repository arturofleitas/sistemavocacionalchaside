package com.tesis.vocacional.model;

import java.time.LocalDate;
import java.util.ArrayList; // Importación necesaria
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Test {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String nombre;
	private String descripcion;
	private Boolean estado;

	private LocalDate fechaCreacion;
	
		//Inicializar con ArrayList vacío para evitar el error de desreferenciación
	@OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Pregunta> preguntas = new ArrayList<>();

		//Inicializar con ArrayList vacío
	@OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TestUsuario> testUsuarios = new ArrayList<>();

	public Test() {
		// Constructor vacío
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public LocalDate getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDate fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public List<Pregunta> getPreguntas() {
		return preguntas;
	}

	//Setter seguro para no romper la referencia de Hibernate
	public void setPreguntas(List<Pregunta> preguntas) {
		if (this.preguntas == null) {
			this.preguntas = preguntas;
		} else {
			this.preguntas.clear();
			if (preguntas != null) {
				this.preguntas.addAll(preguntas);
			}
		}
	}

	public List<TestUsuario> getTestUsuarios() {
		return testUsuarios;
	}

	//Setter seguro para no romper la referencia de Hibernate
	public void setTestUsuarios(List<TestUsuario> testUsuarios) {
		if (this.testUsuarios == null) {
			this.testUsuarios = testUsuarios;
		} else {
			this.testUsuarios.clear();
			if (testUsuarios != null) {
				this.testUsuarios.addAll(testUsuarios);
			}
		}
	}
}
