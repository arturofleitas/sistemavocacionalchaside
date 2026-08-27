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
 * Controller encargado de la pantalla "Reportes y Estadísticas".
 *
 * IMPORTANTE (unificación de vistas ADMIN / ESTUDIANTE):
 * La vista reportes.html ahora usa UNA SOLA tabla para ambos roles.
 * La diferencia entre roles ya no está en la estructura HTML (antes admin
 * usaba tabla y estudiante usaba tarjetas), sino simplemente en:
 *   1) Qué datos se cargan (todos los resultados vs. solo los del usuario).
 *   2) Si se muestra o no la columna "Alumno" (th:if="${rol == 'ADMIN'}").
 * Por eso este controller no necesitó cambios de lógica, solo se documenta
 * mejor y se limpia el código de depuración (System.out.println) dejándolo
 * como comentarios opcionales, ya que en producción no deberían usarse prints.
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

    @GetMapping
    public String mostrarReportes(Model model) {
        // Obtenemos el usuario autenticado desde el contexto de seguridad de Spring Security.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuarioActual = usuarioService.buscarPorUsername(username);

        // Si por alguna razón el usuario autenticado no existe en la BD,
        // lo redirigimos al login en lugar de romper la vista.
        if (usuarioActual == null) {
            return "redirect:/login";
        }

        String rol = usuarioActual.getRol();

        if ("ADMIN".equals(rol)) {
            // El administrador ve TODOS los resultados, con datos de usuario y test ya cargados.
            List<Resultado> todosLosResultados = resultadoService.obtenerTodosConUsuarioYTest();
            model.addAttribute("resultados", todosLosResultados);
            model.addAttribute("rol", "ADMIN");
        } else {
            // El estudiante ve únicamente sus propios resultados.
            List<Resultado> resultados = resultadoService.obtenerPorUsuarioId(usuarioActual.getId());
            model.addAttribute("resultados", resultados);
            model.addAttribute("estudiante", usuarioActual);
            model.addAttribute("rol", "ESTUDIANTE");
        }

        return "reportes";
    }
}