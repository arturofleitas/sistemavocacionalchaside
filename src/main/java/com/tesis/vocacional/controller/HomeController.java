package com.tesis.vocacional.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;      // <-- Import correcto de Model
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request) {
        // Obtener la URI actual
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);
        return "home";
    }
}