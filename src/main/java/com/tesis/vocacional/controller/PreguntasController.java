package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.services.PreguntaService;
import com.tesis.vocacional.services.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/preguntas")
public class PreguntasController {

    private final PreguntaService preguntaService;
    private final TestService testService;               // ← Inyecta el servicio de tests

    public PreguntasController(PreguntaService preguntaService, TestService testService) {
        this.preguntaService = preguntaService;
        this.testService = testService;
    }

    /* Muestra la página de gestión de preguntas
     Carga tanto las preguntas como la lista de tests para el formulario */
    @GetMapping
    public String mostrarPreguntas(Model model) {
        // Cargamos todas las preguntas (puede estar vacía)
        model.addAttribute("preguntas", preguntaService.listarTodas());

        // Cargamos todos los tests para el dropdown "Test al que pertenece"
        model.addAttribute("tests", testService.listarTodos());

        // Objeto vacío para el formulario de nueva pregunta
        model.addAttribute("pregunta", new Pregunta());

        return "preguntas";
    }

    // Muestra el formulario en modo edición
    @GetMapping("/editar/{id}")
    public String editarPregunta(@PathVariable int id, Model model) {
        Pregunta pregunta = preguntaService.buscarPorId(id);
        
        model.addAttribute("pregunta", pregunta != null ? pregunta : new Pregunta());
        model.addAttribute("preguntas", preguntaService.listarTodas());
        model.addAttribute("tests", testService.listarTodos());

        return "preguntas";
    }

    // Guarda o actualiza una pregunta (con su test asociado)
    @PostMapping("/guardar")
    public String guardarPregunta(@ModelAttribute Pregunta pregunta) {
        preguntaService.guardarPregunta(pregunta);
        return "redirect:/preguntas";
    }

    
    // Elimina una pregunta
    @GetMapping("/eliminar/{id}")
    public String eliminarPregunta(@PathVariable int id) {
        preguntaService.eliminarPregunta(id);
        return "redirect:/preguntas";
    }
}