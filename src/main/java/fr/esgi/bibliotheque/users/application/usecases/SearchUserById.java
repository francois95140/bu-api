package fr.esgi.bibliotheque.users.application.usecases;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.Optional;

public interface SearchUserById {
    Optional<User> handle(UserId id);
}
