# unione

<div align="center">
  <img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="License" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.0-green" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java" />
  <img src="https://img.shields.io/badge/Spring%20Cloud-4.2.0-blue" alt="Spring Cloud" />
</div>

## Project Introduction
unione cloud is an enterprise-level unified low-code development platform designed to provide one-stop low-code website building services for enterprises. Through visual drag-and-drop and configuration methods, it helps enterprises quickly build various business applications, significantly reducing development costs and technical barriers.

## Features
- **Visual Development**: Drag-and-drop interface design, what you see is what you get
- **Component-based Architecture**: Rich function component library, supporting custom extension
- **Multi-module Design**: Based on microservice architecture, each module is loosely coupled and highly cohesive
- **Security Authentication**: Complete user authentication and permission management system
- **API Management**: Unified interface management and automatic document generation
- **Data Management**: Flexible data model design and management
- **Workflow Support**: Visual workflow design and operation

## Technology Stack
- **Core Framework**: Spring Boot 3.4.0, Spring Cloud 4.2.0
- **Development Language**: Java 21
- **Database**: MySQL 8.0.21
- **Cache**: Redis, JetCache
- **API Documentation**: Swagger 3.0.0, Knife4j 4.5.0
- **Service Registration and Discovery**: Nacos 2023.0.3.2
- **Load Balancing**: Netflix Feign
- **Code Generation**: Custom code generation module
- **Security Framework**: Spring Security, JWT
- **Tool Libraries**: Hutool, Lombok, Jackson

## Project Structure
unione adopts a modular architecture design, with clear responsibilities for each module, facilitating maintenance and expansion:

```
unione/
├── unione-core/         # Core function module, providing basic services and common components
├── unione-beetsql/      # Database access module, based on BeetSQL framework
├── unione-web/          # Web service foundation module, business service extension components
├── unione-gateway/      # API gateway module, unified entrance and routing management
├── unione-job/          # Scheduled task module, managing background scheduled tasks
├── unione-portal/       # Portal module, providing user interface and interaction entrance
├── unione-util/         # Tool class module, providing common utility methods
├── unione-starter/      # Starter module, integrating components and providing startup entrance
└── unione-codegen/      # Code generation module, automatically generating basic code
```

## Installation Tutorial
### Prerequisites
- JDK 21 or higher
- Maven 3.6+ build tool
- MySQL 8.0+ database
- Redis 5.0+ cache service
- Nacos 2023.0.3.2 service registration center

### Steps
1. **Clone the repository from GitHub**
   ```bash
   git clone https://github.com/unione-cloud/unione.git
   cd unione
   ```
1.1. **Clone the repository from Gitee**
   ```bash
   git clone https://gitee.com/unione-cloud/unione.git
   cd unione
   ```

2. **Configure the database**
   - Create database: `CREATE DATABASE unione DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - Configure database connection information (in the configuration files of each module)

3. **Configure Nacos**
   - Start Nacos service
   - Configure service registration information

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Start the service**
   ```bash
   # Start each module in sequence
   cd unione-starter
   mvn spring-boot:run
   ```

## Usage Instructions
1. **Access the management backend**
   - Browser access: `http://localhost:8080/portal`
   - Default username: admin
   - Default password: admin

2. **Create an application**
   - Log in to the management backend
   - Click the "New Application" button
   - Fill in the basic application information
   - Select an application template
   - Click "Create" to complete

3. **Design application interface**
   - Enter the application designer
   - Drag components from the left component library to the canvas
   - Configure component properties and data binding
   - Save and publish the application

4. **API interface calling**
   - Access Swagger documentation: `http://localhost:8080/portal/doc.html`
   - Call the corresponding API according to the interface documentation
   - Note: All interfaces need to carry a valid JWT token

5. **Clone the frontend code from GitHub**
   ```bash
   git clone https://github.com/unione-cloud/unione-admin-vue.git
   cd unione-admin-vue
   npm install
   npm run dev
   ```
5.1. **Clone the frontend code from Gitee**
   ```bash
   git clone https://gitee.com/unione-cloud/unione-admin-vue.git
   cd unione-admin-vue
   npm install
   npm run dev
   ```

## Contribution Guidelines
We welcome community contributions very much! If you want to participate in project development, please follow these steps:

1. **Fork the repository**
   Fork the project to your own account on Gitee

2. **Create a branch**
   ```bash
   git checkout -b Feat_xxx
   ```

3. **Commit your code**
   ```bash
   git add .
   git commit -m "feat: add xxx feature"
   ```

4. **Push the code**
   ```bash
   git push origin Feat_xxx
   ```

5. **Create a Pull Request**
   Submit a Pull Request on Gitee, describing your changes and purpose

## License
This project is open-sourced under the Apache License 2.0. For details, please see the [LICENSE](LICENSE) file.

## Contact Information
- **Project Lead**: Jeking Yang
- **Email**: jeking217@163.com
- **Project Address (GitHub)**: https://github.com/unione-cloud/unione
- **Project Address (Gitee)**: https://gitee.com/unione-cloud/unione
