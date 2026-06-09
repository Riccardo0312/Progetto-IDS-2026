package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Organizer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

	Optional<Organizer> findByEmail(String email);
}
