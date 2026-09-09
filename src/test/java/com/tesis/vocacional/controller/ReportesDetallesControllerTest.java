package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.AccesoReporteService;
import com.tesis.vocacional.services.RespuestaService;
import com.tesis.vocacional.services.ResultadoService;
import com.tesis.vocacional.services.TestUsuarioPreguntaService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de regresión del endpoint /reportes-detalles/test/{id}: el acceso
 * debe respetar la propiedad de la evaluación. Sin permiso se redirige a un
 * destino seguro (sin cargar datos del propietario).
 */
@ExtendWith(MockitoExtension.class)
class ReportesDetallesControllerTest {

    @Mock
    private ResultadoService resultadoService;

    @Mock
    private TestUsuarioPreguntaService testUsuarioPreguntaService;

    @Mock
    private RespuestaService respuestaService;

    @Mock
    private AccesoReporteService accesoReporteService;

    private ReportesDetallesController controlador;

    private Resultado resultadoDeDueno;

    @BeforeEach
    void setUp() {
        controlador = new ReportesDetallesController(resultadoService, testUsuarioPreguntaService,
                respuestaService, accesoReporteService);

        Usuario dueno = new Usuario();
        dueno.setId(1);
        dueno.setNombre("Juan");
        dueno.setApellido("Pérez");

        TestUsuario tu = new TestUsuario();
        tu.setId(10);
        tu.setUsuario(dueno);

        resultadoDeDueno = new Resultado();
        resultadoDeDueno.setId(59);
        resultadoDeDueno.setTest(tu);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarEstudiante() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("lucia", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))));
    }

    private void autenticarAnonimo() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }

    @Test
    void resultadoInexistenteRedirigeSinDatos() {
        autenticarEstudiante();
        when(resultadoService.buscarPorId(999)).thenReturn(null);

        RedirectAttributes redirect = new RedirectAttributesModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);

        String vista = controlador.verDetalleTest(999, request, new ExtendedModelMap(), redirect);
        assertEquals("redirect:/reportes", vista);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void estudianteSinPermisoRedirigeAReportesSinCargarDatos() {
        autenticarEstudiante();
        when(resultadoService.buscarPorId(59)).thenReturn(resultadoDeDueno);
        when(accesoReporteService.puedeAcceder(any(HttpServletRequest.class), eq(resultadoDeDueno)))
                .thenReturn(false);

        RedirectAttributes redirect = new RedirectAttributesModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);

        String vista = controlador.verDetalleTest(59, request, new ExtendedModelMap(), redirect);
        assertEquals("redirect:/reportes", vista);
        assertNotNull(redirect.getFlashAttributes().get("error"));

        // No se consultan respuestas ni preguntas del propietario
        verify(testUsuarioPreguntaService, never()).obtenerPorTestUsuarioId(any(Integer.class));
        verify(respuestaService, never()).obtenerPorTestUsuarioId(any(Integer.class));
    }

    @Test
    void invitadoSinPermisoRedirigeATestPublico() {
        autenticarAnonimo();
        when(resultadoService.buscarPorId(59)).thenReturn(resultadoDeDueno);
        when(accesoReporteService.puedeAcceder(any(HttpServletRequest.class), eq(resultadoDeDueno)))
                .thenReturn(false);

        RedirectAttributes redirect = new RedirectAttributesModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);

        String vista = controlador.verDetalleTest(59, request, new ExtendedModelMap(), redirect);
        assertEquals("redirect:/test/publico", vista);

        verify(testUsuarioPreguntaService, never()).obtenerPorTestUsuarioId(any(Integer.class));
        verify(respuestaService, never()).obtenerPorTestUsuarioId(any(Integer.class));
    }

    @Test
    void conPermisoRenderizaDetalle() {
        autenticarEstudiante();
        when(resultadoService.buscarPorId(59)).thenReturn(resultadoDeDueno);
        when(accesoReporteService.puedeAcceder(any(HttpServletRequest.class), eq(resultadoDeDueno)))
                .thenReturn(true);
        when(testUsuarioPreguntaService.obtenerPorTestUsuarioId(10)).thenReturn(Collections.emptyList());
        when(respuestaService.obtenerPorTestUsuarioId(10)).thenReturn(Collections.emptyList());

        Model model = new ExtendedModelMap();
        HttpServletRequest request = mock(HttpServletRequest.class);

        String vista = controlador.verDetalleTest(59, request, model,
                new RedirectAttributesModelMap());
        assertEquals("detalle-test", vista);
        assertEquals("Juan Pérez", model.getAttribute("alumnoNombre"));
    }
}
