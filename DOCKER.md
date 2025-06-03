# G-Commerce: Spring Boot with Thymeleaf

A full-stack e-commerce web application built with **Spring Boot**, **Thymeleaf**, and **HTMX**. This Docker image contains a complete monolithic application that combines backend services and frontend presentation for a seamless e-commerce experience.

## 🚀 Features

- User management (registration, authentication, profile)
- Product catalog with search and filtering
- Shopping cart functionality
- Purchase history tracking
- Admin panel for user and product management
- Responsive design with light/dark themes
- Role-based access control (ADMIN, USER)
- Advanced logging system with multiple specialized log files
- Container-optimized log rotation and compression
- Security audit trail and performance monitoring

## 🐳 Quick Start

### Prerequisites

- Docker and Docker Compose

### Run with Docker Compose

Create a `docker-compose.yml` file:

```yaml
services:
  mysql:
    image: gussttaav/g-commerce-mysql:latest
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_USER: dbuser
      MYSQL_PASSWORD: dbpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - g-commerce-network
    restart: unless-stopped

  webapp:
    image: gussttaav/g-commerce-thymeleaf:latest
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_USERNAME: dbuser
      SPRING_DATASOURCE_PASSWORD: dbpassword
      # Logging configuration
      LOG_PATH: /app/logs
      LOG_MAX_FILE_SIZE: 50MB
      LOG_MAX_HISTORY: 15
      LOG_TOTAL_SIZE_CAP: 500MB
    volumes:
      # Optional: Mount logs directory for external access
      - ./logs:/app/logs
    ports:
      - "8080:8080"
    networks:
      - g-commerce-network
    restart: unless-stopped

networks:
  g-commerce-network:
    driver: bridge

volumes:
  mysql_data:
```

Then run:
```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

## 📊 Logging System

The application includes a comprehensive logging system designed for production environments and containerized deployments.

### Log Files Generated

- **`g-commerce.log`** - Main application logs with all events
- **`g-commerce-error.log`** - Error-level logs only for quick troubleshooting  
- **`g-commerce-security.log`** - Security events and authentication audit trail
- **`g-commerce-performance.log`** - Performance monitoring and metrics

### Logging Features

- **Automatic Log Rotation**: Prevents disk space issues with time and size-based rotation
- **Log Compression**: Old logs are automatically compressed (.gz format)
- **Async Logging**: Improved application performance through non-blocking log writes
- **Profile-Specific Levels**: Different verbosity for development, testing, and production
- **Container Optimized**: Designed for Docker environments with flexible volume mounting

## 🔧 Environment Variables

### Database Configuration
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | - | Yes |
| `MYSQL_USER` | Database user | - | Yes |
| `MYSQL_PASSWORD` | Database password | - | Yes |
| `MYSQL_HOST` | Database host name and port | localhost:3306 | No |

### Logging Configuration
| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `LOG_PATH` | Directory for log files | `logs` | No |
| `LOG_ARCHIVE` | Directory for archived logs | `logs/archive` | No |
| `LOG_MAX_FILE_SIZE` | Maximum size per log file | `100MB` | No |
| `LOG_MAX_HISTORY` | Days to keep archived logs | `30` | No |
| `LOG_TOTAL_SIZE_CAP` | Total size limit for all logs | `1GB` | No |


## 🗂️ Logging Usage Examples

### Basic Usage (Logs Inside Container)
```bash
# Run with default logging configuration
docker run -p 8080:8080 \
  -e MYSQL_USER=dbuser \
  -e MYSQL_PASSWORD=dbpass \
  gussttaav/g-commerce-thymeleaf:latest
```

### External Log Storage
```bash
# Mount logs directory to host filesystem
docker run -p 8080:8080 \
  -v /host/path/logs:/app/logs \
  -e LOG_PATH=/app/logs \
  -e MYSQL_USER=dbuser \
  -e MYSQL_PASSWORD=dbpass \
  gussttaav/g-commerce-thymeleaf:latest
```

### Custom Log Configuration
```bash
# Configure log rotation and retention
docker run -p 8080:8080 \
  -v ./logs:/app/logs \
  -e LOG_PATH=/app/logs \
  -e LOG_MAX_FILE_SIZE=50MB \
  -e LOG_MAX_HISTORY=15 \
  -e LOG_TOTAL_SIZE_CAP=500MB \
  gussttaav/g-commerce-thymeleaf:latest
```

### Production Docker Compose
```yaml
services:
  mysql:
    image: gussttaav/g-commerce-mysql:latest
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - g-commerce-network
    restart: unless-stopped

  webapp:
    image: gussttaav/g-commerce-thymeleaf:latest
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_PROFILES_ACTIVE: prod
      # Production logging configuration
      LOG_PATH: /app/logs
      LOG_MAX_FILE_SIZE: 100MB
      LOG_MAX_HISTORY: 30
      LOG_TOTAL_SIZE_CAP: 1GB
    ports:
      - "8080:8080"
    volumes:
      # External log storage for monitoring and backup
      - /var/log/g-commerce:/app/logs
    networks:
      - g-commerce-network
    restart: unless-stopped
    # Health check
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  g-commerce-network:
    driver: bridge

volumes:
  mysql_data:
```

## 🔍 Log Monitoring and Troubleshooting

### Accessing Logs

**View logs in real-time:**
```bash
# Main application logs
docker exec -it <container_name> tail -f /app/logs/g-commerce.log

# Error logs only
docker exec -it <container_name> tail -f /app/logs/g-commerce-error.log

# Security events
docker exec -it <container_name> tail -f /app/logs/g-commerce-security.log
```

**With mounted volumes:**
```bash
# View from host filesystem
tail -f ./logs/g-commerce.log
tail -f ./logs/g-commerce-error.log
```

### Log Analysis

The logs include structured information for easy parsing:
- **Timestamp**: ISO format with milliseconds
- **Log Level**: DEBUG, INFO, WARN, ERROR
- **Thread**: Execution thread name
- **Logger**: Java class name
- **Message**: Actual log content

Example log entry:
```
2024-06-03 10:15:30.123 INFO  [http-nio-8080-exec-1] c.g.commerce.controllers.ProductController - User admin accessed product catalog
```

## 📦 Docker Image Details

- **Base Image**: `eclipse-temurin:21-jdk`
- **Exposed Port**: 8080
- **Working Directory**: `/workspace/app`
- **Entry Point**: `java -jar app.jar`
- **Log Directory**: `/workspace/app/logs` (default)
- **Health Check**: Available at `/actuator/health`


## 🛠️ Technical Stack

- **Backend**: Spring Boot 3.4.4, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTMX, Bootstrap 5
- **Database**: MySQL 9 (separate container)
- **Logging**: Logback with custom configuration
- **Monitoring**: Spring Boot Actuator endpoints

## 🔄 Related Images

- **Database Image**: `gussttaav/g-commerce-mysql:latest`
  - Contains pre-configured MySQL schema for G-Commerce
  - Recommended for use with this application

## 🚀 Performance Considerations

### Logging Performance
- **Async Appenders**: Non-blocking log writes prevent application slowdown
- **Log Compression**: Automatic compression saves disk space
- **Selective Logging**: Different levels per environment (DEBUG in dev, INFO in prod)

### Container Resources
- **Memory**: Recommended minimum 512MB RAM
- **Storage**: Plan for log growth (default 1GB total cap)
- **CPU**: Async logging reduces CPU overhead

## 📝 License

This project is licensed under the MIT License.

## 🔗 Links

- **Source Code**: [GitHub Repository](https://github.com/gussttaav/g-commerce-thymeleaf)
- **Issues**: [GitHub Issues](https://github.com/gussttaav/g-commerce-thymeleaf/issues)
- **Live Demo**: [G-Commerce Demo on Render](https://g-commerce-thymeleaf-latest.onrender.com/)