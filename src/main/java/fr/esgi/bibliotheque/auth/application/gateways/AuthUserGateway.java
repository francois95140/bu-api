package fr.esgi.bibliotheque.auth.application.gateways;

import fr.esgi.bibliotheque.users.domain.User;

import java.util.Optional;

public interface AuthUserGateway {
    Optional<User> findByEmail(String email);
    Optional<User> findById(String userId);
}
