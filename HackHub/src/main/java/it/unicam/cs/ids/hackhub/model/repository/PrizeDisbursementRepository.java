package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.PrizeDisbursement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeDisbursementRepository extends JpaRepository<PrizeDisbursement, Long> {

	Optional<PrizeDisbursement> findByHackathonId(Long hackathonId);
}
