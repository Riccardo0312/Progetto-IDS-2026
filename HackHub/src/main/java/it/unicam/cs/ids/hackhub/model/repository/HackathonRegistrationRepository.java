package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

	boolean existsByHackathonIdAndTeamId(Long hackathonId, Long teamId);

}
