package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;   

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String mostrarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("usuario", new Usuario());
        return "usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable int id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
        }
        return "usuarios";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Model model) {
        try {
            // ENCRIPTA LA CONTRASEÑA ANTES DE GUARDAR
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }

            usuarioService.guardarUsuario(usuario);

            return "redirect:/usuarios";   // Redirige y refresca la lista
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable int id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios";
    }
}