package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Team;
import java.util.List;

public interface OrganizerService {

    Hackathon createHackathon(Hackathon hackathon, Long organizerId,
                              Long judgeId, List<Long> mentorIds);

    void addMentorToHackathon(Long hackathonId, Long mentorId);

    void proclaimWinner(Long hackathonId, Team winningTeam);

    List<Hackathon> getHackathonsByOrganizer(Long organizerId);
}