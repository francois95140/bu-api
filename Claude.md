# CLAUDE.md — Gestionnaire de bibliothèque universitaire (bu-api)

> Ce fichier est lu automatiquement par Claude Code à chaque session.
> Il contient le contexte, l'architecture, les règles et les conventions du projet.
> **Tout code généré doit respecter ce document, sans exception.**

---

## 1. Contexte du projet

Projet scolaire (mémoire) : développement d'un **gestionnaire de bibliothèque universitaire** exposé via une API REST.

### Objectifs produit
- Centraliser le catalogue (ouvrages physiques + ressources numériques)
- Gérer les emprunts, retours, prolongations, réservations
- Réduire les frictions côté étudiant (recherche, disponibilité, réservation)
- Donner aux bibliothécaires une vue claire des stocks, retards, pénalités

### Personas
- **Étudiant** : recherche, emprunt, prolongation, réservation, historique
- **Enseignant / Chercheur** : idem + durées d'emprunt plus longues + listes de lecture
- **Bibliothécaire** : gestion du catalogue, exemplaires, prêts/retours, pénalités
- **Administrateur** : paramétrage, droits, référentiels, intégrations (SSO, mail)

---

## 2. Stack technique

| Élément | Version |
|---|---|
| Java | 25 (LTS) |
| Spring Boot | 4.0.x |
| Spring Data JPA / Hibernate | via Boot |
| Spring Security | via Boot |
| PostgreSQL | 16+ |
| SpringDoc OpenAPI | 2.7.x |
| JJWT (JSON Web Token) | 0.12.x |
| Jakarta Bean Validation | via Boot |
| Spring Boot Actuator | via Boot |
| Lombok | dernière stable |
| Maven | 4.x |
| JUnit 5 + Mockito + Testcontainers | via Boot |

---

## 3. Architecture globale : Hexagonale (Ports & Adapters)

Chaque module métier est découpé en **3 couches strictes** :

```
{module}/
├── domain/           → Entités JPA + Value Objects (IDs)
├── application/
│   ├── models/       → Requêtes entrantes (validation)
│   ├── usecases/     → Interfaces des cas d'usage (ports entrants)
│   ├── services/     → Handlers (implémentations des usecases)
│   └── gateways/     → Interfaces de persistance (ports sortants)
└── infrastructure/
    ├── rest/         → Controllers + DTOs réponse + Mappers
    └── persistence/  → Adapters JPA (implémentations des gateways)
```

### Règle d'or (NON NÉGOCIABLE)

Les couches **`domain/`** et **`application/`** ne connaissent PAS :
- Spring (pas d'annotations `@Service`, `@Component`, `@Autowired`)
- JPA/Hibernate (sauf les annotations sur les entités du domaine, qui restent en `domain/` par simplicité pédagogique)
- Aucun détail d'infrastructure (pas de `HttpServletRequest`, pas de `ResponseEntity`, etc.)

Exception tolérée pour ce projet scolaire : les entités du `domain/` portent les annotations JPA (`@Entity`, `@Table`). C'est un compromis pragmatique assumé et à défendre dans le mémoire (pure DDD voudrait une séparation entité domaine / entité persistance).

Les **Handlers** en `application/services/` portent `@Service` et `@Transactional` — c'est le seul endroit de la couche application où Spring apparaît. Justification : sans cela, il faudrait une couche de configuration manuelle qui alourdit un projet scolaire.

---

## 4. Modules métier

### 4.1 `catalog/` — Catalogue
- **Domain** : `Work`, `Copy`, `WorkId`, `CopyId`
- **Usecases** : `CreateWork`, `UpdateWork`, `SearchWorkById`, `SearchWorks`, `AddCopy`, `UpdateCopyStatus`

### 4.2 `users/` — Utilisateurs & rôles
- **Domain** : `User`, `UserId`, `UserCategory` (enum)
- **Usecases** : `RegisterUser`, `UpdateUser`, `SearchUserById`, `SearchUsers`, `BlockUser`, `UnblockUser`

### 4.3 `auth/` — Authentification
- **Domain** : objets de token
- **Usecases** : `Authenticate`, `RefreshToken`

### 4.4 `circulation/` — Prêts / retours / prolongations
- **Domain** : `Loan`, `LoanId`
- **Usecases** : `BorrowCopy`, `ReturnCopy`, `RenewLoan`, `DeclareLost`, `SearchLoansByUser`

### 4.5 `reservation/` — Réservations (Hold)
- **Domain** : `Hold`, `HoldId`, `QueuePosition`
- **Usecases** : `RequestHold`, `CancelHold`, `PickupHold`, `ExpireHolds` (scheduled)

### 4.6 `penalty/` — Pénalités
- **Domain** : `Penalty`, `PenaltyId`
- **Usecases** : `CreatePenalty`, `ClearPenalty`, `SearchPenaltiesByUser`

### 4.7 `policy/` — Règles paramétrables
- **Domain** : `Policy`, `PolicyId`
- **Usecases** : `UpdatePolicy`, `GetPolicyForCategory`

### 4.8 `reporting/` — Statistiques
- **Usecases** : `GetTopLoans`, `GetOverdueStats`, `GetAcquisitionsReport`

---

## 5. Couche `shared/` (transversale)

### 5.1 Identité et temps

```java
interface DomainIdGenerator { String generate(); }
class UUIDGenerator implements DomainIdGenerator { ... }  // @Component

interface TimeProvider { Instant now(); }
class SystemUtcTimeProvider implements TimeProvider { ... }

@Configuration
class TimeConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean TimeProvider timeProvider(Clock clock) { ... }
}
```

**Justification** : injecter `TimeProvider` plutôt qu'appeler `Instant.now()` rend les tests déterministes (on peut fixer le temps pour tester les règles de retard, d'expiration de Hold, etc.).

### 5.2 Gestion d'erreurs centralisée

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    // MethodArgumentNotValidException  → 400 + liste ValidationError
    // NoSuchElementException           → 404
    // BusinessException                → 409 (conflit métier)
    // AccessDeniedException            → 403
    // Exception générique              → 500
}

record ErrorResponse(
    Instant timestamp, int status, String error,
    String message, String path, List<ValidationError> errors
)
record ValidationError(String field, Object rejectedValue, String message)
class BusinessException extends RuntimeException { ... }
class ResourceNotFoundException extends RuntimeException { ... }
```

### 5.3 Logging & Traçabilité

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

### 5.4 Validateurs custom

```java
@Constraint(validatedBy = IsbnValidator.class)
@interface ValidIsbn { ... }       // ISBN-10 ou ISBN-13 valide

@Constraint(validatedBy = BarcodeValidator.class)
@interface ValidBarcode { ... }    // Format interne de code-barres

@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@interface ValidDateRange { ... }  // dueAt > startAt sur les Loans
```

Messages de validation dans `ValidationMessages.properties` (français).

### 5.5 OpenAPI / Swagger

```java
@Configuration
class OpenApiConfig {
    @Bean OpenAPI openAPI() {
        // Titre, description, version, contact, licence
        // Serveurs : localhost:8080 (dev) + domaine prod
        // Security scheme : bearer JWT
    }
}
```

UI disponible sur `/swagger-ui.html`, JSON sur `/api-docs`.

### 5.6 Sécurité (JWT)
- `JwtTokenService` dans `shared/security/`
- `JwtAuthenticationFilter` dans `shared/security/`
- `SecurityConfig` dans `shared/config/`
- Token signé HS256, expiration 24h
- Payload : `sub` (userId), `email`, `roles`, `iat`, `exp`

---

## 6. Patterns à appliquer SYSTÉMATIQUEMENT

### 6.1 Value Objects pour les IDs
Chaque entité a son propre type d'ID sous forme de **record embedded** :

```java
@Embeddable
public record WorkId(UUID value) {
    public static WorkId generate(DomainIdGenerator gen) {
        return new WorkId(UUID.fromString(gen.generate()));
    }
    public static WorkId of(String value) { return new WorkId(UUID.fromString(value)); }
}
```

**Justification** : évite de confondre un `UserId` avec un `WorkId` — type safety forte. Un classique DDD.

### 6.2 Factory sur les entités
Pas de `new Work(...)` dans le code métier. Toujours :

```java
public class Work {
    private Work() {} // JPA

    public static Work create(String title, String isbn, ..., DomainIdGenerator gen, TimeProvider time) {
        var work = new Work();
        work.id = WorkId.generate(gen);
        work.title = title;
        work.createdAt = time.now();
        return work;
    }

    // Méthodes métier :
    public void updateTitle(String newTitle) { ... }
    public boolean isAvailable() { ... }
}
```

### 6.3 Gateway (port sortant) ≠ Spring Data Repository
Dans `application/gateways/`, l'interface est **pure Java**, indépendante de Spring :

```java
public interface WorkRepository {
    Work save(Work work);
    Optional<Work> findById(WorkId id);
    List<Work> search(WorkFilters filters);
}
```

Dans `infrastructure/persistence/`, on a DEUX classes :
```java
// Interface interne Spring Data
interface SpringJpaWorkRepository extends JpaRepository<Work, WorkId> {
    @Query("SELECT w FROM Work w LEFT JOIN FETCH w.copies WHERE w.id = :id")
    Optional<Work> findByIdWithCopies(@Param("id") WorkId id);
}

// Adapter qui implémente le gateway
@Component
class JpaWorkRepository implements WorkRepository {
    private final SpringJpaWorkRepository jpa;
    // délégation...
}
```

**Justification** : la logique métier (`application/services/`) ne dépend que de l'interface `WorkRepository`. On peut remplacer l'implémentation (ex : `InMemoryWorkRepository` pour les tests) sans toucher au reste.

### 6.4 Un Handler par UseCase
```java
public interface CreateWork {
    Work execute(CreateWorkRequest request);
}

@Service
@Transactional
class CreateWorkHandler implements CreateWork {
    private final WorkRepository workRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    @Override
    public Work execute(CreateWorkRequest request) {
        var work = Work.create(request.title(), request.isbn(), ..., idGenerator, timeProvider);
        return workRepository.save(work);
    }
}
```

**Pas de "WorkService" fourre-tout** qui fait 40 choses. Un usecase = une interface = un handler.

### 6.5 DTOs distincts Summary vs Detail
- `WorkSummaryDto` : pour les listes (pas de copies chargées)
- `WorkDetailDto` : pour la vue détaillée (avec copies)

Même principe pour `User`, `Loan`, etc.

### 6.6 Mappers dédiés
```java
@Component
class WorkMapper {
    public WorkSummaryDto toSummary(Work w) { ... }
    public WorkDetailDto toDetail(Work w) { ... }
}
```

Pas de logique de mapping dans les Controllers ou Services.

### 6.7 JOIN FETCH explicites
Pour éviter le **N+1**, les requêtes qui récupèrent une entité avec ses associations utilisent `LEFT JOIN FETCH` explicitement.

### 6.8 Specifications JPA pour recherche dynamique
Pour la recherche catalogue avec filtres multiples :
```java
class WorkSpecifications {
    static Specification<Work> hasTitle(String title) { ... }
    static Specification<Work> hasLanguage(String lang) { ... }
    static Specification<Work> publishedAfter(int year) { ... }
}

class WorkSpecificationBuilder {
    Specification<Work> build(WorkFilters filters) { ... }
}
```

### 6.9 Locking pessimiste sur les ressources critiques
Pour les prêts concurrents (deux emprunts simultanés du même exemplaire) :
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Copy c WHERE c.id = :id")
Optional<Copy> findByIdForUpdate(@Param("id") CopyId id);
```

**Justification** : sans lock, deux lecteurs pourraient emprunter le même exemplaire en même temps (race condition). Point important à défendre dans le mémoire.

### 6.10 Idempotence sur les scans
Les endpoints `borrow` et `return` utilisent un header `Idempotency-Key` pour éviter les doubles actions en cas de re-scan par le bibliothécaire.

---

## 7. Modèle de données (conceptuel)

### Entités principales

| Entité | Champs essentiels |
|---|---|
| `Work` | id, isbn, title, authors, publisher, year, subjects, language, description |
| `Copy` | id, barcode, work_id, status, campus_id, shelf, condition |
| `User` | id, email, password_hash, category, status, created_at |
| `Loan` | id, copy_id, user_id, start_at, due_at, returned_at, renew_count, status |
| `Hold` | id, work_id, user_id, status, queue_position, pickup_until, created_at |
| `Penalty` | id, user_id, type, amount, reason, status, created_at |
| `Policy` | id, user_category, max_loans, loan_duration_days, max_renewals |
| `AuditLog` | id, actor_id, action, target_type, target_id, payload_json, at |

### Enums d'état

**`Copy.status`** : `AVAILABLE`, `ON_LOAN`, `RESERVED`, `IN_TRANSIT`, `IN_PROCESSING`, `LOST`, `DAMAGED`, `WITHDRAWN`

**`Loan.status`** : `ACTIVE`, `OVERDUE`, `RETURNED`, `LOST_DECLARED`

**`Hold.status`** : `REQUESTED`, `QUEUED`, `READY_FOR_PICKUP`, `PICKED_UP`, `EXPIRED`, `CANCELLED`

**`User.status`** : `ACTIVE`, `BLOCKED`, `SUSPENDED`

**`UserCategory`** : `STUDENT`, `TEACHER`, `LIBRARIAN`, `ADMIN`

---

## 8. Règles métier à respecter IMPÉRATIVEMENT

### 8.1 Disponibilité d'un exemplaire
Un `Copy` est disponible si et seulement si :
- `status = AVAILABLE`
- **ET** pas de `Hold` `READY_FOR_PICKUP` pour un autre utilisateur

### 8.2 Quotas par défaut (paramétrables via Policy)
- **Étudiant** : 5 emprunts max / 21 jours / 2 prolongations max
- **Enseignant** : 20 emprunts max / 60 jours / 3 prolongations max

### 8.3 Prolongation autorisée si :
- Pas de Hold en attente sur le Work correspondant
- Pas de pénalité bloquante sur l'utilisateur
- `renew_count < policy.max_renewals`
- `Loan.status = ACTIVE` (pas `OVERDUE`)

### 8.4 Réservation (Hold)
- File d'attente **FIFO** par défaut (tri par `created_at`)
- Passe à `READY_FOR_PICKUP` dès qu'un exemplaire redevient `AVAILABLE`
- Délai de retrait : 3 jours ouvrés (paramétrable)
- Si délai expiré → `EXPIRED` et proposition au suivant dans la file (job scheduled)

### 8.5 Retards & sanctions
- Au-delà de X jours de retard → compte `BLOCKED` (paramétrable)
- Pénalité : forfaitaire ou par jour (paramétrable via Policy)
- Levée automatique après paiement/validation par bibliothécaire

---

## 9. Sécurité & Permissions (RBAC)

### Rôles
- `READER` (étudiants, enseignants) : search, view, reserve, renew, view own account
- `LIBRARIAN` : + manage works/copies, loans/returns, override actions, manage holds
- `ADMIN` : + manage users, policies, campuses, integrations, reporting

### Endpoints protégés
- Tous les endpoints `/api/**` protégés
- **Sauf** : `/api/auth/**`, `/api/works/search` (lecture publique), `/swagger-ui/**`, `/api-docs/**`

### Règles de sécurité critiques
- **Jamais** exposer `password_hash` dans une réponse API (`@JsonIgnore` + DTO dédié)
- Passwords hashés avec **BCrypt** (force ≥ 10)
- JWT signé HS256, secret ≥ 256 bits, stocké en variable d'environnement (jamais en dur dans `application.properties`)

---

## 10. Exigences non-fonctionnelles (NFR)

| NFR | Exigence |
|---|---|
| Traçabilité | Toute action de circulation (prêt/retour/override) auditée dans `audit_log` |
| RGPD | Historique consultable, purge paramétrable |
| Performance | Recherche catalogue < 200ms en conditions normales (index sur title, isbn, authors) |
| Observabilité | Logs structurés + trace_id (MDC) + Actuator `/health`, `/metrics`, `/info` |
| Idempotence | Endpoints de scan (prêt/retour) idempotents via `Idempotency-Key` header |
| Résilience | Retry sur notifications mail (max 3 tentatives, backoff exponentiel) |
| Disponibilité cible | 99.5% (intra universitaire) |

---

## 11. Structure de fichiers type (module `catalog/`)

```
fr.bu.api/
├── BuApiApplication.java
├── shared/
│   ├── DomainIdGenerator.java, UUIDGenerator.java
│   ├── TimeProvider.java, SystemUtcTimeProvider.java, TimeConfig.java
│   ├── config/OpenApiConfig.java, SecurityConfig.java
│   ├── security/JwtTokenService.java, JwtAuthenticationFilter.java
│   ├── error/BusinessException.java, ResourceNotFoundException.java,
│   │         ErrorResponse.java, ValidationError.java, GlobalExceptionHandler.java
│   ├── logging/RequestLoggingFilter.java
│   └── validation/ValidIsbn.java, IsbnValidator.java,
│                  ValidBarcode.java, BarcodeValidator.java
├── catalog/
│   ├── domain/
│   │   ├── Work.java, WorkId.java
│   │   └── Copy.java, CopyId.java
│   ├── application/
│   │   ├── models/CreateWorkRequest.java, UpdateWorkRequest.java,
│   │   │          AddCopyRequest.java, WorkFilters.java
│   │   ├── usecases/CreateWork.java, UpdateWork.java,
│   │   │            SearchWorkById.java, SearchWorks.java,
│   │   │            AddCopy.java, UpdateCopyStatus.java
│   │   ├── services/CreateWorkHandler.java, UpdateWorkHandler.java,
│   │   │            SearchWorkByIdHandler.java, SearchWorksHandler.java,
│   │   │            AddCopyHandler.java, UpdateCopyStatusHandler.java
│   │   └── gateways/WorkRepository.java, CopyRepository.java
│   └── infrastructure/
│       ├── rest/
│       │   ├── WorkController.java, CopyController.java
│       │   ├── WorkSummaryDto.java, WorkDetailDto.java, CopyDto.java
│       │   └── mapper/WorkMapper.java, CopyMapper.java
│       └── persistence/
│           ├── SpringJpaWorkRepository.java, JpaWorkRepository.java
│           ├── SpringJpaCopyRepository.java, JpaCopyRepository.java
│           ├── WorkSpecifications.java, WorkSpecificationBuilder.java
│           └── InMemoryWorkRepository.java  (pour tests)
└── [users/, auth/, circulation/, reservation/, penalty/, policy/, reporting/]
```

---

## 12. Règles pour Claude Code

Quand tu (Claude Code) travailles sur ce projet :

1. **Respecte l'architecture hexagonale** à 3 couches (section 3) sans JAMAIS mélanger
2. **Ne génère JAMAIS** :
    - de Service "fourre-tout" qui fait plusieurs usecases
    - d'entité JPA exposée directement dans un controller (toujours passer par DTO + Mapper)
    - de dépendance de `domain/` ou `application/` vers `infrastructure/`
    - de `UUID.randomUUID()` ou `Instant.now()` dans le code métier (utilise `DomainIdGenerator` et `TimeProvider`)
3. **Applique systématiquement** les patterns de la section 6 (Value Object ID, Factory, Gateway, Handler, DTO Summary/Detail, Mapper)
4. **Ne contourne JAMAIS** les règles métier de la section 8
5. **Ajoute un test** pour chaque règle métier non-triviale (quota, prolongation refusée, file FIFO, lock concurrent)
6. **Utilise Lombok avec parcimonie** : `@Getter` sur les entités, `@RequiredArgsConstructor` sur les handlers et adapters. Évite `@Data` sur les entités JPA (problèmes avec `equals/hashCode` et lazy loading).
7. **Documente** les choix non-évidents avec un commentaire `// Justification : ...` (utile pour le mémoire)
8. **Avant toute modification structurelle** (renommer un module, changer une relation JPA), demande confirmation
9. **Pose des questions** si une règle métier est ambiguë plutôt que d'inventer
10. **Ne crée pas** de nouveaux modules sans nécessité — reste dans ceux définis en section 4
11. **Ne lance pas** `mvn clean install` automatiquement à chaque fichier — laisse l'utilisateur tester
12. **Respecte la règle du JOIN FETCH** : toute méthode `findByIdWith...` doit utiliser `LEFT JOIN FETCH` explicite

---

## 13. État d'avancement du projet

> Mise à jour à chaque fin d'étape.

- [x] Étape 1 : Setup du projet (dépendances, arborescence, config Postgres)
- [ ] Étape 2 : Couche `shared/` (IdGenerator, TimeProvider, GlobalExceptionHandler, RequestLoggingFilter, validateurs custom, OpenAPI)
- [ ] Étape 3 : Module `users/` + `auth/` (Spring Security + JWT)
- [ ] Étape 4 : Module `catalog/` (Work, Copy + recherche avec Specifications)
- [ ] Étape 5 : Module `circulation/` (Loan + locking pessimiste + idempotence)
- [ ] Étape 6 : Module `reservation/` (Hold, file FIFO, job d'expiration)
- [ ] Étape 7 : Module `penalty/` + `policy/`
- [ ] Étape 8 : Module `reporting/` + audit log + finitions NFR