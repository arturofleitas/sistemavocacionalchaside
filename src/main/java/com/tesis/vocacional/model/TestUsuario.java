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
	private SesionInvitado sesionInvitado;

	@ManyToOne
	private Test test;
	
	// NOTA (fix): se quitó "orphanRemoval = true" de esta relación. El
	// Resultado se crea y persiste directamente vía ResultadoService (sin
	// pasar por esta lista), y este mismo TestUsuario se guarda de nuevo al
	// finalizar el test (ver CalculoTestService.calcularYGuardar). Con
	// orphanRemoval activo, al hacer merge de un TestUsuario "nuevo" (cuya
	// colección en memoria sigue vacía) Hibernate interpreta el Resultado
	// recién insertado como huérfano y lo borra en la misma transacción,
	// perdiendo el resultado justo al finalizar. "cascade = ALL" se
	// mantiene: sigue siendo necesario para el borrado en cascada usado por
	// UsuarioService.eliminarUsuarioConDatos().
	@OneToMany(mappedBy = "test",
	           cascade = CascadeType.ALL)
	private List<Resultado> resultados = new ArrayList<>();

	// NOTA (fix): mismo problema que en "resultados". Cada TestUsuarioPregunta
	// se crea y persiste directamente vía TestUsuarioPreguntaService a medida
	// que se responde cada pregunta, sin agregarse a esta lista en memoria.
	// Con orphanRemoval activo, el merge final de TestUsuario (por ejemplo al
	// completar el test) borraba TODAS las respuestas ya guardadas. Se quita
	// orphanRemoval y se conserva "cascade = ALL" por la misma razón que arriba.
	@OneToMany(mappedBy = "testUsuario",
	           cascade = CascadeType.ALL)
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

	public SesionInvitado getSesionInvitado() {
		return sesionInvitado;
	}

	public void setSesionInvitado(SesionInvitado sesionInvitado) {
		this.sesionInvitado = sesionInvitado;
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
