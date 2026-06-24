package it.unicam.cs.ids.hackhub.security;

import it.unicam.cs.ids.hackhub.model.repository.RevokedTokenRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenCleanupScheduler(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Scheduled(fixedRateString = "${token.cleanup.rate-ms:3600000}")
    @Transactional
    public void purgeExpired() {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
