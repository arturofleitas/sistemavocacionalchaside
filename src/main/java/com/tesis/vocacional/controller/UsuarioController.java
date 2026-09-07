package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final int PASSWORD_MIN_LENGTH = 5;

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    /**Configura el conversor personalizado para el campo 'estado':
     * acepta tanto "true"/"false" (lo que envía el <select> actual)
     * como "ACTIVO"/"INACTIVO", por si el texto de las opciones cambia.*/
    @InitBinder
    public void initBinder(WebDataBinder binder) {
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
    }

    /**
     * Muestra la página de gestión de usuarios con la lista y el formulario vacío.
     */
    @GetMapping
    public String mostrarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("usuario", new Usuario());
        return "usuarios";
    }

    /**
     * Muestra el formulario de edición cargando los datos del usuario seleccionado.
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
        model.addAttribute("abrirModal", true);
        return "usuarios";
    }

    /**
     * Guarda (crea o actualiza) un usuario gestionando la encriptación de contraseñas.
     */
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modalError", "Error en el formulario: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("abrirModal", true);
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }

        // Contraseña nueva (no vacía) debe cumplir el mínimo; vacía = conservar la actual (edición).
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()
                && usuario.getPassword().length() < PASSWORD_MIN_LENGTH) {
            model.addAttribute("modalError", "La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres.");
            model.addAttribute("abrirModal", true);
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }

        try {
            // Encriptar contraseña solo si se proporcionó una nueva o no está vacía
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
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
            model.addAttribute("modalError", e.getMessage());
            model.addAttribute("abrirModal", true);
            model.addAttribute("usuarios", usuarioService.listarTodos());
            model.addAttribute("usuario", usuario);
            return "usuarios";
        }
    }

    /**Alterna el estado del usuario (si está activo lo desactiva, y viceversa).*/
    @GetMapping("/alternar-estado/{id}")
    public String alternarEstadoUsuario(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            boolean nuevoEstado = usuarioService.alternarEstado(id);
            String mensaje = nuevoEstado ? "El usuario ha sido activado correctamente." : "El usuario ha sido desactivado correctamente.";
            redirectAttributes.addFlashAttribute("success", mensaje);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    /**Devuelve el resumen de datos que se eliminarán al borrar la cuenta de un usuario (JSON).*/
    @GetMapping("/eliminar/detalle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detalleEliminacion(@PathVariable int id) {
        try {
            Map<String, Object> resumen = usuarioService.resumenEliminacion(id);
            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Elimina definitivamente la cuenta de un usuario y sus datos asociados.
     * Es una acción irreversible: requiere re-autenticación (contraseña del
     * administrador actual) según recomendaciones OWASP, y no permite
     * eliminar la propia cuenta. Devuelve JSON para que el error se muestre
     * dentro del modal sin cerrarlo.
     */
    @PostMapping("/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@RequestParam int id,
                                                               @RequestParam String password) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        try {
            // 1) Re-autenticación: la contraseña no puede ser nula/vacía
            if (password == null || password.trim().isEmpty()) {
                respuesta.put("success", false);
                respuesta.put("error", "Debes ingresar tu contraseña para confirmar la eliminación.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario adminActual = (auth != null) ? usuarioService.buscarPorUsername(auth.getName()) : null;

            if (adminActual == null) {
                respuesta.put("success", false);
                respuesta.put("error", "No se pudo verificar la sesión del administrador.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            // 2) Verificar la contraseña del administrador
            if (!passwordEncoder.matches(password, adminActual.getPassword())) {
                respuesta.put("success", false);
                respuesta.put("error", "Contraseña incorrecta. No se realizó la eliminación.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            // 3) No permitir eliminar la propia cuenta
            if (id == adminActual.getId()) {
                respuesta.put("success", false);
                respuesta.put("error", "No puedes eliminar tu propia cuenta.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            // 4) Ejecutar la eliminación (cuenta + datos asociados)
            usuarioService.eliminarUsuarioConDatos(id);
            respuesta.put("success", true);
            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            respuesta.put("success", false);
            respuesta.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
}