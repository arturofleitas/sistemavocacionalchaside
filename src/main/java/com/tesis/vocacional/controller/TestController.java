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

		// Calcular puntajes
		Map<String, Integer> puntajes = new HashMap<>();
		for (int i = 0; i < preguntas.size(); i++) {
			if ("SI".equalsIgnoreCase(respuestas.get(i))) {
				String letraCategoria = preguntas.get(i).getCategoria();
				puntajes.put(letraCategoria, puntajes.getOrDefault(letraCategoria, 0) + 1);
			}
		}

		String letraGanadora = null;
		int maxPuntaje = -1;
		for (Map.Entry<String, Integer> entry : puntajes.entrySet()) {
			if (entry.getValue() > maxPuntaje) {
				maxPuntaje = entry.getValue();
				letraGanadora = entry.getKey();
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

		String nombreCompleto = "No se pudo determinar un perfil";
		if (letraGanadora != null) {
			nombreCompleto = nombreCategoria.getOrDefault(letraGanadora, "Perfil no reconocido");
		}

		// Guardar resultado en la base de datos
		if (testUsuario != null && letraGanadora != null) {
			try {
				Resultado resultado = new Resultado();
				resultado.setPerfil(letraGanadora); // o nombreCompleto según prefieras
				resultado.setPuntaje(maxPuntaje);
				resultado.setTest(testUsuario);
				resultado.setFechaRealizacion(LocalDateTime.now());
				resultadoService.guardar(resultado);

				// Marcar TestUsuario como completado (no se modifica la fecha)
				testUsuario.setEstado("COMPLETADO");
				testUsuarioService.guardar(testUsuario);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("errorGuardado", e.getMessage());
			}
		}

		model.addAttribute("categoriaGanadora", nombreCompleto);
		model.addAttribute("letraGanadora", letraGanadora);
		model.addAttribute("maxPuntaje", maxPuntaje);
		model.addAttribute("totalPreguntas", preguntas.size());
		model.addAttribute("descripcion", "Realiza nuevamente el test para obtener un diagnóstico más preciso.");
		model.addAttribute("fortalezas", "Consulta con un orientador vocacional.");
		model.addAttribute("carreras", "Explora diferentes áreas según tus intereses.");

		// Limpiar sesión
		session.removeAttribute("preguntas");
		session.removeAttribute("respuestas");
		session.removeAttribute("indiceActual");
		session.removeAttribute("testUsuario");

		return "test-resultado";
	}
}