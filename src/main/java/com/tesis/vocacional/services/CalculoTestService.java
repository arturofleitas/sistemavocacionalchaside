package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.TestUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de cálculo del test CHASIDE, reutilizable tanto para usuarios
 * registrados como para accesos de invitado.
 *
 * Reglas de puntuación (matriz CHASIDE):
 *  - Cada respuesta "SI" suma exactamente 1 punto en el área y la dimensión
 *    que corresponden a su pregunta. Cada "NO" suma 0.
 *  - No se suman intereses y aptitudes entre sí.
 *  - Rangos: 0–10 para intereses y 0–4 para aptitudes, por área.
 *
 * Áreas predominantes por dimensión:
 *  - Se calcula el puntaje máximo de la dimensión.
 *  - Si el máximo es positivo, se seleccionan TODAS las áreas con ese puntaje.
 *  - Si el máximo es 0, no se presentan áreas para esa dimensión.
 */
@Service
public class CalculoTestService {

    private static final List<String> AREAS = Arrays.asList("C", "H", "A", "S", "I", "D", "E");

    private static final Map<String, String> NOMBRE_CATEGORIA = new HashMap<>();

    static {
        NOMBRE_CATEGORIA.put("C", "ADMINISTRATIVAS Y CONTABLES");
        NOMBRE_CATEGORIA.put("H", "HUMANÍSTICAS, CIENCIAS JURÍDICAS Y SOCIALES");
        NOMBRE_CATEGORIA.put("A", "ARTÍSTICAS");
        NOMBRE_CATEGORIA.put("S", "CIENCIAS DE LA SALUD");
        NOMBRE_CATEGORIA.put("I", "INGENIERÍAS, CARRERAS TÉCNICAS Y COMPUTACIÓN");
        NOMBRE_CATEGORIA.put("D", "DEFENSA Y SEGURIDAD");
        NOMBRE_CATEGORIA.put("E", "CIENCIAS AGRARIAS, DE LA NATURALEZA, ZOOLÓGICAS Y BIOLÓGICAS");
    }

    private final ResultadoService resultadoService;
    private final TestUsuarioService testUsuarioService;

    public CalculoTestService(ResultadoService resultadoService, TestUsuarioService testUsuarioService) {
        this.resultadoService = resultadoService;
        this.testUsuarioService = testUsuarioService;
    }

    /**
     * Calcula los puntajes del test a partir de las preguntas y las respuestas
     * (cada respuesta es "SI" o "NO"), sin persistir nada.
     */
    public ResultadoCalculo calcular(List<Pregunta> preguntas, List<String> respuestas) {
        Map<String, Integer> puntajesInteres = inicializarMapaPuntajes();
        Map<String, Integer> puntajesAptitud = inicializarMapaPuntajes();
        int inconsistencias = 0;

        int n = Math.min(preguntas.size(), respuestas == null ? 0 : respuestas.size());
        for (int i = 0; i < n; i++) {
            if (!"SI".equalsIgnoreCase(respuestas.get(i))) {
                // "NO" suma 0
                continue;
            }
            Pregunta p = preguntas.get(i);
            String area = p.getCategoria();
            String dimension = p.getDimension();

            // Datos inválidos o faltantes: no se puntúa, se contabiliza como
            // inconsistencia (no se interpreta como un resultado legítimo de cero).
            if (area == null || !AREAS.contains(area)
                    || dimension == null
                    || (!"INTERES".equalsIgnoreCase(dimension) && !"APTITUD".equalsIgnoreCase(dimension))) {
                inconsistencias++;
                continue;
            }

            if ("INTERES".equalsIgnoreCase(dimension)) {
                puntajesInteres.merge(area, 1, Integer::sum);
            } else {
                puntajesAptitud.merge(area, 1, Integer::sum);
            }
        }

        int maxInteres = valorMaximo(puntajesInteres);
        int maxAptitud = valorMaximo(puntajesAptitud);

        // Rango permitido: 0–10 para intereses y 0–4 para aptitudes por área.
        // Un puntaje fuera de rango indica un problema (p. ej. ítems mal asignados)
        // y se informa como inconsistencia, sin recortarlo artificialmente.
        boolean fueraDeRango = false;
        for (String area : AREAS) {
            if (puntajesInteres.get(area) != null && puntajesInteres.get(area) > 10) {
                fueraDeRango = true;
            }
            if (puntajesAptitud.get(area) != null && puntajesAptitud.get(area) > 4) {
                fueraDeRango = true;
            }
        }
        if (fueraDeRango) {
            inconsistencias++;
        }

        List<String> interesesPredominantes = areasConMaximo(puntajesInteres, maxInteres);
        List<String> aptitudesPredominantes = areasConMaximo(puntajesAptitud, maxAptitud);

        ResultadoCalculo calculo = new ResultadoCalculo();
        calculo.setPuntajesInteres(puntajesInteres);
        calculo.setPuntajesAptitud(puntajesAptitud);
        calculo.setInteresesPredominantes(interesesPredominantes);
        calculo.setAptitudesPredominantes(aptitudesPredominantes);
        calculo.setMaxInteres(maxInteres);
        calculo.setMaxAptitud(maxAptitud);
        calculo.setInteresCero(maxInteres == 0);
        calculo.setAptitudCero(maxAptitud == 0);
        calculo.setTotalPreguntas(preguntas.size());
        calculo.setInconsistencias(inconsistencias);
        calculo.setPuntajesFueraDeRango(fueraDeRango);
        calculo.setNombresCategoria(NOMBRE_CATEGORIA);
        return calculo;
    }

    /**
     * Calcula los puntajes y, si se proporciona una evaluación (TestUsuario),
     * persiste el resultado y marca la evaluación como COMPLETADO. Si no hay
     * evaluación, solo calcula (caso invitado sin persistencia opcional).
     */
    @Transactional
    public ResultadoCalculo calcularYGuardar(List<Pregunta> preguntas, List<String> respuestas, TestUsuario testUsuario) {
        ResultadoCalculo calculo = calcular(preguntas, respuestas);

        if (testUsuario != null) {
            Resultado resultado = new Resultado();
            resultado.setTest(testUsuario);
            resultado.setFechaRealizacion(LocalDateTime.now());
            resultado.setInteresesPrincipales(String.join(",", calculo.getInteresesPredominantes()));
            resultado.setAptitudesPrincipales(String.join(",", calculo.getAptitudesPredominantes()));
            resultado.setPuntajesInteres(calculo.getPuntajesInteres().toString());
            resultado.setPuntajesAptitud(calculo.getPuntajesAptitud().toString());

            // El campo "perfil" se conserva por compatibilidad de esquema, pero ya
            // no representa un "perfil combinado": se guarda solo la primera área
            // predominante de cada dimensión separada por "+" (o vacío si no hay).
            String perfil = (calculo.getInteresesPredominantes().isEmpty()
                    ? ""
                    : calculo.getInteresesPredominantes().get(0))
                    + (calculo.getAptitudesPredominantes().isEmpty()
                            ? ""
                            : "+" + calculo.getAptitudesPredominantes().get(0));
            resultado.setPerfil(perfil);

            int maxPuntaje = Math.max(calculo.getMaxInteres(), calculo.getMaxAptitud());
            resultado.setPuntaje(maxPuntaje);

            resultadoService.guardar(resultado);
            testUsuario.setEstado("COMPLETADO");
            testUsuarioService.guardar(testUsuario);
        }

        return calculo;
    }

    private Map<String, Integer> inicializarMapaPuntajes() {
        Map<String, Integer> mapa = new HashMap<>();
        for (String area : AREAS) {
            mapa.put(area, 0);
        }
        return mapa;
    }

    private int valorMaximo(Map<String, Integer> puntajes) {
        int max = 0;
        for (int v : puntajes.values()) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /**
     * Devuelve todas las áreas cuyo puntaje es exactamente igual al máximo,
     * en orden estable C-H-A-S-I-D-E. Si el máximo es 0, devuelve lista vacía.
     */
    private List<String> areasConMaximo(Map<String, Integer> puntajes, int maximo) {
        List<String> areas = new ArrayList<>();
        if (maximo <= 0) {
            return areas;
        }
        for (String area : AREAS) {
            if (puntajes.get(area) != null && puntajes.get(area) == maximo) {
                areas.add(area);
            }
        }
        return areas;
    }
}
