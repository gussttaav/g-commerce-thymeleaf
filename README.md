# G-Commerce Spring Boot with Thymeleaf

A full-stack e-commerce web application built with **Spring Boot**, **Thymeleaf**, and **HTMX**. This project combines backend services and frontend presentation in a single monolithic application, providing a seamless user experience without the need for a separate frontend application.

This project is based on the original [G-commerce backend api](https://github.com/gussttaav/g-commerce-springboot-api) and [G-commerce frontend](https://github.com/gussttaav/g-commerce-front-web-no-framework) projects, combining them into a unified application.


## 🚀 Features

- **User Management**
  - User registration and authentication with Spring Security
  - Role-based authorization (ADMIN, USER)
  - Profile management and password updates
  - Paginated user listing for administrators

- **Product Management**
  - Product creation, update, delete, and list operations
  - Product status management (active/inactive)
  - Text search across product name and description fields
  - Responsive product grid display

- **Shopping Cart**
  - Client-side cart functionality with JavaScript
  - Add/remove products with real-time updates
  - Cart persistence during session
  - Automatic total calculations

- **Purchase System**
  - Checkout process
  - Purchase history tracking with pagination
  - Role-specific purchase restrictions

- **Responsive UI with Thymeleaf & HTMX**
  - Server-side rendering with Thymeleaf templates
  - HTMX for dynamic content loading without page refreshes
  - Thymeleaf fragments for component reusability
  - Light/dark theme support
  - Bootstrap 5 for responsive layouts

- **Security Features**
  - Spring Security integration
  - Form-Based authentication
  - CSRF protection
  - Input validation
  - User role enforcement

## 🛠️ Technologies

- **Backend**
  - Java 21
  - Spring Boot 3.4.5
  - Spring Security
  - Spring Data JPA
  - MySQL 9
  - H2 Database (for testing)

- **Frontend**
  - Thymeleaf
  - HTMX
  - Bootstrap 5
  - JavaScript (ES6+)
  - CSS3

- **DevOps & Tools**
  - Docker & Docker Compose
  - Maven
  - GitHub Actions for CI/CD
  - Lombok
  - JUnit 5 & Mockito

## 📋 Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven
- Git

## 🔧 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/gussttaav/g-commerce-thymeleaf.git
   cd g-commerce-thymeleaf
   ```

2. Create a `.env` file in the root directory with the following variables:
   ```env
   MYSQL_ROOT_PASSWORD=your_root_password
   MYSQL_USER=your_database_user
   MYSQL_PASSWORD=your_database_password
   ADMIN_EMAIL=admin@example.com
   ADMIN_PASSWORD=your_admin_password
   ```

3. Set up the database using one of these two options:

   **Option A: Using Docker Compose** (Recommended)
   ```bash
   docker-compose -f docker-compose.db.yml up -d
   ```
   This will start the MySQL database container with the pre-configured schema.

   **Option B: Using the SQL script directly**
   - Set up your MySQL database server
   - Run the initialization script ./mysql-init/shopping_db.sql
   - Update the application.yml file with your database connection details

4. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application will be available at `http://localhost:8080`.

## 🐳 Docker Deployment

The application is deployed using two Docker images:

1. **G-Commerce Application** (This repository)
   - Combines Spring Boot backend and Thymeleaf frontend
   - Available at `gussttaav/g-commerce-thymeleaf:latest`

2. **Database** (Reusing existing image)
   - MySQL 9 with pre-configured schema
   - Available at `gussttaav/g-commerce-mysql:latest`


## 📁 Project Structure

The project follows a standard Spring Boot structure with Thymeleaf templates:

```
src/
├── main/
│   ├── java/
│   │   └── com/gplanet/commerce/
│   │       ├── controllers/       # MVC Controllers
│   │       ├── dtos/              # Data Transfer Objects
│   │       ├── entities/          # JPA Entities
│   │       ├── exceptions/        # Custom Exceptions
│   │       ├── repositories/      # Spring Data Repositories
│   │       ├── security/          # Security Configuration
│   │       ├── services/          # Business Logic
│   │       └── utilities/         # Helper Classes
│   └── resources/
│       ├── static/                # Static Assets
│       │   ├── css/              # Stylesheets
│       │   ├── img/              # Images
│       │   └── js/               # JavaScript Files
│       └── templates/             # Thymeleaf Templates
│           ├── compras/          # Purchase-related Templates
│           ├── productos/        # Product-related Templates
│           └── usuarios/         # User-related Templates
├── test/
│   ├── java/
│   │   └── com/gplanet/commerce/
│   │       ├── controllers/       # Controllers Tests
│   │       ├── integration/       # Integration Tests
│   │       ├── repositories/      # Spring Data Repositories Tests
│   │       ├── security/          # Security Configuration Tests
│   │       ├── services/          # JUnit services tests
│   └── resources/
└── docker-compose.db.yml          # Docker Compose for Database
```

## 🔄 Key Differences from Previous Version

This application differs from the previous version in several key ways:

1. **Monolithic Architecture**: Frontend and backend are integrated into a single application, simplifying deployment and development.

2. **Server-Side Rendering**: Uses Thymeleaf templates rendered on the server rather than a separate client-side application.

3. **HTMX Integration**: Leverages HTMX for dynamic content loading without full page refreshes, providing a SPA-like experience.

4. **Template Fragments**: Uses Thymeleaf fragments for component reusability and consistency across pages.

5. **Simplified Deployment**: Requires only two Docker images (application and database) instead of three.

6. **Reduced Network Overhead**: Eliminates API calls between separate frontend and backend services.

## 📡 Frontend Technology

### Thymeleaf Templates

The application uses Thymeleaf for server-side rendering, with templates organized by domain:

- `templates/layout.html`: Main layout template with common elements
- `templates/productos/`: Product-related templates
- `templates/usuarios/`: User-related templates
- `templates/compras/`: Purchase-related templates

### HTMX Integration

HTMX is used to provide a modern, dynamic user experience without the complexity of a full SPA framework:

- Dynamic content loading
- Form submissions without page refreshes
- Partial page updates
- Server-sent events

### CSS and JavaScript

- Bootstrap 5 for responsive layouts
- Custom CSS for theme customization
- Client-side JavaScript cart logic
- Theme switching between light and dark modes

## 🧪 Testing

The application includes comprehensive tests for all layers:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UsuarioControllerTest

# Run tests with coverage report
mvn verify
```

## 🛡️ Security

- Form-based authentication with Spring Security
- Role-based access control (ADMIN, USER)
- CSRF protection for form submissions
- Input validation
- Secure password storage with BCrypt

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.