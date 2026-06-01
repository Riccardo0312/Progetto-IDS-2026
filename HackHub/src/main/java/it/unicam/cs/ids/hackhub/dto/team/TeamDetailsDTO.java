package it.unicam.cs.ids.hackhub.dto.team;

import java.util.List;

public record TeamDetailsDTO(Long teamId,
                             String teamName,
                             UserSummaryDTO teamLeader,
                             List<UserSummaryDTO> members,
                             List<HackathonSummaryDTO> registeredHackathons) {
}
