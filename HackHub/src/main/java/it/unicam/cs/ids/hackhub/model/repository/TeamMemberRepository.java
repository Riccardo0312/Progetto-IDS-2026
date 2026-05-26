package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.TeamMember;
import it.unicam.cs.ids.hackhub.model.TeamRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserIdAndRole(Long teamId, Long userId, TeamRole role);

    Optional<TeamMember> findByTeamIdAndRole(Long teamId, TeamRole role);

    long countByTeamIdAndRole(Long teamId, TeamRole role);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);
}
