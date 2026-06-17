package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.RegistrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

	List<HackathonRegistration> findByHackathonId(Long hackathonId);

	List<HackathonRegistration> findByHackathonIdAndStatus(Long hackathonId, RegistrationStatus status);

	Optional<HackathonRegistration> findByHackathonIdAndTeamId(Long hackathonId, Long teamId);

	boolean existsByHackathonIdAndTeamId(Long hackathonId, Long teamId);

	void deleteByTeamId(Long teamId);

}
