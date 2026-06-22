package it.unicam.cs.ids.hackhub.model.repository;

import it.unicam.cs.ids.hackhub.model.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {

    List<ViolationReport> findByHackathonId(Long hackathonId);

    List<ViolationReport> findByHackathonIdAndTeamId(Long hackathonId, Long teamId);

}
