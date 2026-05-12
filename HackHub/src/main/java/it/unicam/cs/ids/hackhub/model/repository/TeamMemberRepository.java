package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
