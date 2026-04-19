package fr.esgi.bibliotheque.reservation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.reservation.application.gateways.HoldRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationCopyGateway;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationWorkGateway;
import fr.esgi.bibliotheque.reservation.application.models.RequestHoldRequest;
import fr.esgi.bibliotheque.reservation.application.usecases.RequestHold;
import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestHoldHandler implements RequestHold {

    // Délai de retrait en jours (à déplacer dans Policy quand le module sera disponible)
    private static final int PICKUP_DELAY_DAYS = 3;

    private final HoldRepository holdRepository;
    private final ReservationCopyGateway copyGateway;
    private final ReservationWorkGateway workGateway;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public RequestHoldHandler(HoldRepository holdRepository,
                               ReservationCopyGateway copyGateway,
                               ReservationWorkGateway workGateway,
                               DomainIdGenerator idGenerator,
                               TimeProvider timeProvider) {
        this.holdRepository = holdRepository;
        this.copyGateway = copyGateway;
        this.workGateway = workGateway;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public Hold handle(RequestHoldRequest request) {
        var workId = new WorkId(request.workId());
        var userId = new UserId(request.userId());

        if (holdRepository.findActiveByWorkIdAndUserId(workId, userId).isPresent()) {
            throw new BusinessException("Une réservation active existe déjà pour cet ouvrage");
        }

        int position = holdRepository.countActiveByWorkId(workId) + 1;
        var hold = Hold.create(workId, userId, position, idGenerator, timeProvider);

        // Si un exemplaire est disponible, on le réserve immédiatement
        workGateway.findFirstAvailableCopyForWork(workId).ifPresent(copy -> {
            copy.updateStatus(CopyStatus.RESERVED);
            copyGateway.save(copy);
            hold.markReadyForPickup(copy.getId(), PICKUP_DELAY_DAYS, timeProvider);
        });

        return holdRepository.save(hold);
    }
}
