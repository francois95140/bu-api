package fr.esgi.bibliotheque.dev;

import fr.esgi.bibliotheque.catalog.domain.*;
import fr.esgi.bibliotheque.circulation.domain.*;
import fr.esgi.bibliotheque.penalty.domain.*;
import fr.esgi.bibliotheque.policy.domain.*;
import fr.esgi.bibliotheque.reservation.domain.*;
import fr.esgi.bibliotheque.users.domain.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

// Justification : équivalent du DatabaseSeeder Laravel — s'exécute au démarrage en profil dev uniquement
@Component
@Profile("dev")
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final SeedService seedService;

    public DatabaseSeeder(SeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedService.seed();
        } catch (Exception e) {
            log.error("Seed failed: {}", e.getMessage(), e);
        }
    }

    // Justification : méthode séparée en @Service pour que @Transactional soit appliqué via proxy Spring
    @Service
    @Profile("dev")
    static class SeedService {

        private static final Logger log = LoggerFactory.getLogger(SeedService.class);

        private final EntityManager em;
        private final PasswordEncoder passwordEncoder;

        SeedService(EntityManager em, PasswordEncoder passwordEncoder) {
            this.em = em;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public void seed() {
        if (alreadySeeded()) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding database...");
        var now = Instant.now();
        var hash = passwordEncoder.encode("password123");

        // ── POLICIES ──────────────────────────────────────────────────────────
        em.persist(policy("pol-student",   UserCategory.STUDENT,   5,  21, 2, 14, PenaltyType.PER_DAY,  "0.10", 3, now));
        em.persist(policy("pol-teacher",   UserCategory.TEACHER,  20,  60, 3, 30, PenaltyType.PER_DAY,  "0.20", 3, now));
        em.persist(policy("pol-librarian", UserCategory.LIBRARIAN, 20, 60, 3, 30, PenaltyType.FLAT_FEE, "2.00", 3, now));
        em.persist(policy("pol-admin",     UserCategory.ADMIN,     20, 60, 3, 30, PenaltyType.FLAT_FEE, "2.00", 3, now));

        // ── USERS ─────────────────────────────────────────────────────────────
        var admin     = user("u-admin-001", "Admin",  "Système",  "admin@univ.fr",         hash, UserCategory.ADMIN,     UserStatus.ACTIVE,  now);
        var librarian = user("u-lib-001",   "Marie",  "Martin",   "bibliothecaire@univ.fr", hash, UserCategory.LIBRARIAN, UserStatus.ACTIVE,  now);
        var student1  = user("u-stu-001",   "Lucas",  "Bernard",  "etudiant@univ.fr",       hash, UserCategory.STUDENT,   UserStatus.ACTIVE,  now);
        var student2  = user("u-stu-002",   "Emma",   "Dubois",   "etudiant2@univ.fr",      hash, UserCategory.STUDENT,   UserStatus.BLOCKED, now);
        var teacher   = user("u-tea-001",   "Sophie", "Leclerc",  "enseignant@univ.fr",     hash, UserCategory.TEACHER,   UserStatus.ACTIVE,  now);
        em.persist(admin); em.persist(librarian); em.persist(student1); em.persist(student2); em.persist(teacher);

        // ── WORKS ─────────────────────────────────────────────────────────────
        var work1 = Work.create(new WorkId("w-001"), "9780132350884", "Clean Code",
                "Robert C. Martin", "Prentice Hall", 2008, "Génie logiciel", "FR", "Guide du code propre");
        var work2 = Work.create(new WorkId("w-002"), "9780201633610", "Design Patterns",
                "Gang of Four", "Addison-Wesley", 1994, "Architecture logicielle", "EN", "Patrons de conception");
        var work3 = Work.create(new WorkId("w-003"), "9780135957059", "The Pragmatic Programmer",
                "David Thomas", "Addison-Wesley", 2019, "Génie logiciel", "EN", "Pragmatisme en développement");
        em.persist(work1); em.persist(work2); em.persist(work3);

        // ── COPIES ────────────────────────────────────────────────────────────
        var copy1 = Copy.create(new CopyId("c-001"), "BC001", work1, "CAMPUS-A", "INF-01", "NEUF", now);
        var copy2 = Copy.create(new CopyId("c-002"), "BC002", work1, "CAMPUS-A", "INF-01", "BON",  now);
        copy2.updateStatus(CopyStatus.ON_LOAN);

        var copy3 = Copy.create(new CopyId("c-003"), "BC003", work2, "CAMPUS-A", "INF-02", "NEUF", now);
        var copy4 = Copy.create(new CopyId("c-004"), "BC004", work2, "CAMPUS-B", "INF-02", "BON",  now);
        copy4.updateStatus(CopyStatus.ON_LOAN);

        var copy5 = Copy.create(new CopyId("c-005"), "BC005", work3, "CAMPUS-A", "INF-03", "NEUF",       now);
        var copy6 = Copy.create(new CopyId("c-006"), "BC006", work3, "CAMPUS-B", "INF-03", "ACCEPTABLE", now);
        copy6.updateStatus(CopyStatus.RESERVED);

        em.persist(copy1); em.persist(copy2); em.persist(copy3);
        em.persist(copy4); em.persist(copy5); em.persist(copy6);

        // ── LOANS ─────────────────────────────────────────────────────────────
        // Actif : student1 a emprunté copy2 (dueAt dans 10 jours)
        em.persist(loan("l-001", copy2.getId(), student1.getId(),
                now.minus(5, ChronoUnit.DAYS), now.plus(10, ChronoUnit.DAYS),
                0, LoanStatus.ACTIVE, "idem-borrow-001", null));

        // En retard : student2 a copy4 depuis 30 jours
        em.persist(loan("l-002", copy4.getId(), student2.getId(),
                now.minus(30, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS),
                1, LoanStatus.OVERDUE, "idem-borrow-002", null));

        // Retourné : historique student1
        em.persist(loan("l-003", copy1.getId(), student1.getId(),
                now.minus(30, ChronoUnit.DAYS), now.minus(10, ChronoUnit.DAYS),
                0, LoanStatus.RETURNED, "idem-borrow-003", now.minus(12, ChronoUnit.DAYS)));

        // Retourné : historique teacher (2 prolongations)
        em.persist(loan("l-004", copy3.getId(), teacher.getId(),
                now.minus(60, ChronoUnit.DAYS), now.minus(20, ChronoUnit.DAYS),
                2, LoanStatus.RETURNED, "idem-borrow-004", now.minus(22, ChronoUnit.DAYS)));

        // work4 : toutes les copies empruntées → pour tester la file d'attente QUEUED
        var work4 = Work.create(new WorkId("w-004"), "9780201485677", "Refactoring",
                "Martin Fowler", "Addison-Wesley", 1999, "Génie logiciel", "EN", "Améliorer la conception du code existant");
        em.persist(work4);

        var copy7 = Copy.create(new CopyId("c-007"), "BC007", work4, "CAMPUS-A", "INF-04", "BON", now);
        copy7.updateStatus(CopyStatus.ON_LOAN);
        var copy8 = Copy.create(new CopyId("c-008"), "BC008", work4, "CAMPUS-B", "INF-04", "BON", now);
        copy8.updateStatus(CopyStatus.ON_LOAN);
        em.persist(copy7); em.persist(copy8);

        // Prêt actif teacher sur copy7 (pour tester retour + activation hold)
        em.persist(loan("l-005", copy7.getId(), teacher.getId(),
                now.minus(10, ChronoUnit.DAYS), now.plus(5, ChronoUnit.DAYS),
                0, LoanStatus.ACTIVE, "idem-borrow-005", null));

        // Prêt actif student1 sur copy8 — renewCount=2 (quota max étudiant atteint)
        em.persist(loan("l-006", copy8.getId(), student1.getId(),
                now.minus(15, ChronoUnit.DAYS), now.plus(6, ChronoUnit.DAYS),
                2, LoanStatus.ACTIVE, "idem-borrow-006", null));

        // ── HOLDS ─────────────────────────────────────────────────────────────
        // READY_FOR_PICKUP : student1 attend work3/copy6
        em.persist(hold("h-001", work3.getId(), student1.getId(), copy6.getId(),
                HoldStatus.READY_FOR_PICKUP, 1, now.minus(1, ChronoUnit.DAYS), now.plus(2, ChronoUnit.DAYS)));

        // QUEUED : teacher attend work1 (copy2 empruntée)
        em.persist(hold("h-002", work1.getId(), teacher.getId(), null,
                HoldStatus.QUEUED, 1, now.minus(2, ChronoUnit.DAYS), null));

        // CANCELLED : historique
        em.persist(hold("h-003", work2.getId(), student1.getId(), null,
                HoldStatus.CANCELLED, 1, now.minus(10, ChronoUnit.DAYS), null));

        // ── PENALTIES ─────────────────────────────────────────────────────────
        // Active (cause du blocage student2)
        em.persist(penalty("p-001", student2.getId(),
                "Retard de 7 jours sur 'Design Patterns'", new BigDecimal("0.70"),
                PenaltyStatus.PENDING, now.minus(1, ChronoUnit.DAYS), null));

        // Levée (historique student1)
        em.persist(penalty("p-002", student1.getId(),
                "Retard de 3 jours sur 'Clean Code'", new BigDecimal("0.30"),
                PenaltyStatus.CLEARED, now.minus(20, ChronoUnit.DAYS), now.minus(18, ChronoUnit.DAYS)));

        log.info("Database seeded successfully.");
        log.info("Accounts (password: password123): admin@univ.fr | bibliothecaire@univ.fr | etudiant@univ.fr | etudiant2@univ.fr | enseignant@univ.fr");
    }

    private boolean alreadySeeded() {
        return ((Long) em.createQuery("SELECT COUNT(u) FROM User u WHERE u.id.value = 'u-admin-001'")
                .getSingleResult()) > 0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Policy policy(String id, UserCategory category, int maxLoans, int loanDays, int maxRenewals,
                           int blockDays, PenaltyType type, String amount, int pickupDays, Instant now) {
        var p = instantiate(Policy.class);
        set(p, "id", new PolicyId(id));
        set(p, "userCategory", category);
        set(p, "maxLoans", maxLoans);
        set(p, "loanDurationDays", loanDays);
        set(p, "maxRenewals", maxRenewals);
        set(p, "overdueBlockThresholdDays", blockDays);
        set(p, "penaltyType", type);
        set(p, "penaltyAmount", new BigDecimal(amount));
        set(p, "pickupDelayDays", pickupDays);
        set(p, "updatedAt", now);
        return p;
    }

    private User user(String id, String firstName, String lastName, String email,
                       String hash, UserCategory category, UserStatus status, Instant now) {
        var u = instantiate(User.class);
        set(u, "id", new UserId(id));
        set(u, "firstName", firstName);
        set(u, "lastName", lastName);
        set(u, "email", email);
        set(u, "passwordHash", hash);
        set(u, "category", category);
        set(u, "status", status);
        set(u, "createdAt", now);
        return u;
    }

    private Loan loan(String id, CopyId copyId, UserId userId, Instant startAt, Instant dueAt,
                       int renewCount, LoanStatus status, String borrowKey, Instant returnedAt) {
        var l = instantiate(Loan.class);
        set(l, "id", new LoanId(id));
        set(l, "copyId", copyId);
        set(l, "userId", userId);
        set(l, "startAt", startAt);
        set(l, "dueAt", dueAt);
        set(l, "returnedAt", returnedAt);
        set(l, "renewCount", renewCount);
        set(l, "status", status);
        set(l, "borrowIdempotencyKey", borrowKey);
        return l;
    }

    private Hold hold(String id, WorkId workId, UserId userId, CopyId copyId,
                       HoldStatus status, int position, Instant createdAt, Instant pickupUntil) {
        var h = instantiate(Hold.class);
        set(h, "id", new HoldId(id));
        set(h, "workId", workId);
        set(h, "userId", userId);
        set(h, "copyId", copyId);
        set(h, "status", status);
        set(h, "queuePosition", position);
        set(h, "createdAt", createdAt);
        set(h, "pickupUntil", pickupUntil);
        return h;
    }

    private Penalty penalty(String id, UserId userId, String reason, BigDecimal amount,
                             PenaltyStatus status, Instant createdAt, Instant clearedAt) {
        var p = instantiate(Penalty.class);
        set(p, "id", new PenaltyId(id));
        set(p, "userId", userId);
        set(p, "reason", reason);
        set(p, "amount", amount);
        set(p, "status", status);
        set(p, "createdAt", createdAt);
        set(p, "clearedAt", clearedAt);
        return p;
    }

    private <T> T instantiate(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Seed: cannot instantiate " + clazz.getSimpleName(), e);
        }
    }

    private void set(Object target, String field, Object value) {
        try {
            var f = findField(target.getClass(), field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Seed: cannot set field '" + field + "' on " + target.getClass().getSimpleName(), e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
            throw e;
        }
    }
    } // SeedService
}
