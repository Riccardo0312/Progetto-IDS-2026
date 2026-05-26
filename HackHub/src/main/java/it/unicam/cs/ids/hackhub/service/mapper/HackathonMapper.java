package it.unicam.cs.ids.hackhub.service.mapper;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.team.TeamSummaryDTO;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct {@code Hackathon} -> {@link HackathonResponseDTO}.
 *
 * <p>Il flag {@code prizeDisbursed} non è un campo dell'entità ma una
 * proiezione calcolata altrove (esistenza di una {@code PrizeDisbursement} con
 * esito {@code SUCCESS}). Viene passato come parametro al metodo di mapping.
 */
@Mapper(componentModel = "spring")
public interface HackathonMapper {

	@Mapping(target = "winningTeam", source = "hackathon.winningTeam")
	@Mapping(target = "prizeDisbursed", source = "prizeDisbursed")
	HackathonResponseDTO toResponse(Hackathon hackathon, boolean prizeDisbursed);

	default TeamSummaryDTO toTeamSummary(Team team) {
		if (team == null) {
			return null;
		}
		return new TeamSummaryDTO(team.getId(), team.getName());
	}
}
