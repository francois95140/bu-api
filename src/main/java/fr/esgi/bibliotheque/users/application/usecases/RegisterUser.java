package fr.esgi.bibliotheque.users.application.usecases;

import fr.esgi.bibliotheque.users.application.models.RegisterUserRequest;
import fr.esgi.bibliotheque.users.domain.UserId;

public interface RegisterUser {
    UserId handle(RegisterUserRequest request);
}
