package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.services.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gestion-test")
public class GestionTestController {

    private final TestService testService;

    public GestionTestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public String mostrarGestionTest(Model model) {
        model.addAttribute("tests", testService.listarTodos());
        model.addAttribute("test", new Test());
        return "gestion-test";
    }

    @GetMapping("/editar/{id}")
    public String editarTest(@PathVariable int id, Model model) {
        Test test = testService.buscarPorId(id);
        if (test != null) {
            model.addAttribute("test", test);
        }
        model.addAttribute("tests", testService.listarTodos());
        return "gestion-test";
    }

    @PostMapping("/guardar")
    public String guardarTest(@ModelAttribute Test test) {
        testService.guardar(test);
        return "redirect:/gestion-test";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTest(@PathVariable int id) {
        testService.eliminar(id);
        return "redirect:/gestion-test";
    }
}