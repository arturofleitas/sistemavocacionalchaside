package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.services.AccesoReporteService;
import com.tesis.vocacional.services.RespuestaService;
import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.TestUsuarioPreguntaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/reportes-detalles")
public class ReportesDetallesController {

  
    private final ResultadoService resultadoService;
    private final TestUsuarioPreguntaService testUsuarioPreguntaService;
    private final RespuestaService respuestaService;
    private final AccesoReporteService accesoReporteService;

    // Mapa de nombres completos de las áreas (orden fijo para la vista)
    private static final Map<String, String> NOMBRE_CATEGORIA = new LinkedHashMap<>();
    static {
        NOMBRE_CATEGORIA.put("C", "ADMINISTRATIVAS Y CONTABLES");
        NOMBRE_CATEGORIA.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
        NOMBRE_CATEGORIA.put("A", "ARTÍSTICAS");
        NOMBRE_CATEGORIA.put("S", "CIENCIAS DE LA SALUD");
        NOMBRE_CATEGORIA.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
        NOMBRE_CATEGORIA.put("D", "DEFENSA Y SEGURIDAD");
        NOMBRE_CATEGORIA.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");
    }

    public ReportesDetallesController(ResultadoService resultadoService,
                                      TestUsuarioPreguntaService testUsuarioPreguntaService,
                                      RespuestaService respuestaService,
                                      AccesoReporteService accesoReporteService) {
        this.resultadoService = resultadoService;
        this.testUsuarioPreguntaService = testUsuarioPreguntaService;
        this.respuestaService = respuestaService;
        this.accesoReporteService = accesoReporteService;
    }

    @GetMapping("/test/{id}")
    public String verDetalleTest(@PathVariable Integer id, HttpServletRequest request, Model model,
                                 RedirectAttributes redirectAttributes) {
        Resultado resultado = resultadoService.buscarPorId(id);
        if (resultado == null || resultado.getTest() == null) {
            return sinPermiso(redirectAttributes);
        }

        // Control de acceso centralizado: se verifica la propiedad de la evaluación
        // ANTES de cargar cualquier dato en el modelo. Si no hay permiso, se redirige
        // a un lugar seguro con un mensaje, sin exponer nombres, respuestas, puntajes
        // ni datos del propietario.
        if (!accesoReporteService.puedeAcceder(request, resultado)) {
            return sinPermiso(redirectAttributes);
        }

        Integer testUsuarioId = resultado.getTest().getId();
        List<TestUsuarioPregunta> preguntas = testUsuarioPreguntaService.obtenerPorTestUsuarioId(testUsuarioId);
        List<Respuesta> respuestas = respuestaService.obtenerPorTestUsuarioId(testUsuarioId);

        Map<Integer, String> mapaRespuestas = new HashMap<>();
        for (Respuesta r : respuestas) {
            if (r.getPregunta() != null && r.getPregunta().getPregunta() != null) {
                String valorStr = (r.getValor() != null) ? (r.getValor() ? "Sí" : "No") : "Sin responder";
                mapaRespuestas.put(r.getPregunta().getPregunta().getId(), valorStr);
            }
        }

        // Letras de las áreas predominantes para mostrar en la vista (todas las
        // que empatan en el máximo: no hay un orden real entre ellas)
        List<String> interesesPredominantes = areaLetras(resultado.getInteresesPrincipales());
        List<String> aptitudesPredominantes = areaLetras(resultado.getAptitudesPrincipales());

        // Puntaje por área, reconstruido desde el snapshot guardado en Resultado.
        // Se usa para mostrar el acierto de cada área predominante y la barra de
        // progreso, con el mismo diseño que la vista de resultado del alumno.
        Map<String, Integer> puntajesInteres = parsearPuntajes(resultado.getPuntajesInteres());
        Map<String, Integer> puntajesAptitud = parsearPuntajes(resultado.getPuntajesAptitud());

        // El máximo de cada dimensión es el puntaje de cualquiera de sus áreas
        // predominantes (por definición, todas empatan en ese valor).
        int maxInteres = interesesPredominantes.isEmpty() ? 0
                : puntajesInteres.getOrDefault(interesesPredominantes.get(0), 0);
        int maxAptitud = aptitudesPredominantes.isEmpty() ? 0
                : puntajesAptitud.getOrDefault(aptitudesPredominantes.get(0), 0);

        // Nombre del alumno con protección ante datos incompletos
        String alumnoNombre = "-";
        if (resultado.getTest().getUsuario() != null) {
            alumnoNombre = resultado.getTest().getUsuario().getNombre() + " "
                    + resultado.getTest().getUsuario().getApellido();
        }

        model.addAttribute("resultado", resultado);
        model.addAttribute("alumnoNombre", alumnoNombre);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("mapaRespuestas", mapaRespuestas);
        model.addAttribute("interesesPredominantes", interesesPredominantes);
        model.addAttribute("aptitudesPredominantes", aptitudesPredominantes);
        model.addAttribute("puntajesInteres", puntajesInteres);
        model.addAttribute("puntajesAptitud", puntajesAptitud);
        model.addAttribute("maxInteres", maxInteres);
        model.addAttribute("maxAptitud", maxAptitud);
        model.addAttribute("categoriaMap", NOMBRE_CATEGORIA);

        return "detalle-test";
    }

    /**
     * Convierte una cadena de letras de áreas separadas por coma en una lista.
     * Si la cadena es nula o vacía devuelve una lista vacía.
     */
    private List<String> areaLetras(String areasStr) {
        List<String> letras = new ArrayList<>();
        if (areasStr == null || areasStr.isBlank()) {
            return letras;
        }
        for (String letra : areasStr.split(",")) {
            String t = letra.trim();
            if (!t.isEmpty()) {
                letras.add(t);
            }
        }
        return letras;
    }

    /**
     * Redirige a un destino seguro cuando el usuario no tiene permiso para ver la
     * evaluación. Los usuarios autenticados van a su listado de reportes; los
     * invitados (sin autenticación) vuelven a la página pública del test. En ningún
     * caso se cargan datos del propietario.
     */
    private String sinPermiso(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "No tenés permisos para ver esta evaluación.");
        return estaAutenticado() ? "redirect:/reportes" : "redirect:/test/publico";
    }

    private boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * Reconstruye el mapa área -> puntaje a partir del snapshot de texto guardado
     * en Resultado (formato Map.toString(), ej. "{A=5, C=6, ...}"). Las áreas
     * ausentes o con un valor no numérico quedan en 0.
     */
    private Map<String, Integer> parsearPuntajes(String puntajesStr) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String area : NOMBRE_CATEGORIA.keySet()) {
            mapa.put(area, 0);
        }
        if (puntajesStr == null || puntajesStr.isBlank()) {
            return mapa;
        }
        String contenido = puntajesStr.trim();
        if (contenido.startsWith("{") && contenido.endsWith("}")) {
            contenido = contenido.substring(1, contenido.length() - 1);
        }
        for (String par : contenido.split(",")) {
            String[] kv = par.trim().split("=");
            if (kv.length == 2 && mapa.containsKey(kv[0].trim())) {
                try {
                    mapa.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                } catch (NumberFormatException ignored) {
                    // Valor no numérico: se conserva el 0 por defecto.
                }
            }
        }
        return mapa;
    }
}