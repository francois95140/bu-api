package fr.esgi.bibliotheque.circulation.application.gateways;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.Optional;

public interface CirculationUserGateway {
    Optional<User> findById(UserId id);
}
