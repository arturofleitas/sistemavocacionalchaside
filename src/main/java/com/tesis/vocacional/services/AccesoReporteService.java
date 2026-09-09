package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Resultado;
import com.tesis.vocacional.model.SesionInvitado;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Control de acceso centralizado para los reportes privados (detalle, respuestas,
 * impresión y cualquier endpoint relacionado).
 *
 * Las reglas son:
 *  - ADMIN: puede consultar cualquier evaluación.
 *  - ESTUDIANTE: solo su propia evaluación (comparación por ID del {@link Usuario},
 *    nunca por nombre visible).
 *  - Sin autenticación: no puede consultar reportes privados.
 *  - INVITADO: solo su evaluación mediante una sesión de invitado válida y vigente;
 *    una sesión revocada o vencida no otorga acceso, y tampoco permite acceder a una
 *    evaluación que ya fue vinculada a una cuenta.
 */
@Service
public class AccesoReporteService {

    private final UsuarioService usuarioService;
    private final SesionInvitadoService sesionInvitadoService;

    public AccesoReporteService(UsuarioService usuarioService,
                                SesionInvitadoService sesionInvitadoService) {
        this.usuarioService = usuarioService;
        this.sesionInvitadoService = sesionInvitadoService;
    }

    /**
     * Determina si el usuario (autenticado o invitado) que realiza la petición
     * puede acceder al resultado indicado.
     */
    public boolean puedeAcceder(HttpServletRequest request, Resultado resultado) {
        if (resultado == null || resultado.getTest() == null) {
            return false;
        }

        // 1) Usuario autenticado (Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Usuario usuario = usuarioService.buscarPorUsername(auth.getName());
            if (usuario == null) {
                return false;
            }
            // ADMIN: acceso a cualquier evaluación
            if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
                return true;
            }
            // ESTUDIANTE: únicamente su propia evaluación, comparando IDs
            TestUsuario testUsuario = resultado.getTest();
            if (testUsuario.getUsuario() != null
                    && testUsuario.getUsuario().getId() == usuario.getId()) {
                return true;
            }
            return false;
        }

        // 2) Sin autenticación: solo acceso de invitado mediante su sesión válida
        Optional<SesionInvitado> sesion = resolverSesion(request);
        if (sesion.isEmpty()) {
            return false;
        }
        TestUsuario testUsuario = resultado.getTest();
        if (testUsuario.getSesionInvitado() != null
                && testUsuario.getSesionInvitado().getId().equals(sesion.get().getId())) {
            return true;
        }
        return false;
    }

    /**
     * Resuelve la sesión de invitado a partir de la cookie, solo si está vigente
     * (no revocada y sin expirar). Una sesión revocada devuelve vacío, por lo que
     * no otorga acceso.
     */
    private Optional<SesionInvitado> resolverSesion(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
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
}
