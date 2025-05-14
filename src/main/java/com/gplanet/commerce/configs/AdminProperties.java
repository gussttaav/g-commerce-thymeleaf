package com.gplanet.commerce.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Configuration properties for the default admin user.
 * This class is used to load admin credentials from the application properties
 * file.
 * 
 * @author Gustavo
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "admin.default")
public class AdminProperties {
  /**
   * The email address for the default admin user.
   */
  private String email;

  /**
   * The password for the default admin user.
   */
  private String password;
}
