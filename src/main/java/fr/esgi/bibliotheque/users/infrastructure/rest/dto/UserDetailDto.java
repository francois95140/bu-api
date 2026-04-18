package fr.esgi.bibliotheque.users.infrastructure.rest.dto;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import fr.esgi.bibliotheque.users.domain.UserStatus;

import java.time.Instant;

public record UserDetailDto(
    String id,
    String firstName,
    String lastName,
    String email,
    UserCategory category,
    UserStatus status,
    Instant createdAt
) {
    public static UserDetailDto from(User user) {
        return new UserDetailDto(
            user.getId().value(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getCategory(),
            user.getStatus(),
            user.getCreatedAt()
        );
    }
}
