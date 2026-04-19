package fr.esgi.bibliotheque.circulation.domain;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.users.domain.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "loans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "loan_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private LoanId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "copy_id"))
    private CopyId copyId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    private UserId userId;

    private Instant startAt;
    private Instant dueAt;
    private Instant returnedAt;

    private int renewCount;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(unique = true)
    private String borrowIdempotencyKey;

    private String returnIdempotencyKey;

    public static Loan create(CopyId copyId, UserId userId, int durationDays,
                               String idempotencyKey, DomainIdGenerator gen, TimeProvider time) {
        var loan = new Loan();
        loan.id = new LoanId(gen.generate());
        loan.copyId = copyId;
        loan.userId = userId;
        loan.startAt = time.now();
        loan.dueAt = time.now().plusSeconds((long) durationDays * 86400);
        loan.renewCount = 0;
        loan.status = LoanStatus.ACTIVE;
        loan.borrowIdempotencyKey = idempotencyKey;
        return loan;
    }

    public void returnCopy(String idempotencyKey, TimeProvider time) {
        if (this.status == LoanStatus.RETURNED) {
            return; // idempotent : déjà retourné
        }
        this.returnedAt = time.now();
        this.status = LoanStatus.RETURNED;
        this.returnIdempotencyKey = idempotencyKey;
    }

    public void renew(int extraDays, TimeProvider time) {
        if (this.status != LoanStatus.ACTIVE) {
            throw new BusinessException("Seul un prêt actif peut être prolongé");
        }
        this.dueAt = this.dueAt.plusSeconds((long) extraDays * 86400);
        this.renewCount++;
    }

    public void declareLost() {
        if (this.status == LoanStatus.RETURNED) {
            throw new BusinessException("Un prêt déjà retourné ne peut pas être déclaré perdu");
        }
        this.status = LoanStatus.LOST_DECLARED;
    }

    public boolean isActive() {
        return this.status == LoanStatus.ACTIVE;
    }
}
