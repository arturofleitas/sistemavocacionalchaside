package com.tesis.vocacional.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Resultado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String perfil; // letra: C, H, A, S, I, D, E
	private int puntaje;

	@ManyToOne
	private TestUsuario test;

	private LocalDateTime fechaRealizacion;

	// Campo transitorio (no se guarda en BD) para fecha formateada
	@Transient
	private String fechaFormateada;

	// Mapa estático para convertir letra → nombre completo
	private static final Map<String, String> nombreCategoria = new HashMap<>();
	static {
		nombreCategoria.put("C", "ADMINISTRATIVAS Y CONTABLES");
		nombreCategoria.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
		nombreCategoria.put("A", "ARTÍSTICAS");
		nombreCategoria.put("S", "CIENCIAS DE LA SALUD");
		nombreCategoria.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
		nombreCategoria.put("D", "DEFENSA Y SEGURIDAD");
		nombreCategoria.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");
	}

	// Getter para nombre completo de la categoría (para usar directamente en la
	// vista)
	public String getNombrePerfil() {
		return nombreCategoria.getOrDefault(perfil, "Perfil no reconocido");
	}

	// Getter para fecha formateada (se invoca automáticamente al cargar el objeto)
	public String getFechaFormateada() {
		if (fechaRealizacion != null) {
			return fechaRealizacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		}
		return "";
	}

	// Getters y Setters estándar
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public LocalDateTime getFechaRealizacion() {
		return fechaRealizacion;
	}

	public void setFechaRealizacion(LocalDateTime fechaRealizacion) {
		this.fechaRealizacion = fechaRealizacion;
	}
}