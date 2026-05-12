package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
