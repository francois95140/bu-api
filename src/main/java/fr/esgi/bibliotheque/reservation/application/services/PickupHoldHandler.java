package fr.esgi.bibliotheque.reservation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.reservation.application.gateways.HoldRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationCopyGateway;
import fr.esgi.bibliotheque.reservation.application.usecases.PickupHold;
import fr.esgi.bibliotheque.reservation.domain.HoldId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PickupHoldHandler implements PickupHold {

    private final HoldRepository holdRepository;
    private final ReservationCopyGateway copyGateway;

    public PickupHoldHandler(HoldRepository holdRepository, ReservationCopyGateway copyGateway) {
        this.holdRepository = holdRepository;
        this.copyGateway = copyGateway;
    }

    @Override
    public void handle(HoldId id) {
        var hold = holdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + id.value()));

        hold.pickup();
        holdRepository.save(hold);

        // L'exemplaire passe ON_LOAN via le circuit normal d'emprunt
        if (hold.getCopyId() != null) {
            copyGateway.findByIdForUpdate(hold.getCopyId()).ifPresent(copy -> {
                copy.updateStatus(CopyStatus.ON_LOAN);
                copyGateway.save(copy);
            });
        }
    }
}
