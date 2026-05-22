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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes-detalles")
public class ReportesDetallesController {

    private final ResultadoService resultadoService;
    private final TestUsuarioPreguntaService testUsuarioPreguntaService;
    private final RespuestaService respuestaService;

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
        if (resultado == null) return "redirect:/reportes";

        Integer testUsuarioId = resultado.getTest().getId();
        List<TestUsuarioPregunta> preguntas = testUsuarioPreguntaService.obtenerPorTestUsuarioId(testUsuarioId);
        List<Respuesta> respuestas = respuestaService.obtenerPorTestUsuarioId(testUsuarioId);

        Map<Integer, String> mapaRespuestas = new HashMap<>();
        for (Respuesta r : respuestas) {
            if (r.getPregunta() != null && r.getPregunta().getPregunta() != null) {
                mapaRespuestas.put(r.getPregunta().getPregunta().getId(), r.getValor());
            }
        }

        model.addAttribute("resultado", resultado);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("mapaRespuestas", mapaRespuestas);
        return "detalle-test";
    }
}