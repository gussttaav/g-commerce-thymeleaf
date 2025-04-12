package com.gplanet.commerce.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gplanet.commerce.dtos.pagination.PaginatedResponse;
import com.gplanet.commerce.dtos.usuario.ActualizacionUsuarioDTO;
import com.gplanet.commerce.dtos.usuario.CambioPasswdDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioAdminDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioDTO;
import com.gplanet.commerce.dtos.usuario.UsuarioResponseDTO;
import com.gplanet.commerce.entities.Usuario;
import com.gplanet.commerce.exceptions.EmailAlreadyExistsException;
import com.gplanet.commerce.exceptions.InvalidPasswordException;
import com.gplanet.commerce.exceptions.PasswordMismatchException;
import com.gplanet.commerce.exceptions.ResourceNotFoundException;
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

    @GetMapping("/authenticated")
    @ResponseBody
    public ResponseEntity<Boolean> checkAuthentication(Authentication authentication) {
        return ResponseEntity.ok(authentication != null && authentication.isAuthenticated());
    }

    @GetMapping("/admin/listar")
    public String listarUsuarios(Model model) {
        Page<UsuarioResponseDTO> usuariosPage = usuarioService.listarUsuarios(0, 10, "nombre", "ASC");
        PaginatedResponse<UsuarioResponseDTO> paginatedResponse = PaginatedResponse.fromPage(usuariosPage);

        model.addAttribute("activePage", "adminUsuarios");
        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        return "usuarios/lista";
    }

    @PostMapping("/admin/filtrar")
    public String filtrarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {
        
        Page<UsuarioResponseDTO> usuariosPage = usuarioService.listarUsuarios(
            page, size, sort, direction);
        PaginatedResponse<UsuarioResponseDTO> paginatedResponse = PaginatedResponse.fromPage(usuariosPage);

        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("pagination", paginatedResponse);
        return "usuarios/lista-usuario-page :: usuario-page";
    }

    @PostMapping("/admin/change-role/{id}")
    public String cambiarRol(@PathVariable Long id, Model model) {
        try{
            UsuarioResponseDTO updatedUser = usuarioService.cambiarRol(id);
            model.addAttribute("usuario", updatedUser);
            ToastUtil.success(model, "User role updated successfully");
            return "usuarios/lista-usuario-row :: usuario-row";
        } 
        catch(ResourceNotFoundException e){
            ToastUtil.error(model, "User not found!");
            return "empty :: empty";
        } 
        catch (Exception e) {
            ToastUtil.error(model, "Error updating user role. Please try again!");
            return "empty :: empty";
        }
    }

    @GetMapping("/admin/registrar")
    public String showAddUserModal(Model model) {
        return "usuarios/nuevo-modal :: userModal";
    }

    @PostMapping("/admin/registrar")
    public String createUser(@Valid UsuarioAdminDTO usuarioDTO, Model model) {
        try {
            UsuarioResponseDTO newUser = usuarioService.registrarUsuario(usuarioDTO);
            model.addAttribute("usuario", newUser);
            ToastUtil.success(model, "User created successfully");
            return "usuarios/lista-usuario-row :: usuario-row";
        } catch (Exception e) {
            log.error("Error creating user", e);
            ToastUtil.error(model, "Error creating user: " + e.getMessage());
            return "empty :: empty";
        }
    }
}
