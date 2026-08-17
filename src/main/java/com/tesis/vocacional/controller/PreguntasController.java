package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.services.PreguntaService;
import com.tesis.vocacional.services.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/preguntas")
public class PreguntasController {

    private static final Logger log = LoggerFactory.getLogger(PreguntasController.class);
    private final PreguntaService preguntaService;
    private final TestService testService;

    public PreguntasController(PreguntaService preguntaService, TestService testService) {
        this.preguntaService = preguntaService;
        this.testService = testService;
    }

    /**
     * Muestra la vista de gestión de preguntas.
     * Carga la lista de preguntas (ordenadas) y los tests para el formulario.
     */
    @GetMapping
    public String mostrarPreguntas(Model model) {
        model.addAttribute("preguntas", preguntaService.listarTodas());
        model.addAttribute("tests", testService.listarTodos());
        model.addAttribute("pregunta", new Pregunta()); // objeto vacío para nuevo registro
        return "preguntas";
    }

    /**
     * Carga el formulario de edición con los datos de la pregunta seleccionada.
     */
    @GetMapping("/editar/{id}")
    public String editarPregunta(@PathVariable int id, Model model) {
        Pregunta pregunta = preguntaService.buscarPorId(id);
        if (pregunta == null) {
            log.warn("Intento de editar pregunta inexistente con ID: {}", id);
            return "redirect:/preguntas?error=Pregunta no encontrada";
        }

        model.addAttribute("pregunta", pregunta);
        model.addAttribute("preguntas", preguntaService.listarTodas());
        model.addAttribute("tests", testService.listarTodos());
        return "preguntas";
    }

    /**
     * Guarda (crea o actualiza) una pregunta.
     * Captura excepciones de validación y las muestra en la vista.
     */
    @PostMapping("/guardar")
    public String guardarPregunta(@ModelAttribute Pregunta pregunta, Model model) {
        try {
            preguntaService.guardarPregunta(pregunta);
            log.info("Pregunta guardada correctamente: {}", pregunta.getNumero());
            return "redirect:/preguntas";
        } catch (RuntimeException e) {
            log.error("Error al guardar pregunta: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pregunta", pregunta);
            model.addAttribute("tests", testService.listarTodos());
            return "preguntas";
        }
    }

    /**
     * Elimina una pregunta por su ID.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarPregunta(@PathVariable int id) {
        preguntaService.eliminarPregunta(id);
        return "redirect:/preguntas";
    }
}