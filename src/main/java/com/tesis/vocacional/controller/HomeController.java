package com.tesis.vocacional.controller;

import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.TestService;
import com.tesis.vocacional.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**Controlador para la página de inicio (panel de control).
 * Proporciona los datos estadísticos necesarios para la vista home.*/
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final UsuarioService usuarioService;
    private final TestService testService;
    private final ResultadoService resultadoService;

    /**
     * Constructor con inyección de dependencias.
     */
    public HomeController(UsuarioService usuarioService,
                          TestService testService,
                          ResultadoService resultadoService) {
        this.usuarioService = usuarioService;
        this.testService = testService;
        this.resultadoService = resultadoService;
    }

    /**
     * Muestra la página de inicio del panel de control.
     * Pasa al modelo la URI actual (para resaltar el menú) y las estadísticas del sistema.
     *
     * @param model   Modelo de Spring para pasar datos a la vista.
     * @param request Objeto HttpServletRequest para obtener la URI actual.
     * @return Nombre de la vista "home".
     */
    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        // URI actual para el menú (resaltar página activa)
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);

        // Cargar estadísticas del sistema
        try {
            long totalUsuarios = usuarioService.count();
            long testsActivos = testService.countActivos();
            long testsRealizados = resultadoService.count();

            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("testsActivos", testsActivos);
            model.addAttribute("testsRealizados", testsRealizados);

            log.info("Estadísticas cargadas: usuarios={}, tests activos={}, tests realizados={}",
                    totalUsuarios, testsActivos, testsRealizados);
        } catch (Exception e) {
            // Si falla la consulta, asignar valores por defecto
            log.error("Error al cargar estadísticas: {}", e.getMessage());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("testsActivos", 0);
            model.addAttribute("testsRealizados", 0);
            model.addAttribute("errorEstadisticas", "No se pudieron cargar los datos");
        }

        return "home";
    }
}