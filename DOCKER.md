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


## 🔧 Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | - | Yes |
| `MYSQL_USER` | Database user | - | Yes |
| `MYSQL_PASSWORD` | Database password | - | Yes |
| `MYSQL_HOST` | Database host name and port | localhost:3306 | No |


## 📦 Docker Image Details

- Base Image: `eclipse-temurin:21-jdk`
- Exposed Port: 8080
- Working Directory: `/workspace/app`
- Entry Point: `java -jar app.jar`

## 🛠️ Technical Stack

- **Backend**: Spring Boot 3.4.4, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTMX, Bootstrap 5
- **Database**: MySQL 9 (separate container)

## 🔄 Related Images

- **Database Image**: `gussttaav/g-commerce-mysql:latest`
  - Contains pre-configured MySQL schema for G-Commerce
  - Recommended for use with this application

## 📝 License

This project is licensed under the MIT License.

## 🔗 Links

- **Source Code**: [GitHub Repository](https://github.com/gussttaav/g-commerce-thymeleaf)
- **Issues**: [GitHub Issues](https://github.com/gussttaav/g-commerce-thymeleaf/issues)