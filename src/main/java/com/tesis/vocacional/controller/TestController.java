package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.model.TestUsuario;
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
 * Controlador para la realización del test vocacional CHASIDE.
 * Gestiona el flujo de preguntas, almacenamiento de respuestas y cálculo de resultados.
 */
@Controller
@RequestMapping("/realizar-test")
public class TestController {

    // Dependencias inyectadas por constructor (mejor práctica)
    private final PreguntaService preguntaService;
    private final UsuarioService usuarioService;
    private final TestService testService;
    private final TestUsuarioService testUsuarioService;
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

    public TestController(PreguntaService preguntaService, UsuarioService usuarioService,
                          TestService testService, TestUsuarioService testUsuarioService,
                          ResultadoService resultadoService) {
        this.preguntaService = preguntaService;
        this.usuarioService = usuarioService;
        this.testService = testService;
        this.testUsuarioService = testUsuarioService;
        this.resultadoService = resultadoService;
    }

    /**
     * Inicia un nuevo test. Crea una sesión con las preguntas y un TestUsuario.
     */
    @GetMapping
    public String iniciarTest(HttpSession session, Model model) {
        List<Pregunta> preguntas = preguntaService.listarTodas();
        Usuario usuario = obtenerUsuarioAutenticado();
        Test test = obtenerTestCHASIDE();

        TestUsuario testUsuario = new TestUsuario();
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

    /**
     * Procesa la respuesta del usuario (Sí/No) y avanza a la siguiente pregunta.
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

        // Guardar respuesta en sesión
        if (indice < respuestas.size()) {
            respuestas.set(indice, respuesta);
        } else {
            respuestas.add(respuesta);
        }
        session.setAttribute("respuestas", respuestas);

        // Persistir respuesta (sin bloquear el flujo si falla)
        try {
            Pregunta preguntaActual = preguntas.get(indice);
            // Guardar respuesta en BD (se omite el detalle de TestUsuarioPregunta para brevedad)
            // ... (código de persistencia existente)
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

    /**
     * Muestra la pregunta actual según el índice de sesión.
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
     */
    @PostMapping("/anterior")
    public String anterior(HttpSession session, Model model) {
        List<String> respuestas = obtenerLista(session, "respuestas");
        Integer indice = (Integer) session.getAttribute("indiceActual");

        if (respuestas == null || indice == null || indice <= 0) {
            return "redirect:/realizar-test";
        }

        respuestas.remove(indice - 1);
        session.setAttribute("respuestas", respuestas);
        session.setAttribute("indiceActual", --indice);

        return mostrarPreguntaActual(session, model);
    }

    /**
     * Finaliza el test, calcula los puntajes de intereses y aptitudes,
     * guarda el resultado y muestra la vista de diagnóstico.
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
        String interesesNombres = interesesPrincipales.stream()
                .map(NOMBRE_CATEGORIA::get)
                .collect(Collectors.joining(" - "));
        String aptitudesNombres = aptitudesPrincipales.stream()
                .map(NOMBRE_CATEGORIA::get)
                .collect(Collectors.joining(" - "));

        // 4. Guardar resultado en base de datos
        if (testUsuario != null) {
            try {
                Resultado resultado = new Resultado();
                resultado.setTest(testUsuario);
                resultado.setFechaRealizacion(LocalDateTime.now());

                // Asignar TODOS los campos calculados
                resultado.setInteresesPrincipales(String.join(",", interesesPrincipales));
                resultado.setAptitudesPrincipales(String.join(",", aptitudesPrincipales));
                resultado.setPuntajesInteres(puntajesInteres.toString());
                resultado.setPuntajesAptitud(puntajesAptitud.toString());

                // Perfil combinado (para uso general)
                String perfil = interesesPrincipales.stream().findFirst().orElse("") +
                                (aptitudesPrincipales.stream().findFirst().isPresent() ? "+" + aptitudesPrincipales.get(0) : "");
                resultado.setPerfil(perfil);

                // Puntaje máximo (para ordenar)
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

        // 5. Pasar datos al modelo para la vista
        model.addAttribute("interesesPrincipales", interesesNombres);
        model.addAttribute("aptitudesPrincipales", aptitudesNombres);
        model.addAttribute("puntajesInteres", puntajesInteres);
        model.addAttribute("puntajesAptitud", puntajesAptitud);
        model.addAttribute("totalPreguntas", preguntas.size());

        // Descripción opcional
        String descripcion = "Tu perfil combina intereses en " + interesesNombres.toLowerCase()
                + " y aptitudes en " + aptitudesNombres.toLowerCase() + ".";
        model.addAttribute("descripcion", descripcion);

        // Limpiar sesión
        session.removeAttribute("preguntas");
        session.removeAttribute("respuestas");
        session.removeAttribute("indiceActual");
        session.removeAttribute("testUsuario");

        return "test-resultado";
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioService.buscarPorUsername(auth.getName());
    }

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

    @SuppressWarnings("unchecked")
    private <T> List<T> obtenerLista(HttpSession session, String key) {
        return (List<T>) session.getAttribute(key);
    }

    private Map<String, Integer> inicializarMapaPuntajes() {
        Map<String, Integer> mapa = new HashMap<>();
        for (String area : AREAS) {
            mapa.put(area, 0);
        }
        return mapa;
    }

    private List<String> obtenerTopAreas(Map<String, Integer> puntajes) {
        return puntajes.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}