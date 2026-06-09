package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.SupportRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

	List<SupportRequest> findByHackathonId(Long hackathonId);

	List<SupportRequest> findByHackathonIdAndCallProposalIsNullAndSupportResponseIsNull(
			Long hackathonId);

	List<SupportRequest> findByTeamId(Long teamId);

	Optional<SupportRequest> findByIdAndTeamId(Long supportRequestId, Long teamId);

	}
