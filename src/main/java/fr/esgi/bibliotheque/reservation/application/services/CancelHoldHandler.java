package fr.esgi.bibliotheque.reservation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.reservation.application.gateways.HoldRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationCopyGateway;
import fr.esgi.bibliotheque.reservation.application.usecases.CancelHold;
import fr.esgi.bibliotheque.reservation.domain.HoldId;
import fr.esgi.bibliotheque.reservation.domain.HoldStatus;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CancelHoldHandler implements CancelHold {

    private final HoldRepository holdRepository;
    private final ReservationCopyGateway copyGateway;

    public CancelHoldHandler(HoldRepository holdRepository, ReservationCopyGateway copyGateway) {
        this.holdRepository = holdRepository;
        this.copyGateway = copyGateway;
    }

    @Override
    public void handle(HoldId id) {
        var hold = holdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + id.value()));

        hold.cancel();
        holdRepository.save(hold);

        // Si un exemplaire était réservé pour ce hold, on le remet disponible
        if (hold.getCopyId() != null && hold.getStatus() == HoldStatus.CANCELLED) {
            copyGateway.findByIdForUpdate(hold.getCopyId()).ifPresent(copy -> {
                copy.updateStatus(CopyStatus.AVAILABLE);
                copyGateway.save(copy);
            });
        }
    }
}
