package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
}
