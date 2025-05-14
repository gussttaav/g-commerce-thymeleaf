package com.gplanet.commerce.entities;

import lombok.Data;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing a user in the system.
 * Users can have different roles (ADMIN or USER) and can make purchases.
 *
 * @author Gustavo
 * @version 1.0
 */
@Data
@Entity
@Table(name = "usuario")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  /**
   * The user's full name.
   */
  private String nombre;
  
  /**
   * The user's email address. Must be unique in the system.
   */
  @Column(unique = true)
  private String email;
  
  /**
   * The user's encrypted password.
   */
  private String password;
  
  /**
   * The user's role in the system.
   */
  @Enumerated(EnumType.STRING)
  private Role rol;
  
  /**
   * Timestamp when the user account was created.
   */
  @Column(name = "fecha_creacion")
  private LocalDateTime fechaCreacion;
  
  /**
   * Enumeration of possible user roles in the system.
   */
  public enum Role {
    /** Administrator role with full system access. */
    ADMIN,
    
    /** Regular user role with limited access. */
    USER
  }
}
