package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

	private final ResultadoService resultadoService;
	private final UsuarioService usuarioService;

	public ReportesController(ResultadoService resultadoService, UsuarioService usuarioService) {
		this.resultadoService = resultadoService;
		this.usuarioService = usuarioService;
	}

	@GetMapping
	public String mostrarReportes(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
		System.out.println("=== REPORTES ===");
		System.out.println("Usuario autenticado: " + username);
		Usuario usuarioActual = usuarioService.buscarPorUsername(username);
		
		if (usuarioActual == null) {
			System.out.println("ERROR: Usuario no encontrado en BD");
			return "redirect:/login";
		}
		
		String rol = usuarioActual.getRol();
		System.out.println("Rol: " + rol);

		if ("ADMIN".equals(rol)) {
			System.out.println("Cargando vista ADMIN");
			// Llamada corregida según tu estructura actual del service
			List<Resultado> todosLosResultados = resultadoService.obtenerTodosConUsuarioYTest();
			model.addAttribute("resultados", todosLosResultados);
			model.addAttribute("rol", "ADMIN");
		} else {
			System.out.println("Cargando vista ESTUDIANTE");
			List<Resultado> resultados = resultadoService.obtenerPorUsuarioId(usuarioActual.getId());
			System.out.println("Resultados encontrados: " + resultados.size());
			model.addAttribute("resultados", resultados);
			model.addAttribute("estudiante", usuarioActual);
			model.addAttribute("rol", "ESTUDIANTE");
		}
		return "reportes";
	}
}
