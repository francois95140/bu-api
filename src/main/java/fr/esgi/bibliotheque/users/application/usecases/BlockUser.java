package fr.esgi.bibliotheque.users.application.usecases;

import fr.esgi.bibliotheque.users.domain.UserId;

public interface BlockUser {
    void handle(UserId id);
}
