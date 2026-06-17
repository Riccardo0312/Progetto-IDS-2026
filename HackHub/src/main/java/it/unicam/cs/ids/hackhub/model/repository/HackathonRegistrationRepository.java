package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

	List<HackathonRegistration> findByHackathonId(Long hackathonId);

	boolean existsByHackathonIdAndTeamId(Long hackathonId, Long teamId);

	void deleteByTeamId(Long teamId);

}
