# Clean Code Refactor Plan

This branch adds a safe first refactor pass without risking a breaking rewrite on `main`.

## What should be changed immediately

### 1) Secrets and environment configuration
- Move database credentials out of `application.properties`
- Move the JWT secret out of `JwtService`
- Use environment variables in local/dev/prod profiles
- Rotate any leaked secrets that were already committed

Recommended configuration:

```properties
spring.application.name=startProject

spring.datasource.url=${DB_URL:jdbc:mysql://127.0.0.1:3306/jomap}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.show-sql=${SHOW_SQL:false}
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=${HIBERNATE_SQL_LOG_LEVEL:WARN}
logging.level.org.hibernate.orm.jdbc.bind=${HIBERNATE_BIND_LOG_LEVEL:OFF}

app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
app.security.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

### 2) JWT service
Replace the hard-coded secret with injected properties and validate configuration on startup.

### 3) JWT filter
- Remove `System.out.println`
- Do not log raw bearer tokens
- Fail safely and continue the filter chain for invalid tokens

### 4) Authentication messages
Use one safe login failure message:
- `Invalid email or password`

Do not reveal whether the email exists.

### 5) Exception handling
Handle at least:
- `BadRequestException`
- `DuplicateResourceException`
- `MethodArgumentNotValidException`
- `AccessDeniedException`
- a generic fallback

### 6) Current user lookup
Create one reusable component/service for authenticated-user lookup instead of repeating `getCurrentUser()` in every service.

### 7) Controllers
- Remove duplicated `@CrossOrigin` annotations when global CORS is already configured
- Do not validate IDs using `findAll().size()`
- Return consistent responses

## High-priority file targets
- `src/main/resources/application.properties`
- `src/main/java/com/start/demo/Services/Auth/JwtService.java`
- `src/main/java/com/start/demo/Config/JwtAuthFilter.java`
- `src/main/java/com/start/demo/Config/SecurityConfig.java`
- `src/main/java/com/start/demo/Services/Auth/AuthServiceImpl.java`
- `src/main/java/com/start/demo/Controllers/UserController.java`
- `src/main/java/com/start/demo/Exciptions/GlobalExceptionHandler.java`

## Suggested next pass
- rename typo-heavy classes/packages such as `Exciptions`, `Implemintation`, `Storyrepo`
- centralize DTO mapping
- replace repeated EntityManager queries with repositories where appropriate
- add tests for auth and post/story flows
