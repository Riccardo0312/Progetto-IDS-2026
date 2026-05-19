package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ISubmissionService {
    Submission uploadSubmission(Long registrationId, MultipartFile file) throws IOException;
    Submission updateSubmission(Long submissionId, MultipartFile file) throws IOException;
}
