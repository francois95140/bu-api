package fr.esgi.bibliotheque.auth.application.services;

import fr.esgi.bibliotheque.auth.application.gateways.AuthUserGateway;
import fr.esgi.bibliotheque.auth.application.models.AuthRequest;
import fr.esgi.bibliotheque.auth.application.usecases.Authenticate;
import fr.esgi.bibliotheque.auth.domain.TokenPair;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.security.JwtTokenService;
import fr.esgi.bibliotheque.users.domain.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthenticateHandler implements Authenticate {

    private final AuthUserGateway userGateway;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticateHandler(AuthUserGateway userGateway,
                                JwtTokenService jwtTokenService,
                                PasswordEncoder passwordEncoder) {
        this.userGateway = userGateway;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public TokenPair handle(AuthRequest request) {
        var user = userGateway.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Email ou mot de passe incorrect");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Compte bloqué ou suspendu");
        }

        var roles = List.of("ROLE_" + mapRole(user.getCategory().name()));
        var access = jwtTokenService.generateAccessToken(user.getId().value(), user.getEmail(), roles);
        var refresh = jwtTokenService.generateRefreshToken(user.getId().value());
        return new TokenPair(access, refresh);
    }

    private String mapRole(String category) {
        return switch (category) {
            case "LIBRARIAN" -> "LIBRARIAN";
            case "ADMIN" -> "ADMIN";
            default -> "READER"; // STUDENT, TEACHER
        };
    }
}
