package com.tesis.vocacional.services;

import java.util.List;
import java.util.Map;

/**
 * Resultado del cálculo de puntajes de un test CHASIDE.
 *
 * Es independiente del propietario (usuario registrado o invitado) para que
 * pueda reutilizarse en ambos flujos.
 *
 * Conserva internamente los 14 puntajes (7 de intereses y 7 de aptitudes) para
 * la comprobación del cálculo, y expone las áreas predominantes por dimensión:
 * todas las áreas cuyo puntaje es exactamente igual al máximo positivo, sin
 * recortarlas a dos o tres y sin combinarlas en un "perfil" único.
 */
public class ResultadoCalculo {

    private Map<String, Integer> puntajesInteres;
    private Map<String, Integer> puntajesAptitud;
    private List<String> interesesPredominantes;
    private List<String> aptitudesPredominantes;
    private int maxInteres;
    private int maxAptitud;
    private boolean interesCero;
    private boolean aptitudCero;
    private int totalPreguntas;
    private int inconsistencias;
    private boolean puntajesFueraDeRango;
    private Map<String, String> nombresCategoria;

    public Map<String, Integer> getPuntajesInteres() {
        return puntajesInteres;
    }

    public void setPuntajesInteres(Map<String, Integer> puntajesInteres) {
        this.puntajesInteres = puntajesInteres;
    }

    public Map<String, Integer> getPuntajesAptitud() {
        return puntajesAptitud;
    }

    public void setPuntajesAptitud(Map<String, Integer> puntajesAptitud) {
        this.puntajesAptitud = puntajesAptitud;
    }

    public List<String> getInteresesPredominantes() {
        return interesesPredominantes;
    }

    public void setInteresesPredominantes(List<String> interesesPredominantes) {
        this.interesesPredominantes = interesesPredominantes;
    }

    public List<String> getAptitudesPredominantes() {
        return aptitudesPredominantes;
    }

    public void setAptitudesPredominantes(List<String> aptitudesPredominantes) {
        this.aptitudesPredominantes = aptitudesPredominantes;
    }

    public int getMaxInteres() {
        return maxInteres;
    }

    public void setMaxInteres(int maxInteres) {
        this.maxInteres = maxInteres;
    }

    public int getMaxAptitud() {
        return maxAptitud;
    }

    public void setMaxAptitud(int maxAptitud) {
        this.maxAptitud = maxAptitud;
    }

    public boolean isInteresCero() {
        return interesCero;
    }

    public void setInteresCero(boolean interesCero) {
        this.interesCero = interesCero;
    }

    public boolean isAptitudCero() {
        return aptitudCero;
    }

    public void setAptitudCero(boolean aptitudCero) {
        this.aptitudCero = aptitudCero;
    }

    public int getTotalPreguntas() {
        return totalPreguntas;
    }

    public void setTotalPreguntas(int totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }

    public int getInconsistencias() {
        return inconsistencias;
    }

    public void setInconsistencias(int inconsistencias) {
        this.inconsistencias = inconsistencias;
    }

    public boolean isPuntajesFueraDeRango() {
        return puntajesFueraDeRango;
    }

    public void setPuntajesFueraDeRango(boolean puntajesFueraDeRango) {
        this.puntajesFueraDeRango = puntajesFueraDeRango;
    }

    public Map<String, String> getNombresCategoria() {
        return nombresCategoria;
    }

    public void setNombresCategoria(Map<String, String> nombresCategoria) {
        this.nombresCategoria = nombresCategoria;
    }
}
