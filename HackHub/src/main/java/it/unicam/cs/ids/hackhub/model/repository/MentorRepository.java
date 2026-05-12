package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
}
