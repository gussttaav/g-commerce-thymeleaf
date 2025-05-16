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

/**
 * Controller class that handles user-related operations.
 * Provides endpoints for user registration, authentication, profile management,
 * and administrative user management.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

  private final UsuarioService usuarioService;

  /**
   * Displays the login form page.
   * 
   * @param model           Spring MVC model
   * @param error           Optional error parameter indicating login failure
   * @param registroExitoso Optional parameter indicating successful registration
   * @return View name for login page
   */
  @GetMapping("/login")
  public String mostrarFormularioLogin(Model model,
      @RequestParam(required = false) String error,
      @RequestParam(required = false) String registroExitoso) {
    if (log.isDebugEnabled()) {
      log.debug("Accessing login form");
    }
    
    if (error != null) {
      log.warn("Failed login attempt");
      String errorMessage = "Login failed. Please check your email and password.";
      model.addAttribute("loginError", errorMessage);
    }
    if (registroExitoso != null) {
      log.info("User successfully registered, redirecting to login");
      model.addAttribute("registroExitoso", "Registration successful! Please login.");
    }
    return "usuarios/login";
  }

  /**
   * Displays the user registration form.
   * 
   * @param model Spring MVC model
   * @return View name for registration page
   */
  @GetMapping("/registro")
  public String mostrarFormularioRegistro(Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Accessing registration form");
    }
    model.addAttribute("usuario", new UsuarioDTO());
    return "usuarios/registro";
  }

  /**
   * Processes user registration form submission.
   * 
   * @param usuarioDTO Data transfer object containing user registration
   *                   information
   * @param result     Validation results for the registration data
   * @param model      Spring MVC model
   * @return View name for registration success or error page
   */
  @PostMapping("/registro")
  public String registrarUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO,
      BindingResult result,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to register new user: {}", usuarioDTO.getEmail());
    }
    
    if (result.hasErrors()) {
      log.warn("Registration validation errors: {}", result.getAllErrors());
      return "usuarios/registro";
    }

    try {
      usuarioService.registrarUsuario(usuarioDTO);
      log.info("User successfully registered: {}", usuarioDTO.getEmail());
      return "redirect:/usuarios/login?registroExitoso";
    } catch (EmailAlreadyExistsException e) {
      log.error("Error registering user - Email already exists: {}", usuarioDTO.getEmail(), e);
      model.addAttribute("registroError", e.getMessage());
      return "usuarios/registro";
    }
  }

  /**
   * Displays the user profile page.
   * 
   * @param model          Spring MVC model
   * @param authentication Current user's authentication
   * @return View name for profile page
   */
  @GetMapping("/perfil")
  public String mostrarPerfil(Model model, Authentication authentication) {
    if (log.isDebugEnabled()) {
      log.debug("Accessing user profile: {}", authentication.getName());
    }
    Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
    ActualizacionUsuarioDTO perfilDTO = new ActualizacionUsuarioDTO(
        usuario.getNombre(), usuario.getEmail());

    model.addAttribute("usuario", perfilDTO);
    return "usuarios/perfil";
  }

  /**
   * Updates user profile information.
   * 
   * @param authentication Current user's authentication
   * @param perfilDTO      Data transfer object containing profile update
   *                       information
   * @param result         Validation results for the profile data
   * @param model          Spring MVC model
   * @return View name for profile page with success/error messages
   */
  @PostMapping("/perfil")
  public String actualizarPerfil(Authentication authentication,
      @Valid @ModelAttribute("usuario") ActualizacionUsuarioDTO perfilDTO,
      BindingResult result,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to update profile for user: {}", authentication.getName());
    }
    
    if (result.hasErrors()) {
      log.warn("Profile update validation errors: {}", result.getAllErrors());
      return "usuarios/perfil";
    }

    try {
      usuarioService.actualizarPerfil(authentication.getName(), perfilDTO);
      log.info("Profile successfully updated for user: {}", authentication.getName());
      ToastUtil.success(model, "Profile updated successfully");
    } catch (EmailAlreadyExistsException e) {
      log.error("Error updating profile - Email already exists for user: {}", authentication.getName(), e);
      ToastUtil.error(model, e.getMessage());
    }

    return "usuarios/perfil";
  }

  /**
   * Displays the password change form.
   * 
   * @param model          Spring MVC model
   * @param authentication Current user's authentication
   * @return View name for password change page
   */
  @GetMapping("/password")
  public String mostrarFormularioCambioPassword(Model model, Authentication authentication) {
    if (log.isDebugEnabled()) {
      log.debug("Accessing password change form for user: {}", authentication.getName());
    }
    model.addAttribute("cambioPasswd", new CambioPasswdDTO());
    return "usuarios/password";
  }

  /**
   * Processes password change request.
   * 
   * @param authentication Current user's authentication
   * @param passwordDTO  Data transfer object containing password change
   *                       information
   * @param result         Validation results for the password data
   * @param model          Spring MVC model
   * @return View name for password page with success/error messages
   */
  @PostMapping("/password")
  public String changePassword(Authentication authentication,
      @Valid @ModelAttribute("cambioPasswd") CambioPasswdDTO passwordDTO,
      BindingResult result,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to change password for user: {}", authentication.getName());
    }
    
    if (result.hasErrors()) {
      log.warn("Password change validation errors: {}", result.getAllErrors());
      return "usuarios/password";
    }

    try {
      usuarioService.changePassword(authentication.getName(), passwordDTO);
      log.info("Password successfully changed for user: {}", authentication.getName());
      ToastUtil.success(model, "Password changed successfully");
    } catch (InvalidPasswordException | PasswordMismatchException e) {
      log.error("Error changing password for user: {}", authentication.getName(), e);
      ToastUtil.error(model, e.getMessage());
    }

    return "usuarios/password";
  }

  /**
   * Checks if the current user is authenticated.
   * 
   * @param authentication Current user's authentication
   * @return ResponseEntity containing authentication status
   */
  @GetMapping("/authenticated")
  @ResponseBody
  public ResponseEntity<Boolean> checkAuthentication(Authentication authentication) {
    if (log.isDebugEnabled()) {
      log.debug("Checking user authentication");
    }
    return ResponseEntity.ok(authentication != null && authentication.isAuthenticated());
  }

  /**
   * Lists all users with pagination (admin only).
   * 
   * @param model Spring MVC model
   * @return View name for user list page
   */
  @GetMapping("/admin/listar")
  public String listarUsuarios(Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Listing users (page 0)");
    }
    Page<UsuarioResponseDTO> usuariosPage = usuarioService.listarUsuarios(0, 10, "nombre", "ASC");
    PaginatedResponse<UsuarioResponseDTO> paginatedResponse = PaginatedResponse.fromPage(usuariosPage);

    model.addAttribute("activePage", "adminUsuarios");
    model.addAttribute("usuarios", usuariosPage.getContent());
    model.addAttribute("pagination", paginatedResponse);
    return "usuarios/lista";
  }

  /**
   * Filters and paginates user list (admin only).
   * 
   * @param page      Page number (zero-based)
   * @param size      Items per page
   * @param sort      Sort field
   * @param direction Sort direction
   * @param model     Spring MVC model
   * @return Fragment name containing filtered results
   */
  @GetMapping("/admin/filtrar")
  public String filtrarUsuarios(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "nombre") String sort,
      @RequestParam(defaultValue = "ASC") String direction,
      Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Filtering users - page: {}, size: {}, sort: {} {}", page, size, sort, direction);
    }

    Page<UsuarioResponseDTO> usuariosPage = usuarioService.listarUsuarios(
        page, size, sort, direction);
    PaginatedResponse<UsuarioResponseDTO> paginatedResponse = PaginatedResponse.fromPage(usuariosPage);

    model.addAttribute("usuarios", usuariosPage.getContent());
    model.addAttribute("pagination", paginatedResponse);
    return "usuarios/lista-usuario-page :: usuario-page";
  }

  /**
   * Toggles user role between ADMIN and USER (admin only).
   * 
   * @param id    ID of the user whose role should be changed
   * @param model Spring MVC model
   * @return Fragment name containing updated user row or error message
   */
  @PostMapping("/admin/change-role/{id}")
  public String cambiarRol(@PathVariable Long id, Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to change role for user ID: {}", id);
    }
    try {
      UsuarioResponseDTO updatedUser = usuarioService.cambiarRol(id);
      log.info("Role successfully changed for user ID: {}", id);
      model.addAttribute("usuario", updatedUser);
      ToastUtil.success(model, "User role updated successfully");
      return "usuarios/lista-usuario-row :: usuario-row";
    } catch (ResourceNotFoundException e) {
      log.error("Error changing role - User not found with ID: {}", id, e);
      ToastUtil.error(model, "User not found!");
      return "empty :: empty";
    }
  }

  /**
   * Shows the modal form for adding a new user (admin only).
   * 
   * @param model Spring MVC model
   * @return Fragment name containing user modal form
   */
  @GetMapping("/admin/registrar")
  public String showAddUserModal(Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Showing modal for new user registration");
    }
    return "usuarios/nuevo-modal :: userModal";
  }

  /**
   * Creates a new user through admin interface.
   * 
   * @param usuarioDTO Data transfer object containing new user information
   * @param model      Spring MVC model
   * @return Fragment name containing new user row or error message
   */
  @PostMapping("/admin/registrar")
  public String createUser(@Valid @ModelAttribute UsuarioAdminDTO usuarioDTO, BindingResult result, Model model) {
    if (log.isDebugEnabled()) {
      log.debug("Attempting to create new user: {}", usuarioDTO.getEmail());
    }
    
    if (result.hasErrors()) {
      log.error("User creation validation errors: {}", result.getAllErrors());
      ToastUtil.error(model, "Error creating user!");
      return "empty :: empty";
    }

    UsuarioResponseDTO newUser = usuarioService.registrarUsuario(usuarioDTO);
    log.info("User successfully created: {}", usuarioDTO.getEmail());
    model.addAttribute("usuario", newUser);
    ToastUtil.success(model, "User created successfully");
    return "usuarios/lista-usuario-row :: usuario-row";
  }
}
