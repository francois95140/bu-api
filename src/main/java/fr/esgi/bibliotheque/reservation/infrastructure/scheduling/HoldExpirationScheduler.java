package fr.esgi.bibliotheque.reservation.infrastructure.scheduling;

import fr.esgi.bibliotheque.reservation.application.usecases.ExpireHolds;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpirationScheduler {

    private final ExpireHolds expireHolds;

    public HoldExpirationScheduler(ExpireHolds expireHolds) {
        this.expireHolds = expireHolds;
    }

    // Justification : exécution toutes les heures pour traiter les réservations expirées en temps quasi-réel
    @Scheduled(fixedRateString = "${library.hold.expiration-check-ms:3600000}")
    public void run() {
        expireHolds.handle();
    }
}
