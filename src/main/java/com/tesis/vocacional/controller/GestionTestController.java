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
	public String guardarTest(@ModelAttribute Test testFormulario) {
				// Si el test tiene ID, significa que estamos editando un registro existente
		if (testFormulario.getId() != 0) {
			Test testExistente = testService.buscarPorId(testFormulario.getId());

			if (testExistente != null) {
				// Actualizamos únicamente los campos planos modificados en la vista
				testExistente.setNombre(testFormulario.getNombre());
				testExistente.setDescripcion(testFormulario.getDescripcion());
				testExistente.setEstado(testFormulario.getEstado());
				testExistente.setFechaCreacion(testFormulario.getFechaCreacion());

				
				testService.guardar(testExistente);
			}
		} else {
				// Si el ID es 0, es un registro totalmente nuevo y se guarda directo
			testService.guardar(testFormulario);
		}

		return "redirect:/gestion-test";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarTest(@PathVariable int id) {
		testService.eliminar(id);
		return "redirect:/gestion-test";
	}
}