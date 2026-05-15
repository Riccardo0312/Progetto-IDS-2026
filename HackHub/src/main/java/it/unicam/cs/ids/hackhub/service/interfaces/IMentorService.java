package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.MentoringCallProposal;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import java.util.List;

public interface IMentorService {

	List<SupportRequest> getAssignedHackathonSupportRequests(Long mentorId, Long hackathonId);

	MentoringCallProposal proposeCall(Long mentorId, Long hackathonId, Long supportRequestId);

	ViolationReport reportViolation(
			Long mentorId, Long hackathonId, Long teamId, String description);

}
