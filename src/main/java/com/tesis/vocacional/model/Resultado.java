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
     * Devuelve el perfil recomendado (combinación de interés principal + aptitud principal)
     * en formato legible para la tabla de historial.
     * Ejemplo: "Artísticas + Salud"
     */
    public String getNombrePerfil() {
        // Obtener el primer interés principal y la primera aptitud principal
        String interesPrincipal = "";
        String aptitudPrincipal = "";

        if (interesesPrincipales != null && !interesesPrincipales.isEmpty()) {
            String primeraLetra = interesesPrincipales.split(",")[0].trim();
            interesPrincipal = nombreCategoria.getOrDefault(primeraLetra, primeraLetra);
        }
        if (aptitudesPrincipales != null && !aptitudesPrincipales.isEmpty()) {
            String primeraLetra = aptitudesPrincipales.split(",")[0].trim();
            aptitudPrincipal = nombreCategoria.getOrDefault(primeraLetra, primeraLetra);
        }

        if (interesPrincipal.isEmpty() && aptitudPrincipal.isEmpty()) {
            return "Perfil no reconocido";
        } else if (interesPrincipal.isEmpty()) {
            return aptitudPrincipal;
        } else if (aptitudPrincipal.isEmpty()) {
            return interesPrincipal;
        } else {
            return interesPrincipal + " + " + aptitudPrincipal;
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