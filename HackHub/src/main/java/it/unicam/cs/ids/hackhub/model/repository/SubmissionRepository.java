package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
