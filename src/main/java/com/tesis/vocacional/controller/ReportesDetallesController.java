package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.services.RespuestaService;
import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.TestUsuarioPreguntaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/reportes-detalles")
public class ReportesDetallesController {

  
    private final ResultadoService resultadoService;
    private final TestUsuarioPreguntaService testUsuarioPreguntaService;
    private final RespuestaService respuestaService;

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
                                      RespuestaService respuestaService) {
        this.resultadoService = resultadoService;
        this.testUsuarioPreguntaService = testUsuarioPreguntaService;
        this.respuestaService = respuestaService;
    }

    @GetMapping("/test/{id}")
    public String verDetalleTest(@PathVariable Integer id, Model model) {
        Resultado resultado = resultadoService.buscarPorId(id);
        if (resultado == null || resultado.getTest() == null) {
            return "redirect:/reportes";
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

        // No necesitamos puntajes para gráficos, solo los nombres de intereses y aptitudes
        String interesesNombres = construirNombresAreas(resultado.getInteresesPrincipales());
        String aptitudesNombres = construirNombresAreas(resultado.getAptitudesPrincipales());

        // Construir perfil recomendado con nombres completos
        String perfilRecomendado = construirPerfilRecomendadoCompleto(resultado.getInteresesPrincipales(),
                                                                     resultado.getAptitudesPrincipales());

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
        model.addAttribute("interesesNombres", interesesNombres);
        model.addAttribute("aptitudesNombres", aptitudesNombres);
        model.addAttribute("perfilRecomendado", perfilRecomendado);

        return "detalle-test";
    }

    /**
     * Construye el perfil recomendado con los NOMBRES COMPLETOS de las áreas.
     * Ejemplo: "DEFENSA Y SEGURIDAD + ARTÍSTICAS"
     */
    private String construirPerfilRecomendadoCompleto(String interesesStr, String aptitudesStr) {
        String interesPrincipal = "";
        String aptitudPrincipal = "";

        if (interesesStr != null && !interesesStr.isEmpty()) {
            String primeraLetra = interesesStr.split(",")[0].trim();
            interesPrincipal = NOMBRE_CATEGORIA.getOrDefault(primeraLetra, primeraLetra);
        }
        if (aptitudesStr != null && !aptitudesStr.isEmpty()) {
            String primeraLetra = aptitudesStr.split(",")[0].trim();
            aptitudPrincipal = NOMBRE_CATEGORIA.getOrDefault(primeraLetra, primeraLetra);
        }

        if (interesPrincipal.isEmpty() && aptitudPrincipal.isEmpty()) {
            return "Sin perfil definido";
        } else if (interesPrincipal.isEmpty()) {
            return aptitudPrincipal;
        } else if (aptitudPrincipal.isEmpty()) {
            return interesPrincipal;
        } else {
            return interesPrincipal + " + " + aptitudPrincipal;
        }
    }

    /**
     * Construye una cadena con los nombres de las áreas a partir de un string de letras separadas por coma.
     */
    private String construirNombresAreas(String areasStr) {
        if (areasStr == null || areasStr.isEmpty()) {
            return "No definido";
        }
        List<String> nombres = new ArrayList<>();
        for (String letra : areasStr.split(",")) {
            String nombre = NOMBRE_CATEGORIA.get(letra.trim());
            if (nombre != null) nombres.add(nombre);
        }
        return nombres.isEmpty() ? "No definido" : String.join(", ", nombres);
    }
}