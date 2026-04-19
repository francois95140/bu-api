package fr.esgi.bibliotheque.penalty.domain;

import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.users.domain.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "penalties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "penalty_seq")
    @SequenceGenerator(name = "penalty_seq", sequenceName = "penalty_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private PenaltyId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    private UserId userId;

    private String reason;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PenaltyStatus status;

    private Instant createdAt;
    private Instant clearedAt;

    public static Penalty create(UserId userId, String reason, BigDecimal amount,
                                  DomainIdGenerator gen, TimeProvider time) {
        var penalty = new Penalty();
        penalty.id = new PenaltyId(gen.generate());
        penalty.userId = userId;
        penalty.reason = reason;
        penalty.amount = amount;
        penalty.status = PenaltyStatus.PENDING;
        penalty.createdAt = time.now();
        return penalty;
    }

    public void clear(TimeProvider time) {
        this.status = PenaltyStatus.CLEARED;
        this.clearedAt = time.now();
    }

    public boolean isPending() {
        return this.status == PenaltyStatus.PENDING;
    }
}
