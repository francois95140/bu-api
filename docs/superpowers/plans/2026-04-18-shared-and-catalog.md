# Shared Layer + Module Catalog — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implémenter la couche shared transversale et le module `catalog` (Work + Copy) en architecture hexagonale, avec CRUD complet exposé via REST.

**Architecture:** Architecture hexagonale (Ports & Adapters) — domain/ et application/ sans Spring ni JPA infrastructure. Les Handlers portent @Service/@Transactional. Les gateways sont des interfaces dans application/, implémentées par des adapters JPA dans infrastructure/.

**Tech Stack:** Java 25, Spring Boot 4.0.3, Spring Data JPA, PostgreSQL 16, Lombok, Jakarta Bean Validation, SpringDoc OpenAPI 2.7, Spring Boot Actuator, JUnit 5, Mockito.

---

## File Map

```
pom.xml                                                        (modifié)
compose.yaml                                                   (corrigé — DB name)
src/main/resources/application.yml                            (enrichi)
src/main/resources/logback-spring.xml                         (créé)
src/main/resources/ValidationMessages.properties              (créé)

fr/esgi/bibliotheque/
├── shared/
│   ├── DomainIdGenerator.java
│   ├── UUIDGenerator.java
│   ├── TimeProvider.java
│   ├── SystemUtcTimeProvider.java
│   ├── TimeConfig.java
│   ├── config/OpenApiConfig.java
│   ├── error/
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── ErrorResponse.java
│   │   ├── ValidationError.java
│   │   └── GlobalExceptionHandler.java
│   ├── logging/RequestLoggingFilter.java
│   └── validation/
│       ├── ValidIsbn.java
│       ├── IsbnValidator.java
│       ├── ValidBarcode.java
│       └── BarcodeValidator.java
│
└── catalog/
    ├── domain/
    │   ├── WorkId.java
    │   ├── CopyId.java
    │   ├── CopyStatus.java
    │   ├── Work.java
    │   └── Copy.java
    ├── application/
    │   ├── gateways/
    │   │   ├── WorkRepository.java
    │   │   └── CopyRepository.java
    │   ├── models/
    │   │   ├── CreateWorkRequest.java
    │   │   ├── UpdateWorkRequest.java
    │   │   ├── AddCopyRequest.java
    │   │   ├── UpdateCopyStatusRequest.java
    │   │   └── WorkFilters.java
    │   ├── usecases/
    │   │   ├── CreateWork.java
    │   │   ├── UpdateWork.java
    │   │   ├── SearchWorkById.java
    │   │   ├── SearchWorkByIdWithCopies.java
    │   │   ├── SearchWorks.java
    │   │   ├── DeleteWork.java
    │   │   ├── AddCopy.java
    │   │   └── UpdateCopyStatus.java
    │   └── services/
    │       ├── CreateWorkHandler.java
    │       ├── UpdateWorkHandler.java
    │       ├── SearchWorkByIdHandler.java
    │       ├── SearchWorkByIdWithCopiesHandler.java
    │       ├── SearchWorksHandler.java
    │       ├── DeleteWorkHandler.java
    │       ├── AddCopyHandler.java
    │       └── UpdateCopyStatusHandler.java
    └── infrastructure/
        ├── persistence/
        │   ├── SpringJpaWorkRepository.java
        │   ├── JpaWorkRepository.java
        │   ├── SpringJpaCopyRepository.java
        │   ├── JpaCopyRepository.java
        │   ├── WorkSpecifications.java
        │   └── WorkSpecificationBuilder.java
        └── rest/
            ├── dto/
            │   ├── WorkSummaryDto.java
            │   ├── WorkDetailDto.java
            │   └── CopyDto.java
            ├── mapper/
            │   └── WorkMapper.java
            └── WorkController.java

src/test/java/fr/esgi/bibliotheque/
└── catalog/
    └── application/services/
        ├── CreateWorkHandlerTest.java
        └── AddCopyHandlerTest.java
```

---

## Task 1 — Dépendances Maven et configuration

**Files:**
- Modify: `pom.xml`
- Modify: `compose.yaml`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/resources/logback-spring.xml`
- Create: `src/main/resources/ValidationMessages.properties`

- [ ] **Step 1.1 — Corriger compose.yaml (DB name mismatch)**

Remplacer `POSTGRES_DB: coworking` par `POSTGRES_DB: bibliotheque` :

```yaml
services:
  postgres:
    image: 'postgres:16.9'
    environment:
      - 'POSTGRES_DB=bibliotheque'
      - 'POSTGRES_USER=esgi'
      - 'POSTGRES_PASSWORD=esgi'
    ports:
      - '5432:5432'
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

- [ ] **Step 1.2 — Ajouter les dépendances manquantes dans pom.xml**

Remplacer le bloc `<dependencies>` existant par :

```xml
<dependencies>
    <!-- Spring Boot starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- OpenAPI / Swagger UI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.7.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- PostgreSQL driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Tests -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Et mettre à jour le plugin Maven pour exclure Lombok du JAR :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

- [ ] **Step 1.3 — Enrichir application.yml**

```yaml
spring:
  application:
    name: bibliotheque

  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/bibliotheque
    username: esgi
    password: esgi

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        show_sql: false

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: alpha
    try-it-out-enabled: true
  api-docs:
    path: /api-docs

management:
  endpoints:
    web:
      exposure:
        include: health,info,loggers
```

- [ ] **Step 1.4 — Créer logback-spring.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="logs"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/bibliotheque.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/bibliotheque.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="fr.esgi.bibliotheque" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
    </springProfile>

    <springProfile name="!dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

- [ ] **Step 1.5 — Créer ValidationMessages.properties**

```properties
# Work
ValidIsbn.message=L''ISBN fourni n''est pas valide (format ISBN-10 ou ISBN-13 attendu)
ValidBarcode.message=Le code-barres fourni n''est pas valide

# Champs communs
NotBlank.message=Ce champ est obligatoire
Size.message=Ce champ doit contenir entre {min} et {max} caractères
NotNull.message=Ce champ ne peut pas être null
Positive.message=La valeur doit être positive
```

- [ ] **Step 1.6 — Vérifier que le projet compile**

```bash
cd /Users/aymeric/Downloads/bu-api && ./mvnw compile -q
```

Résultat attendu : `BUILD SUCCESS` sans erreur.

- [ ] **Step 1.7 — Commit**

```bash
git add pom.xml compose.yaml src/main/resources/
git commit -m "chore: add missing dependencies and fix configuration"
```

---

## Task 2 — Shared : utilitaires d'identité et de temps

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/shared/DomainIdGenerator.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/UUIDGenerator.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/TimeProvider.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/SystemUtcTimeProvider.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/TimeConfig.java`

- [ ] **Step 2.1 — DomainIdGenerator**

```java
package fr.esgi.bibliotheque.shared;

public interface DomainIdGenerator {
    String generate();
}
```

- [ ] **Step 2.2 — UUIDGenerator**

```java
package fr.esgi.bibliotheque.shared;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UUIDGenerator implements DomainIdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
```

- [ ] **Step 2.3 — TimeProvider**

```java
package fr.esgi.bibliotheque.shared;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}
```

- [ ] **Step 2.4 — SystemUtcTimeProvider**

```java
package fr.esgi.bibliotheque.shared;

import java.time.Clock;
import java.time.Instant;

public class SystemUtcTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemUtcTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}
```

- [ ] **Step 2.5 — TimeConfig**

```java
package fr.esgi.bibliotheque.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TimeProvider timeProvider(Clock clock) {
        return new SystemUtcTimeProvider(clock);
    }
}
```

- [ ] **Step 2.6 — Compiler**

```bash
./mvnw compile -q
```

Résultat attendu : `BUILD SUCCESS`.

- [ ] **Step 2.7 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/shared/
git commit -m "feat(shared): add DomainIdGenerator, TimeProvider and TimeConfig"
```

---

## Task 3 — Shared : gestion d'erreurs centralisée

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/shared/error/BusinessException.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/error/ResourceNotFoundException.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/error/ErrorResponse.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/error/ValidationError.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/error/GlobalExceptionHandler.java`

- [ ] **Step 3.1 — BusinessException**

```java
package fr.esgi.bibliotheque.shared.error;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3.2 — ResourceNotFoundException**

```java
package fr.esgi.bibliotheque.shared.error;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3.3 — ValidationError**

```java
package fr.esgi.bibliotheque.shared.error;

public record ValidationError(String field, Object rejectedValue, String message) {}
```

- [ ] **Step 3.4 — ErrorResponse**

```java
package fr.esgi.bibliotheque.shared.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> errors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> errors) {
        this(Instant.now(), status, error, message, path, errors);
    }
}
```

- [ ] **Step 3.5 — GlobalExceptionHandler**

```java
package fr.esgi.bibliotheque.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
            .toList();
        log.warn("Validation failed on {}: {}", req.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(
            new ErrorResponse(400, "Bad Request", "Erreur de validation", req.getRequestURI(), errors)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found on {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ErrorResponse(404, "Not Found", ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest req) {
        log.warn("Business rule violation on {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ErrorResponse(409, "Conflict", ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error on {}", req.getRequestURI(), ex);
        return ResponseEntity.internalServerError().body(
            new ErrorResponse(500, "Internal Server Error", "Une erreur inattendue s'est produite", req.getRequestURI())
        );
    }
}
```

- [ ] **Step 3.6 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 3.7 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/shared/error/
git commit -m "feat(shared): add centralized error handling"
```

---

## Task 4 — Shared : RequestLoggingFilter et validateurs custom

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/shared/logging/RequestLoggingFilter.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/validation/ValidIsbn.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/validation/IsbnValidator.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/validation/ValidBarcode.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/validation/BarcodeValidator.java`
- Create: `src/main/java/fr/esgi/bibliotheque/shared/config/OpenApiConfig.java`

- [ ] **Step 4.1 — RequestLoggingFilter**

```java
package fr.esgi.bibliotheque.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_TRACE_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        long start = System.currentTimeMillis();
        log.info("→ {} {}", request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("← {} {} {}ms", request.getMethod(), request.getRequestURI(),
                System.currentTimeMillis() - start);
            MDC.remove(MDC_TRACE_KEY);
        }
    }
}
```

- [ ] **Step 4.2 — ValidIsbn annotation**

```java
package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIsbn {
    String message() default "{ValidIsbn.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

- [ ] **Step 4.3 — IsbnValidator**

Valide ISBN-10 (9 chiffres + chiffre/X) et ISBN-13 (13 chiffres, vérification Luhn) :

```java
package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {

    private static final String ISBN_10 = "\\d{9}[\\dX]";
    private static final String ISBN_13 = "\\d{13}";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true; // @NotBlank gère le null
        String clean = value.replace("-", "").replace(" ", "");
        return clean.matches(ISBN_10) || (clean.matches(ISBN_13) && isIsbn13Valid(clean));
    }

    private boolean isIsbn13Valid(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (isbn.charAt(12) - '0');
    }
}
```

- [ ] **Step 4.4 — ValidBarcode annotation**

```java
package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BarcodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBarcode {
    String message() default "{ValidBarcode.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

- [ ] **Step 4.5 — BarcodeValidator**

Format attendu : lettres majuscules + chiffres, 8–20 caractères :

```java
package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BarcodeValidator implements ConstraintValidator<ValidBarcode, String> {

    private static final String BARCODE_PATTERN = "[A-Z0-9]{8,20}";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        return value.matches(BARCODE_PATTERN);
    }
}
```

- [ ] **Step 4.6 — OpenApiConfig**

```java
package fr.esgi.bibliotheque.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliothequeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Bibliothèque Universitaire API")
                .description("API REST de gestion de bibliothèque universitaire")
                .version("v1.0.0")
                .contact(new Contact().name("ESGI")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Développement")
            ));
    }
}
```

- [ ] **Step 4.7 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 4.8 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/shared/
git commit -m "feat(shared): add RequestLoggingFilter, validators and OpenApiConfig"
```

---

## Task 5 — Catalog : Domain (Work, Copy)

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/domain/WorkId.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/domain/CopyId.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/domain/CopyStatus.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/domain/Work.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/domain/Copy.java`

- [ ] **Step 5.1 — WorkId (Value Object)**

```java
package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record WorkId(@Column(name = "id", updatable = false) String value) {}
```

- [ ] **Step 5.2 — CopyId (Value Object)**

```java
package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CopyId(@Column(name = "id", updatable = false) String value) {}
```

- [ ] **Step 5.3 — CopyStatus**

```java
package fr.esgi.bibliotheque.catalog.domain;

public enum CopyStatus {
    AVAILABLE,
    ON_LOAN,
    RESERVED,
    IN_TRANSIT,
    IN_PROCESSING,
    LOST,
    DAMAGED,
    WITHDRAWN
}
```

- [ ] **Step 5.4 — Work (Entité)**

```java
package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "works")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_seq")
    @SequenceGenerator(name = "work_seq", sequenceName = "work_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private WorkId id;

    private String isbn;
    private String title;
    private String authors;
    private String publisher;
    private Integer year;
    private String subject;
    private String language;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copy> copies = new ArrayList<>();

    public static Work create(WorkId id, String isbn, String title, String authors,
                               String publisher, Integer year, String subject,
                               String language, String description) {
        return Work.builder()
            .id(id)
            .isbn(isbn)
            .title(title)
            .authors(authors)
            .publisher(publisher)
            .year(year)
            .subject(subject)
            .language(language)
            .description(description)
            .build();
    }

    public void update(String title, String authors, String publisher,
                        Integer year, String subject, String language, String description) {
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.year = year;
        this.subject = subject;
        this.language = language;
        this.description = description;
    }
}
```

- [ ] **Step 5.5 — Copy (Entité)**

```java
package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "copies")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Copy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "copy_seq")
    @SequenceGenerator(name = "copy_seq", sequenceName = "copy_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private CopyId id;

    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_technical_id")
    private Work work;

    @Enumerated(EnumType.STRING)
    private CopyStatus status;

    private String campusId;
    private String shelf;
    private String condition;

    public static Copy create(CopyId id, String barcode, Work work,
                               String campusId, String shelf, String condition) {
        return Copy.builder()
            .id(id)
            .barcode(barcode)
            .work(work)
            .status(CopyStatus.AVAILABLE)
            .campusId(campusId)
            .shelf(shelf)
            .condition(condition)
            .build();
    }

    public void updateStatus(CopyStatus newStatus) {
        this.status = newStatus;
    }
}
```

- [ ] **Step 5.6 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 5.7 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/catalog/domain/
git commit -m "feat(catalog): add domain entities Work and Copy with Value Objects"
```

---

## Task 6 — Catalog : Application layer (gateways, models, usecases)

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/gateways/WorkRepository.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/gateways/CopyRepository.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/models/CreateWorkRequest.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/models/UpdateWorkRequest.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/models/AddCopyRequest.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/models/UpdateCopyStatusRequest.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/models/WorkFilters.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/usecases/*.java` (8 interfaces)

- [ ] **Step 6.1 — WorkRepository (gateway interface)**

```java
package fr.esgi.bibliotheque.catalog.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;

import java.util.List;
import java.util.Optional;

public interface WorkRepository {
    Work save(Work work);
    Optional<Work> findById(WorkId id);
    Optional<Work> findByIdWithCopies(WorkId id);
    List<Work> findAll(WorkFilters filters);
    void deleteById(WorkId id);
    boolean existsByIsbn(String isbn);
}
```

- [ ] **Step 6.2 — CopyRepository (gateway interface)**

```java
package fr.esgi.bibliotheque.catalog.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;

import java.util.Optional;

public interface CopyRepository {
    Copy save(Copy copy);
    Optional<Copy> findById(CopyId id);
    boolean existsByBarcode(String barcode);
}
```

- [ ] **Step 6.3 — WorkFilters**

```java
package fr.esgi.bibliotheque.catalog.application.models;

public record WorkFilters(String title, String isbn, String authors, String subject) {
    public static WorkFilters empty() {
        return new WorkFilters(null, null, null, null);
    }
}
```

- [ ] **Step 6.4 — CreateWorkRequest**

```java
package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.shared.validation.ValidIsbn;
import jakarta.validation.constraints.*;

public record CreateWorkRequest(
    @NotBlank(message = "L'ISBN est obligatoire")
    @ValidIsbn
    String isbn,

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 255)
    String title,

    @NotBlank(message = "Les auteurs sont obligatoires")
    @Size(max = 500)
    String authors,

    @Size(max = 255)
    String publisher,

    @Min(value = 1000, message = "L'année doit être valide")
    @Max(value = 2100)
    Integer year,

    @Size(max = 255)
    String subject,

    @Size(max = 10)
    String language,

    String description
) {}
```

- [ ] **Step 6.5 — UpdateWorkRequest**

```java
package fr.esgi.bibliotheque.catalog.application.models;

import jakarta.validation.constraints.*;

public record UpdateWorkRequest(
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 255)
    String title,

    @NotBlank(message = "Les auteurs sont obligatoires")
    @Size(max = 500)
    String authors,

    @Size(max = 255)
    String publisher,

    @Min(value = 1000) @Max(value = 2100)
    Integer year,

    @Size(max = 255)
    String subject,

    @Size(max = 10)
    String language,

    String description
) {}
```

- [ ] **Step 6.6 — AddCopyRequest**

```java
package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.shared.validation.ValidBarcode;
import jakarta.validation.constraints.*;

public record AddCopyRequest(
    @NotBlank(message = "Le code-barres est obligatoire")
    @ValidBarcode
    String barcode,

    @NotBlank(message = "Le campus est obligatoire")
    String campusId,

    @Size(max = 50)
    String shelf,

    @Size(max = 100)
    String condition
) {}
```

- [ ] **Step 6.7 — UpdateCopyStatusRequest**

```java
package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCopyStatusRequest(
    @NotNull(message = "Le statut est obligatoire")
    CopyStatus status
) {}
```

- [ ] **Step 6.8 — Usecase interfaces (toutes en une passe)**

```java
// CreateWork.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface CreateWork {
    WorkId handle(CreateWorkRequest request);
}

// UpdateWork.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.UpdateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface UpdateWork {
    void handle(WorkId id, UpdateWorkRequest request);
}

// SearchWorkById.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import java.util.Optional;
public interface SearchWorkById {
    Optional<Work> handle(WorkId id);
}

// SearchWorkByIdWithCopies.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import java.util.Optional;
public interface SearchWorkByIdWithCopies {
    Optional<Work> handle(WorkId id);
}

// SearchWorks.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import java.util.List;
public interface SearchWorks {
    List<Work> handle(WorkFilters filters);
}

// DeleteWork.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface DeleteWork {
    void handle(WorkId id);
}

// AddCopy.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface AddCopy {
    CopyId handle(WorkId workId, AddCopyRequest request);
}

// UpdateCopyStatus.java
package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.UpdateCopyStatusRequest;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
public interface UpdateCopyStatus {
    void handle(CopyId id, UpdateCopyStatusRequest request);
}
```

- [ ] **Step 6.9 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 6.10 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/catalog/application/
git commit -m "feat(catalog): add application layer — gateways, models and usecases"
```

---

## Task 7 — Catalog : Handlers (application/services)

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/CreateWorkHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/UpdateWorkHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/SearchWorkByIdHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/SearchWorkByIdWithCopiesHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/SearchWorksHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/DeleteWorkHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/AddCopyHandler.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/application/services/UpdateCopyStatusHandler.java`

- [ ] **Step 7.1 — CreateWorkHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.CreateWork;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateWorkHandler implements CreateWork {

    private static final Logger log = LoggerFactory.getLogger(CreateWorkHandler.class);

    private final WorkRepository workRepository;
    private final DomainIdGenerator idGenerator;

    public CreateWorkHandler(WorkRepository workRepository, DomainIdGenerator idGenerator) {
        this.workRepository = workRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public WorkId handle(CreateWorkRequest request) {
        if (workRepository.existsByIsbn(request.isbn())) {
            throw new BusinessException("Un ouvrage avec l'ISBN " + request.isbn() + " existe déjà");
        }
        WorkId id = new WorkId(idGenerator.generate());
        Work work = Work.create(id, request.isbn(), request.title(), request.authors(),
            request.publisher(), request.year(), request.subject(),
            request.language(), request.description());
        workRepository.save(work);
        log.info("Work created with id={}", id.value());
        return id;
    }
}
```

- [ ] **Step 7.2 — UpdateWorkHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.UpdateWorkRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.UpdateWork;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateWorkHandler implements UpdateWork {

    private final WorkRepository workRepository;

    public UpdateWorkHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public void handle(WorkId id, UpdateWorkRequest request) {
        var work = workRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + id.value()));
        work.update(request.title(), request.authors(), request.publisher(),
            request.year(), request.subject(), request.language(), request.description());
        workRepository.save(work);
    }
}
```

- [ ] **Step 7.3 — SearchWorkByIdHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.usecases.SearchWorkById;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SearchWorkByIdHandler implements SearchWorkById {

    private final WorkRepository workRepository;

    public SearchWorkByIdHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public Optional<Work> handle(WorkId id) {
        return workRepository.findById(id);
    }
}
```

- [ ] **Step 7.4 — SearchWorkByIdWithCopiesHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.usecases.SearchWorkByIdWithCopies;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SearchWorkByIdWithCopiesHandler implements SearchWorkByIdWithCopies {

    private final WorkRepository workRepository;

    public SearchWorkByIdWithCopiesHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public Optional<Work> handle(WorkId id) {
        return workRepository.findByIdWithCopies(id);
    }
}
```

- [ ] **Step 7.5 — SearchWorksHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.application.usecases.SearchWorks;
import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchWorksHandler implements SearchWorks {

    private final WorkRepository workRepository;

    public SearchWorksHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public List<Work> handle(WorkFilters filters) {
        return workRepository.findAll(filters);
    }
}
```

- [ ] **Step 7.6 — DeleteWorkHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.usecases.DeleteWork;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteWorkHandler implements DeleteWork {

    private final WorkRepository workRepository;

    public DeleteWorkHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public void handle(WorkId id) {
        if (workRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Ouvrage introuvable : " + id.value());
        }
        workRepository.deleteById(id);
    }
}
```

- [ ] **Step 7.7 — AddCopyHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.AddCopy;
import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddCopyHandler implements AddCopy {

    private static final Logger log = LoggerFactory.getLogger(AddCopyHandler.class);

    private final WorkRepository workRepository;
    private final CopyRepository copyRepository;
    private final DomainIdGenerator idGenerator;

    public AddCopyHandler(WorkRepository workRepository, CopyRepository copyRepository,
                           DomainIdGenerator idGenerator) {
        this.workRepository = workRepository;
        this.copyRepository = copyRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public CopyId handle(WorkId workId, AddCopyRequest request) {
        var work = workRepository.findById(workId)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + workId.value()));
        if (copyRepository.existsByBarcode(request.barcode())) {
            throw new BusinessException("Un exemplaire avec le code-barres " + request.barcode() + " existe déjà");
        }
        CopyId id = new CopyId(idGenerator.generate());
        Copy copy = Copy.create(id, request.barcode(), work, request.campusId(),
            request.shelf(), request.condition());
        copyRepository.save(copy);
        log.info("Copy created with id={} for workId={}", id.value(), workId.value());
        return id;
    }
}
```

- [ ] **Step 7.8 — UpdateCopyStatusHandler**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.models.UpdateCopyStatusRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.UpdateCopyStatus;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCopyStatusHandler implements UpdateCopyStatus {

    private final CopyRepository copyRepository;

    public UpdateCopyStatusHandler(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @Override
    public void handle(CopyId id, UpdateCopyStatusRequest request) {
        var copy = copyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exemplaire introuvable : " + id.value()));
        copy.updateStatus(request.status());
        copyRepository.save(copy);
    }
}
```

- [ ] **Step 7.9 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 7.10 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/catalog/application/services/
git commit -m "feat(catalog): add application handlers (CreateWork, AddCopy, Search, Update, Delete)"
```

---

## Task 8 — Tests unitaires des Handlers

**Files:**
- Create: `src/test/java/fr/esgi/bibliotheque/catalog/application/services/CreateWorkHandlerTest.java`
- Create: `src/test/java/fr/esgi/bibliotheque/catalog/application/services/AddCopyHandlerTest.java`

- [ ] **Step 8.1 — Écrire CreateWorkHandlerTest**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWorkHandlerTest {

    @Mock WorkRepository workRepository;
    @Mock DomainIdGenerator idGenerator;

    CreateWorkHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateWorkHandler(workRepository, idGenerator);
    }

    @Test
    void shouldCreateWorkAndReturnId() {
        when(workRepository.existsByIsbn("9782070360024")).thenReturn(false);
        when(idGenerator.generate()).thenReturn("uuid-123");
        when(workRepository.save(any(Work.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateWorkRequest("9782070360024", "Les Misérables", "Victor Hugo",
            "Gallimard", 1862, "Roman", "fr", null);

        WorkId result = handler.handle(request);

        assertThat(result.value()).isEqualTo("uuid-123");
        verify(workRepository).save(any(Work.class));
    }

    @Test
    void shouldThrowWhenIsbnAlreadyExists() {
        when(workRepository.existsByIsbn("9782070360024")).thenReturn(true);

        var request = new CreateWorkRequest("9782070360024", "Les Misérables", "Victor Hugo",
            null, null, null, null, null);

        assertThatThrownBy(() -> handler.handle(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("9782070360024");

        verify(workRepository, never()).save(any());
    }
}
```

- [ ] **Step 8.2 — Lancer le test (il doit passer)**

```bash
./mvnw test -pl . -Dtest=CreateWorkHandlerTest -q
```

Résultat attendu : `Tests run: 2, Failures: 0, Errors: 0`.

- [ ] **Step 8.3 — Écrire AddCopyHandlerTest**

```java
package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.domain.*;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCopyHandlerTest {

    @Mock WorkRepository workRepository;
    @Mock CopyRepository copyRepository;
    @Mock DomainIdGenerator idGenerator;

    AddCopyHandler handler;

    Work existingWork;

    @BeforeEach
    void setUp() {
        handler = new AddCopyHandler(workRepository, copyRepository, idGenerator);
        existingWork = Work.create(new WorkId("work-1"), "9782070360024", "Les Misérables",
            "Victor Hugo", "Gallimard", 1862, "Roman", "fr", null);
    }

    @Test
    void shouldAddCopyToExistingWork() {
        when(workRepository.findById(new WorkId("work-1"))).thenReturn(Optional.of(existingWork));
        when(copyRepository.existsByBarcode("BC12345678")).thenReturn(false);
        when(idGenerator.generate()).thenReturn("copy-uuid-1");
        when(copyRepository.save(any(Copy.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AddCopyRequest("BC12345678", "campus-a", "A1", "Bon état");
        CopyId result = handler.handle(new WorkId("work-1"), request);

        assertThat(result.value()).isEqualTo("copy-uuid-1");
        verify(copyRepository).save(any(Copy.class));
    }

    @Test
    void shouldThrowWhenWorkNotFound() {
        when(workRepository.findById(new WorkId("unknown"))).thenReturn(Optional.empty());

        var request = new AddCopyRequest("BC12345678", "campus-a", null, null);

        assertThatThrownBy(() -> handler.handle(new WorkId("unknown"), request))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(copyRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExists() {
        when(workRepository.findById(new WorkId("work-1"))).thenReturn(Optional.of(existingWork));
        when(copyRepository.existsByBarcode("BC12345678")).thenReturn(true);

        var request = new AddCopyRequest("BC12345678", "campus-a", null, null);

        assertThatThrownBy(() -> handler.handle(new WorkId("work-1"), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("BC12345678");
    }
}
```

- [ ] **Step 8.4 — Lancer les tests**

```bash
./mvnw test -pl . -Dtest=AddCopyHandlerTest -q
```

Résultat attendu : `Tests run: 3, Failures: 0, Errors: 0`.

- [ ] **Step 8.5 — Commit**

```bash
git add src/test/
git commit -m "test(catalog): add unit tests for CreateWorkHandler and AddCopyHandler"
```

---

## Task 9 — Catalog : Infrastructure persistence

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/WorkSpecifications.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/WorkSpecificationBuilder.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/SpringJpaWorkRepository.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/JpaWorkRepository.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/SpringJpaCopyRepository.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/JpaCopyRepository.java`

- [ ] **Step 9.1 — WorkSpecifications**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.data.jpa.domain.Specification;

public final class WorkSpecifications {

    private WorkSpecifications() {}

    public static Specification<Work> titleLike(String title) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Work> isbnEquals(String isbn) {
        return (root, query, cb) ->
            cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Work> authorsLike(String authors) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("authors")), "%" + authors.toLowerCase() + "%");
    }

    public static Specification<Work> subjectEquals(String subject) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("subject")), "%" + subject.toLowerCase() + "%");
    }
}
```

- [ ] **Step 9.2 — WorkSpecificationBuilder**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.data.jpa.domain.Specification;

public class WorkSpecificationBuilder {

    public Specification<Work> build(WorkFilters filters) {
        Specification<Work> spec = Specification.where(null);
        if (filters.title() != null && !filters.title().isBlank()) {
            spec = spec.and(WorkSpecifications.titleLike(filters.title()));
        }
        if (filters.isbn() != null && !filters.isbn().isBlank()) {
            spec = spec.and(WorkSpecifications.isbnEquals(filters.isbn()));
        }
        if (filters.authors() != null && !filters.authors().isBlank()) {
            spec = spec.and(WorkSpecifications.authorsLike(filters.authors()));
        }
        if (filters.subject() != null && !filters.subject().isBlank()) {
            spec = spec.and(WorkSpecifications.subjectEquals(filters.subject()));
        }
        return spec;
    }
}
```

- [ ] **Step 9.3 — SpringJpaWorkRepository**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SpringJpaWorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {

    Optional<Work> findByIdValue(String value);

    @Query("SELECT DISTINCT w FROM Work w LEFT JOIN FETCH w.copies WHERE w.id.value = :value")
    Optional<Work> findByIdValueWithCopies(@Param("value") String value);

    boolean existsByIdValue(String value);

    boolean existsByIsbn(String isbn);
}
```

- [ ] **Step 9.4 — JpaWorkRepository (adapter)**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaWorkRepository implements WorkRepository {

    private final SpringJpaWorkRepository spring;
    private final WorkSpecificationBuilder specBuilder;

    public JpaWorkRepository(SpringJpaWorkRepository spring) {
        this.spring = spring;
        this.specBuilder = new WorkSpecificationBuilder();
    }

    @Override
    public Work save(Work work) {
        return spring.save(work);
    }

    @Override
    public Optional<Work> findById(WorkId id) {
        return spring.findByIdValue(id.value());
    }

    @Override
    public Optional<Work> findByIdWithCopies(WorkId id) {
        return spring.findByIdValueWithCopies(id.value());
    }

    @Override
    public List<Work> findAll(WorkFilters filters) {
        var spec = specBuilder.build(filters);
        return spring.findAll(spec, PageRequest.of(0, 1000, Sort.by("title").ascending())).getContent();
    }

    @Override
    public void deleteById(WorkId id) {
        spring.findByIdValue(id.value()).ifPresent(spring::delete);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return spring.existsByIsbn(isbn);
    }
}
```

- [ ] **Step 9.5 — SpringJpaCopyRepository**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringJpaCopyRepository extends JpaRepository<Copy, Long> {

    Optional<Copy> findByIdValue(String value);

    boolean existsByBarcode(String barcode);
}
```

- [ ] **Step 9.6 — JpaCopyRepository (adapter)**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCopyRepository implements CopyRepository {

    private final SpringJpaCopyRepository spring;

    public JpaCopyRepository(SpringJpaCopyRepository spring) {
        this.spring = spring;
    }

    @Override
    public Copy save(Copy copy) {
        return spring.save(copy);
    }

    @Override
    public Optional<Copy> findById(CopyId id) {
        return spring.findByIdValue(id.value());
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return spring.existsByBarcode(barcode);
    }
}
```

- [ ] **Step 9.7 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 9.8 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/persistence/
git commit -m "feat(catalog): add JPA persistence adapters for Work and Copy"
```

---

## Task 10 — Catalog : Infrastructure REST (DTOs, Mapper, Controller)

**Files:**
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/dto/WorkSummaryDto.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/dto/WorkDetailDto.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/dto/CopyDto.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/mapper/WorkMapper.java`
- Create: `src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/WorkController.java`

- [ ] **Step 10.1 — CopyDto**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;

public record CopyDto(
    String id,
    String barcode,
    CopyStatus status,
    String campusId,
    String shelf,
    String condition
) {
    public static CopyDto from(Copy copy) {
        return new CopyDto(
            copy.getId().value(),
            copy.getBarcode(),
            copy.getStatus(),
            copy.getCampusId(),
            copy.getShelf(),
            copy.getCondition()
        );
    }
}
```

- [ ] **Step 10.2 — WorkSummaryDto (liste — sans copies)**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Work;

public record WorkSummaryDto(
    String id,
    String isbn,
    String title,
    String authors,
    String publisher,
    Integer year,
    String subject,
    String language
) {
    public static WorkSummaryDto from(Work work) {
        return new WorkSummaryDto(
            work.getId().value(),
            work.getIsbn(),
            work.getTitle(),
            work.getAuthors(),
            work.getPublisher(),
            work.getYear(),
            work.getSubject(),
            work.getLanguage()
        );
    }
}
```

- [ ] **Step 10.3 — WorkDetailDto (détail — avec copies)**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Work;
import java.util.List;

public record WorkDetailDto(
    String id,
    String isbn,
    String title,
    String authors,
    String publisher,
    Integer year,
    String subject,
    String language,
    String description,
    List<CopyDto> copies
) {
    public static WorkDetailDto from(Work work) {
        return new WorkDetailDto(
            work.getId().value(),
            work.getIsbn(),
            work.getTitle(),
            work.getAuthors(),
            work.getPublisher(),
            work.getYear(),
            work.getSubject(),
            work.getLanguage(),
            work.getDescription(),
            work.getCopies().stream().map(CopyDto::from).toList()
        );
    }
}
```

- [ ] **Step 10.4 — WorkMapper**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.rest.mapper;

import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkDetailDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkSummaryDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkMapper {

    public WorkSummaryDto toSummaryDto(Work work) {
        return WorkSummaryDto.from(work);
    }

    public WorkDetailDto toDetailDto(Work work) {
        return WorkDetailDto.from(work);
    }

    public List<WorkSummaryDto> toSummaryDtoList(List<Work> works) {
        return works.stream().map(this::toSummaryDto).toList();
    }
}
```

- [ ] **Step 10.5 — WorkController**

```java
package fr.esgi.bibliotheque.catalog.infrastructure.rest;

import fr.esgi.bibliotheque.catalog.application.models.*;
import fr.esgi.bibliotheque.catalog.application.usecases.*;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkDetailDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkSummaryDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.mapper.WorkMapper;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final CreateWork createWork;
    private final UpdateWork updateWork;
    private final SearchWorkById searchWorkById;
    private final SearchWorkByIdWithCopies searchWorkByIdWithCopies;
    private final SearchWorks searchWorks;
    private final DeleteWork deleteWork;
    private final AddCopy addCopy;
    private final UpdateCopyStatus updateCopyStatus;
    private final WorkMapper mapper;

    public WorkController(CreateWork createWork, UpdateWork updateWork,
                           SearchWorkById searchWorkById,
                           SearchWorkByIdWithCopies searchWorkByIdWithCopies,
                           SearchWorks searchWorks, DeleteWork deleteWork,
                           AddCopy addCopy, UpdateCopyStatus updateCopyStatus,
                           WorkMapper mapper) {
        this.createWork = createWork;
        this.updateWork = updateWork;
        this.searchWorkById = searchWorkById;
        this.searchWorkByIdWithCopies = searchWorkByIdWithCopies;
        this.searchWorks = searchWorks;
        this.deleteWork = deleteWork;
        this.addCopy = addCopy;
        this.updateCopyStatus = updateCopyStatus;
        this.mapper = mapper;
    }

    @GetMapping
    public List<WorkSummaryDto> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String authors,
            @RequestParam(required = false) String subject) {
        return mapper.toSummaryDtoList(searchWorks.handle(new WorkFilters(title, isbn, authors, subject)));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateWorkRequest request,
                                        UriComponentsBuilder ucb) {
        WorkId id = createWork.handle(request);
        var uri = ucb.path("/api/works/{id}").buildAndExpand(id.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public WorkDetailDto getById(@PathVariable String id) {
        return searchWorkByIdWithCopies.handle(new WorkId(id))
            .map(mapper::toDetailDto)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id,
                                        @Valid @RequestBody UpdateWorkRequest request) {
        updateWork.handle(new WorkId(id), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteWork.handle(new WorkId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workId}/copies")
    public ResponseEntity<Void> addCopy(@PathVariable String workId,
                                         @Valid @RequestBody AddCopyRequest request,
                                         UriComponentsBuilder ucb) {
        CopyId copyId = addCopy.handle(new WorkId(workId), request);
        var uri = ucb.path("/api/copies/{id}").buildAndExpand(copyId.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PatchMapping("/copies/{copyId}/status")
    public ResponseEntity<Void> updateCopyStatus(@PathVariable String copyId,
                                                   @Valid @RequestBody UpdateCopyStatusRequest request) {
        updateCopyStatus.handle(new CopyId(copyId), request);
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 10.6 — Compiler**

```bash
./mvnw compile -q
```

- [ ] **Step 10.7 — Lancer tous les tests**

```bash
./mvnw test -q
```

Résultat attendu : `Tests run: X, Failures: 0, Errors: 0`.

- [ ] **Step 10.8 — Commit**

```bash
git add src/main/java/fr/esgi/bibliotheque/catalog/infrastructure/rest/
git commit -m "feat(catalog): add REST layer — DTOs, WorkMapper and WorkController"
```

---

## Task 11 — Vérification finale

- [ ] **Step 11.1 — Vérifier que l'application démarre**

Démarrer PostgreSQL avec Docker Compose puis lancer l'application :

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Résultat attendu : `Started BibliothequeApplication` sans erreur.

- [ ] **Step 11.2 — Vérifier Swagger UI**

Ouvrir `http://localhost:8080/swagger-ui.html` — la documentation de l'API `/api/works` doit apparaître avec tous les endpoints.

- [ ] **Step 11.3 — Test manuel : créer un ouvrage**

```bash
curl -s -X POST http://localhost:8080/api/works \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9782070360024","title":"Les Misérables","authors":"Victor Hugo","publisher":"Gallimard","year":1862,"subject":"Roman","language":"fr"}' \
  -i
```

Résultat attendu : `HTTP/1.1 201 Created` avec `Location: /api/works/{uuid}`.

- [ ] **Step 11.4 — Test manuel : lister les ouvrages**

```bash
curl -s http://localhost:8080/api/works | python3 -m json.tool
```

Résultat attendu : liste JSON avec l'ouvrage créé.

- [ ] **Step 11.5 — Test manuel : ajouter un exemplaire**

```bash
WORK_ID=<id retourné à l'étape 11.3>
curl -s -X POST http://localhost:8080/api/works/$WORK_ID/copies \
  -H "Content-Type: application/json" \
  -d '{"barcode":"BC12345678","campusId":"campus-a","shelf":"A1","condition":"Bon état"}' \
  -i
```

Résultat attendu : `HTTP/1.1 201 Created`.

- [ ] **Step 11.6 — Commit final**

```bash
git add .
git commit -m "feat(catalog): complete catalog module with hexagonal architecture"
```

---

## Récapitulatif des endpoints REST

| Méthode | URL | Description | Réponse |
|---|---|---|---|
| `GET` | `/api/works` | Liste (filtres: title, isbn, authors, subject) | `200 List<WorkSummaryDto>` |
| `POST` | `/api/works` | Créer un ouvrage | `201 + Location` |
| `GET` | `/api/works/{id}` | Détail avec exemplaires | `200 WorkDetailDto` |
| `PUT` | `/api/works/{id}` | Mettre à jour | `200` |
| `DELETE` | `/api/works/{id}` | Supprimer | `204` |
| `POST` | `/api/works/{workId}/copies` | Ajouter un exemplaire | `201 + Location` |
| `PATCH` | `/api/copies/{copyId}/status` | Changer statut d'un exemplaire | `200` |