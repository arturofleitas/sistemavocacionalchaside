package com.tesis.vocacional.config;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone el usuario autenticado (nombre, apellido y rol) a todas las vistas
 * para mostrarlo en el encabezado de cada interfaz.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UsuarioService usuarioService;

    public GlobalModelAttributes(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("currentUser")
    public Usuario currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return usuarioService.buscarPorUsername(auth.getName());
    }
}
