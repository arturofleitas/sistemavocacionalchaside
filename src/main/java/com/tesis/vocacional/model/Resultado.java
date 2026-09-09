package com.tesis.vocacional.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String perfil;
    private int puntaje;
    private String interesesPrincipales;
    private String aptitudesPrincipales;
    private String puntajesInteres;
    private String puntajesAptitud;

    @ManyToOne
    private TestUsuario test;

    private LocalDateTime fechaRealizacion;

    @Transient
    private String fechaFormateada;

    private static final Map<String, String> nombreCategoria = new HashMap<>();
    static {
        nombreCategoria.put("C", "Administrativas y Contables");
        nombreCategoria.put("H", "Humanísticas, Jurídicas y Sociales");
        nombreCategoria.put("A", "Artísticas");
        nombreCategoria.put("S", "Ciencias de la Salud");
        nombreCategoria.put("I", "Ingenierías, Técnicas y Computación");
        nombreCategoria.put("D", "Defensa y Seguridad");
        nombreCategoria.put("E", "Ciencias Agrarias, Naturales y Biológicas");
    }

    /**
     * Devuelve una descripción de las áreas predominantes por dimensión, sin
     * combinarlas en un perfil único. Ejemplo:
     * "Intereses: Artísticas - Defensa | Aptitudes: Salud".
     */
    public String getNombrePerfil() {
        String interes = getNombresIntereses();
        String aptitud = getNombresAptitudes();

        boolean sinInteres = "No definido".equals(interes);
        boolean sinAptitud = "No definido".equals(aptitud);

        if (sinInteres && sinAptitud) {
            return "No se identificaron áreas predominantes";
        } else if (sinInteres) {
            return "Aptitudes: " + aptitud;
        } else if (sinAptitud) {
            return "Intereses: " + interes;
        } else {
            return "Intereses: " + interes + " | Aptitudes: " + aptitud;
        }
    }

    /**
     * Devuelve los nombres completos de los intereses principales separados por " - ".
     * Útil para vistas detalladas.
     */
    public String getNombresIntereses() {
        if (interesesPrincipales == null || interesesPrincipales.isEmpty()) {
            return "No definido";
        }
        return Arrays.stream(interesesPrincipales.split(","))
                .map(String::trim)
                .map(nombreCategoria::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" - "));
    }

    /**
     * Devuelve los nombres completos de las aptitudes principales separados por " - ".
     * Útil para vistas detalladas.
     */
    public String getNombresAptitudes() {
        if (aptitudesPrincipales == null || aptitudesPrincipales.isEmpty()) {
            return "No definido";
        }
        return Arrays.stream(aptitudesPrincipales.split(","))
                .map(String::trim)
                .map(nombreCategoria::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" - "));
    }

    public String getFechaFormateada() {
        if (fechaRealizacion != null) {
            return fechaRealizacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

    public String getInteresesPrincipales() { return interesesPrincipales; }
    public void setInteresesPrincipales(String interesesPrincipales) { this.interesesPrincipales = interesesPrincipales; }

    public String getAptitudesPrincipales() { return aptitudesPrincipales; }
    public void setAptitudesPrincipales(String aptitudesPrincipales) { this.aptitudesPrincipales = aptitudesPrincipales; }

    public String getPuntajesInteres() { return puntajesInteres; }
    public void setPuntajesInteres(String puntajesInteres) { this.puntajesInteres = puntajesInteres; }

    public String getPuntajesAptitud() { return puntajesAptitud; }
    public void setPuntajesAptitud(String puntajesAptitud) { this.puntajesAptitud = puntajesAptitud; }

    public TestUsuario getTest() { return test; }
    public void setTest(TestUsuario test) { this.test = test; }

    public LocalDateTime getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDateTime fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
}