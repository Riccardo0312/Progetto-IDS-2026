package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

	List<Submission> findByRegistrationHackathonId(Long hackathonId);

	boolean existsByIdAndRegistrationHackathonId(Long submissionId, Long hackathonId);

}
