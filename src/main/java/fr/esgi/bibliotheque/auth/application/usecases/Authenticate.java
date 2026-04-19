package fr.esgi.bibliotheque.auth.application.usecases;

import fr.esgi.bibliotheque.auth.application.models.AuthRequest;
import fr.esgi.bibliotheque.auth.domain.TokenPair;

public interface Authenticate {
    TokenPair handle(AuthRequest request);
}
