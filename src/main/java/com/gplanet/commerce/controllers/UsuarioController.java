package com.gplanet.commerce.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gplanet.commerce.dtos.ActualizacionUsuarioDTO;
import com.gplanet.commerce.dtos.CambioPasswdDTO;
import com.gplanet.commerce.dtos.UsuarioDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.exceptions.InvalidPasswordException;
import com.gplanet.commerce.exceptions.PasswordMismatchException;
import com.gplanet.commerce.services.UsuarioService;
import com.gplanet.commerce.utilities.ToastUtil;

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
        } 
        catch (EmailAlreadyExistsException e) {
            model.addAttribute("registroError", e.getMessage());
            return "usuarios/registro";
        }
        catch (Exception e) {
            model.addAttribute("registroError", "Error al registrar usuario!");
            return "usuarios/registro";
        }
    }    

    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        ActualizacionUsuarioDTO perfilDTO = new ActualizacionUsuarioDTO(
            usuario.getNombre(), usuario.getEmail());

        model.addAttribute("usuario", perfilDTO);
        return "usuarios/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(Authentication authentication,
                                 @Valid @ModelAttribute("usuario") ActualizacionUsuarioDTO perfilDTO,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            return "usuarios/perfil";
        }
        
        try {
            usuarioService.actualizarPerfil(authentication.getName(), perfilDTO);
            ToastUtil.success(model, "Profile updated successfully");
        } 
        catch (EmailAlreadyExistsException e) {
            ToastUtil.error(model, e.getMessage());
        } 
        catch (Exception e) {
            ToastUtil.error(model, "Error updating profile. Please try again.");
        }

        return "usuarios/perfil";
    }

    @GetMapping("/password")
    public String mostrarFormularioCambioPassword(Model model, Authentication authentication) {
        model.addAttribute("cambioPasswd", new CambioPasswdDTO());
        return "usuarios/password";
    }

    @PostMapping("/password")
    public String cambiarContraseña(Authentication authentication,
                                  @Valid @ModelAttribute("cambioPasswd") CambioPasswdDTO contraseñaDTO,
                                  BindingResult result,
                                  Model model) {
        if (result.hasErrors()) {
            return "usuarios/password";
        }
        
        try {
            usuarioService.cambiarContraseña(authentication.getName(), contraseñaDTO);
            ToastUtil.success(model, "Password changed successfully");
        } 
        catch(InvalidPasswordException | PasswordMismatchException e){
            ToastUtil.error(model, e.getMessage());
        } 
        catch (Exception e) {
            ToastUtil.error(model, "Error changing password. Please try again.");
        }

        return "usuarios/password";
    }
}
