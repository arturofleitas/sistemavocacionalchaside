package com.tesis.vocacional.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Catálogo de carreras sugeridas por área CHASIDE (fuente: Carreras_chaside.doc).
 *
 * Es un único punto de verdad para las tres vistas de resultado (estudiante,
 * invitado y detalle del test). Se usa desde Thymeleaf así:
 *   ${@carrerasChaside.nombre('I')}
 *   ${@carrerasChaside.carreras('I')}
 */
@Component("carrerasChaside")
public class CarrerasChaside {

    private static final Map<String, String> NOMBRES = new LinkedHashMap<>();
    private static final Map<String, List<String>> CARRERAS = new LinkedHashMap<>();

    static {
        NOMBRES.put("C", "Administrativas y Contables");
        NOMBRES.put("H", "Humanísticas, Ciencias Jurídicas y Sociales");
        NOMBRES.put("A", "Artísticas");
        NOMBRES.put("S", "Ciencias de la Salud");
        NOMBRES.put("I", "Ingenierías, Carreras Técnicas y Computación");
        NOMBRES.put("D", "Defensa y Seguridad");
        NOMBRES.put("E", "Ciencias Agrarias, de la Naturaleza, Zoológicas y Biológicas");

        CARRERAS.put("C", Arrays.asList(
                "Administración de Empresas",
                "Contabilidad / Auditoría",
                "Comercio Internacional / Exterior",
                "Marketing y Comercialización",
                "Recursos Humanos / Relaciones Laborales",
                "Economía",
                "Hotelería y Turismo",
                "Logística y Transporte"));
        CARRERAS.put("H", Arrays.asList(
                "Derecho / Abogacía",
                "Psicología",
                "Sociología",
                "Trabajo Social",
                "Ciencias Políticas / Relaciones Internacionales",
                "Filosofía",
                "Historia",
                "Antropología / Arqueología"));
        CARRERAS.put("A", Arrays.asList(
                "Diseño Gráfico / Diseño Industrial",
                "Arquitectura",
                "Artes Visuales / Plásticas",
                "Música y Composición",
                "Actuación / Teatro / Danza",
                "Cine, TV y Producción Audiovisual",
                "Diseño de Indumentaria y Moda",
                "Fotografía"));
        CARRERAS.put("S", Arrays.asList(
                "Medicina General",
                "Enfermería",
                "Odontología",
                "Nutrición",
                "Kinesiología / Fisioterapia",
                "Psicopedagogía",
                "Farmacia y Bioquímica",
                "Veterinaria"));
        CARRERAS.put("I", Arrays.asList(
                "Ingeniería (Civil, Mecánica, Industrial, Química, etc.)",
                "Licenciatura en Sistemas / Informática / Programación",
                "Ciencias de la Computación",
                "Matemática / Estadística",
                "Física",
                "Ciencia de Datos / Inteligencia Artificial",
                "Telecomunicaciones"));
        CARRERAS.put("D", Arrays.asList(
                "Licenciatura en Seguridad Ciudadana / Pública",
                "Carrera Militar (Ejército, Armada, Fuerza Aérea)",
                "Carrera Policial / Investigación Criminal",
                "Prefectura y Gendarmería",
                "Bomberos / Gestión de Riesgos y Desastres",
                "Perito Forense / Criminalística"));
        CARRERAS.put("E", Arrays.asList(
                "Agronomía / Ingeniería Agronómica",
                "Biología / Biotecnología",
                "Geología",
                "Ciencias Ambientales / Ecología",
                "Astronomía",
                "Meteorología",
                "Oceanografía"));
    }

    /**
     * Recorta el podio a lo que se muestra: SIEMPRE como máximo el top 3 (si el 3.°
     * empata con el 4.° o más, los sobrantes no se muestran; entre las empatadas
     * se conserva el orden estable C-H-A-S-I-D-E de la lista recibida).
     * Única excepción: si TODAS las categorías tienen exactamente el mismo
     * puntaje, se muestran todas.
     */
    public List<String> recortarTop(List<String> top, Map<String, Integer> puntajes) {
        if (top == null) {
            return Collections.<String>emptyList();
        }
        if (top.size() <= 3) {
            return top;
        }
        if (top.size() == NOMBRES.size() && puntajes != null) {
            Integer primero = puntajes.get(top.get(0));
            boolean todasIguales = primero != null;
            for (String letra : top) {
                if (todasIguales && !primero.equals(puntajes.get(letra))) {
                    todasIguales = false;
                }
            }
            if (todasIguales) {
                return top;
            }
        }
        return new ArrayList<>(top.subList(0, 3));
    }

    /** Nombre completo de la categoría (el mismo en todas las vistas). */
    public String nombre(String letra) {
        String n = letra == null ? null : NOMBRES.get(letra);
        return n != null ? n : "";
    }

    /** Carreras sugeridas para la categoría; lista vacía si la letra no existe. */
    public List<String> carreras(String letra) {
        List<String> lista = letra == null ? null : CARRERAS.get(letra);
        return lista != null ? new ArrayList<>(lista) : Collections.<String>emptyList();
    }
}
