package com.tesis.vocacional.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.services.PreguntaService;

@Controller
@RequestMapping("/preguntas")
public class PreguntasController {
	private final PreguntaService preguntaService;

    public PreguntasController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping
    public String mostrarPreguntas(Model model) {
        model.addAttribute("pregunta", new Pregunta());
        model.addAttribute("preguntas", preguntaService.listarTodas());
        return "preguntas";
    }

    @PostMapping("/guardar")
    public String guardarPregunta(@ModelAttribute Pregunta pregunta) {
        preguntaService.guardarPregunta(pregunta);
        return "redirect:/preguntas";
    }

    @GetMapping("/editar/{id}")
    public String editarPregunta(@PathVariable int id, Model model) {
        Pregunta pregunta = preguntaService.buscarPorId(id);
        if (pregunta != null) {
            model.addAttribute("pregunta", pregunta);
            model.addAttribute("preguntas", preguntaService.listarTodas());
            return "preguntas";
        }
        return "redirect:/preguntas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPregunta(@PathVariable int id) {
        preguntaService.eliminarPregunta(id);
        return "redirect:/preguntas";
    }
}
