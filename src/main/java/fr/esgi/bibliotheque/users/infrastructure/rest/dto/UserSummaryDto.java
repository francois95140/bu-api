package fr.esgi.bibliotheque.users.infrastructure.rest.dto;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import fr.esgi.bibliotheque.users.domain.UserStatus;

public record UserSummaryDto(
    String id,
    String firstName,
    String lastName,
    String email,
    UserCategory category,
    UserStatus status
) {
    public static UserSummaryDto from(User user) {
        return new UserSummaryDto(
            user.getId().value(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getCategory(),
            user.getStatus()
        );
    }
}
