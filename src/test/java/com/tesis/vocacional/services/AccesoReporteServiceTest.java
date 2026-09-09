package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.SesionInvitado;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import jakarta.servlet.http.Cookie;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de regresión del control de acceso centralizado a reportes privados.
 */
@ExtendWith(MockitoExtension.class)
class AccesoReporteServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SesionInvitadoService sesionInvitadoService;

    private AccesoReporteService servicio;

    private Usuario dueno;
    private Usuario otroEstudiante;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        servicio = new AccesoReporteService(usuarioService, sesionInvitadoService);

        dueno = new Usuario();
        dueno.setId(1);
        dueno.setUsername("dueno");
        dueno.setRol("ESTUDIANTE");

        otroEstudiante = new Usuario();
        otroEstudiante.setId(2);
        otroEstudiante.setUsername("otro");
        otroEstudiante.setRol("ESTUDIANTE");

        admin = new Usuario();
        admin.setId(3);
        admin.setUsername("admin");
        admin.setRol("ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- Utilidades ----------

    private Resultado resultadoDeUsuario(Usuario usuario) {
        TestUsuario tu = new TestUsuario();
        tu.setUsuario(usuario);
        Resultado r = new Resultado();
        r.setTest(tu);
        return r;
    }

    private Resultado resultadoDeInvitado(SesionInvitado sesion) {
        TestUsuario tu = new TestUsuario();
        tu.setSesionInvitado(sesion);
        Resultado r = new Resultado();
        r.setTest(tu);
        return r;
    }

    private void autenticarComo(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))));
    }

    private void autenticarAnonimo() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }

    private HttpServletRequest requestConCookie(String token) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(SesionInvitadoService.COOKIE_NAME, token)});
        return request;
    }

    // ---------- Escenarios ----------

    @Test
    void duenoPuedeAccederASuPropiaEvaluacion() {
        autenticarComo(dueno);
        when(usuarioService.buscarPorUsername("dueno")).thenReturn(dueno);
        Resultado resultado = resultadoDeUsuario(dueno);

        assertTrue(servicio.puedeAcceder(mock(HttpServletRequest.class), resultado));
    }

    @Test
    void otroEstudianteNoPuedeAcceder() {
        autenticarComo(otroEstudiante);
        when(usuarioService.buscarPorUsername("otro")).thenReturn(otroEstudiante);
        Resultado resultado = resultadoDeUsuario(dueno);

        assertFalse(servicio.puedeAcceder(mock(HttpServletRequest.class), resultado));
    }

    @Test
    void adminPuedeAccederACualquierEvaluacion() {
        autenticarComo(admin);
        when(usuarioService.buscarPorUsername("admin")).thenReturn(admin);
        Resultado resultado = resultadoDeUsuario(dueno);

        assertTrue(servicio.puedeAcceder(mock(HttpServletRequest.class), resultado));
    }

    @Test
    void sinAutenticacionYSinSesionInvitadoNoPuedeAcceder() {
        autenticarAnonimo();
        Resultado resultado = resultadoDeUsuario(dueno);

        assertFalse(servicio.puedeAcceder(mock(HttpServletRequest.class), resultado));
    }

    @Test
    void invitadoPuedeAccederASuPropiaEvaluacion() {
        autenticarAnonimo();
        SesionInvitado sesion = new SesionInvitado();
        sesion.setId(UUID.randomUUID());
        when(sesionInvitadoService.resolverPorToken("tokenA")).thenReturn(Optional.of(sesion));
        Resultado resultado = resultadoDeInvitado(sesion);

        assertTrue(servicio.puedeAcceder(requestConCookie("tokenA"), resultado));
    }

    @Test
    void invitadosDistintosNoPuedenAccederEntreSi() {
        autenticarAnonimo();
        SesionInvitado sesionA = new SesionInvitado();
        sesionA.setId(UUID.randomUUID());
        SesionInvitado sesionB = new SesionInvitado();
        sesionB.setId(UUID.randomUUID());
        when(sesionInvitadoService.resolverPorToken("tokenA")).thenReturn(Optional.of(sesionA));
        Resultado resultadoDeB = resultadoDeInvitado(sesionB);

        assertFalse(servicio.puedeAcceder(requestConCookie("tokenA"), resultadoDeB));
    }

    @Test
    void sesionRevocadaNoPermiteAcceder() {
        autenticarAnonimo();
        SesionInvitado sesion = new SesionInvitado();
        sesion.setId(UUID.randomUUID());
        // Sesión revocada: resolverPorToken devuelve vacío
        when(sesionInvitadoService.resolverPorToken("tokenRevocado")).thenReturn(Optional.empty());
        Resultado resultado = resultadoDeInvitado(sesion);

        assertFalse(servicio.puedeAcceder(requestConCookie("tokenRevocado"), resultado));
    }

    @Test
    void invitadoVinculadoACuentaNoPermiteAccesoPorSesionRevocada() {
        autenticarAnonimo();
        SesionInvitado sesion = new SesionInvitado();
        sesion.setId(UUID.randomUUID());
        // Evaluación ya vinculada a una cuenta: la sesión fue revocada y el test tiene dueño
        when(sesionInvitadoService.resolverPorToken("tokenRevocado")).thenReturn(Optional.empty());
        TestUsuario tu = new TestUsuario();
        tu.setUsuario(dueno);
        Resultado resultado = new Resultado();
        resultado.setTest(tu);

        assertFalse(servicio.puedeAcceder(requestConCookie("tokenRevocado"), resultado));
    }

    @Test
    void duenoVinculadoPuedeAccederTrasVinculacion() {
        autenticarComo(dueno);
        when(usuarioService.buscarPorUsername("dueno")).thenReturn(dueno);
        TestUsuario tu = new TestUsuario();
        tu.setUsuario(dueno);
        Resultado resultado = new Resultado();
        resultado.setTest(tu);

        assertTrue(servicio.puedeAcceder(mock(HttpServletRequest.class), resultado));
    }

    @Test
    void resultadoNuloOsinTestNoEsAccesible() {
        autenticarComo(admin);

        assertFalse(servicio.puedeAcceder(mock(HttpServletRequest.class), null));
        assertFalse(servicio.puedeAcceder(mock(HttpServletRequest.class), new Resultado()));
    }
}
