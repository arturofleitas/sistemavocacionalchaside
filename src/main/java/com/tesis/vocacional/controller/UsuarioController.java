package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Configura los conversores personalizados:
     * - Para el campo 'estado': convierte "ACTIVO" a true, "INACTIVO" a false.
     * - Para el campo 'cedula': convierte String a int, validando el rango.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Conversor para Boolean (ACTIVO/INACTIVO)
        binder.registerCustomEditor(Boolean.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    String upper = text.trim().toUpperCase();
                    if ("ACTIVO".equals(upper)) {
                        setValue(Boolean.TRUE);
                    } else if ("INACTIVO".equals(upper)) {
                        setValue(Boolean.FALSE);
                    } else {
                        setValue(Boolean.parseBoolean(upper));
                    }
                }
            }
        });

        // Conversor para cédula (String a int) con validación de rango
        binder.registerCustomEditor(Integer.class, "cedula", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(0); // valor por defecto si está vacío (opcional)
                } else {
                    try {
                        long valor = Long.parseLong(text.trim());
                        if (valor < 0 || valor > Integer.MAX_VALUE) {
                            throw new NumberFormatException("La cédula debe estar entre 0 y " + Integer.MAX_VALUE);
                        }
                        setValue((int) valor);
                    } catch (NumberFormatException e) {
                        setValue(null);
                        // Lanzar una excepción de validación personalizada
                        throw new IllegalArgumentException("Cédula inválida: debe ser un número entero entre 0 y 2,147,483,647");
                    }
                }
            }
        });
    }

    /**
     * Muestra la página de gestión de usuarios.
     */
    @GetMapping
    public String mostrarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("usuario", new Usuario());
        return "usuarios";
    }

    /**
     * Muestra el formulario de edición con los datos del usuario.
     * Si el ID es válido, carga el usuario; si no, redirige a la lista.
     */
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable int id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario != null) {
            model.addAttribute("usuario", usuario);
        } else {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios";
    }

    /**
     * Guarda (crea o actualiza) un usuario.
     * Encripta la contraseña si es nueva.
     * Captura errores de validación (como cédula inválida) y los muestra en la vista.
     */
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, BindingResult bindingResult, Model model) {
        // Si hay errores de conversión (como cédula inválida), se añaden mensajes
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Error en el formulario: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }

        try {
            // Encriptar contraseña solo si se proporcionó una nueva
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
                // Si es edición y la contraseña está vacía, mantener la existente
                if (usuario.getId() > 0) {
                    Usuario existing = usuarioService.buscarPorId(usuario.getId());
                    if (existing != null && existing.getPassword() != null) {
                        usuario.setPassword(existing.getPassword());
                    }
                }
            }

            usuarioService.guardarUsuario(usuario);
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }
    }

    /**
     * Elimina un usuario por su ID.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable int id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios";
    }
}