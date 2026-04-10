🧁 CakeMart Backend

CakeMart Backend is a Spring Boot REST API for an e-commerce cake store.
It provides secure authentication using JWT, PostgreSQL database integration, and stateless API security.

🚀 Tech Stack

Java 17+

Spring Boot

Spring Security

JWT (JSON Web Tokens)

PostgreSQL (Docker)

Gradle

BCrypt Password Encoding

🔐 Features

User Registration

User Login

JWT Token Generation

Stateless Authentication

Protected API Endpoints

PostgreSQL Database Integration

Password Encryption (BCrypt)

📂 Project Structure
com.cakemart.cakemartbackend
│
├── controller      # REST Controllers
├── dto             # Data Transfer Objects
├── model           # JPA Entities
├── repository      # Database Repositories
├── security        # JWT + Security Configuration
└── CakemartBackendApplication.java
⚙️ Setup Instructions
1️⃣ Clone Repository
git clone https://github.com/YOUR_USERNAME/cakemart-backend.git
cd cakemart-backend
2️⃣ Run PostgreSQL with Docker
docker run --name postgres-spring-boot \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_DB=postgres \
-p 5332:5432 \
-d postgres:15

Verify container is running:

docker ps
3️⃣ Configure application.properties
spring.datasource.url=jdbc:postgresql://localhost:5332/postgres
spring.datasource.username=postgres
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

server.port=8080

jwt.secret=MySuperSecretKeyForJwtThatIsAtLeast32CharactersLong12345
jwt.expiration-ms=86400000
4️⃣ Run the Application

Using IntelliJ:

Reload Gradle

Build → Rebuild Project

Run CakemartBackendApplication

App will start at:

http://localhost:8080
📌 API Endpoints
📝 Register

POST /api/auth/register

{
  "name": "Dev",
  "email": "dev@test.com",
  "password": "1234",
  "role": "USER"
}

Response:

Registered
🔑 Login

POST /api/auth/login

{
  "email": "dev@test.com",
  "password": "1234"
}

Response:

{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
🔒 Protected Routes

All routes except:

/api/auth/**

require this header:

Authorization: Bearer <JWT_TOKEN>
🛡️ Authentication Flow

User registers

Password is hashed using BCrypt

User logs in

Backend generates JWT token

Frontend stores token

Token sent in Authorization header for protected requests

JWT filter validates token and authenticates request

🧪 Example Protected Request
GET /api/test
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Response:

You are authenticated ✅
📈 Future Improvements

Product CRUD APIs

Cart Management

Order Processing

Admin Role Authorization

Global Exception Handling

Swagger API Documentation

Deployment (AWS / Render / Railway)

👨‍💻 Author

Dev Patel
Backend Developer – CakeMart
