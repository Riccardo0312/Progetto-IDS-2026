package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.support.CreateMentoringCallProposalDTO;
import it.unicam.cs.ids.hackhub.dto.support.CreateSupportRequestDTO;
import it.unicam.cs.ids.hackhub.dto.support.CreateSupportResponseDTO;
import it.unicam.cs.ids.hackhub.dto.support.MentoringCallProposalDTO;
import it.unicam.cs.ids.hackhub.dto.support.SupportRequestResponseDTO;
import it.unicam.cs.ids.hackhub.dto.support.SupportResponseDTO;
import it.unicam.cs.ids.hackhub.model.MentoringCallProposal;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.SupportResponse;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentorService;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentoringRequestService;
import it.unicam.cs.ids.hackhub.service.mapper.SupportRequestMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SupportRequestController {

	private final IMentoringRequestService mentoringRequestService;
	private final IMentorService mentorService;
	private final SupportRequestMapper supportRequestMapper;

	public SupportRequestController(
			IMentoringRequestService mentoringRequestService,
			IMentorService mentorService,
			SupportRequestMapper supportRequestMapper) {
		this.mentoringRequestService = mentoringRequestService;
		this.mentorService = mentorService;
		this.supportRequestMapper = supportRequestMapper;
	}

	@PostMapping("/registrations/{registrationId}/support-requests")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('USER')")
	public SupportRequestResponseDTO createSupportRequest(
			@PathVariable Long registrationId,
			@Valid @RequestBody CreateSupportRequestDTO request,
			Authentication authentication) {
		SupportRequest supportRequest = mentoringRequestService.createSupportRequest(
				registrationId,
				authentication.getName(),
				request.description());
		return supportRequestMapper.toResponse(supportRequest);
	}

	@GetMapping("/teams/{teamId}/support-requests")
	@PreAuthorize("@hackHubAuthorizationService.isTeamMember(#teamId, authentication.name)")
	public List<SupportRequestResponseDTO> getTeamSupportRequests(
			@PathVariable Long teamId,
			Authentication authentication) {
		return mentoringRequestService.getTeamSupportRequests(teamId, authentication.getName()).stream()
				.map(supportRequestMapper::toResponse)
				.toList();
	}

	@GetMapping("/teams/{teamId}/support-requests/{supportRequestId}")
	@PreAuthorize("@hackHubAuthorizationService.isTeamMember(#teamId, authentication.name)")
	public SupportRequestResponseDTO getTeamSupportRequest(
			@PathVariable Long teamId,
			@PathVariable Long supportRequestId,
			Authentication authentication) {
		SupportRequest supportRequest = mentoringRequestService.getTeamSupportRequest(
				teamId,
				supportRequestId,
				authentication.getName());
		return supportRequestMapper.toResponse(supportRequest);
	}

	@GetMapping("/mentors/{mentorId}/hackathons/{hackathonId}/support-requests")
	@PreAuthorize(
			"hasRole('MENTOR') "
					+ "and @hackHubAuthorizationService.isMentorSelf(#mentorId, authentication.name) "
					+ "and @hackHubAuthorizationService.isAssignedMentor(#hackathonId, authentication.name)")
	public List<SupportRequestResponseDTO> getHackathonSupportRequests(
			@PathVariable Long mentorId,
			@PathVariable Long hackathonId) {
		return mentorService.getOpenAssignedHackathonSupportRequests(mentorId, hackathonId).stream()
				.map(supportRequestMapper::toResponse)
				.toList();
	}

	@GetMapping("/mentors/{mentorId}/hackathons/{hackathonId}/support-requests/{supportRequestId}")
	@PreAuthorize(
			"hasRole('MENTOR') "
					+ "and @hackHubAuthorizationService.isMentorSelf(#mentorId, authentication.name) "
					+ "and @hackHubAuthorizationService.isAssignedMentor(#hackathonId, authentication.name)")
	public SupportRequestResponseDTO getHackathonSupportRequest(
			@PathVariable Long mentorId,
			@PathVariable Long hackathonId,
			@PathVariable Long supportRequestId) {
		SupportRequest supportRequest = mentorService.getAssignedHackathonSupportRequest(
				mentorId,
				hackathonId,
				supportRequestId);
		return supportRequestMapper.toResponse(supportRequest);
	}

	@PostMapping("/mentors/{mentorId}/hackathons/{hackathonId}/support-requests/{supportRequestId}/call-proposal")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(
			"hasRole('MENTOR') "
					+ "and @hackHubAuthorizationService.isMentorSelf(#mentorId, authentication.name) "
					+ "and @hackHubAuthorizationService.isAssignedMentor(#hackathonId, authentication.name)")
	public MentoringCallProposalDTO proposeCall(
			@PathVariable Long mentorId,
			@PathVariable Long hackathonId,
			@PathVariable Long supportRequestId,
			@Valid @RequestBody CreateMentoringCallProposalDTO request) {
		MentoringCallProposal callProposal = mentorService.proposeCall(
				mentorId,
				hackathonId,
				supportRequestId,
				request.proposedSlots());
		return supportRequestMapper.toResponse(callProposal);
	}

	@PostMapping("/mentors/{mentorId}/hackathons/{hackathonId}/support-requests/{supportRequestId}/response")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize(
			"hasRole('MENTOR') "
					+ "and @hackHubAuthorizationService.isMentorSelf(#mentorId, authentication.name) "
					+ "and @hackHubAuthorizationService.isAssignedMentor(#hackathonId, authentication.name)")
	public SupportResponseDTO respondToSupportRequest(
			@PathVariable Long mentorId,
			@PathVariable Long hackathonId,
			@PathVariable Long supportRequestId,
			@Valid @RequestBody CreateSupportResponseDTO request) {
		SupportResponse supportResponse = mentorService.respondToSupportRequest(
				mentorId,
				hackathonId,
				supportRequestId,
				request.message());
		return supportRequestMapper.toResponse(supportResponse);
	}
}
