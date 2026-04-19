package fr.esgi.bibliotheque.users.application.usecases;

import fr.esgi.bibliotheque.users.application.models.UpdateUserRequest;
import fr.esgi.bibliotheque.users.domain.UserId;

public interface UpdateUser {
    void handle(UserId id, UpdateUserRequest request);
}
