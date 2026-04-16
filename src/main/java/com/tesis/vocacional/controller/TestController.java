package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.TestRealizado;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.PreguntaService;
import com.tesis.vocacional.services.TestRealizadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/realizar-test")
public class TestController {

    private final TestRealizadoService testRealizadoService;

    private final PreguntaService preguntaService;

    public TestController(PreguntaService preguntaService, TestRealizadoService testRealizadoService) {
        this.preguntaService = preguntaService;
        this.testRealizadoService = testRealizadoService;
    }

    @GetMapping
    public String iniciarTest(HttpSession session, Model model) {
        List<Pregunta> preguntas = preguntaService.listarTodas();
        session.setAttribute("preguntas", preguntas);
        session.setAttribute("respuestas", new ArrayList<String>());
        session.setAttribute("indiceActual", 0);

        return mostrarPreguntaActual(session, model);
    }

    @PostMapping("/responder")
    public String responder(@RequestParam String respuesta, HttpSession session, Model model) {
        List<String> respuestas = (List<String>) session.getAttribute("respuestas");
        respuestas.add(respuesta);

        int indice = (int) session.getAttribute("indiceActual");
        session.setAttribute("indiceActual", indice + 1);

        // Si es la última pregunta → calcular resultado
        if (indice + 1 == ((List<?>) session.getAttribute("preguntas")).size()) {
            return "redirect:/test-resultado";
        }

        return mostrarPreguntaActual(session, model);
    }

    private String mostrarPreguntaActual(HttpSession session, Model model) {
        List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
        int indice = (int) session.getAttribute("indiceActual");

        model.addAttribute("pregunta", preguntas.get(indice));
        model.addAttribute("progreso", indice + 1);
        model.addAttribute("total", preguntas.size());
        model.addAttribute("esUltimaPregunta", indice == preguntas.size() - 1);

        return "realizar-test";
    }

    // ==================== RESULTADO FINAL ====================
    @GetMapping("/test-resultado")
    public String mostrarResultado(HttpSession session, Model model) {
        List<Pregunta> preguntas = (List<Pregunta>) session.getAttribute("preguntas");
        List<String> respuestas = (List<String>) session.getAttribute("respuestas");
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado"); // ← Necesitamos esto

        if (preguntas == null || respuestas == null || respuestas.size() != preguntas.size()) {
            return "redirect:/realizar-test";
        }

        // Calcular puntajes
        Map<String, Integer> puntajes = new HashMap<>();
        for (int i = 0; i < preguntas.size(); i++) {
            if ("SI".equalsIgnoreCase(respuestas.get(i))) {
                String cat = preguntas.get(i).getCategoria();
                puntajes.put(cat, puntajes.getOrDefault(cat, 0) + 1);
            }
        }

        // Guardar en base de datos
        TestRealizado resultado = new TestRealizado();
        resultado.setUsuario(usuarioLogueado);
        resultado.setPuntajeC(puntajes.getOrDefault("C", 0));
        resultado.setPuntajeH(puntajes.getOrDefault("H", 0));
        resultado.setPuntajeA(puntajes.getOrDefault("A", 0));
        resultado.setPuntajeS(puntajes.getOrDefault("S", 0));
        resultado.setPuntajeI(puntajes.getOrDefault("I", 0));
        resultado.setPuntajeD(puntajes.getOrDefault("D", 0));
        resultado.setPuntajeE(puntajes.getOrDefault("E", 0));

        testRealizadoService.guardar(resultado);   // ← Guardado aquí

        // Pasar datos a la vista
        model.addAttribute("puntajes", puntajes);
        model.addAttribute("ranking", new ArrayList<>(puntajes.entrySet()));
        model.addAttribute("totalPreguntas", preguntas.size());
        model.addAttribute("resultadoId", resultado.getId());

        // Limpiar sesión
        session.removeAttribute("preguntas");
        session.removeAttribute("respuestas");

        return "test-resultado";
    
    }
}