package fr.esgi.bibliotheque.policy.domain;

import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_seq")
    @SequenceGenerator(name = "policy_seq", sequenceName = "policy_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private PolicyId id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private UserCategory userCategory;

    private int maxLoans;
    private int loanDurationDays;
    private int maxRenewals;
    private int overdueBlockThresholdDays;

    @Enumerated(EnumType.STRING)
    private PenaltyType penaltyType;

    @Column(precision = 10, scale = 2)
    private BigDecimal penaltyAmount;

    private int pickupDelayDays;
    private Instant updatedAt;

    public static Policy createDefault(UserCategory category, DomainIdGenerator gen, TimeProvider time) {
        var policy = new Policy();
        policy.id = new PolicyId(gen.generate());
        policy.userCategory = category;
        policy.pickupDelayDays = 3;
        policy.penaltyType = PenaltyType.PER_DAY;
        policy.overdueBlockThresholdDays = 14;
        policy.updatedAt = time.now();

        switch (category) {
            case TEACHER, LIBRARIAN, ADMIN -> {
                policy.maxLoans = 20;
                policy.loanDurationDays = 60;
                policy.maxRenewals = 3;
                policy.penaltyAmount = BigDecimal.valueOf(0.20);
            }
            default -> {
                policy.maxLoans = 5;
                policy.loanDurationDays = 21;
                policy.maxRenewals = 2;
                policy.penaltyAmount = BigDecimal.valueOf(0.10);
            }
        }
        return policy;
    }

    public void update(int maxLoans, int loanDurationDays, int maxRenewals,
                        int overdueBlockThresholdDays, PenaltyType penaltyType,
                        BigDecimal penaltyAmount, int pickupDelayDays, TimeProvider time) {
        this.maxLoans = maxLoans;
        this.loanDurationDays = loanDurationDays;
        this.maxRenewals = maxRenewals;
        this.overdueBlockThresholdDays = overdueBlockThresholdDays;
        this.penaltyType = penaltyType;
        this.penaltyAmount = penaltyAmount;
        this.pickupDelayDays = pickupDelayDays;
        this.updatedAt = time.now();
    }
}
