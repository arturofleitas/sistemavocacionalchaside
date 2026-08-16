package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.PreguntaService;
import com.tesis.vocacional.services.RespuestaService;
import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.TestService;
import com.tesis.vocacional.services.TestUsuarioPreguntaService;
import com.tesis.vocacional.services.TestUsuarioService;
import com.tesis.vocacional.services.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/realizar-test")
public class TestController {

	private final PreguntaService preguntaService;
	private final UsuarioService usuarioService;
	private final TestService testService;
	private final TestUsuarioService testUsuarioService;
	private final TestUsuarioPreguntaService testUsuarioPreguntaService;
	private final RespuestaService respuestaService;
	private final ResultadoService resultadoService;

	// Constructor con todas las dependencias
	public TestController(PreguntaService preguntaService, UsuarioService usuarioService, TestService testService,
			TestUsuarioService testUsuarioService, TestUsuarioPreguntaService testUsuarioPreguntaService,
			RespuestaService respuestaService, ResultadoService resultadoService) {
		this.preguntaService = preguntaService;
		this.usuarioService = usuarioService;
		this.testService = testService;
		this.testUsuarioService = testUsuarioService;
		this.testUsuarioPreguntaService = testUsuarioPreguntaService;
		this.respuestaService = respuestaService;
		this.resultadoService = resultadoService;
	}

	// Inicia un nuevo test: carga preguntas, crea TestUsuario y comienza
	@GetMapping
	public String iniciarTest(HttpSession session, Model model) {
		List<Pregunta> preguntas = preguntaService.listarTodas();

		// Obtener usuario autenticado
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Usuario usuario = usuarioService.buscarPorUsername(auth.getName());

		// Buscar el test por nombre "CHASIDE"
		Test test = testService.buscarPorNombre("CHASIDE");
		if (test == null) {
			// Si no existe, lo creamos automáticamente
			test = new Test();
			test.setNombre("CHASIDE");
			test.setDescripcion("Test vocacional basado en categorías C-H-A-S-I-D-E");
			test.setEstado(true);
			test.setFechaCreacion(LocalDate.now());
			test = testService.guardar(test);
			System.out.println("Test CHASIDE creado automáticamente con ID: " + test.getId());
		}

		// Crear TestUsuario (sesión del test)
		TestUsuario testUsuario = new TestUsuario(); // El constructor asigna fecha = LocalDate.now()
		testUsuario.setUsuario(usuario);
		testUsuario.setTest(test);
		testUsuario.setEstado("EN_CURSO");
		testUsuario = testUsuarioService.guardar(testUsuario);

		// Guardar en sesión
		session.setAttribute("preguntas", preguntas);
		session.setAttribute("respuestas", new ArrayList<String>());
		session.setAttribute("indiceActual", 0);
		session.setAttribute("testUsuario", testUsuario);

		return mostrarPreguntaActual(session, model);
	}

	// Procesa la respuesta enviada por el usuario ("SI" o "NO")
	@PostMapping("/responder")
	public String responder(@RequestParam String respuesta, HttpSession session, Model model) {
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		Integer indice = (Integer) session.getAttribute("indiceActual");
		TestUsuario testUsuario = (TestUsuario) session.getAttribute("testUsuario");

		if (respuestas == null || preguntas == null || indice == null || testUsuario == null) {
			return "redirect:/realizar-test";
		}

		if (indice >= preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		// Actualizar lista de respuestas en sesión
		if (indice < respuestas.size()) {
			respuestas.set(indice, respuesta);
		} else {
			respuestas.add(respuesta);
		}
		session.setAttribute("respuestas", respuestas);

		// Persistir la respuesta en la base de datos
		try {
			Pregunta preguntaActual = preguntas.get(indice);
			// Buscar o crear TestUsuarioPregunta
			Optional<TestUsuarioPregunta> optTUP = testUsuarioPreguntaService.buscarPorTestUsuarioYPregunta(testUsuario,
					preguntaActual);
			TestUsuarioPregunta tup;
			if (optTUP.isPresent()) {
				tup = optTUP.get();
			} else {
				tup = new TestUsuarioPregunta();
				tup.setTestUsuario(testUsuario);
				tup.setPregunta(preguntaActual);
				tup = testUsuarioPreguntaService.guardar(tup);
			}
			// Crear la respuesta
			Respuesta respuestaEntity = new Respuesta();
			respuestaEntity.setPregunta(tup);
			respuestaEntity.setFechaRespuesta(LocalDate.now());
			respuestaEntity.setValor(respuesta); // Asegúrate que Respuesta tenga el campo 'valor'
			respuestaService.guardar(respuestaEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}

		indice++;
		session.setAttribute("indiceActual", indice);

		if (indice == preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		return mostrarPreguntaActual(session, model);
	}

	private String mostrarPreguntaActual(HttpSession session, Model model) {
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		if (preguntas == null || indice == null || indice < 0 || indice >= preguntas.size()) {
			return "redirect:/realizar-test";
		}

		Pregunta preguntaActual = preguntas.get(indice);
		model.addAttribute("pregunta", preguntaActual);
		model.addAttribute("progreso", indice + 1);
		model.addAttribute("total", preguntas.size());
		model.addAttribute("esUltimaPregunta", indice == preguntas.size() - 1);

		return "realizar-test";
	}

	@PostMapping("/anterior")
	public String anterior(HttpSession session, Model model) {
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		if (respuestas == null || indice == null || indice <= 0) {
			return "redirect:/realizar-test";
		}

		respuestas.remove(indice - 1);
		session.setAttribute("respuestas", respuestas);

		indice--;
		session.setAttribute("indiceActual", indice);

		return mostrarPreguntaActual(session, model);
	}

	@GetMapping("/test-resultado")
	public String mostrarResultado(HttpSession session, Model model) {
		List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
		List<String> respuestas = (List<String>) session.getAttribute("respuestas");
		TestUsuario testUsuario = (TestUsuario) session.getAttribute("testUsuario");

		if (preguntas == null || respuestas == null || respuestas.size() != preguntas.size()) {
			return "redirect:/realizar-test";
		}

		// Mapa de áreas (7)
		List<String> areas = Arrays.asList("C", "H", "A", "S", "I", "D", "E");

		// Inicializar mapas de puntajes para intereses y aptitudes
		Map<String, Integer> puntajesInteres = new HashMap<>();
		Map<String, Integer> puntajesAptitud = new HashMap<>();
		for (String area : areas) {
			puntajesInteres.put(area, 0);
			puntajesAptitud.put(area, 0);
		}

		// Procesar cada respuesta
		for (int i = 0; i < preguntas.size(); i++) {
			if ("SI".equalsIgnoreCase(respuestas.get(i))) {
				Pregunta pregunta = preguntas.get(i);
				String area = pregunta.getCategoria();
				String dimension = pregunta.getDimension();
				if (dimension == null) {
					// Si no tiene dimensión, se puede manejar como error o asignar por defecto
					// Por ahora, asumimos que todas son de interés (para no romper)
					dimension = "INTERES";
				}
				if ("INTERES".equalsIgnoreCase(dimension)) {
					puntajesInteres.put(area, puntajesInteres.get(area) + 1);
				} else if ("APTITUD".equalsIgnoreCase(dimension)) {
					puntajesAptitud.put(area, puntajesAptitud.get(area) + 1);
				}
			}
		}

		// Ordenar áreas por puntaje (descendente) para intereses
		List<Map.Entry<String, Integer>> interesesOrdenados = new ArrayList<>(puntajesInteres.entrySet());
		interesesOrdenados.sort((a, b) -> b.getValue().compareTo(a.getValue()));

		List<Map.Entry<String, Integer>> aptitudesOrdenados = new ArrayList<>(puntajesAptitud.entrySet());
		aptitudesOrdenados.sort((a, b) -> b.getValue().compareTo(a.getValue()));

		// Obtener las dos primeras áreas con puntaje > 0 (o todas si hay empate)
		List<String> interesesPrincipales = new ArrayList<>();
		for (int i = 0; i < Math.min(2, interesesOrdenados.size()); i++) {
			if (interesesOrdenados.get(i).getValue() > 0) {
				interesesPrincipales.add(interesesOrdenados.get(i).getKey());
			}
		}
		// Si no hay intereses > 0, se puede dejar vacío o poner "Ninguno"

		List<String> aptitudesPrincipales = new ArrayList<>();
		for (int i = 0; i < Math.min(2, aptitudesOrdenados.size()); i++) {
			if (aptitudesOrdenados.get(i).getValue() > 0) {
				aptitudesPrincipales.add(aptitudesOrdenados.get(i).getKey());
			}
		}

		// Mapeo de letra a nombre completo
		Map<String, String> nombreCategoria = new HashMap<>();
		nombreCategoria.put("C", "ADMINISTRATIVAS Y CONTABLES");
		nombreCategoria.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
		nombreCategoria.put("A", "ARTÍSTICAS");
		nombreCategoria.put("S", "CIENCIAS DE LA SALUD");
		nombreCategoria.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
		nombreCategoria.put("D", "DEFENSA Y SEGURIDAD");
		nombreCategoria.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");

		// Construir nombres completos para mostrar
		String interesesNombres = interesesPrincipales.stream().map(nombreCategoria::get)
				.collect(Collectors.joining(" - "));
		String aptitudesNombres = aptitudesPrincipales.stream().map(nombreCategoria::get)
				.collect(Collectors.joining(" - "));

		// Guardar resultado en la base de datos
		if (testUsuario != null && !interesesPrincipales.isEmpty() || !aptitudesPrincipales.isEmpty()) {
			try {
				Resultado resultado = new Resultado();
				// Guardar las letras principales (por si se necesita)
				resultado.setPerfil(
						String.join(",", interesesPrincipales) + "|" + String.join(",", aptitudesPrincipales));
				// Guardar los puntajes (opcionalmente como JSON)
				resultado.setPuntajesInteres(puntajesInteres.toString());
				resultado.setPuntajesAptitud(puntajesAptitud.toString());
				// También podemos guardar los nombres completos en campos separados si se
				// agregaron
				resultado.setTest(testUsuario);
				resultado.setFechaRealizacion(LocalDateTime.now());
				resultadoService.guardar(resultado);

				testUsuario.setEstado("COMPLETADO");
				testUsuarioService.guardar(testUsuario);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("errorGuardado", e.getMessage());
			}
		}

		// Pasar datos al modelo
		model.addAttribute("interesesPrincipales", interesesNombres);
		model.addAttribute("aptitudesPrincipales", aptitudesNombres);
		model.addAttribute("puntajesInteres", puntajesInteres);
		model.addAttribute("puntajesAptitud", puntajesAptitud);
		model.addAttribute("totalPreguntas", preguntas.size());

		// Limpiar sesión
		session.removeAttribute("preguntas");
		session.removeAttribute("respuestas");
		session.removeAttribute("indiceActual");
		session.removeAttribute("testUsuario");

		return "test-resultado";
	}
}