package fr.esgi.bibliotheque.users.application.usecases;

import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.domain.User;

import java.util.List;

public interface SearchUsers {
    List<User> handle(UserFilters filters);
}
