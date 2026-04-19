package fr.esgi.bibliotheque.reservation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.reservation.application.gateways.HoldRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationCopyGateway;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationWorkGateway;
import fr.esgi.bibliotheque.reservation.application.usecases.ExpireHolds;
import fr.esgi.bibliotheque.reservation.domain.HoldStatus;
import fr.esgi.bibliotheque.shared.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpireHoldsHandler implements ExpireHolds {

    private static final int PICKUP_DELAY_DAYS = 3;

    private final HoldRepository holdRepository;
    private final ReservationCopyGateway copyGateway;
    private final ReservationWorkGateway workGateway;
    private final TimeProvider timeProvider;

    public ExpireHoldsHandler(HoldRepository holdRepository,
                               ReservationCopyGateway copyGateway,
                               ReservationWorkGateway workGateway,
                               TimeProvider timeProvider) {
        this.holdRepository = holdRepository;
        this.copyGateway = copyGateway;
        this.workGateway = workGateway;
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle() {
        var expired = holdRepository.findExpiredReadyForPickup(timeProvider.now());

        for (var hold : expired) {
            hold.expire();
            holdRepository.save(hold);

            // Remettre l'exemplaire disponible
            if (hold.getCopyId() != null) {
                copyGateway.findByIdForUpdate(hold.getCopyId()).ifPresent(copy -> {
                    copy.updateStatus(CopyStatus.AVAILABLE);
                    copyGateway.save(copy);
                });
            }

            // Activer le prochain hold en file FIFO
            var nextHolds = holdRepository.findActiveByWorkId(hold.getWorkId());
            nextHolds.stream()
                .filter(h -> h.getStatus() == HoldStatus.QUEUED)
                .findFirst()
                .ifPresent(next -> {
                    workGateway.findFirstAvailableCopyForWork(hold.getWorkId()).ifPresent(copy -> {
                        copy.updateStatus(CopyStatus.RESERVED);
                        copyGateway.save(copy);
                        next.markReadyForPickup(copy.getId(), PICKUP_DELAY_DAYS, timeProvider);
                        holdRepository.save(next);
                    });
                });
        }
    }
}
