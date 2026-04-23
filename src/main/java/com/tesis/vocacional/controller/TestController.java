package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.services.PreguntaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/realizar-test")
public class TestController {

	private final PreguntaService preguntaService;

	public TestController(PreguntaService preguntaService) {
		this.preguntaService = preguntaService;
	}

	// Inicia un nuevo test: carga todas las preguntas desde la base de datos,
	// limpia las respuestas anteriores y comienza desde la primera pregunta
	@GetMapping
	public String iniciarTest(HttpSession session, Model model) {
		// Obtiene todas las preguntas (deben estar en el orden definido)
		List<Pregunta> preguntas = preguntaService.listarTodas();

		// Guarda en sesión: lista de preguntas, lista vacía de respuestas, índice inicial 0
		session.setAttribute("preguntas", preguntas);
		session.setAttribute("respuestas", new ArrayList<String>());
		session.setAttribute("indiceActual", 0);

		// Muestra la primera pregunta
		return mostrarPreguntaActual(session, model);
	}

	// Procesa la respuesta enviada por el usuario ("SI" o "NO"). Almacena la
	// respuesta, incrementa el índice y redirige a la siguiente pregunta o al resultado.
	@PostMapping("/responder")
	public String responder(@RequestParam String respuesta, HttpSession session, Model model) {
		// Recupera datos de sesión con null safety
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		// Validación: si falta información, reinicia el test
		if (respuestas == null || preguntas == null || indice == null) {
			return "redirect:/realizar-test";
		}

		// Protección: si ya completamos todas las preguntas, va directamente al resultado
		if (indice >= preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		// Reemplazar respuesta si ya existe para esta pregunta (cuando se retrocede y se cambia)
		// Si el índice actual ya tiene una respuesta almacenada (porque el usuario retrocedió), la reemplazamos.
		// De lo contrario, añadimos la nueva respuesta al final.
		if (indice < respuestas.size()) {
			// Ya existe una respuesta para esta pregunta por ejemplo, al retroceder y cambiar
			respuestas.set(indice, respuesta);
		} else {
			// Es una pregunta nueva, añadimos la respuesta
			respuestas.add(respuesta);
		}
		session.setAttribute("respuestas", respuestas);

		// Incrementa el índice para la siguiente pregunta
		indice++;
		session.setAttribute("indiceActual", indice);

		// ¿Terminamos?
		if (indice == preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		// Sino, muestro la siguiente pregunta
		return mostrarPreguntaActual(session, model);
	}

	// Método auxiliar que extrae la pregunta actual de la sesión y la pasa al
	// modelo para que Thymeleaf la renderice.
	private String mostrarPreguntaActual(HttpSession session, Model model) {
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		// Validaciones de seguridad
		if (preguntas == null || indice == null || indice < 0 || indice >= preguntas.size()) {
			// Si el índice es inválido, reinicia el test
			return "redirect:/realizar-test";
		}

		// Obten la pregunta correspondiente al índice actual
		Pregunta preguntaActual = preguntas.get(indice);

		// Pasa datos a la vista
		model.addAttribute("pregunta", preguntaActual);
		model.addAttribute("progreso", indice + 1);
		model.addAttribute("total", preguntas.size());
		model.addAttribute("esUltimaPregunta", indice == preguntas.size() - 1);

		return "realizar-test";
	}

	// Permite retroceder a la pregunta anterior. Decrementa el índice actual y elimina la última respuesta almacenada.
	@PostMapping("/anterior")
	public String anterior(HttpSession session, Model model) {
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		if (respuestas == null || indice == null || indice <= 0) {
			// No se puede retroceder desde la primera pregunta o si la sesión es inválida
			return "redirect:/realizar-test";
		}

		// Eliminamos la respuesta de la pregunta actual (la que se había respondido)
		// Al retroceder, la respuesta de la pregunta actual ya no es válida,
		// porque el usuario puede cambiarla. La eliminamos para que al avanzar de nuevo se reemplace correctamente.
		respuestas.remove(indice - 1);
		session.setAttribute("respuestas", respuestas);

		// Decrementamos el índice
		indice--;
		session.setAttribute("indiceActual", indice);

		// Mostramos la pregunta anterior
		return mostrarPreguntaActual(session, model);
	}

	// Calcula el resultado final del test basándose en las respuestas almacenadas.
	// Cuenta cuántas veces se respondió "SI" por cada categoría y elige la
	// categoría con mayor puntaje. Finalmente, limpia la sesión y muestra la página de resultados.// 
	@GetMapping("/test-resultado")
	public String mostrarResultado(HttpSession session, Model model) {
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");

		if (preguntas == null || respuestas == null || respuestas.size() != preguntas.size()) {
			return "redirect:/realizar-test";
		}

		// Contar respuestas "SI" por categoría (las categorías son letras: C, H, A, S, I, D, E)
		Map<String, Integer> puntajes = new HashMap<>();
		for (int i = 0; i < preguntas.size(); i++) {
			if ("SI".equalsIgnoreCase(respuestas.get(i))) {
				String letraCategoria = preguntas.get(i).getCategoria(); // Ej: "C", "H", etc.
				puntajes.put(letraCategoria, puntajes.getOrDefault(letraCategoria, 0) + 1);
			}
		}

		// Determinar la letra de la categoría ganadora
		String letraGanadora = null;
		int maxPuntaje = -1;
		for (Map.Entry<String, Integer> entry : puntajes.entrySet()) {
			if (entry.getValue() > maxPuntaje) {
				maxPuntaje = entry.getValue();
				letraGanadora = entry.getKey();
			}
		}

		// ---- MAPEO DE LETRA A NOMBRE COMPLETO Y DIAGNÓSTICO ----
		Map<String, String> nombreCategoria = new HashMap<>();
		nombreCategoria.put("C", "ADMINISTRATIVAS Y CONTABLES");
		nombreCategoria.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
		nombreCategoria.put("A", "ARTÍSTICAS");
		nombreCategoria.put("S", "CIENCIAS DE LA SALUD");
		nombreCategoria.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
		nombreCategoria.put("D", "DEFENSA Y SEGURIDAD");
		nombreCategoria.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");

		// Obtener los valores para la categoría ganadora (si existe)
		String nombreCompleto = "No se pudo determinar un perfil";
		String textoDescripcion = "Realiza nuevamente el test para obtener un diagnóstico más preciso.";
		String textoFortalezas = "Consulta con un orientador vocacional.";
		String textoCarreras = "Explora diferentes áreas según tus intereses.";

		if (letraGanadora != null) {
			nombreCompleto = nombreCategoria.getOrDefault(letraGanadora, "Perfil no reconocido");
		}

		// Agregar atributos al modelo
		model.addAttribute("categoriaGanadora", nombreCompleto); // Nombre completo
		model.addAttribute("letraGanadora", letraGanadora); // Por si quieres mostrarla
		model.addAttribute("maxPuntaje", maxPuntaje);
		model.addAttribute("totalPreguntas", preguntas.size());
		model.addAttribute("descripcion", textoDescripcion);
		model.addAttribute("fortalezas", textoFortalezas);
		model.addAttribute("carreras", textoCarreras);

		// Limpiar sesión
		session.removeAttribute("preguntas");
		session.removeAttribute("respuestas");
		session.removeAttribute("indiceActual");

		return "test-resultado";
	}
}