package fr.esgi.bibliotheque.shared;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}
