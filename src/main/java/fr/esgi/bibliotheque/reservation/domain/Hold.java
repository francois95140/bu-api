package fr.esgi.bibliotheque.reservation.domain;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
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
@Table(name = "holds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hold {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hold_seq")
    @SequenceGenerator(name = "hold_seq", sequenceName = "hold_seq", allocationSize = 1)
    @Column(name = "technical_id")
    private Long technicalId;

    @Embedded
    private HoldId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "work_id"))
    private WorkId workId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "copy_id"))
    private CopyId copyId;

    @Enumerated(EnumType.STRING)
    private HoldStatus status;

    private int queuePosition;
    private Instant pickupUntil;
    private Instant createdAt;

    public static Hold create(WorkId workId, UserId userId, int queuePosition,
                               DomainIdGenerator gen, TimeProvider time) {
        var hold = new Hold();
        hold.id = new HoldId(gen.generate());
        hold.workId = workId;
        hold.userId = userId;
        hold.queuePosition = queuePosition;
        // Premier de la file → REQUESTED, sinon QUEUED
        hold.status = queuePosition == 1 ? HoldStatus.REQUESTED : HoldStatus.QUEUED;
        hold.createdAt = time.now();
        return hold;
    }

    public void markReadyForPickup(CopyId copyId, int pickupDays, TimeProvider time) {
        this.copyId = copyId;
        this.status = HoldStatus.READY_FOR_PICKUP;
        this.pickupUntil = time.now().plusSeconds((long) pickupDays * 86400);
    }

    public void pickup() {
        if (this.status != HoldStatus.READY_FOR_PICKUP) {
            throw new BusinessException("La réservation n'est pas prête pour le retrait");
        }
        this.status = HoldStatus.PICKED_UP;
    }

    public void cancel() {
        if (this.status == HoldStatus.PICKED_UP) {
            throw new BusinessException("Une réservation déjà retirée ne peut pas être annulée");
        }
        this.status = HoldStatus.CANCELLED;
    }

    public void expire() {
        this.status = HoldStatus.EXPIRED;
    }

    public boolean isActive() {
        return this.status == HoldStatus.REQUESTED
            || this.status == HoldStatus.QUEUED
            || this.status == HoldStatus.READY_FOR_PICKUP;
    }
}
