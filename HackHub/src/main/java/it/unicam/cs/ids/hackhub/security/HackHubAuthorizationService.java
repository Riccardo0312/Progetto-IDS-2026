package it.unicam.cs.ids.hackhub.security;

import it.unicam.cs.ids.hackhub.model.TeamRole;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentorRepository;
import it.unicam.cs.ids.hackhub.model.repository.OrganizerRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Controlli di ownership per SpEL nelle {@code @PreAuthorize}.
 *
 * <p>L'identità è sempre il principal JWT ({@code authentication.name} = email),
 * mai un parametro inviato dal client. Tutti i metodi sono null-safe e
 * ritornano {@code false} se l'entità non esiste.
 *
 * <p>Bean name {@code hackHubAuthorizationService} -> usabile come
 * {@code @hackHubAuthorizationService.isOrganizerSelf(#organizerId, authentication.name)}.
 */
@Service("hackHubAuthorizationService")
public class HackHubAuthorizationService {

	private final UserRepository userRepository;
	private final OrganizerRepository organizerRepository;
	private final MentorRepository mentorRepository;
	private final JudgeRepository judgeRepository;
	private final TeamMemberRepository teamMemberRepository;

	public HackHubAuthorizationService(
			UserRepository userRepository,
			OrganizerRepository organizerRepository,
			MentorRepository mentorRepository,
			JudgeRepository judgeRepository,
			TeamMemberRepository teamMemberRepository) {
		this.userRepository = userRepository;
		this.organizerRepository = organizerRepository;
		this.mentorRepository = mentorRepository;
		this.judgeRepository = judgeRepository;
		this.teamMemberRepository = teamMemberRepository;
	}

	/** L'utente {@code userId} è il principal. */
	public boolean isSameUser(Long userId, String principalEmail) {
		if (userId == null || principalEmail == null) {
			return false;
		}
		return userRepository.findByEmail(principalEmail)
				.map(u -> u.getId().equals(userId))
				.orElse(false);
	}

	/** Il principal è l'organizer {@code organizerId}. */
	public boolean isOrganizerSelf(Long organizerId, String principalEmail) {
		if (organizerId == null || principalEmail == null) {
			return false;
		}
		return organizerRepository.findByEmail(principalEmail)
				.map(o -> o.getId().equals(organizerId))
				.orElse(false);
	}

	/** Il principal è membro (o leader) del team {@code teamId}. */
	public boolean isTeamMember(Long teamId, String principalEmail) {
		if (teamId == null || principalEmail == null) {
			return false;
		}
		return userRepository.findByEmail(principalEmail)
				.map(u -> teamMemberRepository.existsByTeamIdAndUserId(teamId, u.getId()))
				.orElse(false);
	}

	/** Il principal è il leader del team {@code teamId}. */
	public boolean isTeamLeader(Long teamId, String principalEmail) {
		if (teamId == null || principalEmail == null) {
			return false;
		}
		return userRepository.findByEmail(principalEmail)
				.map(u -> teamMemberRepository.existsByTeamIdAndUserIdAndRole(
						teamId, u.getId(), TeamRole.LEADER))
				.orElse(false);
	}

	/** Il principal è un mentor assegnato all'hackathon {@code hackathonId}. */
	public boolean isAssignedMentor(Long hackathonId, String principalEmail) {
		if (hackathonId == null || principalEmail == null) {
			return false;
		}
		return mentorRepository.findByEmail(principalEmail)
					.map(m -> mentorRepository.existsByIdAndSupportedHackathonsId(
							m.getId(), hackathonId))
					.orElse(false);
	}

	/** Il principal è il mentor {@code mentorId}. */
	public boolean isMentorSelf(Long mentorId, String principalEmail) {
		if (mentorId == null || principalEmail == null) {
			return false;
		}
		return mentorRepository.findByEmail(principalEmail)
				.map(mentor -> mentor.getId().equals(mentorId))
				.orElse(false);
	}

	/** Il principal è un judge assegnato all'hackathon {@code hackathonId}. */
	public boolean isAssignedJudge(Long hackathonId, String principalEmail) {
		if (hackathonId == null || principalEmail == null) {
			return false;
		}
		return judgeRepository.findByEmail(principalEmail)
				.map(j -> judgeRepository.existsByIdAndAssignedHackathonsId(
						j.getId(), hackathonId))
				.orElse(false);
	}

	/** Il principal è il judge {@code judgeId}. */
	public boolean isJudgeSelf(Long judgeId, String principalEmail) {
		if (judgeId == null || principalEmail == null) {
			return false;
		}
		return judgeRepository.findByEmail(principalEmail)
				.map(judge -> judge.getId().equals(judgeId))
				.orElse(false);
	}
}
