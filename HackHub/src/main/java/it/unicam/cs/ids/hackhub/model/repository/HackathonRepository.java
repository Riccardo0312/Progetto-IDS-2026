package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

	List<Hackathon> findByStatus(HackathonStatus status);

	List<Hackathon> findAllByOrderByStartDateAsc();
}
