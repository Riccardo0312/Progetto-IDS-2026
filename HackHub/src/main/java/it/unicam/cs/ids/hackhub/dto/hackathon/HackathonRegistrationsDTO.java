package it.unicam.cs.ids.hackhub.dto.hackathon;

import it.unicam.cs.ids.hackhub.dto.team.TeamSummaryDTO;
import java.util.List;

public record HackathonRegistrationsDTO(long totalRegistrations, List<TeamSummaryDTO> teams) {}
