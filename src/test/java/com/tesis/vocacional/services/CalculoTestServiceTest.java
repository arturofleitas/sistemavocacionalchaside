package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Pregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de regresión del cálculo del test CHASIDE.
 *
 * Los valores esperados se construyen desde la matriz CHASIDE (70 ítems de
 * intereses: 10 por área C-H-A-S-I-D-E; 28 ítems de aptitudes: 4 por área),
 * no desde la función que se está probando.
 */
class CalculoTestServiceTest {

    private CalculoTestService servicio;

    // Matriz CHASIDE: numero de pregunta -> area (por dimension)
    private static final Map<String, List<Integer>> INTERESES = new HashMap<>();
    private static final Map<String, List<Integer>> APTITUDES = new HashMap<>();

    static {
        INTERESES.put("C", List.of(98, 12, 64, 53, 85, 1, 78, 20, 71, 91));
        INTERESES.put("H", List.of(9, 34, 80, 25, 95, 67, 41, 74, 56, 89));
        INTERESES.put("A", List.of(21, 45, 96, 57, 28, 11, 50, 3, 81, 36));
        INTERESES.put("S", List.of(33, 92, 70, 8, 87, 62, 23, 44, 16, 52));
        INTERESES.put("I", List.of(75, 6, 19, 38, 60, 27, 83, 54, 47, 97));
        INTERESES.put("D", List.of(84, 31, 48, 73, 5, 65, 14, 37, 58, 24));
        INTERESES.put("E", List.of(77, 42, 88, 17, 93, 32, 68, 49, 35, 61));

        APTITUDES.put("C", List.of(15, 51, 2, 46));
        APTITUDES.put("H", List.of(63, 30, 72, 86));
        APTITUDES.put("A", List.of(22, 39, 76, 82));
        APTITUDES.put("S", List.of(69, 40, 29, 4));
        APTITUDES.put("I", List.of(26, 59, 90, 10));
        APTITUDES.put("D", List.of(13, 66, 18, 43));
        APTITUDES.put("E", List.of(94, 7, 79, 55));
    }

    private static final List<String> AREAS = List.of("C", "H", "A", "S", "I", "D", "E");

    @BeforeEach
    void setUp() {
        servicio = new CalculoTestService(null, null);
    }

    // ---------- Helpers ----------

    private List<Pregunta> construirPreguntas() {
        List<Pregunta> preguntas = new ArrayList<>();
        for (int n = 1; n <= 98; n++) {
            String area = areaDe(n);
            String dim = dimensionDe(n);
            Pregunta p = new Pregunta();
            p.setNumero(n);
            p.setCategoria(area);
            p.setDimension(dim);
            preguntas.add(p);
        }
        return preguntas;
    }

    private String areaDe(int n) {
        for (String a : AREAS) {
            if (INTERESES.get(a).contains(n) || APTITUDES.get(a).contains(n)) {
                return a;
            }
        }
        throw new IllegalStateException("Ítem sin área: " + n);
    }

    private String dimensionDe(int n) {
        for (String a : AREAS) {
            if (INTERESES.get(a).contains(n)) {
                return "INTERES";
            }
            if (APTITUDES.get(a).contains(n)) {
                return "APTITUD";
            }
        }
        throw new IllegalStateException("Ítem sin dimensión: " + n);
    }

    private List<String> respuestasConSolo(int... numerosSi) {
        List<String> respuestas = new ArrayList<>();
        for (int n = 1; n <= 98; n++) {
            boolean si = false;
            for (int s : numerosSi) {
                if (s == n) {
                    si = true;
                    break;
                }
            }
            respuestas.add(si ? "SI" : "NO");
        }
        return respuestas;
    }

    private List<String> respuestasConSi(Map<String, Integer> conteoInteres, Map<String, Integer> conteoAptitud) {
        List<String> respuestas = new ArrayList<>();
        Map<String, Integer> usadosInteres = new HashMap<>();
        Map<String, Integer> usadosAptitud = new HashMap<>();
        for (int n = 1; n <= 98; n++) {
            String area = areaDe(n);
            String dim = dimensionDe(n);
            boolean si = false;
            if ("INTERES".equals(dim) && conteoInteres != null) {
                int objetivo = conteoInteres.getOrDefault(area, 0);
                int usados = usadosInteres.getOrDefault(area, 0);
                if (usados < objetivo) {
                    si = true;
                    usadosInteres.put(area, usados + 1);
                }
            } else if ("APTITUD".equals(dim) && conteoAptitud != null) {
                int objetivo = conteoAptitud.getOrDefault(area, 0);
                int usados = usadosAptitud.getOrDefault(area, 0);
                if (usados < objetivo) {
                    si = true;
                    usadosAptitud.put(area, usados + 1);
                }
            }
            respuestas.add(si ? "SI" : "NO");
        }
        return respuestas;
    }

    // ---------- Escenarios ----------

    @Test
    void todasNoDaCeroEnTodasLasAreas() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(), respuestasConSolo());

        for (String a : AREAS) {
            assertEquals(0, c.getPuntajesInteres().get(a));
            assertEquals(0, c.getPuntajesAptitud().get(a));
        }
        assertTrue(c.isInteresCero());
        assertTrue(c.isAptitudCero());
        assertTrue(c.getInteresesPredominantes().isEmpty());
        assertTrue(c.getAptitudesPredominantes().isEmpty());
        assertFalse(c.isPuntajesFueraDeRango());
    }

    @Test
    void todasSiDaMaximosYConservaTodosLosEmpates() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSolo(java.util.stream.IntStream.rangeClosed(1, 98).toArray()));

        for (String a : AREAS) {
            assertEquals(10, c.getPuntajesInteres().get(a));
            assertEquals(4, c.getPuntajesAptitud().get(a));
        }
        // Todas las áreas empatadas: deben conservarse las 7, no recortarse a 2-3.
        assertEquals(AREAS, c.getInteresesPredominantes());
        assertEquals(AREAS, c.getAptitudesPredominantes());
        assertEquals(10, c.getMaxInteres());
        assertEquals(4, c.getMaxAptitud());
    }

    @Test
    void maximoUnicoEnIntereses() {
        // Solo área C con intereses respondidos Sí
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of("C", 10), Map.of()));

        assertEquals(List.of("C"), c.getInteresesPredominantes());
        assertEquals(10, c.getMaxInteres());
        assertEquals(0, c.getMaxAptitud());
        assertTrue(c.isAptitudCero());
        assertFalse(c.isInteresCero());
    }

    @Test
    void empateDeDosAreasEnIntereses() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of("C", 10, "H", 10), Map.of()));

        assertEquals(List.of("C", "H"), c.getInteresesPredominantes());
    }

    @Test
    void empateDeTresAreasEnIntereses() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of("C", 10, "H", 10, "A", 10), Map.of()));

        assertEquals(List.of("C", "H", "A"), c.getInteresesPredominantes());
    }

    @Test
    void ejemploInteresesMuestraSoloHSyECon9() {
        // C=7, H=9, A=8, S=9, I=6, D=8, E=9
        Map<String, Integer> conteo = new HashMap<>();
        conteo.put("C", 7); conteo.put("H", 9); conteo.put("A", 8); conteo.put("S", 9);
        conteo.put("I", 6); conteo.put("D", 8); conteo.put("E", 9);
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(conteo, Map.of()));

        assertEquals(7, c.getPuntajesInteres().get("C"));
        assertEquals(9, c.getPuntajesInteres().get("H"));
        assertEquals(8, c.getPuntajesInteres().get("A"));
        assertEquals(9, c.getPuntajesInteres().get("S"));
        assertEquals(6, c.getPuntajesInteres().get("I"));
        assertEquals(8, c.getPuntajesInteres().get("D"));
        assertEquals(9, c.getPuntajesInteres().get("E"));

        // Solo H, S y E con 9
        assertEquals(List.of("H", "S", "E"), c.getInteresesPredominantes());
    }

    @Test
    void ejemploAptitudesMuestraSoloACon4() {
        // C=2, H=1, A=4, S=3, I=0, D=2, E=1
        Map<String, Integer> conteo = new HashMap<>();
        conteo.put("C", 2); conteo.put("H", 1); conteo.put("A", 4); conteo.put("S", 3);
        conteo.put("I", 0); conteo.put("D", 2); conteo.put("E", 1);
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of(), conteo));

        assertEquals(2, c.getPuntajesAptitud().get("C"));
        assertEquals(1, c.getPuntajesAptitud().get("H"));
        assertEquals(4, c.getPuntajesAptitud().get("A"));
        assertEquals(3, c.getPuntajesAptitud().get("S"));
        assertEquals(0, c.getPuntajesAptitud().get("I"));
        assertEquals(2, c.getPuntajesAptitud().get("D"));
        assertEquals(1, c.getPuntajesAptitud().get("E"));

        assertEquals(List.of("A"), c.getAptitudesPredominantes());
    }

    @Test
    void interesesCeroYAptitudesPositivas() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of(), Map.of("A", 4)));

        assertTrue(c.isInteresCero());
        assertFalse(c.isAptitudCero());
        assertTrue(c.getInteresesPredominantes().isEmpty());
        assertEquals(List.of("A"), c.getAptitudesPredominantes());
    }

    @Test
    void aptitudesCeroEInteresesPositivas() {
        ResultadoCalculo c = servicio.calcular(construirPreguntas(),
                respuestasConSi(Map.of("D", 10), Map.of()));

        assertFalse(c.isInteresCero());
        assertTrue(c.isAptitudCero());
        assertEquals(List.of("D"), c.getInteresesPredominantes());
        assertTrue(c.getAptitudesPredominantes().isEmpty());
    }

    @Test
    void modificarUnaRespuestaActualizaSinAcumular() {
        List<Pregunta> preguntas = construirPreguntas();
        // Ítem 1 es C/INTERES. Primero SI, luego NO: el puntaje debe reflejar el cambio.
        ResultadoCalculo conSi = servicio.calcular(preguntas, respuestasConSolo(1));
        assertEquals(1, conSi.getPuntajesInteres().get("C"));

        ResultadoCalculo sinSi = servicio.calcular(preguntas, respuestasConSolo());
        assertEquals(0, sinSi.getPuntajesInteres().get("C"));
    }

    @Test
    void mismaSecuenciaComoInvitadoYRegistradoDaMismosPuntajes() {
        // El cálculo es el mismo sin importar el propietario (se reutiliza el servicio).
        Map<String, Integer> conteo = new HashMap<>();
        conteo.put("H", 9); conteo.put("E", 9);
        List<Pregunta> preguntas = construirPreguntas();
        List<String> respuestas = respuestasConSi(conteo, Map.of("A", 4));

        ResultadoCalculo c1 = servicio.calcular(preguntas, respuestas);
        ResultadoCalculo c2 = servicio.calcular(preguntas, respuestas);

        assertEquals(c1.getPuntajesInteres(), c2.getPuntajesInteres());
        assertEquals(c1.getPuntajesAptitud(), c2.getPuntajesAptitud());
        assertEquals(c1.getInteresesPredominantes(), c2.getInteresesPredominantes());
        assertEquals(c1.getAptitudesPredominantes(), c2.getAptitudesPredominantes());
    }

    @Test
    void cadaItemIndividualIncrementaSoloSuAreaYDimension() {
        List<Pregunta> preguntas = construirPreguntas();
        for (int n = 1; n <= 98; n++) {
            String area = areaDe(n);
            String dim = dimensionDe(n);
            ResultadoCalculo c = servicio.calcular(preguntas, respuestasConSolo(n));

            Map<String, Integer> mapa = "INTERES".equals(dim)
                    ? c.getPuntajesInteres() : c.getPuntajesAptitud();
            assertEquals(1, mapa.get(area), "Ítem " + n + " debe sumar 1 en " + dim + " " + area);
            // Las demás áreas deben quedar en 0 en esa dimensión.
            for (String a : AREAS) {
                if (!a.equals(area)) {
                    assertEquals(0, mapa.get(a), "Ítem " + n + " no debe sumar en " + a);
                }
            }
            // La otra dimensión queda en 0.
            Map<String, Integer> otra = "INTERES".equals(dim)
                    ? c.getPuntajesAptitud() : c.getPuntajesInteres();
            for (String a : AREAS) {
                assertEquals(0, otra.get(a));
            }
        }
    }

    @Test
    void puntajeFueraDeRangoSeInformaComoInconsistencia() {
        // Construir una pregunta con dimension correcta pero repetida para forzar >10.
        // Simulamos un ítem duplicado en la dimensión INTERES del área H.
        List<Pregunta> preguntas = construirPreguntas();
        Pregunta extra = new Pregunta();
        extra.setNumero(99);
        extra.setCategoria("H");
        extra.setDimension("INTERES");
        preguntas.add(extra);

        List<String> respuestas = new ArrayList<>();
        for (int n = 1; n <= 99; n++) {
            respuestas.add("SI");
        }

        ResultadoCalculo c = servicio.calcular(preguntas, respuestas);
        assertTrue(c.isPuntajesFueraDeRango());
        assertTrue(c.getInconsistencias() > 0);
        assertEquals(11, c.getPuntajesInteres().get("H"));
    }
}
