package fr.esgi.bibliotheque.auth.application.services;

import fr.esgi.bibliotheque.auth.application.gateways.AuthUserGateway;
import fr.esgi.bibliotheque.auth.application.models.RefreshRequest;
import fr.esgi.bibliotheque.auth.application.usecases.RefreshToken;
import fr.esgi.bibliotheque.auth.domain.TokenPair;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.security.JwtTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RefreshTokenHandler implements RefreshToken {

    private final AuthUserGateway userGateway;
    private final JwtTokenService jwtTokenService;

    public RefreshTokenHandler(AuthUserGateway userGateway, JwtTokenService jwtTokenService) {
        this.userGateway = userGateway;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public TokenPair handle(RefreshRequest request) {
        if (!jwtTokenService.isValid(request.refreshToken())) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        var claims = jwtTokenService.extractClaims(request.refreshToken());
        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException("Token fourni n'est pas un refresh token");
        }

        var userId = claims.getSubject();
        var user = userGateway.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        var roles = List.of("ROLE_" + mapRole(user.getCategory().name()));
        var access = jwtTokenService.generateAccessToken(user.getId().value(), user.getEmail(), roles);
        var refresh = jwtTokenService.generateRefreshToken(user.getId().value());
        return new TokenPair(access, refresh);
    }

    private String mapRole(String category) {
        return switch (category) {
            case "LIBRARIAN" -> "LIBRARIAN";
            case "ADMIN" -> "ADMIN";
            default -> "READER";
        };
    }
}
