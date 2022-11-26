package com.robosoft.VirtualLearn.AdminPanel.controller;

import com.robosoft.VirtualLearn.AdminPanel.entity.FinalTest;
import com.robosoft.VirtualLearn.AdminPanel.entity.UserAnswers;
import com.robosoft.VirtualLearn.AdminPanel.request.CertificateRequest;
import com.robosoft.VirtualLearn.AdminPanel.service.FinalTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class FinalTestController {
    @Autowired

    FinalTestService finalTestService;

    @GetMapping("/finalTest")
    public ResponseEntity<?> getFinalTest(@RequestParam Integer testId) {

        String status = finalTestService.checkForCompletedStatus(testId);
        if(status != null)
            return new ResponseEntity<>(Collections.singletonMap("message",status),HttpStatus.NOT_ACCEPTABLE);
        FinalTest moduleTest = finalTestService.finalTestService(testId);
        if (moduleTest == null)
            return new ResponseEntity<>(Collections.singletonMap("message","Invalid Test Id"),HttpStatus.NOT_FOUND);
        return ResponseEntity.of(Optional.of(moduleTest));
    }

    @PostMapping("/finalSubmit")
    public ResponseEntity<?> submitUserAnswers(@RequestBody UserAnswers userAnswers) throws IOException, ParseException {
        float testPercentage = finalTestService.userAnswers(userAnswers);
        finalTestService.certificate(userAnswers.getTestId());
        return new ResponseEntity<>(Collections.singletonMap("Test Percentage", testPercentage),HttpStatus.OK);
    }

    @GetMapping("/result")
    public ResponseEntity<?> getFinalTestResult(@RequestParam Integer testId) throws IOException {
        return new ResponseEntity<>(Collections.singletonMap("Approval Rate", finalTestService.getFinalTestResult(testId)),HttpStatus.OK);
    }

    @GetMapping("/viewCertificate")
    public Map getCertificate(@RequestBody CertificateRequest certificateRequest) throws IOException, ParseException {
        return Collections.singletonMap("message", finalTestService.viewCertificate(certificateRequest.getTestId()));
    }

    @GetMapping("/pdf")
    public Map getPdf(@RequestParam Integer courseId) throws IOException {
        return Collections.singletonMap("certificate", finalTestService.getPdfUrl(courseId));
    }
}
