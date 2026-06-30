package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

	boolean existsBySubmissionId(Long submissionId);

	Optional<Evaluation> findBySubmissionId(Long submissionId);

	@Query("SELECT e FROM Evaluation e " +
			"JOIN FETCH e.submission s " +
			"JOIN FETCH s.registration r " +
			"JOIN FETCH r.team t " +
			"WHERE r.hackathon.id = :hackathonId " +
			"ORDER BY e.score DESC")
	List<Evaluation> findByHackathonIdOrderByScoreDesc(@Param("hackathonId") Long hackathonId);
}
