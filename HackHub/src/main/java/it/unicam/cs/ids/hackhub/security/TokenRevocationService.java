package it.unicam.cs.ids.hackhub.security;

import it.unicam.cs.ids.hackhub.model.RevokedToken;
import it.unicam.cs.ids.hackhub.model.repository.RevokedTokenRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public TokenRevocationService(
            RevokedTokenRepository revokedTokenRepository, JwtService jwtService) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public void revoke(String token) {
        String jti = jwtService.extractJti(token);
        if (jti == null || revokedTokenRepository.existsById(jti)) return;
        Instant expiresAt = jwtService.extractExpiration(token).toInstant();
        revokedTokenRepository.save(new RevokedToken(jti, expiresAt));
    }

    public boolean isRevoked(String token) {
        String jti = jwtService.extractJti(token);
        return jti != null && revokedTokenRepository.existsById(jti);
    }
}
