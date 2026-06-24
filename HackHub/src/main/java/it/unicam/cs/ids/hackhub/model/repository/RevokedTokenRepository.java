package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    void deleteByExpiresAtBefore(Instant threshold);
}
