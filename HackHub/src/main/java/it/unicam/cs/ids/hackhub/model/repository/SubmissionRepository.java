package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

	List<Submission> findByRegistrationHackathonId(Long hackathonId);

	boolean existsByIdAndRegistrationHackathonId(Long submissionId, Long hackathonId);

	/**
	 * Sottomissioni di un hackathon che possiedono una valutazione fatta dal
	 * giudice indicato. Alimenta lo step "mostra le sottomissioni gia valutate
	 * dal giudice" del caso d'uso di correzione.
	 */
	@Query("SELECT s FROM Submission s "
			+ "JOIN s.evaluation e "
			+ "WHERE s.registration.hackathon.id = :hackathonId "
			+ "AND e.judge.id = :judgeId")
	List<Submission> findEvaluatedByJudge(
			@Param("hackathonId") Long hackathonId, @Param("judgeId") Long judgeId);

}
