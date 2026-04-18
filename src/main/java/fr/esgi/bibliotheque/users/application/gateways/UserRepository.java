package fr.esgi.bibliotheque.users.application.gateways;

import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    List<User> search(UserFilters filters);
    boolean existsByEmail(String email);
}
