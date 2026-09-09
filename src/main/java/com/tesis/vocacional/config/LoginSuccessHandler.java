package com.tesis.vocacional.config;

import com.tesis.vocacional.model.SesionInvitado;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.SesionInvitadoService;
import com.tesis.vocacional.services.TestUsuarioService;
import com.tesis.vocacional.services.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

/**
 * Tras un login exitoso, si en el navegador existe una sesión de invitado con un
 * test ya finalizado y todavía no asociado a ninguna cuenta, se lo vincula
 * automáticamente al usuario que acaba de iniciar sesión. De esta forma el
 * resultado queda guardado en su historial y aparece en la pantalla de reportes.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;
    private final TestUsuarioService testUsuarioService;
    private final SesionInvitadoService sesionInvitadoService;

    public LoginSuccessHandler(UsuarioService usuarioService,
                               TestUsuarioService testUsuarioService,
                               SesionInvitadoService sesionInvitadoService) {
        this.usuarioService = usuarioService;
        this.testUsuarioService = testUsuarioService;
        this.sesionInvitadoService = sesionInvitadoService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());

        resolverSesion(request).ifPresent(sesion -> {
            testUsuarioService.buscarPorSesionInvitado(sesion).ifPresent(testUsuario -> {
                if (usuario != null
                        && "COMPLETADO".equalsIgnoreCase(testUsuario.getEstado())
                        && testUsuario.getUsuario() == null) {
                    testUsuario.setUsuario(usuario);
                    testUsuario.setSesionInvitado(null);
                    testUsuarioService.guardar(testUsuario);
                    sesionInvitadoService.revocar(sesion);
                    response.addCookie(borrarCookie());
                }
            });
        });

        response.sendRedirect("/home");
    }

    private Optional<SesionInvitado> resolverSesion(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie c : cookies) {
            if (SesionInvitadoService.COOKIE_NAME.equals(c.getName())) {
                return sesionInvitadoService.resolverPorToken(c.getValue());
            }
        }
        return Optional.empty();
    }

    private Cookie borrarCookie() {
        Cookie cookie = new Cookie(SesionInvitadoService.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
