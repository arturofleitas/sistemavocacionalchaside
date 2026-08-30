package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.*;
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

/**
 * Controlador para la realización del test vocacional CHASIDE. Gestiona el
 * flujo de preguntas, almacenamiento de respuestas y cálculo de resultados.
 */
@Controller
@RequestMapping("/realizar-test")
public class TestController {

	// Dependencias inyectadas por constructor
	private final PreguntaService preguntaService;
	private final UsuarioService usuarioService;
	private final TestService testService;
	private final TestUsuarioService testUsuarioService;
	private final TestUsuarioPreguntaService testUsuarioPreguntaService;
	private final RespuestaService respuestaService;
	private final ResultadoService resultadoService;

	// Constantes para evitar repetir literales
	private static final List<String> AREAS = Arrays.asList("C", "H", "A", "S", "I", "D", "E");
	private static final Map<String, String> NOMBRE_CATEGORIA = new HashMap<>();

	static {
		NOMBRE_CATEGORIA.put("C", "ADMINISTRATIVAS Y CONTABLES");
		NOMBRE_CATEGORIA.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
		NOMBRE_CATEGORIA.put("A", "ARTÍSTICAS");
		NOMBRE_CATEGORIA.put("S", "CIENCIAS DE LA SALUD");
		NOMBRE_CATEGORIA.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
		NOMBRE_CATEGORIA.put("D", "DEFENSA Y SEGURIDAD");
		NOMBRE_CATEGORIA.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");
	}

	/**
	 * Constructor con inyección de todas las dependencias necesarias.
	 */
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

	/** Inicia un nuevo test o reanuda el test EN_CURSO del usuario. */
	@GetMapping
	public String iniciarTest(HttpSession session, Model model) {
		List<Pregunta> preguntas = preguntaService.listarTodas();
		Usuario usuario = obtenerUsuarioAutenticado();
		Test test = obtenerTestCHASIDE();

		// Reanudar test en curso si existe (persistencia del progreso) o crear uno nuevo
		TestUsuario testUsuario = testUsuarioService.buscarEnCurso(usuario).orElse(null);

		if (testUsuario == null) {
			testUsuario = new TestUsuario();
			testUsuario.setUsuario(usuario);
			testUsuario.setTest(test);
			testUsuario.setEstado("EN_CURSO");
			testUsuario = testUsuarioService.guardar(testUsuario);
		}

		// Reconstruir las respuestas ya guardadas cuando se retoma el test
		List<String> respuestas = reconstruirRespuestas(testUsuario, preguntas);
		int indiceActual = respuestas.size();

		// Si ya se respondieron todas las preguntas, ir directo al resultado
		if (indiceActual >= preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		// Guardar el estado en sesión
		session.setAttribute("preguntas", preguntas);
		session.setAttribute("respuestas", respuestas);
		session.setAttribute("testUsuario", testUsuario);
		session.setAttribute("indiceActual", indiceActual);

		// Aviso visual solo si realmente hay progreso (no en el primer ingreso)
		boolean retomando = indiceActual > 0;
		model.addAttribute("retomando", retomando);

		return mostrarPreguntaActual(session, model);
	}

	/**
	 * Procesa la respuesta del usuario (Sí/No), la persiste y avanza a la siguiente
	 * pregunta.
	 *
	 * @param respuesta Valor de la respuesta ("SI" o "NO").
	 * @param session   Sesión HTTP con el estado actual.
	 * @param model     Modelo para la vista.
	 * @return Redirección o vista de la siguiente pregunta.
	 */
	@PostMapping("/responder")
	public String responder(@RequestParam String respuesta, HttpSession session, Model model) {
		List<String> respuestas = obtenerLista(session, "respuestas");
		List<Pregunta> preguntas = obtenerLista(session, "preguntas");
		Integer indice = (Integer) session.getAttribute("indiceActual");
		TestUsuario testUsuario = (TestUsuario) session.getAttribute("testUsuario");

		if (respuestas == null || preguntas == null || indice == null || testUsuario == null) {
			return "redirect:/realizar-test";
		}
		if (indice >= preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}

		// Validar que la respuesta sea "SI" o "NO" (impide valores manipulados)
		if (!"SI".equalsIgnoreCase(respuesta) && !"NO".equalsIgnoreCase(respuesta)) {
			model.addAttribute("error", "Respuesta inválida. Seleccioná Sí o No para continuar.");
			return mostrarPreguntaActual(session, model);
		}

		// Persistir primero; solo si tiene éxito se actualiza la sesión y se avanza
		try {
			Pregunta preguntaActual = preguntas.get(indice);

			// Buscar o crear TestUsuarioPregunta (relación entre sesión y pregunta)
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

			// Eliminar respuestas previas de esta pregunta para evitar duplicados
			respuestaService.eliminarPorTestUsuarioPregunta(tup);

			// Crear la respuesta (valor booleano)
			Respuesta respuestaEntity = new Respuesta();
			respuestaEntity.setPregunta(tup);
			respuestaEntity.setFechaRespuesta(LocalDate.now());
			// Convertir "SI" a true, cualquier otra cosa a false
			respuestaEntity.setValor("SI".equalsIgnoreCase(respuesta));
			respuestaService.guardar(respuestaEntity);

		} catch (Exception e) {
			e.printStackTrace();
			// Mensaje controlado: no se avanza para no perder la pregunta actual
			model.addAttribute("error", "No se pudo guardar la respuesta. Intentá nuevamente.");
			return mostrarPreguntaActual(session, model);
		}

		// Guardar respuesta en sesión (tras la persistencia exitosa)
		if (indice < respuestas.size()) {
			respuestas.set(indice, respuesta);
		} else {
			respuestas.add(respuesta);
		}
		session.setAttribute("respuestas", respuestas);

		// Avanzar al siguiente índice
		indice++;
		session.setAttribute("indiceActual", indice);

		if (indice == preguntas.size()) {
			return "redirect:/realizar-test/test-resultado";
		}
		return mostrarPreguntaActual(session, model);
	}

	/**
	 * Muestra la pregunta actual según el índice de sesión.
	 * 
	 * @return Nombre de la vista "realizar-test".
	 */
	private String mostrarPreguntaActual(HttpSession session, Model model) {
		List<Pregunta> preguntas = obtenerLista(session, "preguntas");
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

	/**
	 * Permite retroceder a la pregunta anterior.
	 * 
	 * @return Redirección o vista de la pregunta anterior.
	 */
	@PostMapping("/anterior")
	public String anterior(HttpSession session, Model model) {
		List<String> respuestas = obtenerLista(session, "respuestas");
		List<Pregunta> preguntas = obtenerLista(session, "preguntas");
		TestUsuario testUsuario = (TestUsuario) session.getAttribute("testUsuario");
		Integer indice = (Integer) session.getAttribute("indiceActual");

		if (respuestas == null || preguntas == null || testUsuario == null || indice == null || indice <= 0) {
			return "redirect:/realizar-test";
		}

		// Quitar de la sesión la respuesta y volver al índice anterior
		respuestas.remove(indice - 1);
		session.setAttribute("respuestas", respuestas);
		session.setAttribute("indiceActual", --indice);

		// Eliminar la respuesta de la base de datos para que el progreso sea consistente
		Pregunta preguntaAAnular = preguntas.get(indice);
		testUsuarioPreguntaService.buscarPorTestUsuarioYPregunta(testUsuario, preguntaAAnular).ifPresent(tup -> {
			respuestaService.eliminarPorTestUsuarioPregunta(tup);
			testUsuarioPreguntaService.eliminar(tup);
		});

		return mostrarPreguntaActual(session, model);
	}

	/**
	 * Finaliza el test, calcula los puntajes de intereses y aptitudes, guarda el
	 * resultado y muestra la vista de diagnóstico.
	 * 
	 * @return Nombre de la vista "test-resultado" o redirección.
	 */
	@GetMapping("/test-resultado")
	public String mostrarResultado(HttpSession session, Model model) {
		List<Pregunta> preguntas = obtenerLista(session, "preguntas");
		List<String> respuestas = obtenerLista(session, "respuestas");
		TestUsuario testUsuario = (TestUsuario) session.getAttribute("testUsuario");

		if (preguntas == null || respuestas == null || respuestas.size() != preguntas.size()) {
			return "redirect:/realizar-test";
		}

		// 1. Calcular puntajes de intereses y aptitudes
		Map<String, Integer> puntajesInteres = inicializarMapaPuntajes();
		Map<String, Integer> puntajesAptitud = inicializarMapaPuntajes();

		for (int i = 0; i < preguntas.size(); i++) {
			if ("SI".equalsIgnoreCase(respuestas.get(i))) {
				Pregunta p = preguntas.get(i);
				String area = p.getCategoria();
				String dimension = p.getDimension() != null ? p.getDimension() : "INTERES";
				if ("INTERES".equalsIgnoreCase(dimension)) {
					puntajesInteres.merge(area, 1, Integer::sum);
				} else {
					puntajesAptitud.merge(area, 1, Integer::sum);
				}
			}
		}

		// 2. Obtener las áreas principales (top 2 con puntaje > 0)
		List<String> interesesPrincipales = obtenerTopAreas(puntajesInteres);
		List<String> aptitudesPrincipales = obtenerTopAreas(puntajesAptitud);

		// 3. Construir nombres completos para la vista
		String interesesNombres = interesesPrincipales.stream().map(NOMBRE_CATEGORIA::get)
				.collect(Collectors.joining(" - "));
		String aptitudesNombres = aptitudesPrincipales.stream().map(NOMBRE_CATEGORIA::get)
				.collect(Collectors.joining(" - "));

		// 4. Guardar resultado en base de datos
		if (testUsuario != null) {
			try {
				Resultado resultado = new Resultado();
				resultado.setTest(testUsuario);
				resultado.setFechaRealizacion(LocalDateTime.now());

				resultado.setInteresesPrincipales(String.join(",", interesesPrincipales));
				resultado.setAptitudesPrincipales(String.join(",", aptitudesPrincipales));
				resultado.setPuntajesInteres(puntajesInteres.toString());
				resultado.setPuntajesAptitud(puntajesAptitud.toString());

				String perfil = interesesPrincipales.stream().findFirst().orElse("")
						+ (aptitudesPrincipales.stream().findFirst().isPresent() ? "+" + aptitudesPrincipales.get(0)
								: "");
				resultado.setPerfil(perfil);

				int maxPuntaje = puntajesInteres.values().stream().max(Integer::compareTo).orElse(0);
				resultado.setPuntaje(maxPuntaje);

				resultadoService.guardar(resultado);
				testUsuario.setEstado("COMPLETADO");
				testUsuarioService.guardar(testUsuario);

			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("errorGuardado", "Error al guardar el resultado: " + e.getMessage());
			}
		}

		// 5. Calcular valores máximos para escalar las barras
		int maxInteres = puntajesInteres.values().stream().max(Integer::compareTo).orElse(1);
		int maxAptitud = puntajesAptitud.values().stream().max(Integer::compareTo).orElse(1);

		// 6. Perfil combinado: mayor puntaje de interés + mayor puntaje de aptitud (nombre legible)
		String interesNombre = NOMBRE_CATEGORIA.getOrDefault(interesesPrincipales.stream().findFirst().orElse(""), "Sin interés");
		String aptitudNombre = NOMBRE_CATEGORIA.getOrDefault(aptitudesPrincipales.stream().findFirst().orElse(""), "Sin aptitud");
		String perfilCombinado = interesNombre + " + " + aptitudNombre;

		// 7. Pasar datos al modelo
		model.addAttribute("interesesPrincipales", interesesNombres);
		model.addAttribute("aptitudesPrincipales", aptitudesNombres);
		model.addAttribute("puntajesInteres", puntajesInteres); // <--- NUEVO: mapa para iterar en la vista
		model.addAttribute("puntajesAptitud", puntajesAptitud); // <--- NUEVO
		model.addAttribute("maxPuntaje", maxInteres); // <--- NUEVO
		model.addAttribute("maxPuntajeAptitud", maxAptitud); // <--- NUEVO
		model.addAttribute("perfilCombinado", perfilCombinado); // <--- NUEVO
		model.addAttribute("totalPreguntas", preguntas.size());
		model.addAttribute("nombresCategoria", NOMBRE_CATEGORIA);

		String descripcion = "Tu perfil combina intereses en " + interesesNombres.toLowerCase() + " y aptitudes en "
				+ aptitudesNombres.toLowerCase() + ".";
		model.addAttribute("descripcion", descripcion);

		// Limpiar sesión
		session.removeAttribute("preguntas");
		session.removeAttribute("respuestas");
		session.removeAttribute("indiceActual");
		session.removeAttribute("testUsuario");

		return "test-resultado";
	}

	/**
	 * Reconstruye la lista de respuestas desde la base de datos para reanudar el
	 * test. Recorre las preguntas en orden y se detiene en la primera sin respuesta.
	 */
	private List<String> reconstruirRespuestas(TestUsuario testUsuario, List<Pregunta> preguntas) {
		List<String> respuestas = new ArrayList<>();
		if (testUsuario == null || testUsuario.getId() == 0) {
			return respuestas;
		}

		// Mapa preguntaId -> valor de la respuesta guardada
		Map<Integer, Boolean> respuestasPorPregunta = new HashMap<>();
		for (Respuesta r : respuestaService.obtenerPorTestUsuarioId(testUsuario.getId())) {
			if (r.getPregunta() != null && r.getPregunta().getPregunta() != null) {
				respuestasPorPregunta.put(r.getPregunta().getPregunta().getId(), r.getValor());
			}
		}

		// Se recorren en el orden real y se corta en la primera pregunta sin respuesta
		for (Pregunta p : preguntas) {
			Boolean valor = respuestasPorPregunta.get(p.getId());
			if (valor == null) {
				break;
			}
			respuestas.add(Boolean.TRUE.equals(valor) ? "SI" : "NO");
		}
		return respuestas;
	}

	/**
	 * Obtiene el usuario autenticado actualmente.
	 * 
	 * @return Objeto Usuario.
	 */
	private Usuario obtenerUsuarioAutenticado() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return usuarioService.buscarPorUsername(auth.getName());
	}

	/**
	 * Obtiene o crea el test con nombre "CHASIDE".
	 * 
	 * @return Objeto Test.
	 */
	private Test obtenerTestCHASIDE() {
		Test test = testService.buscarPorNombre("CHASIDE");
		if (test == null) {
			test = new Test();
			test.setNombre("CHASIDE");
			test.setDescripcion("Test vocacional basado en categorías C-H-A-S-I-D-E");
			test.setEstado(true);
			test.setFechaCreacion(LocalDate.now());
			test = testService.guardar(test);
		}
		return test;
	}

	/**
	 * Método genérico para obtener una lista de la sesión.
	 * 
	 * @param session Sesión HTTP.
	 * @param key     Clave del atributo.
	 * @param <T>     Tipo de la lista.
	 * @return Lista del tipo especificado.
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> obtenerLista(HttpSession session, String key) {
		return (List<T>) session.getAttribute(key);
	}

	/**
	 * Inicializa un mapa con las 7 áreas vocacionales con valor 0.
	 * 
	 * @return Mapa con claves C, H, A, S, I, D, E y valor 0.
	 */
	private Map<String, Integer> inicializarMapaPuntajes() {
		Map<String, Integer> mapa = new HashMap<>();
		for (String area : AREAS) {
			mapa.put(area, 0);
		}
		return mapa;
	}

	/**
	 * Obtiene las dos áreas con mayor puntaje (excluyendo las que tienen 0).
	 * 
	 * @param puntajes Mapa de áreas y puntajes.
	 * @return Lista con las dos áreas principales.
	 */
	private List<String> obtenerTopAreas(Map<String, Integer> puntajes) {
		return puntajes.entrySet().stream().filter(e -> e.getValue() > 0)
				.sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(2).map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}
}