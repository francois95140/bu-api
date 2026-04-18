# Analyse Architecture Gamoxx — Référence pour bu-api

> Rapport d'analyse du projet `/Users/aymeric/Downloads/gamoxx`
> Utilisé comme modèle de référence pour l'architecture hexagonale de bu-api.

---

## 1. Stack technique

| Élément | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.1 |
| Spring Data JPA / Hibernate | via Boot |
| PostgreSQL | 16.9 |
| SpringDoc OpenAPI | 2.7.0 |
| Jakarta Bean Validation | via Boot |
| Spring Boot Actuator | via Boot |
| Maven | 4.0.1 |

---

## 2. Architecture globale : Hexagonale (Ports & Adapters)

Chaque module métier est découpé en **3 couches strictes** :

```
{module}/
├── domain/           → Entités JPA + Value Objects (IDs)
├── application/
│   ├── models/       → Requêtes entrantes (validation)
│   ├── usecases/     → Interfaces des cas d'usage (ports)
│   ├── services/     → Handlers (implémentations des usecases)
│   └── gateways/     → Interfaces de persistance (ports sortants)
└── infrastructure/
    ├── rest/         → Controllers + DTOs réponse + Mappers
    └── persistence/  → Repositories JPA (adapters sortants)
```

**Règle d'or** : la couche `application/` et `domain/` ne connaissent PAS Spring, JPA, ni aucun détail d'infrastructure.

---

## 3. Modules métier

### 3.1 Module `teams` (Équipes)

**Domain :**
- `Team` — entité JPA, factory `Team.create()`, méthode métier `rename()`
- `Player` — entité JPA, factory `Player.create()`
- `TeamId` — record embedded (Value Object UUID)
- `PlayerId` — record embedded (Value Object UUID)

**Application :**
- Requêtes : `CreateTeamRequest`, `AddPlayerRequest`, `RenameTeamRequest`
- UseCases : `CreateTeam`, `RenameTeam`, `AddTeammate`, `SearchTeamById`, `SearchTeamByIdWithPlayers`, `SearchTeams`
- Handlers : un handler par usecase (`CreateTeamHandler`, etc.)
- Gateways : `TeamRepository`, `PlayerRepository` (interfaces, pas Spring Data)

**Infrastructure REST :**
- `TeamController` → `/api/teams`
- DTOs distincts : `TeamSummaryDTO` (sans joueurs) et `TeamDetailDTO` (avec joueurs)
- `TeamMapper` pour conversion entité → DTO

**Infrastructure Persistence :**
- `SpringJpaTeamRepository` → interface Spring Data JPA interne
- `JpaTeamRepository` → implémente le gateway `TeamRepository`
- Requêtes `@Query` avec `LEFT JOIN FETCH` pour éviter le N+1

**Endpoints :**
```
GET    /api/teams          → liste (résumé, sans joueurs)
POST   /api/teams          → créer → 201 + Location header
GET    /api/teams/{id}     → détail avec joueurs
PUT    /api/teams/{id}     → renommer
POST   /api/teams/{id}/players → ajouter joueur → 201
```

---

### 3.2 Module `tournaments` (Tournois)

**Domain :**
- `Tournament` — entité JPA, factory `Tournament.create()`
- `TournamentId` — record embedded UUID

**Application :**
- `CreateTournamentRequest` avec validation `@ValidDateRange` (annotation custom au niveau classe)
- `TournamentFilters` — record pour filtres de recherche
- `Tournaments` — record wrappant `Collection<Tournament>` (résultat de recherche)
- UseCases : `CreateTournament`, `DeleteTournament`, `SearchTournamentById`, `SearchTournaments`

**Infrastructure Persistence :**
- `TournamentSpecifications` + `TournamentSpecificationBuilder` pour requêtes dynamiques filtrées
- Locking pessimiste : `findByIdForUpdate()` avec `@Lock(PESSIMISTIC_WRITE)` pour concurrence
- `incrementRegisteredTeams()` — UPDATE atomique pour race conditions
- `InMemoryTournamentRepository` — stub pour tests

**Endpoints :**
```
POST   /api/tournaments         → créer → 201
GET    /api/tournaments         → liste (filtre optionnel ?name=...)
GET    /api/tournaments/{id}    → détail
DELETE /api/tournaments/{id}    → supprimer → 204
```

---

### 3.3 Module `registrations` (Inscriptions)

**Domain :**
- `Registration` — entité JPA
- `RegistrationId` — record embedded UUID
- `Participants` — record `(maxTeams, registered)` (Value Object)
- `TournamentSnapshot` — record avec `Participants`, méthode `hasCapacityReached()`

**Application :**
- `TournamentRegistrationCommand` — commande d'inscription
- Gateways cross-modules : `TeamGateway` (vérifie existence), `TournamentGateway` (snapshot + incrément atomique)

**Handler — logique métier complète :**
1. Charger tournoi avec lock pessimiste
2. Vérifier existence de l'équipe
3. Vérifier capacité non atteinte
4. Créer + sauvegarder Registration
5. Incrémenter `registeredTeams` atomiquement

**Endpoint :**
```
POST /api/tournaments/{tournamentId}/registrations → 201 + Location
```

---

## 4. Couche Shared (transversale)

### 4.1 Utilitaires d'identité et de temps

```java
// Interface abstraite (testabilité)
interface DomainIdGenerator { String generate(); }
class UUIDGenerator implements DomainIdGenerator { ... }  // @Component

interface TimeProvider { Instant now(); }
class SystemUtcTimeProvider implements TimeProvider { ... }  // Clock injectable

@Configuration
class TimeConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean TimeProvider timeProvider(Clock clock) { ... }
}
```

### 4.2 Gestion d'erreurs centralisée

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    // MethodArgumentNotValidException  → 400 + liste ValidationError
    // NoSuchElementException           → 404
    // BusinessException                → 409
    // Exception générique              → 500
}

record ErrorResponse(Instant timestamp, int status, String error, String message, String path, List<ValidationError> errors)
record ValidationError(String field, Object rejectedValue, String message)
class BusinessException extends RuntimeException { ... }
```

### 4.3 Logging & Traçabilité

```java
@Component
class RequestLoggingFilter extends OncePerRequestFilter {
    // Ajoute X-Trace-Id header (UUID généré si absent)
    // Stocke traceId en MDC pour tous les logs
    // Log entrée/sortie avec durée ms
    // Nettoie MDC après traitement
    // Ordre HIGHEST_PRECEDENCE
}
```

Fichier `logback-spring.xml` :
- Appenders : CONSOLE (couleurs), FILE (rotation quotidienne), ERROR_FILE
- Profils `dev` et `prod` distincts
- MDC `traceId` dans chaque ligne de log

### 4.4 Validateurs custom

```java
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@interface CountryCode { ... }
// Valide contre Locale.getISOCountries(), case-insensitive

@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@interface ValidDateRange { ... }
// Vérifie que end.isAfter(begin) sur CreateTournamentRequest
```

Messages de validation dans `ValidationMessages.properties` (en français).

### 4.5 OpenAPI / Swagger

```java
@Configuration
class OpenApiConfig {
    @Bean OpenAPI openAPI() {
        // Titre, description, version, contact, licence
        // Serveurs : localhost:8080 (dev) + domaine prod
    }
}
```

UI disponible sur `/swagger-ui.html`, JSON sur `/api-docs`.

---

## 5. Patterns clés à reproduire dans bu-api

| Pattern | Gamoxx | À appliquer dans bu-api |
|---|---|---|
| Value Objects IDs | `TeamId`, `TournamentId` (records embedded) | `WorkId`, `CopyId`, `UserId`, etc. |
| Factory sur entités | `Team.create()`, `Tournament.create()` | `Work.create()`, `Copy.create()` |
| Gateway interfaces | `TeamRepository` (pas Spring Data) | `WorkRepository`, `CopyRepository` |
| Handler par usecase | `CreateTeamHandler`, etc. | `CreateWorkHandler`, etc. |
| DTOs distincts | `TeamSummaryDTO` vs `TeamDetailDTO` | ex: `WorkSummaryDto` vs `WorkDetailDto` |
| Mappers | `TeamMapper` | `WorkMapper`, `CopyMapper` |
| JOIN FETCH explicites | `findByIdWithPlayers()` | `findByIdWithCopies()` |
| Erreurs centralisées | `GlobalExceptionHandler` | Idem + `ResourceNotFoundException` |
| Validateurs custom | `@CountryCode`, `@ValidDateRange` | `@ValidIsbn`, `@ValidBarcode` |
| Locking pessimiste | `findByIdForUpdate()` | Sur `Copy` pour prêt concurrent |
| Trace ID | `RequestLoggingFilter` + MDC | Idem |
| TimeProvider | `TimeProvider` + `Clock` injectable | Idem |
| Specifications | `TournamentSpecificationBuilder` | Pour recherche catalogue |

---

## 6. Structure de fichiers complète (84 fichiers Java)

```
fr.atex.gamoxx/
├── GamoxxApplication.java
├── shared/
│   ├── DomainIdGenerator.java
│   ├── UUIDGenerator.java
│   ├── TimeProvider.java
│   ├── SystemUtcTimeProvider.java
│   ├── TimeConfig.java
│   ├── config/OpenApiConfig.java
│   ├── error/
│   │   ├── BusinessException.java
│   │   ├── ErrorResponse.java
│   │   ├── ValidationError.java
│   │   └── GlobalExceptionHandler.java
│   ├── logging/RequestLoggingFilter.java
│   └── validation/
│       ├── CountryCode.java
│       ├── CountryCodeValidator.java
│       ├── ValidDateRange.java
│       └── DateRangeValidator.java
├── teams/
│   ├── domain/Team.java, Player.java, TeamId.java, PlayerId.java
│   ├── application/
│   │   ├── models/CreateTeamRequest.java, AddPlayerRequest.java, RenameTeamRequest.java
│   │   ├── usecases/CreateTeam.java, RenameTeam.java, AddTeammate.java, SearchTeamById.java, SearchTeamByIdWithPlayers.java, SearchTeams.java
│   │   ├── services/CreateTeamHandler.java, RenameTeamHandler.java, AddTeammateHandler.java, SearchTeamByIdHandler.java, SearchTeamByIdWithPlayersHandler.java, SearchTeamsHandler.java
│   │   └── gateways/TeamRepository.java, PlayerRepository.java
│   └── infrastructure/
│       ├── rest/TeamController.java, TeamSummaryDTO.java, TeamDetailDTO.java, PlayerDTO.java
│       ├── rest/mapper/TeamMapper.java
│       └── persistence/SpringJpaTeamRepository.java, JpaTeamRepository.java, SpringJpaPlayerRepository.java, JpaPlayerRepository.java
├── tournaments/
│   ├── domain/Tournament.java, TournamentId.java
│   ├── application/
│   │   ├── models/CreateTournamentRequest.java, TournamentFilters.java, Tournaments.java
│   │   ├── usecases/CreateTournament.java, DeleteTournament.java, SearchTournamentById.java, SearchTournaments.java
│   │   ├── services/CreateTournamentHandler.java, DeleteTournamentHandler.java, SearchTournamentByIdHandler.java, SearchTournamentsHandler.java
│   │   └── gateways/TournamentRepository.java
│   └── infrastructure/
│       ├── rest/TournamentController.java
│       └── persistence/SpringJpaTournamentRepository.java, JpaTournamentRepository.java, TournamentSpecifications.java, TournamentSpecificationBuilder.java, InMemoryTournamentRepository.java
└── registrations/
    ├── domain/Registration.java, RegistrationId.java, Participants.java, TournamentSnapshot.java
    ├── application/
    │   ├── models/TournamentRegistrationCommand.java
    │   ├── usecases/RegisterToTournament.java
    │   ├── services/RegisterToTournamentHandler.java
    │   └── gateways/RegistrationRepository.java, TeamGateway.java, TournamentGateway.java
    └── infrastructure/
        ├── rest/RegistrationController.java, TournamentRegistrationRequest.java
        └── persistence/SpringJpaRegistrationRepository.java, JpaRegistrationRepository.java, JpaTeamGateway.java, JpaTournamentGateway.java
```