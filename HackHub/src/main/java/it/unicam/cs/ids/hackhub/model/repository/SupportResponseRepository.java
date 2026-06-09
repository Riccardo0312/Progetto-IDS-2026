package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.SupportResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportResponseRepository extends JpaRepository<SupportResponse, Long> {

	boolean existsBySupportRequestId(Long supportRequestId);
}
