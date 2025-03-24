package com.gplanet.commerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gplanet.commerce.dtos.UsuarioDTO;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model, 
                    @RequestParam(required = false) String error,
                    @RequestParam(required = false) String registroExitoso) {

        if (error != null) {
            String errorMessage = "Login failed. Please check your email and password.";
            model.addAttribute("loginError", errorMessage);
        }
        if (registroExitoso != null) {
            model.addAttribute("registroExitoso", "Registration successful! Please login.");
        }
        return "usuarios/login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "usuarios/registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO, 
                                  BindingResult result, 
                                  Model model) {
        if (result.hasErrors()) {
            return "usuarios/registro";
        }
        
        try {
            usuarioService.registrarUsuario(usuarioDTO);
            return "redirect:/usuarios/login?registroExitoso";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("registroError", e.getMessage());
            return "usuarios/registro";
        } catch (Exception e) {
            model.addAttribute("registroError", "Error al registrar usuario!");
            return "usuarios/registro";
        }
    }    
}
