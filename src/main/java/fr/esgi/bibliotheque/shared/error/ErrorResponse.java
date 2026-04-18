package fr.esgi.bibliotheque.shared.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> errors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> errors) {
        this(Instant.now(), status, error, message, path, errors);
    }
}
