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

/**
 * Controlador de reportes y estadísticas del sistema de test vocacional.
 * Diferencias según el rol: - ADMIN: Ve todos los tests realizados (todos los
 * usuarios) + estadísticas globales. - ESTUDIANTE: Ve solo sus propios tests.
 * Los filtros (por nombre de alumno y rango de fechas) se realizan en el
 * cliente (JavaScript) sobre la tabla ya cargada, lo que simplifica el backend
 * y evita problemas de consultas dinámicas.
 */
@Controller
@RequestMapping("/reportes")
public class ReportesController {

	private final ResultadoService resultadoService;
	private final UsuarioService usuarioService;

	public ReportesController(ResultadoService resultadoService, UsuarioService usuarioService) {
		this.resultadoService = resultadoService;
		this.usuarioService = usuarioService;
	}

	/**
	 * Muestra la página de reportes según el rol del usuario autenticado.
	 * @param model Modelo de Spring para pasar atributos a la vista.
	 * @return Nombre de la plantilla Thymeleaf (reportes.html)
	 */
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
			// ... carga admin
			System.out.println("Cargando vista ADMIN");
		} else {
			// ... carga estudiante
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