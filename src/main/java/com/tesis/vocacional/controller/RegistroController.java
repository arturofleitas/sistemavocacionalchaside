package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/registrarse")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registrarse")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        try {
            // Encriptam la contraseña antes de guardar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setRol("ESTUDIANTE");
            usuario.setEstado(true);

            usuarioService.guardarUsuario(usuario);

            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            return "registro";
        }
    }
}